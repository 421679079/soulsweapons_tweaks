package com.starfantasy.soulsfirecontrol.mixin;

import com.starfantasy.soulsfirecontrol.combat.buff.BossPhaseBuffManager;
import com.starfantasy.soulsfirecontrol.combat.guard.DayStalkerGuardBreakTracker;
import com.starfantasy.soulsfirecontrol.combat.twin.TwinBossCombatEffects;
import com.starfantasy.soulsfirecontrol.config.ChaosMonarchConfig;
import net.soulsweaponry.entity.mobs.DayStalker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DayStalker.class, remap = false)
public abstract class DayStalkerMixin {
    @Inject(method = "m_8107_", at = @At("TAIL"))
    private void starfantasy$tickDayStalkerTweaks(CallbackInfo ci) {
        DayStalker boss = (DayStalker) (Object) this;
        DayStalkerGuardBreakTracker.tick(boss);
        BossPhaseBuffManager.tickDayStalker(boss);
        TwinBossCombatEffects.tickDayStalker(boss);
    }

    @ModifyVariable(method = "m_6469_", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float starfantasy$applyPhaseDamageMultiplier(float amount) {
        DayStalker boss = (DayStalker) (Object) this;
        if (amount <= 0.0F) {
            return amount;
        }
        if (!boss.isPhaseTwo()) {
            return amount * ChaosMonarchConfig.getDayStalkerPhaseOneDamageMultiplier();
        }
        if (DayStalkerGuardBreakTracker.isStunned(boss)) {
            return amount;
        }
        return amount * ChaosMonarchConfig.getDayStalkerPhaseTwoNormalDamageMultiplier();
    }
}
