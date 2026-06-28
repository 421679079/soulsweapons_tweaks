package com.starfantasy.soulsfirecontrol.event;

import com.starfantasy.soulsfirecontrol.StarFantasySoulsFireControl;
import com.starfantasy.soulsfirecontrol.util.DayStalkerTweaks;
import com.starfantasy.soulsfirecontrol.util.NightProwlerTweaks;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.soulsweaponry.entity.mobs.DayStalker;
import net.soulsweaponry.entity.mobs.NightProwler;

@Mod.EventBusSubscriber(modid = StarFantasySoulsFireControl.MOD_ID)
public final class TwinBossSummonEvents {
    private TwinBossSummonEvents() {
    }

    @SubscribeEvent
    public static void onTwinBossDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof DayStalker dayStalker) {
            DayStalkerTweaks.discardWarmthSummons(dayStalker);
        }
        if (event.getEntity() instanceof NightProwler nightProwler) {
            NightProwlerTweaks.discardSummonAllies(nightProwler);
        }
    }
}
