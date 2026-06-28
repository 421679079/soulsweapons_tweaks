package com.starfantasy.soulsfirecontrol.combat.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public record ConfiguredMobEffect(MobEffect effect, int level, int durationTicks) {
    public MobEffectInstance createInstance() {
        return new MobEffectInstance(this.effect, this.durationTicks, Math.max(0, this.level - 1), true, true);
    }

    public int amplifier() {
        return Math.max(0, this.level - 1);
    }

    public static List<ConfiguredMobEffect> parseList(List<String> entries) {
        List<ConfiguredMobEffect> effects = new ArrayList<>();
        for (String entry : entries) {
            ConfiguredMobEffect effect = parse(entry);
            if (effect != null) {
                effects.add(effect);
            }
        }
        return List.copyOf(effects);
    }

    private static ConfiguredMobEffect parse(String entry) {
        if (entry == null) {
            return null;
        }
        String trimmed = entry.trim();
        int durationSplit = lastSeparatorBefore(trimmed, trimmed.length());
        int levelSplit = durationSplit <= 0 ? -1 : lastSeparatorBefore(trimmed, durationSplit);
        if (levelSplit <= 0 || durationSplit <= levelSplit || durationSplit >= trimmed.length() - 1) {
            return null;
        }
        ResourceLocation id = ResourceLocation.tryParse(trimmed.substring(0, levelSplit).trim());
        if (id == null) {
            return null;
        }
        MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(id);
        if (effect == null) {
            return null;
        }
        try {
            int level = Integer.parseInt(trimmed.substring(levelSplit + 1, durationSplit).trim());
            int durationTicks = Integer.parseInt(trimmed.substring(durationSplit + 1).trim());
            if (level < 1 || durationTicks < 1) {
                return null;
            }
            return new ConfiguredMobEffect(effect, level, durationTicks);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static int lastSeparatorBefore(String value, int endExclusive) {
        int colon = value.lastIndexOf(':', endExclusive - 1);
        int equals = value.lastIndexOf('=', endExclusive - 1);
        return Math.max(colon, equals);
    }
}
