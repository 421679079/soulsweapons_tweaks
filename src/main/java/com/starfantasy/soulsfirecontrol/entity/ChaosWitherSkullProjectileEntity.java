package com.starfantasy.soulsfirecontrol.entity;

import com.starfantasy.soulsfirecontrol.combat.effect.ConfiguredMobEffect;
import com.starfantasy.soulsfirecontrol.config.ChaosMonarchConfig;
import com.starfantasy.soulsfirecontrol.util.ChaosMonarchTweaks;
import com.starfantasy.soulsfirecontrol.vfx.telegraph.TelegraphVfx;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.soulsweaponry.entity.mobs.ChaosMonarch;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ChaosWitherSkullProjectileEntity extends WitherSkull {
    private static final double EXPLOSION_RADIUS = 2.0D;
    private static final double EXPLOSION_VISUAL_SIZE = EXPLOSION_RADIUS * 2.0D;
    private static final int MAX_AGE_TICKS = 120;

    @Nullable
    private UUID ownerUuid;
    private int effectPhase = 5;
    private float configuredDamage = 10.0F;
    private boolean visualOnly;

    public ChaosWitherSkullProjectileEntity(EntityType<? extends WitherSkull> type, Level level) {
        super(type, level);
    }

    public void configureBarrage(ChaosMonarch boss, int effectPhase, Vec3 velocity, float damage) {
        this.setOwner(boss);
        this.ownerUuid = boss.getUUID();
        this.effectPhase = Math.max(1, Math.min(6, effectPhase));
        this.configuredDamage = Math.max(0.0F, damage);
        this.visualOnly = false;
        this.setDeltaMovement(velocity);
    }

    public void configureVisual(ChaosMonarch boss, Vec3 impact, int travelTicks) {
        int ticks = Math.max(1, travelTicks);
        this.setOwner(boss);
        this.ownerUuid = boss.getUUID();
        this.effectPhase = 5;
        this.configuredDamage = 0.0F;
        this.visualOnly = true;
        this.setDeltaMovement(impact.subtract(this.position()).scale(1.0D / ticks));
    }

    @Override
    public void tick() {
        this.baseTick();
        this.setNoGravity(true);
        if (this.tickCount > MAX_AGE_TICKS) {
            this.discard();
            return;
        }
        if (this.level().isClientSide()) {
            this.moveByVelocity();
            return;
        }
        if (!this.visualOnly) {
            HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hit.getType() != HitResult.Type.MISS) {
                this.onHit(hit);
                return;
            }
        }
        this.moveByVelocity();
    }

    @Override
    protected void onHit(HitResult result) {
        if (!this.level().isClientSide()) {
            if (!this.visualOnly) {
                this.detonate(result.getLocation());
            }
            this.discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (this.visualOnly || entity == this.getOwner() || entity instanceof Projectile) {
            return false;
        }
        if (!(entity instanceof LivingEntity living) || !living.isAlive()) {
            return false;
        }
        if (living instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }
        ChaosMonarch boss = this.resolveBoss();
        if (boss != null && ChaosMonarchTweaks.shouldSkipTarget(boss, living)) {
            return false;
        }
        return super.canHitEntity(entity);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.ownerUuid != null) {
            tag.putUUID("StarfantasyChaosMonarchOwner", this.ownerUuid);
        }
        tag.putInt("StarfantasyEffectPhase", this.effectPhase);
        tag.putFloat("StarfantasyDamage", this.configuredDamage);
        tag.putBoolean("StarfantasyVisualOnly", this.visualOnly);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("StarfantasyChaosMonarchOwner")) {
            this.ownerUuid = tag.getUUID("StarfantasyChaosMonarchOwner");
        }
        this.effectPhase = tag.getInt("StarfantasyEffectPhase");
        this.configuredDamage = tag.getFloat("StarfantasyDamage");
        this.visualOnly = tag.getBoolean("StarfantasyVisualOnly");
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private void detonate(Vec3 center) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        ChaosMonarch boss = this.resolveBoss();
        level.playSound(null, center.x, center.y, center.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0F, 1.0F);
        TelegraphVfx.swordExplosion(this, center, EXPLOSION_VISUAL_SIZE);
        TelegraphVfx.horizontalRoarWave(this, center, 1.0D, 14, EXPLOSION_VISUAL_SIZE);
        if (boss == null || this.configuredDamage <= 0.0F) {
            return;
        }
        DamageSource source = boss.damageSources().fellOutOfWorld();
        List<ConfiguredMobEffect> effects = ConfiguredMobEffect.parseList(
                ChaosMonarchConfig.getChaosMonarchLightningHitEffects(this.effectPhase));
        AABB searchBox = new AABB(center, center).inflate(EXPLOSION_RADIUS);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, searchBox)) {
            if (ChaosMonarchTweaks.shouldSkipTarget(boss, target)
                    || !intersectsSphere(target.getBoundingBox(), center, EXPLOSION_RADIUS)) {
                continue;
            }
            if (ChaosMonarchTweaks.hurtWithoutGuardBreak(boss, target, source, this.configuredDamage)) {
                for (ConfiguredMobEffect effect : effects) {
                    target.addEffect(effect.createInstance());
                }
            }
        }
    }

    private void moveByVelocity() {
        Vec3 movement = this.getDeltaMovement();
        this.setPos(this.getX() + movement.x, this.getY() + movement.y, this.getZ() + movement.z);
        ProjectileUtil.rotateTowardsMovement(this, 0.2F);
    }

    @Nullable
    private ChaosMonarch resolveBoss() {
        Entity owner = this.getOwner();
        if (owner instanceof ChaosMonarch boss) {
            return boss;
        }
        if (this.ownerUuid == null || !(this.level() instanceof ServerLevel level)) {
            return null;
        }
        Entity entity = level.getEntity(this.ownerUuid);
        return entity instanceof ChaosMonarch boss ? boss : null;
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
