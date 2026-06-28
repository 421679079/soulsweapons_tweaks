package com.starfantasy.soulsfirecontrol.mixin;

import com.starfantasy.soulsfirecontrol.combat.draugr.DraugrActionLockTracker;
import com.starfantasy.soulsfirecontrol.combat.guard.GuardBreakTracker;
import com.starfantasy.soulsfirecontrol.util.DraugrBossTweaks;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.soulsweaponry.config.ConfigConstructor;
import net.soulsweaponry.entity.mobs.DraugrBoss;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.soulsweaponry.entity.ai.goal.DraugrBossGoal", remap = false)
public abstract class DraugrBossGoalMixin {
    @Shadow
    @Final
    private DraugrBoss boss;

    @Shadow
    private int attackStatus;

    @Shadow
    private int specialCooldown;

    @Shadow
    private boolean hasPostureBroken;

    @Shadow
    protected abstract boolean isInMeleeRange(LivingEntity target);

    @Shadow
    public abstract boolean applyDamage(LivingEntity target, float baseDamage);

    @Shadow
    public abstract void reset(float cooldownModifier, boolean shieldUp);

    @Inject(method = "reset", at = @At("HEAD"))
    private void starfantasy$clearActionLockOnReset(float cooldownModifier, boolean shieldUp, CallbackInfo ci) {
        DraugrActionLockTracker.clear(this.boss);
    }

