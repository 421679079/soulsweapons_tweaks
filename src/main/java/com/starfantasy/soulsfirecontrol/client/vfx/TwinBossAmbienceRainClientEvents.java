package com.starfantasy.soulsfirecontrol.client.vfx;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.starfantasy.soulsfirecontrol.StarFantasySoulsFireControl;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.soulsweaponry.entity.mobs.DayStalker;
import net.soulsweaponry.entity.mobs.NightProwler;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = StarFantasySoulsFireControl.MOD_ID, value = Dist.CLIENT)
public final class TwinBossAmbienceRainClientEvents {
    private static final ResourceLocation RAIN_TEXTURE =
            new ResourceLocation("minecraft", "textures/environment/rain.png");
    private static final double ACTIVE_SCAN_RADIUS = 128.0D;
    private static final int RENDER_RADIUS = 10;
    private static final int LOWER_HEIGHT = 5;
    private static final int UPPER_HEIGHT = 22;
    private static final float FADE_IN_STEP = 0.025F;
    private static final float FADE_OUT_STEP = 0.04F;
    private static final float BASE_ALPHA = 0.64F;
    private static final float SCROLL_SPEED = 0.085F;
    private static final float TEXEL_HEIGHT = 0.18F;
    private static final float SEED_SCROLL_SCALE = 0.03125F;

    private static float oldNightRainLevel;
    private static float nightRainLevel;
    private static float oldDayRainLevel;
    private static float dayRainLevel;

    private TwinBossAmbienceRainClientEvents() {
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        LocalPlayer player = minecraft.player;
        oldNightRainLevel = nightRainLevel;
        oldDayRainLevel = dayRainLevel;
        if (level == null || player == null) {
            nightRainLevel = 0.0F;
            oldNightRainLevel = 0.0F;
            dayRainLevel = 0.0F;
            oldDayRainLevel = 0.0F;
            return;
        }

        boolean nightRain = level.getEntitiesOfClass(NightProwler.class,
                player.getBoundingBox().inflate(ACTIVE_SCAN_RADIUS),
                boss -> boss.isAlive() && boss.isPhaseTwo()).size() > 0;
        boolean dayRain = level.getEntitiesOfClass(DayStalker.class,
                player.getBoundingBox().inflate(ACTIVE_SCAN_RADIUS),
                boss -> boss.isAlive() && boss.isPhaseTwo()).size() > 0;
        nightRainLevel = easeRainLevel(nightRainLevel, nightRain);
        dayRainLevel = easeRainLevel(dayRainLevel, dayRain);
    }

    @SubscribeEvent
    public static void renderAmbienceRain(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.player == null) {
            return;
        }

        float partialTick = event.getPartialTick();
        float nightIntensity = Mth.lerp(partialTick, oldNightRainLevel, nightRainLevel);
        float dayIntensity = Mth.lerp(partialTick, oldDayRainLevel, dayRainLevel);
        if (nightIntensity <= 0.001F && dayIntensity <= 0.001F) {
            return;
        }

        Camera camera = minecraft.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        int cameraX = Mth.floor(cameraPos.x);
        int cameraY = Mth.floor(cameraPos.y);
        int cameraZ = Mth.floor(cameraPos.z);
        int yMin = cameraY - LOWER_HEIGHT;
        int yMax = cameraY + UPPER_HEIGHT;

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        Matrix4f matrix = poseStack.last().pose();

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, RAIN_TEXTURE);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        float age = (float) level.getGameTime() + partialTick;
        if (dayIntensity > 0.001F) {
            renderRainLayer(buffer, matrix, cameraPos, cameraX, cameraZ, yMin, yMax, age, dayIntensity,
                    false, 1.0F, 0.72F, 0.08F);
        }
        if (nightIntensity > 0.001F) {
            renderRainLayer(buffer, matrix, cameraPos, cameraX, cameraZ, yMin, yMax, age, nightIntensity,
                    true, 0.46F, 0.08F, 1.0F);
        }

        tesselator.end();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        poseStack.popPose();
    }

    private static float easeRainLevel(float current, boolean active) {
        float step = active ? FADE_IN_STEP : -FADE_OUT_STEP;
        return Mth.clamp(current + step, 0.0F, 1.0F);
    }

    private static void renderRainLayer(BufferBuilder buffer, Matrix4f matrix, Vec3 cameraPos,
                                        int cameraX, int cameraZ, int yMin, int yMax, float age,
                                        float intensity, boolean reverse, float red, float green, float blue) {
        for (int z = cameraZ - RENDER_RADIUS; z <= cameraZ + RENDER_RADIUS; z++) {
            for (int x = cameraX - RENDER_RADIUS; x <= cameraX + RENDER_RADIUS; x++) {
                double distanceX = x + 0.5D - cameraPos.x;
                double distanceZ = z + 0.5D - cameraPos.z;
                float distance = Mth.sqrt((float) (distanceX * distanceX + distanceZ * distanceZ));
                float normalized = distance / RENDER_RADIUS;
                if (normalized > 1.15F) {
                    continue;
                }

                float clampedDistance = Mth.clamp(normalized, 0.0F, 1.0F);
                float alpha = ((1.0F - clampedDistance * clampedDistance) * 0.52F + 0.28F)
                        * intensity * BASE_ALPHA;
                if (alpha <= 0.01F) {
                    continue;
                }

                long seed = rainSeed(x, z);
                double angle = ((seed & 1023L) / 1024.0D) * Math.PI * 2.0D;
                double width = 0.34D + ((seed >>> 10) & 127L) / 127.0D * 0.18D;
                double offsetX = Math.cos(angle) * width;
                double offsetZ = Math.sin(angle) * width;
                double centerX = x + 0.5D;
                double centerZ = z + 0.5D;
                float seedOffset = ((seed >>> 17) & 31L) * SEED_SCROLL_SCALE;
                float scroll = (reverse ? -age : age) * SCROLL_SPEED + seedOffset;
                float v0 = yMin * TEXEL_HEIGHT + scroll;
                float v1 = yMax * TEXEL_HEIGHT + scroll;

                vertex(buffer, matrix, centerX - offsetX, yMax, centerZ - offsetZ, 0.0F, v1,
                        red, green, blue, alpha);
                vertex(buffer, matrix, centerX + offsetX, yMax, centerZ + offsetZ, 1.0F, v1,
                        red, green, blue, alpha);
                vertex(buffer, matrix, centerX + offsetX, yMin, centerZ + offsetZ, 1.0F, v0,
                        red, green, blue, alpha);
                vertex(buffer, matrix, centerX - offsetX, yMin, centerZ - offsetZ, 0.0F, v0,
                        red, green, blue, alpha);
            }
        }
    }

    private static void vertex(BufferBuilder buffer, Matrix4f matrix, double x, double y, double z,
                               float u, float v, float red, float green, float blue, float alpha) {
        buffer.vertex(matrix, (float) x, (float) y, (float) z)
                .uv(u, v)
                .color(red, green, blue, alpha)
                .endVertex();
    }

    private static long rainSeed(int x, int z) {
        long seed = (long) x * 3129871L ^ (long) z * 116129781L;
        seed ^= seed >>> 13;
        seed *= 0x5DEECE66DL;
        seed ^= seed >>> 17;
        return seed;
    }
}
