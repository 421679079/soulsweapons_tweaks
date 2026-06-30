package com.starfantasy.soulsfirecontrol.mixin;

import com.starfantasy.soulsfirecontrol.combat.chaosmonarch.ChaosMonarchPhaseManager;
import com.starfantasy.soulsfirecontrol.combat.chaosmonarch.ChaosMonarchAmbienceManager;
import com.starfantasy.soulsfirecontrol.combat.guard.ChaosMonarchGuardBreakTracker;
import com.starfantasy.soulsfirecontrol.util.ChaosMonarchTweaks;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.soulsweaponry.entity.mobs.BossEntity;
import net.soulsweaponry.entity.mobs.ChaosMonarch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ChaosMonarch.class, remap = false)
public abstract class ChaosMonarchMixin extends BossEntity {
    private ChaosMonarchMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level, BossEvent.BossBarColor.PURPLE);
    }

    @Inject(method = "m_8107_", at = @At("TAIL"))
    private void starfantasy$tickChaosMonarchPhaseFramework(CallbackInfo ci) {
        ChaosMonarch boss = (ChaosMonarch) (Object) this;
        ChaosMonarchPhaseManager.tick(boss);
        ChaosMonarchAmbienceManager.tick(boss);
        ChaosMonarchGuardBreakTracker.tick(boss);
        if (boss.getAttack() != ChaosMonarch.Attack.MELEE
                || ChaosMonarchPhaseManager.isTransitioning(boss)
                || boss.isDeadOrDying()) {
            ChaosMonarchTweaks.clearMeleeClashWindow(boss);
        }
        ChaosMonarchTweaks.tickMeleeClashStates();
    }

    @Inject(method = "m_6469_", at = @At("HEAD"), cancellable = true)
    private void starfantasy$replaceChaosMonarchHurt(DamageSource source, float amount,
                                                     CallbackInfoReturnable<Boolean> cir) {
        ChaosMonarch boss = (ChaosMonarch) (Object) this;
        float modifiedAmount = ChaosMonarchPhaseManager.isTransitioning(boss)
                && !ChaosMonarchPhaseManager.bypassesPhaseLock(source) ? 0.0F : amount;
        cir.setReturnValue(super.hurt(source, modifiedAmount));
    }

    @Redirect(method = "m_8024_",
            at = @At(value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/mobs/ChaosMonarch;m_21023_(Lnet/minecraft/world/effect/MobEffect;)Z",
                    remap = false),
            remap = false,
            require = 0)
    private boolean starfantasy$disableDecayHealingBlock(ChaosMonarch boss, MobEffect effect) {
        return false;
    }
}
