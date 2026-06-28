package com.starfantasy.soulsfirecontrol.mixin;

import com.starfantasy.soulsfirecontrol.combat.buff.BossPhaseBuffManager;
import com.starfantasy.soulsfirecontrol.combat.guard.NightProwlerGuardBreakTracker;
import com.starfantasy.soulsfirecontrol.combat.twin.TwinBossCombatEffects;
import com.starfantasy.soulsfirecontrol.config.ChaosMonarchConfig;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.soulsweaponry.entity.mobs.NightProwler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NightProwler.class, remap = false)
public abstract class NightProwlerMixin {
    @Inject(method = "m_8107_", at = @At("TAIL"))
    private void starfantasy$tickNightProwlerTweaks(CallbackInfo ci) {
        NightProwler boss = (NightProwler) (Object) this;
        NightProwlerGuardBreakTracker.tick(boss);
        BossPhaseBuffManager.tickNightProwler(boss);
        TwinBossCombatEffects.tickNightProwler(boss);
    }

    @ModifyVariable(method = "m_6469_", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float starfantasy$applyPhaseDamageMultiplier(float amount) {
        NightProwler boss = (NightProwler) (Object) this;
        if (amount <= 0.0F) {
            return amount;
        }
        if (!boss.isPhaseTwo()) {
            return amount * ChaosMonarchConfig.getNightProwlerPhaseOneDamageMultiplier();
        }
        if (NightProwlerGuardBreakTracker.isStunned(boss)) {
            return amount;
        }
        return amount * ChaosMonarchConfig.getNightProwlerPhaseTwoNormalDamageMultiplier();
    }

    @Redirect(method = "m_6469_",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/damagesource/DamageSource;m_269533_(Lnet/minecraft/tags/TagKey;)Z",
                    ordinal = 1,
                    remap = false),
            remap = false)
    private boolean starfantasy$disableProjectileHealCheck(DamageSource source, TagKey<DamageType> tag) {
        return false;
    }

    @Redirect(method = "m_6469_",
            at = @At(value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/mobs/NightProwler;getAttackAnimation()Lnet/soulsweaponry/entity/mobs/NightProwler$Attacks;",
                    remap = false))
    private NightProwler.Attacks starfantasy$disableStunnedHurtAnimationChecks(NightProwler boss) {
        if (NightProwlerGuardBreakTracker.isStunned(boss)) {
            return NightProwler.Attacks.SPAWN;
        }
        return boss.getAttackAnimation();
    }
}
