/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraftforge.fml.ModLoadingContext
 *  net.minecraftforge.fml.common.Mod
 *  net.minecraftforge.fml.config.IConfigSpec
 *  net.minecraftforge.fml.config.ModConfig$Type
 */
package com.starfantasy.soulsfirecontrol;

import com.starfantasy.soulsfirecontrol.config.ChaosMonarchConfig;
import com.starfantasy.soulsfirecontrol.entity.TwinMeteorEntityRegistry;
import com.starfantasy.soulsfirecontrol.network.SoulsTweaksNetwork;
import com.starfantasy.soulsfirecontrol.vfx.telegraph.TelegraphParticleRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

@Mod(value=StarFantasySoulsFireControl.MOD_ID)
public class StarFantasySoulsFireControl {
    public static final String MOD_ID = "soulsweapons_tweaks";

    public StarFantasySoulsFireControl() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        SoulsTweaksNetwork.register();
        TwinMeteorEntityRegistry.register(modBus);
        TelegraphParticleRegistry.register(modBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, (IConfigSpec)ChaosMonarchConfig.SPEC);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
