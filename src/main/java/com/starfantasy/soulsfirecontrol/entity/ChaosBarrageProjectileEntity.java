package com.starfantasy.soulsfirecontrol.entity;

import com.starfantasy.soulsfirecontrol.combat.chaosmonarch.ChaosMonarchPhaseManager;
import com.starfantasy.soulsfirecontrol.combat.effect.ConfiguredMobEffect;
import com.starfantasy.soulsfirecontrol.config.ChaosMonarchConfig;
import com.starfantasy.soulsfirecontrol.util.ChaosMonarchTweaks;
import com.starfantasy.soulsfirecontrol.vfx.telegraph.TelegraphVfx;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.soulsweaponry.entity.mobs.ChaosMonarch;

import javax.annotation.Nullable;
import java.util.List;

public class ChaosBarrageProjectileEntity extends Projectile {
    public static final int STYLE_FIRE = 1;
    public static final int STYLE_FROST = 2;
    public static final int STYLE_MOONLIGHT = 3;
    public static final int STYLE_NIGHT = 4;
    public static final int STYLE_WITHER_SKULL = 5;

    private static final EntityDataAccessor<Integer> STYLE =
            SynchedEntityData.defineId(ChaosBarrageProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> VISUAL_ONLY =
            SynchedEntityData.defineId(ChaosBarrageProjectileEntity.class, EntityDataSerializers.BOOLEAN);

    private int effectPhase = 1;
    private float configuredDamage = 20.0F;
    private int maxAge = 80;

    public ChaosBarrageProjectileEntity(EntityType<? extends ChaosBarrageProjectileEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public void configureBarrage(@Nullable LivingEntity owner, int style, int effectPhase,
                                 Vec3 velocity, float damage) {
        this.setOwner(owner);
        this.setStyle(style);
        this.effectPhase = clampPhase(effectPhase);
        this.configuredDamage = Math.max(0.0F, damage);
        this.maxAge = 80;
        this.setVisualOnly(false);
        this.setDeltaMovement(velocity);
        this.setNoGravity(true);
    }

    public void configureVisualMeteor(@Nullable LivingEntity owner, Vec3 impact, int travelTicks) {
        int ticks = Math.max(1, travelTicks);
        this.setOwner(owner);
        this.setStyle(STYLE_WITHER_SKULL);
        this.effectPhase = STYLE_WITHER_SKULL;
        this.configuredDamage = 0.0F;
        this.maxAge = ticks;
        this.setVisualOnly(true);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setDeltaMovement(impact.subtract(this.position()).scale(1.0D / ticks));
    }

    public int getStyle() {
        return this.entityData.get(STYLE);
    }

    public boolean isVisualOnly() {
        return this.entityData.get(VISUAL_ONLY);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(STYLE, STYLE_FIRE);
        this.entityData.define(VISUAL_ONLY, false);
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);
        if (this.level().isClientSide()) {
            this.spawnClientTrail();
            this.moveByVelocity();
            return;
        }
        if (this.tickCount > this.maxAge) {
            this.discard();
            return;
        }
        if (!this.isVisualOnly()) {
            HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hit.getType() != HitResult.Type.MISS) {
                this.handleHit(hit);
                return;
            }
        }
        this.moveByVelocity();
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (this.isVisualOnly() || entity == this.getOwner() || entity instanceof Projectile) {
            return false;
        }
        if (!(entity instanceof LivingEntity living) || !living.isAlive()) {
            return false;
        }
        if (living instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }
        LivingEntity owner = this.resolveLivingOwner();
        if (owner instanceof ChaosMonarch boss && ChaosMonarchTweaks.shouldSkipTarget(boss, living)) {
            return false;
        }
        return super.canHitEntity(entity);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setStyle(tag.getInt("Style"));
        this.setVisualOnly(tag.getBoolean("VisualOnly"));
        this.effectPhase = tag.getInt("EffectPhase");
        this.configuredDamage = tag.getFloat("ConfiguredDamage");
        this.maxAge = Math.max(1, tag.getInt("MaxAge"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Style", this.getStyle());
        tag.putBoolean("VisualOnly", this.isVisualOnly());
        tag.putInt("EffectPhase", this.effectPhase);
        tag.putFloat("ConfiguredDamage", this.configuredDamage);
        tag.putInt("MaxAge", this.maxAge);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private void handleHit(HitResult hit) {
        this.detonate(hit instanceof EntityHitResult || hit instanceof BlockHitResult
                ? hit.getLocation()
                : this.position());
        this.discard();
    }

    private void detonate(Vec3 center) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        LivingEntity owner = this.resolveLivingOwner();
        ChaosMonarch boss = owner instanceof ChaosMonarch chaosMonarch ? chaosMonarch : null;
        this.playHitEffects(center);
        if (boss == null || this.configuredDamage <= 0.0F) {
            return;
        }
        DamageSource source = this.getStyle() == STYLE_WITHER_SKULL
                ? this.damageSources().fellOutOfWorld()
                : this.damageSources().indirectMagic(this, owner == null ? this : owner);
        List<ConfiguredMobEffect> effects = hitEffects(boss);
        double radius = 2.0D;
        AABB searchBox = new AABB(center, center).inflate(radius);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, searchBox)) {
            if (ChaosMonarchTweaks.shouldSkipTarget(boss, target)
                    || !intersectsSphere(target.getBoundingBox(), center, radius)) {
                continue;
            }
            if (ChaosMonarchTweaks.hurtWithoutGuardBreak(boss, target, source, this.configuredDamage)) {
                for (ConfiguredMobEffect effect : effects) {
                    target.addEffect(effect.createInstance());
                }
                if (this.getStyle() == STYLE_FIRE && this.effectPhase == 1) {
                    ChaosMonarchTweaks.igniteIfNotBurning(target, 20);
                }
            }
        }
    }

