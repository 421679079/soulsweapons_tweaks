package com.starfantasy.soulsfirecontrol.mixin;

import com.starfantasy.soulsfirecontrol.combat.guard.MoonknightGuardBreakTracker;
import com.starfantasy.soulsfirecontrol.config.ChaosMonarchConfig;
import net.soulsweaponry.entity.mobs.Moonknight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Moonknight.class, remap = false)
public abstract class MoonknightMixin {
    @Inject(method = "m_8107_", at = @At("TAIL"))
    private void starfantasy$tickMoonknightGuardBreak(CallbackInfo ci) {
        MoonknightGuardBreakTracker.tick((Moonknight) (Object) this);
    }

    @ModifyVariable(method = "m_6469_", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float starfantasy$applyPhaseTwoDamageMultiplier(float amount) {
        Moonknight boss = (Moonknight) (Object) this;
        if (amount <= 0.0F || !boss.isPhaseTwo() || MoonknightGuardBreakTracker.isGuardBroken(boss)) {
            return amount;
        }
        return amount * ChaosMonarchConfig.getMoonknightPhaseTwoNormalDamageMultiplier();
    }
}
