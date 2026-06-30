package com.starfantasy.soulsfirecontrol.vfx.telegraph;

import com.starfantasy.soulsfirecontrol.StarFantasySoulsFireControl;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class TelegraphParticleRegistry {
    private static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, StarFantasySoulsFireControl.MOD_ID);

    public static final RegistryObject<SimpleParticleType> ATTACK_WARNING_RING =
            PARTICLES.register("attack_warning_ring", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> ROAR_WAVE =
            PARTICLES.register("roar_wave", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> SWORD_EXPLOSION =
            PARTICLES.register("sword_explosion", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> GROUND_WARNING_RECTANGLE =
            PARTICLES.register("ground_warning_rectangle", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> GUARD_CLASH =
            PARTICLES.register("guard_clash", () -> new SimpleParticleType(true));

    private TelegraphParticleRegistry() {
    }

    public static void register(IEventBus modBus) {
        PARTICLES.register(modBus);
    }
}
