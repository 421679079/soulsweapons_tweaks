/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraftforge.event.entity.living.LivingAttackEvent
 *  net.minecraftforge.event.entity.living.LivingDropsEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.starfantasy.soulsfirecontrol.event;

import com.starfantasy.soulsfirecontrol.util.ChaosMonarchHelper;
import com.starfantasy.soulsfirecontrol.StarFantasySoulsFireControl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=StarFantasySoulsFireControl.MOD_ID)
public final class ChaosMonarchSummonHandler {
    private ChaosMonarchSummonHandler() {
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity target = event.getEntity();
        if (ChaosMonarchHelper.shouldCancelFriendlyFire(target, event.getSource())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (ChaosMonarchHelper.isChaosMonarchSummon((Entity)event.getEntity())) {
            event.getDrops().clear();
        }
    }
}
