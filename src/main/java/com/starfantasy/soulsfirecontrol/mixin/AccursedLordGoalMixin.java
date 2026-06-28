package com.starfantasy.soulsfirecontrol.mixin;

import com.starfantasy.soulsfirecontrol.combat.guard.AccursedLordGuardBreakTracker;
import com.starfantasy.soulsfirecontrol.util.AccursedLordTweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.soulsweaponry.entity.mobs.AccursedLordBoss;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.soulsweaponry.entity.ai.goal.AccursedLordGoal", remap = false)
public abstract class AccursedLordGoalMixin {
    @Shadow
    @Final
    private AccursedLordBoss boss;

    @Shadow
    private int attackCooldown;

    @Shadow
    private int attackStatus;

    @Shadow
    private BlockPos attackPos;

    @Shadow
    private boolean cordsRegistered;

    @Shadow
    private int specialCooldown;

    @Shadow
    private int lavaRadius;

    @Shadow
    private int lavaTimer;

    @Inject(method = "m_8036_", at = @At("HEAD"), cancellable = true)
    private void starfantasy$disableGoalWhileStunned(CallbackInfoReturnable<Boolean> cir) {
        if (AccursedLordGuardBreakTracker.isStunned(this.boss)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "m_8037_", at = @At("HEAD"), cancellable = true)
    private void starfantasy$interruptGoalWhileStunned(CallbackInfo ci) {
        if (!AccursedLordGuardBreakTracker.isStunned(this.boss)) {
            return;
        }
        this.attackCooldown = 20;
        this.specialCooldown = Math.max(this.specialCooldown, 20);
        this.attackStatus = 0;
        this.cordsRegistered = false;
        this.attackPos = null;
        this.lavaRadius = 4;
        this.lavaTimer = 0;
        this.boss.getNavigation().stop();
        this.boss.removePlacedLava();
        this.boss.setAttackAnimation(AccursedLordBoss.AccursedLordAnimations.IDLE);
        ci.cancel();
    }

    @Inject(method = "swordSlam", at = @At("HEAD"))
    private void starfantasy$warnSwordSlam(CallbackInfo ci) {
        AccursedLordTweaks.warnSwordSlam(this.boss, this.attackStatus, this.attackPos);
    }

    @Inject(method = "projectileBarrage", at = @At("HEAD"))
    private void starfantasy$warnProjectileBarrage(LivingEntity target, double distanceToEntity,
                                                   @Coerce Object projectileType, CallbackInfo ci) {
        AccursedLordTweaks.warnProjectileBarrage(this.boss, this.attackStatus);
    }

    @Inject(method = "pullAttack", at = @At("HEAD"))
    private void starfantasy$warnPull(LivingEntity target, CallbackInfo ci) {
        AccursedLordTweaks.warnPull(this.boss, this.attackStatus);
    }

    @Redirect(method = "pullAttack",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;m_147240_(DDD)V",
                    remap = false))
    private void starfantasy$forcePullIgnoringKnockbackResistance(LivingEntity target, double strength,
                                                                  double x, double z) {
        AccursedLordTweaks.forcePullIgnoringKnockbackResistance(target, x, z, strength);
    }

    @Inject(method = "heatWaveAttack", at = @At("HEAD"))
    private void starfantasy$warnHeatWave(CallbackInfo ci) {
        AccursedLordTweaks.warnHeatwave(this.boss, this.attackStatus);
    }

    @Inject(method = "spinAttack", at = @At("HEAD"))
    private void starfantasy$warnSpin(CallbackInfo ci) {
        AccursedLordTweaks.warnSpin(this.boss, this.attackStatus);
    }
}
