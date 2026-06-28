package com.starfantasy.soulsfirecontrol.vfx.telegraph.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.particles.SimpleParticleType;

public final class RoarWaveParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final float endSize;
    private final float increase;
    private final boolean horizontal;

    private RoarWaveParticle(ClientLevel level, double x, double y, double z,
                             double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        this.horizontal = xSpeed < 0.0D;
        float startSize = Math.max(0.1F, (float) Math.abs(xSpeed));
        this.endSize = Math.max(startSize, (float) Math.abs(zSpeed));
        this.increase = (this.endSize - startSize) / Math.max(1.0F, (float) Math.abs(ySpeed) * 0.5F);
        this.lifetime = Math.max(1, (int) Math.round(Math.abs(ySpeed)));
        this.quadSize = startSize;
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.alpha = 0.55F;
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        this.xd = 0.0D;
        this.yd = 0.0D;
        this.zd = 0.0D;
        this.roll = this.random.nextFloat() * (float) Math.PI;
        this.oRoll = this.roll;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        float progress = this.progress(0.0F);
        this.quadSize = Math.min(this.endSize, this.quadSize + this.increase);
        this.alpha = Math.max(0.0F, 0.55F * (1.0F - progress));
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public float getQuadSize(float partialTick) {
        return Math.min(this.endSize, this.quadSize + this.increase * partialTick);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        if (!this.horizontal) {
            super.render(buffer, camera, partialTick);
            return;
        }
        Vec3 cameraPos = camera.getPosition();
        float x = (float) (Mth.lerp((double) partialTick, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp((double) partialTick, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp((double) partialTick, this.zo, this.z) - cameraPos.z());
        float size = this.getQuadSize(partialTick);
        int light = this.getLightColor(partialTick);
        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        buffer.vertex(x - size, y, z - size).uv(u0, v1).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
        buffer.vertex(x - size, y, z + size).uv(u0, v0).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
        buffer.vertex(x + size, y, z + size).uv(u1, v0).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
        buffer.vertex(x + size, y, z - size).uv(u1, v1).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 15728880;
    }

    private float progress(float partialTick) {
        return Math.min(1.0F, (this.age + partialTick) / Math.max(1.0F, (float) this.lifetime));
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new RoarWaveParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
