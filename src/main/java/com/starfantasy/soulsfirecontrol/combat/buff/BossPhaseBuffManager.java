package com.starfantasy.soulsfirecontrol.combat.buff;

import com.starfantasy.soulsfirecontrol.config.ChaosMonarchConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;
import net.soulsweaponry.entity.mobs.DayStalker;
import net.soulsweaponry.entity.mobs.NightProwler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BossPhaseBuffManager {
    private static final int INFINITE_DURATION = -1;
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

    private static void applyPhaseBuffs(LivingEntity boss, List<String> phaseOne, List<String> phaseTwo,
                                        boolean phaseTwoActive, Set<MobEffect> legacyEffects) {
        Map<MobEffect, Integer> desired = parseBuffs(phaseTwoActive ? phaseTwo : phaseOne);
        Set<MobEffect> managed = new HashSet<>(legacyEffects);
        managed.addAll(parseBuffs(phaseOne).keySet());
        managed.addAll(parseBuffs(phaseTwo).keySet());

        for (MobEffect effect : managed) {
            Integer level = desired.get(effect);
            if (level == null || level < 0) {
                if (boss.hasEffect(effect)) {
                    boss.removeEffect(effect);
                }
                continue;
            }
            int amplifier = Math.max(0, level - 1);
            MobEffectInstance current = boss.getEffect(effect);
            if (current == null || current.getAmplifier() != amplifier
                    || current.getDuration() != INFINITE_DURATION) {
                boss.removeEffect(effect);
                boss.addEffect(new MobEffectInstance(effect, INFINITE_DURATION, amplifier, true, true));
            }
        }
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
}