    private List<ConfiguredMobEffect> hitEffects(ChaosMonarch boss) {
        int phase = this.effectPhase > 0
                ? this.effectPhase
                : ChaosMonarchPhaseManager.getCurrentPhase(boss);
        return ConfiguredMobEffect.parseList(ChaosMonarchConfig.getChaosMonarchLightningHitEffects(phase));
    }

    private void playHitEffects(Vec3 center) {
        ServerLevel level = (ServerLevel) this.level();
        level.playSound(null, center.x, center.y, center.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0F, 1.0F);
        TelegraphVfx.swordExplosion(this, center, 4.0D);
        TelegraphVfx.horizontalRoarWave(this, center, 1.0D, 14, 4.0D);
    }

    private void spawnClientTrail() {
        if (this.getStyle() == STYLE_MOONLIGHT) {
            return;
        }
        Vec3 motion = this.getDeltaMovement().scale(-0.18D);
        ParticleOptions primary = switch (this.getStyle()) {
            case STYLE_FROST -> ParticleTypes.SNOWFLAKE;
            case STYLE_NIGHT, STYLE_WITHER_SKULL -> ParticleTypes.DRAGON_BREATH;
            default -> ParticleTypes.FLAME;
        };
        ParticleOptions secondary = switch (this.getStyle()) {
            case STYLE_FROST -> ParticleTypes.END_ROD;
            case STYLE_NIGHT, STYLE_WITHER_SKULL -> ParticleTypes.PORTAL;
            default -> ParticleTypes.SMOKE;
        };
        double spread = this.getStyle() == STYLE_WITHER_SKULL ? 0.3D : 0.22D;
        for (int i = 0; i < 3; ++i) {
            double x = this.getX() + (this.random.nextDouble() - 0.5D) * spread;
            double y = this.getY() + (this.random.nextDouble() - 0.5D) * spread;
            double z = this.getZ() + (this.random.nextDouble() - 0.5D) * spread;
            this.level().addParticle(primary, x, y, z, motion.x, motion.y, motion.z);
            if (i == 0) {
                this.level().addParticle(secondary, x, y, z,
                        motion.x * 0.35D, motion.y * 0.35D, motion.z * 0.35D);
            }
        }
    }

    private void moveByVelocity() {
        Vec3 movement = this.getDeltaMovement();
        this.setPos(this.getX() + movement.x, this.getY() + movement.y, this.getZ() + movement.z);
        ProjectileUtil.rotateTowardsMovement(this, 0.2F);
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

    @Nullable
    private LivingEntity resolveLivingOwner() {
        Entity owner = this.getOwner();
        return owner instanceof LivingEntity living ? living : null;
    }

    private void setStyle(int style) {
        this.entityData.set(STYLE, Math.max(STYLE_FIRE, Math.min(STYLE_WITHER_SKULL, style)));
    }

    private void setVisualOnly(boolean visualOnly) {
        this.entityData.set(VISUAL_ONLY, visualOnly);
    }

    private static int clampPhase(int phase) {
        return Math.max(1, Math.min(6, phase));
    }
}
