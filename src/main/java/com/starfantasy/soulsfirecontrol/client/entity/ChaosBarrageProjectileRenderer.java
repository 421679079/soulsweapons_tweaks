package com.starfantasy.soulsfirecontrol.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.starfantasy.soulsfirecontrol.StarFantasySoulsFireControl;
import com.starfantasy.soulsfirecontrol.entity.ChaosBarrageProjectileEntity;
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

public class ChaosBarrageProjectileRenderer extends EntityRenderer<ChaosBarrageProjectileEntity> {
    private static final ResourceLocation FIRE_TEXTURE =
            new ResourceLocation("minecraft", "textures/item/fire_charge.png");
    private static final ResourceLocation FROST_TEXTURE =
            StarFantasySoulsFireControl.id("textures/entity/chaos_frost_meteor.png");
    private static final ResourceLocation SHOCKWAVE_TEXTURE =
            StarFantasySoulsFireControl.id("textures/particle/night_lightning/thunder_shockwave_ring.png");
    private static final ResourceLocation ENERGY_CORE_TEXTURE =
            StarFantasySoulsFireControl.id("textures/particle/night_lightning/lightning_ball_core.png");
    private static final ResourceLocation NIGHT_TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/enderdragon/dragon_fireball.png");
    private static final ResourceLocation WITHER_SKULL_TEXTURE =
            new ResourceLocation("minecraft", "textures/item/wither_skeleton_skull.png");
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final Vec3 LIGHTNING_RIGHT_SEED = new Vec3(0.45D, 0.82D, 0.31D);

    public ChaosBarrageProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ChaosBarrageProjectileEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (entity.getStyle() == ChaosBarrageProjectileEntity.STYLE_MOONLIGHT) {
            renderLightningProjectile(entity, partialTick, poseStack, buffer);
            super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
            return;
        }
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        float scale = scaleFor(entity);
        poseStack.scale(scale, scale, scale);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(this.getTextureLocation(entity)));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        vertex(consumer, matrix, normal, packedLight, -0.5F, -0.5F, 0.0F, 1.0F);
        vertex(consumer, matrix, normal, packedLight, 0.5F, -0.5F, 1.0F, 1.0F);
        vertex(consumer, matrix, normal, packedLight, 0.5F, 0.5F, 1.0F, 0.0F);
        vertex(consumer, matrix, normal, packedLight, -0.5F, 0.5F, 0.0F, 0.0F);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ChaosBarrageProjectileEntity entity) {
        return switch (entity.getStyle()) {
            case ChaosBarrageProjectileEntity.STYLE_FROST -> FROST_TEXTURE;
            case ChaosBarrageProjectileEntity.STYLE_MOONLIGHT -> ENERGY_CORE_TEXTURE;
            case ChaosBarrageProjectileEntity.STYLE_NIGHT -> NIGHT_TEXTURE;
            case ChaosBarrageProjectileEntity.STYLE_WITHER_SKULL -> WITHER_SKULL_TEXTURE;
            default -> FIRE_TEXTURE;
        };
    }

    private static float scaleFor(ChaosBarrageProjectileEntity entity) {
        return switch (entity.getStyle()) {
            case ChaosBarrageProjectileEntity.STYLE_MOONLIGHT -> 1.9F;
            case ChaosBarrageProjectileEntity.STYLE_WITHER_SKULL -> entity.isVisualOnly() ? 2.0F : 1.45F;
            case ChaosBarrageProjectileEntity.STYLE_FROST, ChaosBarrageProjectileEntity.STYLE_NIGHT -> 1.5F;
            default -> 1.35F;
        };
    }

    private static void renderLightningProjectile(ChaosBarrageProjectileEntity entity, float partialTick,
                                                  PoseStack poseStack, MultiBufferSource buffer) {
        float age = entity.tickCount + partialTick;
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();

        poseStack.pushPose();
        poseStack.mulPose(camera.rotation());

        VertexConsumer shockwaveConsumer = buffer.getBuffer(RenderType.entityTranslucent(SHOCKWAVE_TEXTURE));
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(age * 54.0F + entity.getId() * 13.0F));
        renderBillboardQuad(poseStack, shockwaveConsumer, 1.02F, 0.35F, 0.78F, 1.0F, 0.42F);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(-age * 82.0F + entity.getId() * 7.0F));
        renderBillboardQuad(poseStack, shockwaveConsumer, 0.68F, 0.75F, 0.95F, 1.0F, 0.64F);
        poseStack.popPose();

        VertexConsumer coreConsumer = buffer.getBuffer(RenderType.entityTranslucent(ENERGY_CORE_TEXTURE));
        float pulse = 1.0F + Mth.sin(age * 0.9F + entity.getId() * 0.4F) * 0.08F;
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(age * 96.0F + entity.getId() * 11.0F));
        renderBillboardQuad(poseStack, coreConsumer, 0.66F * pulse, 0.92F, 1.0F, 1.0F, 0.96F);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(-age * 128.0F + entity.getId() * 5.0F));
        renderBillboardQuad(poseStack, coreConsumer, 1.0F * pulse, 0.2F, 0.58F, 1.0F, 0.34F);
        poseStack.popPose();

        poseStack.popPose();
        renderProjectileLightning(entity, age, poseStack, buffer);
    }

    private static void renderProjectileLightning(ChaosBarrageProjectileEntity entity, float age,
                                                  PoseStack poseStack, MultiBufferSource buffer) {
        int refreshFrame = Math.max(0, (int) age / 2);
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();
        for (int i = 0; i < 7; i++) {
            Random random = new Random(lightningSeed(entity.getId(), refreshFrame, i));
            Vec3 start = randomLightningPoint(random, 0.12F, 0.42F);
            Vec3 end = randomLightningPoint(random, 0.72F, 1.28F);
            int segments = 5 + random.nextInt(4);
            Vec3[] path = buildLightningPath(start, end, segments, 0.2F, random);
            float alpha = Mth.lerp(random.nextFloat(), 0.62F, 0.95F);
            float width = Mth.lerp(random.nextFloat(), 0.035F, 0.065F);
            renderLightningPath(matrix, consumer, path, width, alpha);
        }
    }

    private static Vec3 randomLightningPoint(Random random, float minRadius, float maxRadius) {
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double radius = Mth.lerp(random.nextFloat(), minRadius, maxRadius);
        double y = Mth.lerp(random.nextFloat(), -0.55F, 0.55F);
        return new Vec3(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
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
            renderBoltSegment(matrix, consumer, points[i - 1], points[i], width * taper * 2.3F,
                    0.28F, 0.66F, 1.0F, alpha * 0.28F);
            renderBoltSegment(matrix, consumer, points[i - 1], points[i], width * taper,
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

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                               int packedLight, float x, float y, float u, float v) {
        consumer.vertex(matrix, x, y, 0.0F)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }
}
