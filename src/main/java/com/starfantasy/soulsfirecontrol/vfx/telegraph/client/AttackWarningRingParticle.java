package com.starfantasy.soulsfirecontrol.vfx.telegraph.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;

public final class AttackWarningRingParticle extends TextureSheetParticle {
    private static final Map<Integer, AttackWarningRingParticle> ACTIVE_TRACKED_RINGS = new HashMap<>();
    private static final int ORANGE_WARNING_TICKS = 10;
    private final SpriteSet sprites;
    private final float initialRadius;
    private final int trackedEntityId;
    private final double trackedYOffset;
    private final boolean redWarning;
    private final boolean purpleWarning;

    private AttackWarningRingParticle(ClientLevel level, double x, double y, double z,
                                      double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        this.redWarning = xSpeed < 0.0D;
        this.purpleWarning = ySpeed < 0.0D;
        this.initialRadius = Math.max(0.1F, (float) Math.abs(xSpeed));
        this.lifetime = Math.max(1, (int) Math.round(Math.abs(ySpeed)));
        double trackingData = Math.abs(zSpeed);
        this.trackedEntityId = (int) Math.floor(trackingData);
        this.trackedYOffset = (trackingData - this.trackedEntityId) * 10.0D;
        this.quadSize = this.initialRadius;
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.alpha = 1.0F;
        this.updateColor();
        this.xd = 0.0D;
        this.yd = 0.0D;
        this.zd = 0.0D;
        this.roll = 0.0F;
        this.oRoll = 0.0F;
        this.replaceTrackedRing();
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
        float remaining = this.remainingRatio(0.0F);
        this.quadSize = this.initialRadius * remaining;
        this.alpha = Math.max(0.2F, remaining);
        this.updateColor();
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public float getQuadSize(float partialTick) {
        return this.initialRadius * this.remainingRatio(partialTick);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void remove() {
        super.remove();
        if (this.trackedEntityId > 0 && ACTIVE_TRACKED_RINGS.get(this.trackedEntityId) == this) {
            ACTIVE_TRACKED_RINGS.remove(this.trackedEntityId);
        }
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 15728880;
    }

    private float remainingRatio(float partialTick) {
        float progress = (this.age + partialTick) / Math.max(1.0F, (float) this.lifetime);
        return Math.max(0.0F, 1.0F - progress);
    }

    private void updateColor() {
        if (this.purpleWarning) {
            this.rCol = 0.66F;
            this.gCol = 0.08F;
            this.bCol = 1.0F;
            return;
        }
        if (this.redWarning) {
            this.rCol = 1.0F;
            this.gCol = 0.05F;
            this.bCol = 0.03F;
            return;
        }
        int ticksLeft = Math.max(0, this.lifetime - this.age);
        this.rCol = 1.0F;
        this.gCol = ticksLeft <= ORANGE_WARNING_TICKS ? 0.36F : 0.86F;
        this.bCol = ticksLeft <= ORANGE_WARNING_TICKS ? 0.03F : 0.08F;
    }

    private void replaceTrackedRing() {
        if (this.trackedEntityId <= 0) {
            return;
        }
        AttackWarningRingParticle previous = ACTIVE_TRACKED_RINGS.put(this.trackedEntityId, this);
        if (previous != null && previous != this) {
            previous.remove();
        }
    }

    private void updateTrackedPosition() {
        if (this.trackedEntityId <= 0) {
            return;
        }
        Entity trackedEntity = this.level.getEntity(this.trackedEntityId);
        if (trackedEntity != null && trackedEntity.isAlive()) {
            this.setPos(trackedEntity.getX(), trackedEntity.getY() + this.trackedYOffset, trackedEntity.getZ());
        }
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new AttackWarningRingParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
