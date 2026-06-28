package com.starfantasy.soulsfirecontrol.mixin;

import com.starfantasy.soulsfirecontrol.combat.guard.NightShadeGuardBreakTracker;
import net.soulsweaponry.entity.mobs.NightShade;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NightShade.class, remap = false)
public abstract class NightShadeMixin {
    @Shadow
    private boolean hasDuplicated;

    @Inject(method = "m_8107_", at = @At("HEAD"))
    private void starfantasy$disableDuplicatePhase(CallbackInfo ci) {
        NightShade boss = (NightShade) (Object) this;
        if (!boss.isCopy()) {
            this.hasDuplicated = true;
            if (boss.getAttackState() == NightShade.AttackStates.DUPLICATE) {
                boss.setAttackState(NightShade.AttackStates.IDLE);
            }
        }
    }

    @Inject(method = "m_8107_", at = @At("TAIL"))
    private void starfantasy$tickNightShadeTweaks(CallbackInfo ci) {
        NightShadeGuardBreakTracker.tick((NightShade) (Object) this);
    }
}
