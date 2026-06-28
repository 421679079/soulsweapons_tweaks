package com.starfantasy.soulsfirecontrol.mixin;

import com.starfantasy.soulsfirecontrol.combat.guard.MoonknightGuardBreakTracker;
import com.starfantasy.soulsfirecontrol.util.MoonknightTweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.soulsweaponry.entity.mobs.Moonknight;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.soulsweaponry.entity.ai.goal.MoonknightGoal", remap = false)
public abstract class MoonknightGoalMixin {
    @Shadow
    @Final
    private Moonknight boss;

    @Shadow
    private int attackStatus;

    @Shadow
    private BlockPos targetPos;

    @Inject(method = "checkAttackPhaseTwo", at = @At("HEAD"), cancellable = true)
    private void starfantasy$forceCoreBeamAfterGuardBreak(Moonknight.MoonknightPhaseTwo specificPhaseTwo,
                                                          LivingEntity target, CallbackInfo ci) {
        if (specificPhaseTwo == null && MoonknightGuardBreakTracker.shouldForceCoreBeam(this.boss)) {
            this.boss.setPhaseTwoAttack(Moonknight.MoonknightPhaseTwo.CORE_BEAM);
            MoonknightGuardBreakTracker.markCoreBeamStarted(this.boss);
            ci.cancel();
        }
    }

    @Inject(method = "randomAttackPhaseTwo", at = @At("RETURN"), cancellable = true)
    private void starfantasy$removeCoreBeamFromNormalPool(CallbackInfoReturnable<Moonknight.MoonknightPhaseTwo> cir) {
        if (cir.getReturnValue().equals(Moonknight.MoonknightPhaseTwo.CORE_BEAM)) {
            cir.setReturnValue(Moonknight.MoonknightPhaseTwo.HEAVY_SWING);
        }
    }

    @Inject(method = "maceOfSpadesLogic", at = @At("HEAD"))
    private void starfantasy$warnMoonknightMace(LivingEntity target, CallbackInfo ci) {
        MoonknightTweaks.warnMaceOpening(this.boss, this.attackStatus);
        MoonknightTweaks.warnMaceSecond(this.boss, this.attackStatus,
                target == null ? null : target.blockPosition());
    }

    @Inject(method = "obliterateLogic", at = @At("HEAD"))
    private void starfantasy$warnMoonknightObliterate(LivingEntity target, int hitFrame, int followTargetTicks,
                                                      int attackFinishedTicks, float damage, SoundEvent sound,
                                                      boolean isSoundDelayed, CallbackInfo ci) {
        BlockPos warningPos = this.targetPos != null ? this.targetPos : target == null ? null : target.blockPosition();
        MoonknightTweaks.warnObliterate(this.boss, this.attackStatus, warningPos);
    }

    @Inject(method = "swordOfLight", at = @At("HEAD"))
    private void starfantasy$warnMoonknightSwordOfLight(LivingEntity target, CallbackInfo ci) {
        MoonknightTweaks.warnSwordOfLight(this.boss, this.attackStatus, target);
    }

    @Inject(method = "moonveilLogic", at = @At("HEAD"))
    private void starfantasy$warnMoonknightMoonveil(LivingEntity target, CallbackInfo ci) {
        MoonknightTweaks.warnMoonveil(this.boss, this.attackStatus);
    }

    @Inject(method = "thrustLogic", at = @At("HEAD"))
    private void starfantasy$warnMoonknightThrust(LivingEntity target, CallbackInfo ci) {
        MoonknightTweaks.warnThrust(this.boss, this.attackStatus, target);
    }

    @Inject(method = "coreBeam", at = @At("HEAD"))
    private void starfantasy$warnMoonknightCoreBeam(LivingEntity target, CallbackInfo ci) {
        MoonknightTweaks.warnCoreBeam(this.boss, this.attackStatus, target);
    }

    @Inject(method = "heavySwing", at = @At("HEAD"))
    private void starfantasy$warnMoonknightHeavySwing(LivingEntity target, CallbackInfo ci) {
        MoonknightTweaks.warnHeavySwing(this.boss, this.attackStatus);
    }

    @Inject(method = "rupturePhase2", at = @At("HEAD"))
    private void starfantasy$warnMoonknightPhaseTwoRupture(CallbackInfo ci) {
        MoonknightTweaks.warnObliterate(this.boss, this.attackStatus,
                this.targetPos == null ? this.boss.blockPosition() : this.targetPos);
        MoonknightTweaks.warnRupture(this.boss, this.attackStatus);
    }

    @Inject(method = "spawnPillar", at = @At("HEAD"))
    private void starfantasy$warnMoonknightPillarGround(Vec3 pos, int warmup, CallbackInfo ci) {
        MoonknightTweaks.warnMoonlightPillarGround(this.boss, pos, warmup);
    }

    @Inject(method = "ruptureLogic", at = @At("HEAD"))
    private void starfantasy$warnMoonknightRupture(CallbackInfo ci) {
        MoonknightTweaks.warnRupture(this.boss, this.attackStatus);
    }

    @Inject(method = "blindingLightLogic", at = @At("HEAD"))
    private void starfantasy$warnMoonknightBlindingLight(CallbackInfo ci) {
        MoonknightTweaks.warnBlind(this.boss, this.attackStatus);
    }

    @Redirect(method = "summonRemnant",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;m_7967_(Lnet/minecraft/world/entity/Entity;)Z",
                    remap = false))
    private boolean starfantasy$markMoonknightSummonsNoLoot(Level level, Entity entity) {
        MoonknightTweaks.prepareNoLootSummon(entity);
        return level.addFreshEntity(entity);
    }

}
