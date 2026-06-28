package com.starfantasy.soulsfirecontrol.mixin;

import com.starfantasy.soulsfirecontrol.combat.guard.AccursedLordGuardBreakTracker;
import net.soulsweaponry.entity.mobs.AccursedLordBoss;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AccursedLordBoss.class, remap = false)
public abstract class AccursedLordBossMixin {
    @Inject(method = "m_8107_", at = @At("TAIL"))
    private void starfantasy$tickAccursedLordTweaks(CallbackInfo ci) {
        AccursedLordGuardBreakTracker.tick((AccursedLordBoss) (Object) this);
    }
}
