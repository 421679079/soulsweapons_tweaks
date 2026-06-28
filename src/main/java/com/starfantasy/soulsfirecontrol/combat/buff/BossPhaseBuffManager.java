package com.starfantasy.soulsfirecontrol.combat.buff;

import com.starfantasy.soulsfirecontrol.config.ChaosMonarchConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;
import net.soulsweaponry.entity.mobs.ChaosMonarch;
import net.soulsweaponry.entity.mobs.DayStalker;
import net.soulsweaponry.entity.mobs.NightProwler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BossPhaseBuffManager {
    private static final int INFINITE_DURATION = -1;
    private static final String MANAGED_EFFECTS_KEY = "starfantasy_phase_buff_managed_effects";
    private static final String MANAGED_EFFECT_SEPARATOR = ";";
    private static final Set<MobEffect> DAY_STALKER_LEGACY_EFFECTS = Set.of(
            MobEffects.DAMAGE_RESISTANCE,
            MobEffects.MOVEMENT_SPEED);
    private static final Set<MobEffect> NIGHT_PROWLER_LEGACY_EFFECTS = Set.of(
            MobEffects.DAMAGE_BOOST,
            MobEffects.MOVEMENT_SPEED);

    private BossPhaseBuffManager() {
    }

    public static void tickNightProwler(NightProwler boss) {
        if (boss.level().isClientSide()) {
            return;
        }
        applyPhaseBuffs(boss,
                ChaosMonarchConfig.getNightProwlerPhaseOneBuffs(),
                ChaosMonarchConfig.getNightProwlerPhaseTwoBuffs(),
                boss.isPhaseTwo(),
                NIGHT_PROWLER_LEGACY_EFFECTS);
    }

    public static void tickDayStalker(DayStalker boss) {
        if (boss.level().isClientSide()) {
            return;
        }
        applyPhaseBuffs(boss,
                ChaosMonarchConfig.getDayStalkerPhaseOneBuffs(),
                ChaosMonarchConfig.getDayStalkerPhaseTwoBuffs(),
                boss.isPhaseTwo(),
                DAY_STALKER_LEGACY_EFFECTS);
    }

    public static void tickChaosMonarch(ChaosMonarch boss, int phase) {
        if (boss.level().isClientSide()) {
            return;
        }
        applyPhaseBuffs(boss,
                ChaosMonarchConfig.getChaosMonarchPhaseBuffs(phase),
                List.of(
                        ChaosMonarchConfig.getChaosMonarchPhaseBuffs(1),
                        ChaosMonarchConfig.getChaosMonarchPhaseBuffs(2),
                        ChaosMonarchConfig.getChaosMonarchPhaseBuffs(3),
                        ChaosMonarchConfig.getChaosMonarchPhaseBuffs(4),
                        ChaosMonarchConfig.getChaosMonarchPhaseBuffs(5),
                        ChaosMonarchConfig.getChaosMonarchPhaseBuffs(6)),
                Set.of());
    }

    private static void applyPhaseBuffs(LivingEntity boss, List<String> phaseOne, List<String> phaseTwo,
                                        boolean phaseTwoActive, Set<MobEffect> legacyEffects) {
        applyPhaseBuffs(boss, phaseTwoActive ? phaseTwo : phaseOne, List.of(phaseOne, phaseTwo), legacyEffects);
    }

    private static void applyPhaseBuffs(LivingEntity boss, List<String> desiredEntries,
                                        List<List<String>> managedEntryLists, Set<MobEffect> legacyEffects) {
        Map<MobEffect, Integer> desired = parseBuffs(desiredEntries);
        Set<MobEffect> managed = new HashSet<>(legacyEffects);
        managed.addAll(readPreviouslyManagedEffects(boss));
        for (List<String> entries : managedEntryLists) {
            managed.addAll(parseBuffs(entries).keySet());
        }

        for (MobEffect effect : managed) {
            Integer level = desired.get(effect);
            if (level == null || level <= 0) {
                if (boss.hasEffect(effect)) {
                    boss.removeEffect(effect);
                }
                continue;
            }
            int amplifier = Math.max(0, level - 1);
            MobEffectInstance current = boss.getEffect(effect);
            if (current == null) {
                boss.addEffect(new MobEffectInstance(effect, INFINITE_DURATION, amplifier, true, true));
            } else if (current.getAmplifier() > amplifier) {
                boss.removeEffect(effect);
                boss.addEffect(new MobEffectInstance(effect, INFINITE_DURATION, amplifier, true, true));
            } else if (current.getAmplifier() < amplifier) {
                boss.addEffect(new MobEffectInstance(effect, INFINITE_DURATION, amplifier, true, true));
            } else if (current.getDuration() != INFINITE_DURATION) {
                boss.removeEffect(effect);
                boss.addEffect(new MobEffectInstance(effect, INFINITE_DURATION, amplifier, true, true));
            }
        }
        writeManagedEffects(boss, desired);
    }

    private static Map<MobEffect, Integer> parseBuffs(List<String> entries) {
        Map<MobEffect, Integer> buffs = new HashMap<>();
        for (String entry : entries) {
            ConfiguredBuff buff = parseBuff(entry);
            if (buff != null) {
                buffs.put(buff.effect(), buff.level());
            }
        }
        return buffs;
    }

    private static ConfiguredBuff parseBuff(String entry) {
        if (entry == null) {
            return null;
        }
        String trimmed = entry.trim();
        int split = trimmed.lastIndexOf('=');
        if (split < 0) {
            split = trimmed.lastIndexOf(':');
        }
        if (split <= 0 || split >= trimmed.length() - 1) {
            return null;
        }
        ResourceLocation id = ResourceLocation.tryParse(trimmed.substring(0, split).trim());
        if (id == null) {
            return null;
        }
        MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(id);
        if (effect == null) {
            return null;
        }
        try {
            int level = Integer.parseInt(trimmed.substring(split + 1).trim());
            if (level == 0 || level < -1) {
                return null;
            }
            return new ConfiguredBuff(effect, level);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private record ConfiguredBuff(MobEffect effect, int level) {
    }

    private static Set<MobEffect> readPreviouslyManagedEffects(LivingEntity boss) {
        Set<MobEffect> effects = new HashSet<>();
        String encoded = boss.getPersistentData().getString(MANAGED_EFFECTS_KEY);
        if (encoded.isBlank()) {
            return effects;
        }
        for (String rawId : encoded.split(MANAGED_EFFECT_SEPARATOR)) {
            ResourceLocation id = ResourceLocation.tryParse(rawId);
            if (id == null) {
                continue;
            }
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(id);
            if (effect != null) {
                effects.add(effect);
            }
        }
        return effects;
    }

    private static void writeManagedEffects(LivingEntity boss, Map<MobEffect, Integer> desired) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<MobEffect, Integer> entry : desired.entrySet()) {
            if (entry.getValue() == null || entry.getValue() <= 0) {
                continue;
            }
            ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(entry.getKey());
            if (id == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(MANAGED_EFFECT_SEPARATOR);
            }
            builder.append(id);
        }
        if (builder.length() == 0) {
            boss.getPersistentData().remove(MANAGED_EFFECTS_KEY);
        } else {
            boss.getPersistentData().putString(MANAGED_EFFECTS_KEY, builder.toString());
        }
    }
}
