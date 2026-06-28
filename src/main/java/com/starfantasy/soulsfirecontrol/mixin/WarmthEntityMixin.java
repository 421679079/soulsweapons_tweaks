package com.starfantasy.soulsfirecontrol.mixin;

import com.starfantasy.soulsfirecontrol.util.DayStalkerTweaks;
import net.soulsweaponry.entity.mobs.WarmthEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WarmthEntity.class, remap = false)
public abstract class WarmthEntityMixin {
    @Inject(method = "m_8119_", at = @At("TAIL"))
    private void starfantasy$tickSummonedWarmth(CallbackInfo ci) {
        DayStalkerTweaks.tickSummonedWarmth((WarmthEntity) (Object) this);
    }
}
