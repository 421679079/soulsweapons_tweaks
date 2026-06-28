package com.starfantasy.soulsfirecontrol.event;

import com.starfantasy.soulsfirecontrol.StarFantasySoulsFireControl;
import com.starfantasy.soulsfirecontrol.combat.guard.AccursedLordGuardBreakTracker;
import com.starfantasy.soulsfirecontrol.util.AccursedLordTweaks;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.soulsweaponry.entity.mobs.AccursedLordBoss;

@Mod.EventBusSubscriber(modid = StarFantasySoulsFireControl.MOD_ID)
public final class AccursedLordEvents {
    private AccursedLordEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAccursedLordHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof AccursedLordBoss boss) || event.getAmount() <= 0.0F) {
            return;
        }
        if (!AccursedLordGuardBreakTracker.isStunned(boss)) {
            event.setAmount(event.getAmount() * AccursedLordTweaks.NORMAL_DAMAGE_MULTIPLIER);
        }
    }
}
