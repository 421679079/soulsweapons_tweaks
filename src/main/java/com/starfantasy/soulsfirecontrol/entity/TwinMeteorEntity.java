package com.starfantasy.soulsfirecontrol.entity;

import com.starfantasy.soulsfirecontrol.combat.effect.ConfiguredMobEffect;
import com.starfantasy.soulsfirecontrol.config.ChaosMonarchConfig;
import com.starfantasy.soulsfirecontrol.util.NightProwlerTweaks;
import com.starfantasy.soulsfirecontrol.vfx.telegraph.TelegraphVfx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.soulsweaponry.entity.mobs.DayStalker;
import net.soulsweaponry.entity.mobs.NightProwler;
import net.soulsweaponry.entity.mobs.WarmthEntity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class TwinMeteorEntity extends Entity {
    private static final double EXPLOSION_RADIUS = 2.0D;
    private static final double EXPLOSION_VISUAL_SIZE = EXPLOSION_RADIUS * 2.0D;
    private static final float DAMAGE = 20.0F;

    @Nullable
    private UUID ownerUuid;
    @Nullable
    private LivingEntity cachedOwner;
    private double impactX;
    private double impactY;
    private double impactZ;
    private int warningTicks = 40;

    public TwinMeteorEntity(EntityType<? extends TwinMeteorEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public void configure(@Nullable LivingEntity owner, Vec3 impact, int warningTicks) {
        this.cachedOwner = owner;
        this.ownerUuid = owner == null ? null : owner.getUUID();
        this.impactX = impact.x;
        this.impactY = impact.y;
        this.impactZ = impact.z;
        this.warningTicks = Math.max(1, warningTicks);
        Vec3 velocity = impact.subtract(this.position()).scale(1.0D / this.warningTicks);
        this.setDeltaMovement(velocity);
    }

    public boolean isDayMeteor() {
        return this.getType() == TwinMeteorEntityRegistry.DAY_STALKER_METEOR.get();
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        this.noPhysics = true;
        this.setNoGravity(true);
        this.move(MoverType.SELF, this.getDeltaMovement());
        if (this.level().isClientSide()) {
            this.spawnClientTrail();
            return;
        }
        if (this.tickCount >= this.warningTicks) {
            this.detonate();
            this.discard();
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            this.ownerUuid = tag.getUUID("Owner");
        }
        this.impactX = tag.getDouble("ImpactX");
        this.impactY = tag.getDouble("ImpactY");
        this.impactZ = tag.getDouble("ImpactZ");
        this.warningTicks = Math.max(1, tag.getInt("WarningTicks"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.ownerUuid != null) {
            tag.putUUID("Owner", this.ownerUuid);
        }
        tag.putDouble("ImpactX", this.impactX);
        tag.putDouble("ImpactY", this.impactY);
        tag.putDouble("ImpactZ", this.impactZ);
        tag.putInt("WarningTicks", this.warningTicks);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private void spawnClientTrail() {
        Vec3 motion = this.getDeltaMovement().scale(-0.2D);
        double spread = this.isDayMeteor() ? 0.18D : 0.22D;
        for (int i = 0; i < 3; ++i) {
            double x = this.getX() + (this.random.nextDouble() - 0.5D) * spread;
            double y = this.getY() + (this.random.nextDouble() - 0.5D) * spread;
            double z = this.getZ() + (this.random.nextDouble() - 0.5D) * spread;
            if (this.isDayMeteor()) {
                this.level().addParticle(ParticleTypes.FLAME, x, y, z, motion.x, motion.y, motion.z);
                if (i == 0) {
                    this.level().addParticle(ParticleTypes.SMOKE, x, y, z, motion.x * 0.4D, motion.y * 0.4D, motion.z * 0.4D);
                }
            } else {
                this.level().addParticle(ParticleTypes.DRAGON_BREATH, x, y, z, motion.x, motion.y, motion.z);
                if (i == 0) {
                    this.level().addParticle(ParticleTypes.PORTAL, x, y, z, motion.x * 0.3D, motion.y * 0.3D, motion.z * 0.3D);
                }
            }
        }
    }

    private void detonate() {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        LivingEntity owner = this.resolveOwner();
        Vec3 center = new Vec3(this.impactX, this.impactY, this.impactZ);
        level.playSound(null, center.x, center.y, center.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0F, 1.0F);
        TelegraphVfx.swordExplosion(this, center, EXPLOSION_VISUAL_SIZE);
        TelegraphVfx.horizontalRoarWave(this, center, 1.0D, 14, EXPLOSION_VISUAL_SIZE);

        DamageSource source = owner == null
                ? this.damageSources().magic()
                : this.damageSources().indirectMagic(this, owner);
        AABB searchBox = new AABB(center, center).inflate(EXPLOSION_RADIUS);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, searchBox)) {
            if (this.shouldSkipTarget(owner, target)
                    || !intersectsSphere(target.getBoundingBox(), center, EXPLOSION_RADIUS)) {
                continue;
            }
            if (target.hurt(source, DAMAGE)) {
                for (ConfiguredMobEffect effect : this.hitEffects(owner)) {
                    target.addEffect(effect.createInstance());
                }
            }
        }
    }

    private List<ConfiguredMobEffect> hitEffects(@Nullable LivingEntity owner) {
        if (owner instanceof DayStalker) {
            return ConfiguredMobEffect.parseList(ChaosMonarchConfig.getDayStalkerMeteorHitEffects());
        }
        if (owner instanceof NightProwler) {
            return ConfiguredMobEffect.parseList(ChaosMonarchConfig.getNightProwlerMeteorHitEffects());
        }
        return List.of();
    }

    private boolean shouldSkipTarget(@Nullable LivingEntity owner, LivingEntity target) {
        if (target == owner || !target.isAlive() || target.isInvulnerable()
                || target instanceof DayStalker || target instanceof NightProwler
                || target instanceof WarmthEntity) {
            return true;
        }
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return true;
        }
        if (owner instanceof DayStalker dayStalker && dayStalker.isPartner(target)) {
            return true;
        }
        if (owner instanceof NightProwler nightProwler && nightProwler.isPartner(target)) {
            return true;
        }
        return NightProwlerTweaks.areSummonAllies(owner, target);
    }

    @Nullable
    private LivingEntity resolveOwner() {
        if (this.cachedOwner != null && this.cachedOwner.isAlive()) {
            return this.cachedOwner;
        }
        if (this.ownerUuid == null || !(this.level() instanceof ServerLevel level)) {
            return null;
        }
        Entity entity = level.getEntity(this.ownerUuid);
        if (entity instanceof LivingEntity living) {
            this.cachedOwner = living;
            return living;
        }
        return null;
    }

    private static boolean intersectsSphere(AABB box, Vec3 center, double radius) {
        double dx = distanceToInterval(center.x, box.minX, box.maxX);
        double dy = distanceToInterval(center.y, box.minY, box.maxY);
        double dz = distanceToInterval(center.z, box.minZ, box.maxZ);
        return dx * dx + dy * dy + dz * dz <= radius * radius;
    }

    private static double distanceToInterval(double value, double min, double max) {
        if (value < min) {
            return min - value;
        }
        if (value > max) {
            return value - max;
        }
        return 0.0D;
    }
}
