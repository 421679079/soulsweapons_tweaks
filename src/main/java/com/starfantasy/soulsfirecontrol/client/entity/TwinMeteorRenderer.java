package com.starfantasy.soulsfirecontrol.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.starfantasy.soulsfirecontrol.entity.TwinMeteorEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class TwinMeteorRenderer extends EntityRenderer<TwinMeteorEntity> {
    private static final ResourceLocation DAY_TEXTURE =
            new ResourceLocation("minecraft", "textures/item/fire_charge.png");
    private static final ResourceLocation NIGHT_TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/enderdragon/dragon_fireball.png");
    private static final ResourceLocation FROST_TEXTURE =
            new ResourceLocation("soulsweapons_tweaks", "textures/entity/chaos_frost_meteor.png");

    public TwinMeteorRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(TwinMeteorEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        float scale = entity.isDayMeteor() ? 1.35F : 1.5F;
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
    public ResourceLocation getTextureLocation(TwinMeteorEntity entity) {
        if (entity.isFrostMeteor()) {
            return FROST_TEXTURE;
        }
        return entity.isDayMeteor() ? DAY_TEXTURE : NIGHT_TEXTURE;
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
