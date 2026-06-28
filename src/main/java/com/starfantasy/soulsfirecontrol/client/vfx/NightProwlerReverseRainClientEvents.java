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
import net.soulsweaponry.entity.mobs.NightProwler;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = StarFantasySoulsFireControl.MOD_ID, value = Dist.CLIENT)
public final class NightProwlerReverseRainClientEvents {
    private static final ResourceLocation RAIN_TEXTURE =
            new ResourceLocation("minecraft", "textures/environment/rain.png");
    private static final double ACTIVE_SCAN_RADIUS = 128.0D;
    private static final int RENDER_RADIUS = 10;
    private static final int LOWER_HEIGHT = 5;
    private static final int UPPER_HEIGHT = 22;
    private static final float FADE_IN_STEP = 0.025F;
    private static final float FADE_OUT_STEP = 0.04F;
    private static final float BASE_ALPHA = 0.64F;
    private static final float PURPLE_R = 0.46F;
    private static final float PURPLE_G = 0.08F;
    private static final float PURPLE_B = 1.0F;
    private static final float SCROLL_SPEED = 0.085F;

    private static float oldRainLevel;
    private static float rainLevel;

    private NightProwlerReverseRainClientEvents() {
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        LocalPlayer player = minecraft.player;
        oldRainLevel = rainLevel;
        if (level == null || player == null) {
            rainLevel = 0.0F;
            oldRainLevel = 0.0F;
            return;
        }

        boolean shouldRain = level.getEntitiesOfClass(NightProwler.class,
                player.getBoundingBox().inflate(ACTIVE_SCAN_RADIUS),
                boss -> boss.isAlive() && boss.isPhaseTwo()).size() > 0;
        float step = shouldRain ? FADE_IN_STEP : -FADE_OUT_STEP;
        rainLevel = Mth.clamp(rainLevel + step, 0.0F, 1.0F);
    }

    @SubscribeEvent
    public static void renderReverseRain(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.player == null) {
            return;
        }

        float partialTick = event.getPartialTick();
        float intensity = Mth.lerp(partialTick, oldRainLevel, rainLevel);
        if (intensity <= 0.001F) {
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
        for (int z = cameraZ - RENDER_RADIUS; z <= cameraZ + RENDER_RADIUS; z++) {
            for (int x = cameraX - RENDER_RADIUS; x <= cameraX + RENDER_RADIUS; x++) {
                double distanceX = x + 0.5D - cameraPos.x;
                double distanceZ = z + 0.5D - cameraPos.z;
                float distance = Mth.sqrt((float) (distanceX * distanceX + distanceZ * distanceZ));
                float normalized = distance / RENDER_RADIUS;
                if (normalized > 1.15F) {
                    continue;
                }

                float alpha = ((1.0F - Mth.clamp(normalized, 0.0F, 1.0F)
                        * Mth.clamp(normalized, 0.0F, 1.0F)) * 0.52F + 0.28F) * intensity * BASE_ALPHA;
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
                float scroll = age * SCROLL_SPEED + ((seed >>> 17) & 31L) * 0.03125F;
                float v0 = yMin * 0.18F + scroll;
                float v1 = yMax * 0.18F + scroll;

                vertex(buffer, matrix, centerX - offsetX, yMax, centerZ - offsetZ, 0.0F, v1, alpha);
                vertex(buffer, matrix, centerX + offsetX, yMax, centerZ + offsetZ, 1.0F, v1, alpha);
                vertex(buffer, matrix, centerX + offsetX, yMin, centerZ + offsetZ, 1.0F, v0, alpha);
                vertex(buffer, matrix, centerX - offsetX, yMin, centerZ - offsetZ, 0.0F, v0, alpha);
            }
        }

        tesselator.end();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        poseStack.popPose();
    }

    private static void vertex(BufferBuilder buffer, Matrix4f matrix, double x, double y, double z,
                               float u, float v, float alpha) {
        buffer.vertex(matrix, (float) x, (float) y, (float) z)
                .uv(u, v)
                .color(PURPLE_R, PURPLE_G, PURPLE_B, alpha)
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
