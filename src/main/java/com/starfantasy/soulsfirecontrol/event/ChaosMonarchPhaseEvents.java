package com.starfantasy.soulsfirecontrol.event;

import com.starfantasy.soulsfirecontrol.StarFantasySoulsFireControl;
import com.starfantasy.soulsfirecontrol.combat.chaosmonarch.ChaosMonarchPhaseManager;
import com.starfantasy.soulsfirecontrol.combat.guard.ChaosMonarchGuardBreakTracker;
import com.starfantasy.soulsfirecontrol.config.ChaosMonarchConfig;
import com.starfantasy.soulsfirecontrol.util.ChaosMonarchTweaks;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.soulsweaponry.entity.mobs.ChaosMonarch;

@Mod.EventBusSubscriber(modid = StarFantasySoulsFireControl.MOD_ID)
public final class ChaosMonarchPhaseEvents {
    private ChaosMonarchPhaseEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPhaseVisualEntityAttack(LivingAttackEvent event) {
        Entity direct = event.getSource().getDirectEntity();
        if (direct != null && direct.getTags().contains(ChaosMonarchPhaseManager.PHASE_VISUAL_ENTITY_TAG)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onChaosMonarchHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ChaosMonarch boss)) {
            return;
        }
        ChaosMonarchTweaks.tryRecordMeleeClashHit(boss, event.getSource());
        if (ChaosMonarchPhaseManager.bypassesPhaseLock(event.getSource())) {
            return;
        }
        float amount = event.getAmount();
        if (!ChaosMonarchGuardBreakTracker.isStunned(boss)) {
            amount *= ChaosMonarchConfig.getChaosMonarchNormalDamageMultiplier();
        }
        event.setAmount(amount);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onChaosMonarchDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ChaosMonarch boss)) {
            return;
        }
        event.setAmount(ChaosMonarchPhaseManager.clampFinalDamageForPhaseLock(
                boss, event.getSource(), event.getAmount()));
    }

    @SubscribeEvent
    public static void onChaosMonarchSummonDrops(LivingDropsEvent event) {
        if (event.getEntity().getTags().contains(ChaosMonarchTweaks.NO_LOOT_SUMMON_TAG)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onChaosMonarchSummonExperience(LivingExperienceDropEvent event) {
        if (event.getEntity().getTags().contains(ChaosMonarchTweaks.NO_LOOT_SUMMON_TAG)) {
            event.setDroppedExperience(0);
        }
    }
}
