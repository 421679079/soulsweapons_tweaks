package com.starfantasy.soulsfirecontrol.vfx.telegraph.client;

import com.starfantasy.soulsfirecontrol.StarFantasySoulsFireControl;
import com.starfantasy.soulsfirecontrol.vfx.telegraph.TelegraphParticleRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StarFantasySoulsFireControl.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class TelegraphClientEvents {
    private TelegraphClientEvents() {
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(TelegraphParticleRegistry.ATTACK_WARNING_RING.get(), AttackWarningRingParticle.Provider::new);
        event.registerSpriteSet(TelegraphParticleRegistry.ROAR_WAVE.get(), RoarWaveParticle.Provider::new);
        event.registerSpriteSet(TelegraphParticleRegistry.SWORD_EXPLOSION.get(), SwordExplosionParticle.Provider::new);
        event.registerSpriteSet(TelegraphParticleRegistry.GROUND_WARNING_RECTANGLE.get(), GroundWarningRectangleParticle.Provider::new);
    }
}
