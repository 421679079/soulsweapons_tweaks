package com.starfantasy.soulsfirecontrol.vfx.telegraph.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public final class SwordExplosionParticle extends TextureSheetParticle {
    private static final float DEFAULT_SIZE = 3.0F;
    private static final float MIN_RANDOM_SIZE_SCALE = 0.9F;
    private static final float MAX_RANDOM_SIZE_SCALE = 1.1F;
    private static final float FULL_ROTATION = (float) (Math.PI * 2.0D);
    private final SpriteSet sprites;

    private SwordExplosionParticle(ClientLevel level, double x, double y, double z,
                                   double size, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.lifetime = 6;
        float randomScale = MIN_RANDOM_SIZE_SCALE
                + this.random.nextFloat() * (MAX_RANDOM_SIZE_SCALE - MIN_RANDOM_SIZE_SCALE);
        this.quadSize = (float) Math.max(0.1D, size) * randomScale;
        this.roll = this.random.nextFloat() * FULL_ROTATION;
        this.oRoll = this.roll;
        this.hasPhysics = false;
        this.alpha = 1.0F;
        this.xd = 0.0D;
        this.yd = 0.0D;
        this.zd = 0.0D;
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
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 15728880;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            double size = xSpeed > 0.0D ? xSpeed : DEFAULT_SIZE;
            return new SwordExplosionParticle(level, x, y, z, size, this.sprites);
        }
    }
}
