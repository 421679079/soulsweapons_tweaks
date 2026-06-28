package com.starfantasy.soulsfirecontrol.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.soulsweaponry.entity.mobs.ReturningKnight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ReturningKnight.class, remap = false)
public abstract class ReturningKnightMixin {
    @Inject(method = "m_8107_", at = @At("TAIL"))
    private void starfantasy$adjustHalfHealthBuffs(CallbackInfo ci) {
        ReturningKnight boss = (ReturningKnight) (Object) this;
        if (boss.getDeath() || boss.getHealth() > boss.getMaxHealth() * 0.5F) {
            return;
        }
        int resistanceAmplifier = boss.getAttackingPlayers().size() >= 3 ? 3 : 1;
        MobEffectInstance resistance = boss.getEffect(MobEffects.DAMAGE_RESISTANCE);
        if (resistance == null || resistance.getAmplifier() != resistanceAmplifier) {
            boss.removeEffect(MobEffects.DAMAGE_RESISTANCE);
            boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 10, resistanceAmplifier));
        }
        boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 10, 2));
    }
}
