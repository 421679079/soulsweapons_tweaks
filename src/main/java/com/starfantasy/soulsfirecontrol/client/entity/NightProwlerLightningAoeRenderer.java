package com.starfantasy.soulsfirecontrol.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.starfantasy.soulsfirecontrol.StarFantasySoulsFireControl;
import com.starfantasy.soulsfirecontrol.entity.NightProwlerLightningAoeEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.Random;

public class NightProwlerLightningAoeRenderer extends EntityRenderer<NightProwlerLightningAoeEntity> {
    private static final ResourceLocation SHOCKWAVE_TEXTURE =
            StarFantasySoulsFireControl.id("textures/particle/night_lightning/thunder_shockwave_ring.png");
    private static final ResourceLocation ENERGY_CORE_TEXTURE =
            StarFantasySoulsFireControl.id("textures/particle/night_lightning/lightning_ball_core.png");
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final float START_RADIUS_FRACTION = 0.12F;
    private static final float MIN_START_RADIUS = 0.35F;
    private static final float FADE_START_PROGRESS = 0.7F;
    private static final float MAX_ALPHA = 0.95F;
    private static final float CORE_Y_OFFSET = 0.72F;
    private static final float SHOCKWAVE_RADIUS_SCALE = 1.25F;
    private static final float CORE_RADIUS_FRACTION = 0.38F;
    private static final float CORE_MIN_SIZE = 2.1F;
    private static final float CORE_MAX_SIZE = 4.7F;
    private static final float LIGHTNING_RADIUS_SCALE = 2.0F;
    private static final float LIGHTNING_FADE_START_PROGRESS = 0.76F;
    private static final Vec3 LIGHTNING_RIGHT_SEED = new Vec3(0.45D, 0.82D, 0.31D);

    public NightProwlerLightningAoeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(NightProwlerLightningAoeEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float age = entity.tickCount + partialTick;
        float duration = Math.max(1.0F, entity.getDuration());
        float progress = Mth.clamp(age / duration, 0.0F, 1.0F);
        if (progress >= 1.0F) {
            return;
        }
        renderShockwave(entity, progress, poseStack, buffer);
        renderEnergyCore(entity, age, progress, poseStack, buffer);
        renderLightning(entity, age, progress, poseStack, buffer);
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(NightProwlerLightningAoeEntity entity) {
        return SHOCKWAVE_TEXTURE;
    }

    private static void renderShockwave(NightProwlerLightningAoeEntity entity, float progress,
                                        PoseStack poseStack, MultiBufferSource buffer) {
        float easeOut = 1.0F - (float) Math.pow(1.0F - progress, 3.0D);
        float endRadius = entity.getRadius() * SHOCKWAVE_RADIUS_SCALE;
        float startRadius = Math.max(MIN_START_RADIUS, endRadius * START_RADIUS_FRACTION);
        float radius = Mth.lerp(easeOut, startRadius, endRadius);
        float alpha = shockwaveAlpha(progress);
        if (alpha <= 0.0F || radius <= 0.0F) {
            return;
        }

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(SHOCKWAVE_TEXTURE));
        renderHorizontalQuad(poseStack, consumer, radius, 0.02F, alpha);
    }

