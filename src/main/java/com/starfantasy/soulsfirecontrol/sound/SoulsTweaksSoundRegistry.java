package com.starfantasy.soulsfirecontrol.sound;

import com.starfantasy.soulsfirecontrol.StarFantasySoulsFireControl;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class SoulsTweaksSoundRegistry {
    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, StarFantasySoulsFireControl.MOD_ID);

    public static final RegistryObject<SoundEvent> JUST_GUARD_1 =
            SOUNDS.register("just_guard_1",
                    () -> SoundEvent.createVariableRangeEvent(StarFantasySoulsFireControl.id("just_guard_1")));

    private SoulsTweaksSoundRegistry() {
    }

    public static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }
}
