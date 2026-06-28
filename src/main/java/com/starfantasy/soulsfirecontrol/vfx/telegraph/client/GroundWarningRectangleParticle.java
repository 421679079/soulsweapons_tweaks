package com.starfantasy.soulsfirecontrol.vfx.telegraph.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class GroundWarningRectangleParticle extends TextureSheetParticle {
    private static final float BASE_ALPHA = 0.34F;
    private static final int FADE_OUT_TICKS = 4;
    private static final double TRACKING_SENTINEL = 1000.0D;
    private static final double TRACKING_FREEZE_SCALE = 1000.0D;

    private final SpriteSet sprites;
    private final float halfWidth;
    private final float halfLength;
    private final boolean redWarning;
    private final boolean purpleWarning;
    private final int trackedEntityId;
    private final boolean trackGroundBelow;
    private final int trackingFreezeTicks;

    private GroundWarningRectangleParticle(ClientLevel level, double x, double y, double z,
                                           double xSpeed, double ySpeed, double zSpeed,
                                           SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        double encodedZSpeed = Math.abs(zSpeed);
        this.purpleWarning = xSpeed < 0.0D && zSpeed < 0.0D && encodedZSpeed < TRACKING_SENTINEL;
        this.redWarning = xSpeed < 0.0D && !this.purpleWarning;
        this.halfWidth = Math.max(0.1F, (float) Math.abs(xSpeed));
        this.trackGroundBelow = zSpeed < 0.0D && !this.purpleWarning;
        if (encodedZSpeed >= TRACKING_SENTINEL) {
            this.halfLength = this.halfWidth;
            double trackingData = encodedZSpeed - TRACKING_SENTINEL;
            this.trackedEntityId = (int) Math.floor(trackingData);
            double freezeFraction = trackingData - this.trackedEntityId;
            this.trackingFreezeTicks = freezeFraction > 0.0D
                    ? Math.max(1, (int) Math.round(freezeFraction * TRACKING_FREEZE_SCALE))
                    : 0;
        } else {
            this.halfLength = Math.max(0.1F, (float) encodedZSpeed);
            this.trackedEntityId = 0;
            this.trackingFreezeTicks = 0;
        }
        this.lifetime = Math.max(1, (int) Math.round(Math.abs(ySpeed)));
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.alpha = BASE_ALPHA;
        if (this.purpleWarning) {
            this.rCol = 0.72F;
            this.gCol = 0.12F;
            this.bCol = 1.0F;
        } else if (this.redWarning) {
            this.rCol = 1.0F;
            this.gCol = 0.05F;
            this.bCol = 0.03F;
        } else {
            this.rCol = 1.0F;
            this.gCol = 0.86F;
            this.bCol = 0.08F;
        }
        this.xd = 0.0D;
        this.yd = 0.0D;
        this.zd = 0.0D;
        this.updateTrackedPosition();
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.updateTrackedPosition();
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        this.alpha = BASE_ALPHA * fadeFactor(0.0F);
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        Vec3 cameraPos = camera.getPosition();
        float x = (float) (Mth.lerp((double) partialTick, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp((double) partialTick, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp((double) partialTick, this.zo, this.z) - cameraPos.z());
        float progress = easedProgress(partialTick);
        float fade = fadeFactor(partialTick);
        int light = this.getLightColor(partialTick);

        drawRect(buffer, x, y, z, this.halfWidth, this.halfLength, 0.13F * fade, light);
        drawRect(buffer, x, y + 0.01F, z, this.halfWidth * progress, this.halfLength * progress,
                0.36F * fade, light);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 15728880;
    }

    private void drawRect(VertexConsumer buffer, float x, float y, float z,
                          float width, float length, float alpha, int light) {
        if (width <= 0.01F || length <= 0.01F || alpha <= 0.0F) {
            return;
        }
        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        buffer.vertex(x - width, y, z - length).uv(u0, v1).color(this.rCol, this.gCol, this.bCol, alpha).uv2(light).endVertex();
        buffer.vertex(x - width, y, z + length).uv(u0, v0).color(this.rCol, this.gCol, this.bCol, alpha).uv2(light).endVertex();
        buffer.vertex(x + width, y, z + length).uv(u1, v0).color(this.rCol, this.gCol, this.bCol, alpha).uv2(light).endVertex();
        buffer.vertex(x + width, y, z - length).uv(u1, v1).color(this.rCol, this.gCol, this.bCol, alpha).uv2(light).endVertex();
    }

    private float easedProgress(float partialTick) {
        float progress = Math.min(1.0F, (this.age + partialTick) / Math.max(1.0F, (float) this.lifetime));
        float sine = Mth.sin(progress * (float) (Math.PI * 0.5D));
        return sine * sine;
    }

    private float fadeFactor(float partialTick) {
        float remainingTicks = Math.max(0.0F, this.lifetime - this.age - partialTick);
        if (remainingTicks >= FADE_OUT_TICKS) {
            return 1.0F;
        }
        return Mth.clamp(remainingTicks / (float) FADE_OUT_TICKS, 0.0F, 1.0F);
    }

    private void updateTrackedPosition() {
        if (this.trackedEntityId <= 0 || (this.trackingFreezeTicks > 0 && this.age >= this.trackingFreezeTicks)) {
            return;
        }
        Entity trackedEntity = this.level.getEntity(this.trackedEntityId);
        if (trackedEntity != null && trackedEntity.isAlive()) {
            Vec3 trackedPos = this.trackGroundBelow ? this.groundCenterBelow(trackedEntity)
                    : trackedEntity.position().add(0.0D, 0.06D, 0.0D);
            this.setPos(trackedPos.x, trackedPos.y, trackedPos.z);
        }
    }

    private Vec3 groundCenterBelow(Entity trackedEntity) {
        int x = Mth.floor(trackedEntity.getX());
        int z = Mth.floor(trackedEntity.getZ());
        int startY = Math.min(this.level.getMaxBuildHeight() - 1, Mth.floor(trackedEntity.getY()));
        int minY = this.level.getMinBuildHeight() + 1;
        for (int y = startY; y >= minY; --y) {
            BlockPos feet = new BlockPos(x, y, z);
            if (this.level.getBlockState(feet.below()).isFaceSturdy(this.level, feet.below(), Direction.UP)
                    && !this.level.getBlockState(feet).blocksMotion()) {
                return new Vec3(trackedEntity.getX(), y + 0.06D, trackedEntity.getZ());
            }
        }
        return trackedEntity.position().add(0.0D, 0.06D, 0.0D);
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new GroundWarningRectangleParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