    private static void renderEnergyCore(NightProwlerLightningAoeEntity entity, float age, float progress,
                                         PoseStack poseStack, MultiBufferSource buffer) {
        float alpha = coreAlpha(age, progress);
        if (alpha <= 0.0F) {
            return;
        }
        float baseSize = Mth.clamp(entity.getRadius() * CORE_RADIUS_FRACTION, CORE_MIN_SIZE, CORE_MAX_SIZE);
        float pop = 0.7F + 0.3F * (1.0F - (float) Math.pow(1.0F - progress, 2.0D));
        float pulse = 1.0F + Mth.sin(age * 1.7F + entity.getId() * 0.31F) * 0.08F;
        float size = baseSize * pop * pulse;

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(ENERGY_CORE_TEXTURE));
        poseStack.pushPose();
        poseStack.translate(0.0D, CORE_Y_OFFSET, 0.0D);
        poseStack.mulPose(camera.rotation());

        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(age * 28.0F + entity.getId() * 17.0F));
        renderBillboardQuad(poseStack, consumer, size * 1.15F, 0.42F, 0.82F, 1.0F, alpha * 0.5F);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(-age * 43.0F + entity.getId() * 11.0F));
        renderBillboardQuad(poseStack, consumer, size * 0.78F, 1.0F, 1.0F, 1.0F, alpha);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(age * 67.0F + entity.getId() * 5.0F));
        renderBillboardQuad(poseStack, consumer, size * 1.52F, 0.18F, 0.58F, 1.0F, alpha * 0.3F);
        poseStack.popPose();

        poseStack.popPose();
    }

    private static void renderLightning(NightProwlerLightningAoeEntity entity, float age, float progress,
                                        PoseStack poseStack, MultiBufferSource buffer) {
        float alpha = lightningAlpha(progress);
        if (alpha <= 0.0F) {
            return;
        }

        float radius = entity.getRadius() * LIGHTNING_RADIUS_SCALE;
        int arcCount = Mth.clamp(Mth.ceil(radius * 1.35F), 8, 18);
        int refreshFrame = Math.max(0, (int) age / 2);
        float width = Mth.clamp(radius * 0.013F, 0.07F, 0.14F);
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();
        for (int i = 0; i < arcCount; i++) {
            Random random = new Random(lightningSeed(entity.getId(), refreshFrame, i));
            float arcAlpha = alpha * Mth.lerp(random.nextFloat(), 0.62F, 1.0F);
            Vec3 start = randomLightningStart(random, radius);
            Vec3 end = randomLightningEnd(random, radius, i);
            int segments = Mth.clamp(Mth.ceil((float) start.distanceTo(end) * 2.35F), 5, 14);
            Vec3[] path = buildLightningPath(start, end, segments, 0.12F, random);
            renderLightningPath(matrix, consumer, path,
                    width * Mth.lerp(random.nextFloat(), 0.85F, 1.35F), arcAlpha);
        }
    }

    private static float shockwaveAlpha(float progress) {
        if (progress <= FADE_START_PROGRESS) {
            return MAX_ALPHA;
        }
        float fadeProgress = (progress - FADE_START_PROGRESS) / (1.0F - FADE_START_PROGRESS);
        return MAX_ALPHA * Mth.clamp(1.0F - fadeProgress, 0.0F, 1.0F);
    }

    private static float coreAlpha(float age, float progress) {
        float fadeIn = Mth.clamp(age / 1.5F, 0.0F, 1.0F);
        float fadeOut = 1.0F - smoothstep(0.78F, 1.0F, progress);
        return fadeIn * fadeOut;
    }

    private static float lightningAlpha(float progress) {
        if (progress <= LIGHTNING_FADE_START_PROGRESS) {
            return 0.82F;
        }
        float fadeProgress = (progress - LIGHTNING_FADE_START_PROGRESS)
                / (1.0F - LIGHTNING_FADE_START_PROGRESS);
        return 0.82F * Mth.clamp(1.0F - fadeProgress, 0.0F, 1.0F);
    }

    private static long lightningSeed(int entityId, int refreshFrame, int index) {
        long seed = 0x9E3779B97F4A7C15L;
        seed ^= (long) entityId * 0xBF58476D1CE4E5B9L;
        seed ^= (long) refreshFrame * 0x94D049BB133111EBL;
        seed ^= (long) index * 0xD6E8FEB86659FD93L;
        seed ^= seed >>> 30;
        seed *= 0xBF58476D1CE4E5B9L;
        seed ^= seed >>> 27;
        return seed;
    }

    private static Vec3 randomLightningStart(Random random, float radius) {
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double distance = radius * Mth.lerp(random.nextFloat(), 0.015F, 0.075F);
        double y = Mth.lerp(random.nextFloat(), 0.38F, 1.02F);
        return new Vec3(Math.cos(angle) * distance, y, Math.sin(angle) * distance);
    }

    private static Vec3 randomLightningEnd(Random random, float radius, int index) {
        double angle = random.nextDouble() * Math.PI * 2.0D + index * 0.47D;
        double distance = radius * Mth.lerp(random.nextFloat(), 0.22F, 0.68F);
        double y = index % 4 == 0
                ? Mth.lerp(random.nextFloat(), 1.35F, 2.1F)
                : Mth.lerp(random.nextFloat(), 0.18F, 1.18F);
        return new Vec3(Math.cos(angle) * distance, y, Math.sin(angle) * distance);
    }

    private static Vec3[] buildLightningPath(Vec3 start, Vec3 end, int segments, float spreadFactor,
                                             Random random) {
        Vec3[] points = new Vec3[segments + 1];
        Vec3 diff = end.subtract(start);
        double length = Math.max(0.001D, diff.length());
        Vec3 previousOffset = Vec3.ZERO;
        for (int i = 0; i <= segments; i++) {
            float progress = i / (float) segments;
            if (i == 0) {
                points[i] = start;
                continue;
            }
            if (i == segments) {
                points[i] = end;
                continue;
            }
            Vec3 randomVector = new Vec3(random.nextDouble() - 0.5D, random.nextDouble() - 0.5D,
                    random.nextDouble() - 0.5D);
            Vec3 perpendicular = diff.cross(randomVector);
            if (perpendicular.lengthSqr() < 1.0E-5D) {
                perpendicular = diff.cross(new Vec3(0.0D, 1.0D, 0.0D));
            }
            if (perpendicular.lengthSqr() < 1.0E-5D) {
                perpendicular = new Vec3(1.0D, 0.0D, 0.0D);
            }
            double spread = Math.sin(Math.PI * progress) * length * spreadFactor
                    * Mth.lerp(random.nextFloat(), 0.35F, 1.0F);
            previousOffset = previousOffset.scale(0.48D).add(perpendicular.normalize().scale(spread));
            points[i] = start.add(diff.scale(progress)).add(previousOffset);
        }
        return points;
    }

    private static void renderLightningPath(Matrix4f matrix, VertexConsumer consumer, Vec3[] points,
                                            float width, float alpha) {
        for (int i = 1; i < points.length; i++) {
            float progress = i / (float) (points.length - 1);
            float taper = 0.42F + (1.0F - progress) * 0.58F;
            renderBoltSegment(matrix, consumer, points[i - 1], points[i], width * taper * 2.4F,
                    0.34F, 0.74F, 1.0F, alpha * 0.32F);
            renderBoltSegment(matrix, consumer, points[i - 1], points[i], width * taper * 0.82F,
                    0.88F, 0.98F, 1.0F, alpha);
        }
    }

    private static void renderBoltSegment(Matrix4f matrix, VertexConsumer consumer, Vec3 start, Vec3 end,
                                          float width, float red, float green, float blue, float alpha) {
        if (width <= 0.0F || alpha <= 0.0F) {
            return;
        }
        Vec3 diff = end.subtract(start);
        if (diff.lengthSqr() < 1.0E-6D) {
            return;
        }
        Vec3 right = diff.cross(LIGHTNING_RIGHT_SEED);
        if (right.lengthSqr() < 1.0E-6D) {
            right = diff.cross(new Vec3(0.0D, 1.0D, 0.0D));
        }
        if (right.lengthSqr() < 1.0E-6D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        }
        right = right.normalize().scale(width);
        Vec3 back = diff.cross(right).normalize().scale(width);
        renderBoltQuad(matrix, consumer, start.add(right), start.subtract(right), end.subtract(right),
                end.add(right), red, green, blue, alpha);
        renderBoltQuad(matrix, consumer, start.add(back), start.subtract(back), end.subtract(back),
                end.add(back), red, green, blue, alpha * 0.72F);
    }

    private static void renderBoltQuad(Matrix4f matrix, VertexConsumer consumer, Vec3 a, Vec3 b, Vec3 c, Vec3 d,
                                       float red, float green, float blue, float alpha) {
        boltVertex(matrix, consumer, a, red, green, blue, alpha);
        boltVertex(matrix, consumer, b, red, green, blue, alpha);
        boltVertex(matrix, consumer, c, red, green, blue, alpha);
        boltVertex(matrix, consumer, d, red, green, blue, alpha);
    }

    private static void boltVertex(Matrix4f matrix, VertexConsumer consumer, Vec3 point,
                                   float red, float green, float blue, float alpha) {
        consumer.vertex(matrix, (float) point.x, (float) point.y, (float) point.z)
                .color(red, green, blue, alpha)
                .endVertex();
    }

    private static void renderBillboardQuad(PoseStack poseStack, VertexConsumer consumer, float halfSize,
                                            float red, float green, float blue, float alpha) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        texturedVertex(consumer, matrix, normal, -halfSize, -halfSize, 0.0F, 0.0F, 1.0F, red, green, blue, alpha);
        texturedVertex(consumer, matrix, normal, halfSize, -halfSize, 0.0F, 1.0F, 1.0F, red, green, blue, alpha);
        texturedVertex(consumer, matrix, normal, halfSize, halfSize, 0.0F, 1.0F, 0.0F, red, green, blue, alpha);
        texturedVertex(consumer, matrix, normal, -halfSize, halfSize, 0.0F, 0.0F, 0.0F, red, green, blue, alpha);
    }

    private static void renderHorizontalQuad(PoseStack poseStack, VertexConsumer consumer,
                                             float radius, float y, float alpha) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        texturedVertex(consumer, matrix, normal, -radius, y, -radius, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, alpha);
        texturedVertex(consumer, matrix, normal, -radius, y, radius, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, alpha);
        texturedVertex(consumer, matrix, normal, radius, y, radius, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, alpha);
        texturedVertex(consumer, matrix, normal, radius, y, -radius, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, alpha);
    }

    private static void texturedVertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                                       float x, float y, float z, float u, float v,
                                       float red, float green, float blue, float alpha) {
        consumer.vertex(matrix, x, y, z)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(FULL_BRIGHT)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    private static float smoothstep(float edge0, float edge1, float value) {
        float t = Mth.clamp((value - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }
}