    @Inject(method = "singleTarget", at = @At("HEAD"), cancellable = true)
    private void starfantasy$replaceSingleTargetWithoutBleed(LivingEntity target, int maxTicks, int[] frames,
                                                             float damage, float knockback, boolean applyBleed,
                                                             boolean disableShield, boolean shieldUpWhenDone,
                                                             CallbackInfo ci) {
        boolean parry = this.starfantasy$isParryAttack(maxTicks, frames, damage, knockback);
        this.attackStatus = DraugrActionLockTracker.normalizeAttackStatus(this.boss, this.attackStatus, maxTicks);
        DraugrActionLockTracker.lockCurrentAction(this.boss, maxTicks);
        if (!parry) {
            DraugrBossTweaks.warnSingleTargetFrames(this.boss, this.attackStatus, frames, knockback);
        }
        ++this.attackStatus;
        if (parry) {
            DraugrBossTweaks.warnParrySwordAfterShield(this.boss, this.attackStatus);
        }
        this.boss.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 20, false, true));
        if (disableShield) {
            this.boss.updateDisableShield(true);
        }
        for (int frame : frames) {
            if (this.attackStatus == frame && this.isInMeleeRange(target) && this.applyDamage(target, damage)) {
                this.boss.level().playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                        SoundSource.HOSTILE, 1.0F, 1.0F);
                if (this.boss.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getEyeY(),
                            target.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
                if (knockback > 0.0F) {
                    double x = target.getX() - this.boss.getX();
                    double z = target.getZ() - this.boss.getZ();
                    target.knockback(knockback, -x, -z);
                }
            }
            if (this.attackStatus == frame) {
                DraugrBossTweaks.warnNextSingleTargetFrame(this.boss, this.attackStatus, frames, knockback);
            }
        }
        if (this.attackStatus >= maxTicks) {
            this.reset(1.0F, shieldUpWhenDone);
        }
        ci.cancel();
    }

    private boolean starfantasy$isParryAttack(int maxTicks, int[] frames, float damage, float knockback) {
        return this.boss.getState() == DraugrBoss.States.PARRY
                && maxTicks == 40
                && frames.length == 1
                && frames[0] == 26
                && damage == 18.0F
                && knockback == 0.0F;
    }

    @Inject(method = "leapAttack", at = @At("HEAD"))
    private void starfantasy$warnLeapAttack(LivingEntity target, float damage, boolean stunTarget, CallbackInfo ci) {
        this.attackStatus = DraugrActionLockTracker.normalizeAttackStatus(this.boss, this.attackStatus, 35);
        DraugrActionLockTracker.lockCurrentAction(this.boss, 35);
        DraugrBossTweaks.warnLeap(this.boss, this.attackStatus);
    }

    @Inject(method = "runThrust", at = @At("HEAD"), cancellable = true)
    private void starfantasy$replaceRunThrustWithoutBleed(LivingEntity target, CallbackInfo ci) {
        this.attackStatus = DraugrActionLockTracker.normalizeAttackStatus(this.boss, this.attackStatus, 23);
        DraugrActionLockTracker.lockCurrentAction(this.boss, 23);
        DraugrBossTweaks.warnRunThrust(this.boss, this.attackStatus);
        ++this.attackStatus;
        if (this.attackStatus <= 10) {
            this.boss.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 5, 3));
        } else {
            this.boss.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 20, false, true));
        }
        if (this.attackStatus == 13 && this.isInMeleeRange(target) && this.applyDamage(target, 16.0F)) {
            this.boss.level().playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                    SoundSource.HOSTILE, 1.0F, 1.0F);
            if (this.boss.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getEyeY(),
                        target.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
        if (this.attackStatus >= 23) {
            this.reset(1.0F, false);
        }
        ci.cancel();
    }

    @Inject(method = "heavyBlow", at = @At("HEAD"))
    private void starfantasy$warnHeavyBlow(LivingEntity target, CallbackInfo ci) {
        this.attackStatus = DraugrActionLockTracker.normalizeAttackStatus(this.boss, this.attackStatus, 40);
        DraugrActionLockTracker.lockCurrentAction(this.boss, 40);
        DraugrBossTweaks.warnHeavy(this.boss, this.attackStatus);
    }

    @Inject(method = "m_8037_", at = @At("HEAD"), cancellable = true)
    private void starfantasy$disableVanillaHalfHealthPostureBreak(CallbackInfo ci) {
        this.hasPostureBroken = true;
        if (!this.boss.isSpawning()
                && !this.boss.isDeadOrDying()
                && !GuardBreakTracker.isStunned(this.boss)
                && this.boss.getTarget() == null
                && this.boss.getState() != DraugrBoss.States.IDLE
                && !DraugrActionLockTracker.isUninterruptibleOverride(this.boss.getState())) {
            this.reset(0.0F, false);
        }
        if (GuardBreakTracker.isStunned(this.boss)) {
            if (GuardBreakTracker.consumeStunStarted(this.boss)) {
                this.reset(1.0F, false);
            }
            this.boss.setPostureBroken(true);
            ci.cancel();
        }
    }

    @Inject(method = "backstep", at = @At("HEAD"))
    private void starfantasy$lockBackstep(LivingEntity target, CallbackInfo ci) {
        this.attackStatus = DraugrActionLockTracker.normalizeAttackStatus(this.boss, this.attackStatus, 30);
        DraugrActionLockTracker.lockCurrentAction(this.boss, 30);
    }

    @Inject(method = "aoe", at = @At("HEAD"), cancellable = true)
    private void starfantasy$warnAoeAndReplaceBattleCry(int maxTicks, int frame, float damage, float knockback,
                                                        MobEffect[] effects, double boxSize, boolean shieldUpWhenDone,
                                                        CallbackInfo ci) {
        this.attackStatus = DraugrActionLockTracker.normalizeAttackStatus(this.boss, this.attackStatus, maxTicks);
        DraugrActionLockTracker.lockCurrentAction(this.boss, maxTicks);
        if (DraugrBossTweaks.isBattleCry(damage, effects, boxSize, shieldUpWhenDone)) {
            ++this.attackStatus;
            this.boss.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 20, false, true));
            DraugrBossTweaks.tickBattleCry(this.boss, this.attackStatus, frame, boxSize);
            if (this.attackStatus >= maxTicks) {
                this.reset(2.0F, true);
                this.specialCooldown = (int) ConfigConstructor.old_champions_remains_special_cooldown_ticks;
            }
            ci.cancel();
            return;
        }
        if (damage > 0.0F) {
            DraugrBossTweaks.warnAoe(this.boss, this.attackStatus, frame, boxSize);
        }
    }
}
