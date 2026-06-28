package com.starfantasy.soulsfirecontrol.mixin;

import com.starfantasy.soulsfirecontrol.util.DayStalkerTweaks;
import com.starfantasy.soulsfirecontrol.util.NightProwlerTweaks;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.soulsweaponry.entity.AreaEffectSphere;
import net.soulsweaponry.entity.mobs.NightProwler;
import net.soulsweaponry.entity.projectile.NightSkull;
import net.soulsweaponry.registry.EffectRegistry;
import net.soulsweaponry.registry.ParticleRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NightSkull.class, remap = false)
public abstract class NightSkullMixin {
    @Unique
    private static final float STARFANTASY$EXPLOSION_POWER = 2.0F;
    @Unique
    private static final double STARFANTASY$EXPLOSION_RADIUS = STARFANTASY$EXPLOSION_POWER * 2.0D;

    @Unique
    private boolean starfantasy$managedDetonated;

    @Inject(method = "m_5790_", at = @At("HEAD"), cancellable = true)
    private void starfantasy$managedNightProwlerHit(EntityHitResult hitResult, CallbackInfo ci) {
        NightProwler boss = starfantasy$nightProwlerOwner();
        if (boss == null) {
            return;
        }
        Entity target = hitResult.getEntity();
        if (target instanceof LivingEntity living && !starfantasy$isFriendlyTarget(boss, living)) {
            NightSkull skull = (NightSkull) (Object) this;
            DamageSource source = boss.damageSources().indirectMagic(skull, boss);
            float rawDamage = Mth.ceil((float) skull.getDeltaMovement().length() * (float) skull.getBaseDamage());
            if (NightProwlerTweaks.hurtWithoutGuardBreak(
                    boss, living, source, NightProwlerTweaks.modifiedDamage(boss, rawDamage))) {
                starfantasy$applyNightSkullEffects(living);
            }
        }
        starfantasy$managedDetonate(boss);
        ci.cancel();
    }

    @Inject(method = "m_8060_", at = @At("HEAD"), cancellable = true)
    private void starfantasy$managedNightProwlerBlockHit(BlockHitResult hitResult, CallbackInfo ci) {
        NightProwler boss = starfantasy$nightProwlerOwner();
        if (boss == null) {
            return;
        }
        starfantasy$managedDetonate(boss);
        ci.cancel();
    }

    @Inject(method = "detonate", at = @At("HEAD"), cancellable = true)
    private void starfantasy$managedNightProwlerDetonate(CallbackInfo ci) {
        NightProwler boss = starfantasy$nightProwlerOwner();
        if (boss == null) {
            return;
        }
        starfantasy$managedDetonate(boss);
        ci.cancel();
    }

    @Unique
    private NightProwler starfantasy$nightProwlerOwner() {
        Entity owner = ((NightSkull) (Object) this).getOwner();
        return owner instanceof NightProwler boss ? boss : null;
    }

    @Unique
    private void starfantasy$managedDetonate(NightProwler boss) {
        NightSkull skull = (NightSkull) (Object) this;
        if (skull.level().isClientSide || this.starfantasy$managedDetonated) {
            return;
        }
        this.starfantasy$managedDetonated = true;
        Vec3 center = skull.position();
        Level level = skull.level();
        level.playSound(null, skull.getX(), skull.getY(), skull.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0F, 1.0F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, skull.getX(), skull.getY(), skull.getZ(),
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        starfantasy$damageExplosion(boss, center);
        starfantasy$spawnAreaEffect(boss, center);
        skull.discard();
    }

    @Unique
    private void starfantasy$damageExplosion(NightProwler boss, Vec3 center) {
        NightSkull skull = (NightSkull) (Object) this;
        AABB bounds = new AABB(center, center).inflate(STARFANTASY$EXPLOSION_RADIUS);
        DamageSource source = boss.damageSources().indirectMagic(skull, boss);
        for (LivingEntity target : skull.level().getEntitiesOfClass(LivingEntity.class, bounds)) {
            if (starfantasy$isFriendlyTarget(boss, target)) {
                continue;
            }
            double distanceFactor = Math.sqrt(target.distanceToSqr(center)) / STARFANTASY$EXPLOSION_RADIUS;
            if (distanceFactor > 1.0D) {
                continue;
            }
            double exposure = Explosion.getSeenPercent(center, target);
            double impact = (1.0D - distanceFactor) * exposure;
            float rawDamage = (float) ((impact * impact + impact) / 2.0D * 7.0D
                    * STARFANTASY$EXPLOSION_RADIUS + 1.0D);
            if (rawDamage <= 0.0F) {
                continue;
            }
            if (NightProwlerTweaks.hurtWithoutGuardBreak(
                    boss, target, source, NightProwlerTweaks.modifiedDamage(boss, rawDamage))) {
                starfantasy$applyNightSkullEffects(target);
            }
        }
    }

    @Unique
    private void starfantasy$spawnAreaEffect(NightProwler boss, Vec3 center) {
        NightSkull skull = (NightSkull) (Object) this;
        AreaEffectSphere area = new AreaEffectSphere(skull.level(), center.x(), center.y(), center.z());
        area.setOwner(boss);
        area.setParticleAmountModifier(2.0F);
        area.setParticleType((ParticleOptions) ParticleRegistry.DARK_STAR.get());
        area.setRadius(0.5F);
        area.setDuration(80);
        area.setRadiusGrowth((2.5F - area.getRadius()) / (float) area.getDuration());
        area.addEffect(new MobEffectInstance((MobEffect) EffectRegistry.DECAY.get(), 60, 0));
        area.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0));
        skull.level().addFreshEntity(area);
    }

    @Unique
    private void starfantasy$applyNightSkullEffects(LivingEntity target) {
        target.addEffect(new MobEffectInstance((MobEffect) EffectRegistry.DECAY.get(), 60, 0));
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0));
    }

    @Unique
    private boolean starfantasy$isFriendlyTarget(NightProwler boss, LivingEntity target) {
        return target == boss
                || boss.isPartner(target)
                || NightProwlerTweaks.areSummonAllies(boss, target)
                || DayStalkerTweaks.areWarmthAllies(boss, target);
    }
}
