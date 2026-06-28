package com.starfantasy.soulsfirecontrol.mixin;

import com.starfantasy.soulsfirecontrol.util.DayStalkerTweaks;
import com.starfantasy.soulsfirecontrol.util.ChaosMonarchTweaks;
import com.starfantasy.soulsfirecontrol.util.NightProwlerTweaks;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.soulsweaponry.entity.mobs.DayStalker;
import net.soulsweaponry.entity.mobs.NightProwler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class NoLootSoulmassMixin {
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void starfantasy$preventNightProwlerSummonFriendlyFire(DamageSource source, float amount,
                                                                   CallbackInfoReturnable<Boolean> cir) {
        if (starfantasy$isFriendlySummonDamage(source)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"), cancellable = true)
    private void starfantasy$skipSummonedSoulmassLoot(DamageSource source, CallbackInfo ci) {
        if (starfantasy$isNoLootSoulmass()) {
            ci.cancel();
        }
    }

    @Inject(method = "shouldDropLoot", at = @At("HEAD"), cancellable = true)
    private void starfantasy$skipSummonedSoulmassLootFlag(CallbackInfoReturnable<Boolean> cir) {
        if (starfantasy$isNoLootSoulmass()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "shouldDropExperience", at = @At("HEAD"), cancellable = true)
    private void starfantasy$skipSummonedSoulmassExperience(CallbackInfoReturnable<Boolean> cir) {
        if (starfantasy$isNoLootSoulmass()) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private boolean starfantasy$isNoLootSoulmass() {
        Entity entity = (Entity) (Object) this;
        return entity.getTags().contains(NightProwlerTweaks.NO_LOOT_SOULMASS_TAG)
                || entity.getTags().contains(DayStalkerTweaks.NO_LOOT_WARMTH_TAG)
                || entity.getTags().contains(ChaosMonarchTweaks.NO_LOOT_SUMMON_TAG);
    }

    @Unique
    private boolean starfantasy$isFriendlySummonDamage(DamageSource source) {
        Entity target = (Entity) (Object) this;
        Entity attacker = source.getEntity();
        Entity direct = source.getDirectEntity();
        return starfantasy$isFriendlySummonPair(target, attacker)
                || starfantasy$isFriendlySummonPair(target, direct);
    }

    @Unique
    private boolean starfantasy$isFriendlySummonPair(Entity target, Entity attacker) {
        if (target == null || attacker == null || target == attacker) {
            return false;
        }
        return starfantasy$isTwinBossPair(target, attacker)
                || NightProwlerTweaks.areSummonAllies(target, attacker)
                || DayStalkerTweaks.areWarmthAllies(target, attacker);
    }

    @Unique
    private boolean starfantasy$isTwinBossPair(Entity first, Entity second) {
        return first instanceof DayStalker && second instanceof NightProwler
                || first instanceof NightProwler && second instanceof DayStalker;
    }
}
