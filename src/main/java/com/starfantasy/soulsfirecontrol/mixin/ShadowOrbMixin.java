package com.starfantasy.soulsfirecontrol.mixin;

import com.starfantasy.soulsfirecontrol.util.AccursedLordTweaks;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.soulsweaponry.entity.mobs.NightShade;
import net.soulsweaponry.entity.projectile.ShadowOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ShadowOrb.class, remap = false)
public abstract class ShadowOrbMixin {
    @Redirect(method = "m_5790_", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;m_7292_(Lnet/minecraft/world/effect/MobEffectInstance;)Z"))
    private boolean starfantasy$skipNightShadeOrbEffects(LivingEntity target, MobEffectInstance effect,
                                                         EntityHitResult hitResult) {
        ShadowOrb orb = (ShadowOrb) (Object) this;
        if (orb.getOwner() instanceof NightShade) {
            return false;
        }
        return target.addEffect(effect);
    }

    @Inject(method = "m_6532_", at = @At("TAIL"))
    private void starfantasy$explodeAccursedLordOrb(HitResult hitResult, CallbackInfo ci) {
        AccursedLordTweaks.detonateAccursedProjectile((ShadowOrb) (Object) this, hitResult.getLocation());
    }
}
