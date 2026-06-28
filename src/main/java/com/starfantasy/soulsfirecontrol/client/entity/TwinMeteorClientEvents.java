package com.starfantasy.soulsfirecontrol.client.entity;

import com.starfantasy.soulsfirecontrol.StarFantasySoulsFireControl;
import com.starfantasy.soulsfirecontrol.entity.ChaosWitherSkullProjectileEntity;
import com.starfantasy.soulsfirecontrol.entity.TwinMeteorEntityRegistry;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.WitherSkullRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StarFantasySoulsFireControl.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class TwinMeteorClientEvents {
    private TwinMeteorClientEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(TwinMeteorEntityRegistry.DAY_STALKER_METEOR.get(), TwinMeteorRenderer::new);
        event.registerEntityRenderer(TwinMeteorEntityRegistry.NIGHT_PROWLER_METEOR.get(), TwinMeteorRenderer::new);
        event.registerEntityRenderer(TwinMeteorEntityRegistry.CHAOS_FROST_METEOR.get(), TwinMeteorRenderer::new);
        event.registerEntityRenderer(TwinMeteorEntityRegistry.NIGHT_PROWLER_LIGHTNING_AOE.get(),
                NightProwlerLightningAoeRenderer::new);
        event.registerEntityRenderer(TwinMeteorEntityRegistry.CHAOS_BARRAGE_PROJECTILE.get(),
                ChaosBarrageProjectileRenderer::new);
        event.registerEntityRenderer(TwinMeteorEntityRegistry.CHAOS_WITHER_SKULL.get(),
                context -> (EntityRenderer<ChaosWitherSkullProjectileEntity>) (EntityRenderer<?>) new WitherSkullRenderer(context));
    }
}
