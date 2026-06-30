/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraftforge.common.ForgeConfigSpec
 *  net.minecraftforge.common.ForgeConfigSpec$Builder
 *  net.minecraftforge.common.ForgeConfigSpec$ConfigValue
 *  net.minecraftforge.common.ForgeConfigSpec$IntValue
 */
package com.starfantasy.soulsfirecontrol.config;

import java.util.List;
import net.minecraftforge.common.ForgeConfigSpec;

public final class ChaosMonarchConfig {
    public static final List<String> DEFAULT_CHAOS_MONARCH_PHASE_1_BUFFS = List.of("minecraft:strength:1");
    public static final List<String> DEFAULT_CHAOS_MONARCH_PHASE_2_BUFFS = List.of("minecraft:resistance:1");
    public static final List<String> DEFAULT_CHAOS_MONARCH_PHASE_3_BUFFS = List.of("minecraft:haste:1");
    public static final List<String> DEFAULT_CHAOS_MONARCH_PHASE_4_BUFFS = List.of();
    public static final List<String> DEFAULT_CHAOS_MONARCH_PHASE_5_BUFFS = List.of("minecraft:strength:1", "minecraft:resistance:1", "minecraft:haste:1");
    public static final List<String> DEFAULT_CHAOS_MONARCH_PHASE_6_BUFFS = DEFAULT_CHAOS_MONARCH_PHASE_5_BUFFS;
    public static final List<String> DEFAULT_CHAOS_MONARCH_PHASE_1_DEBUFFS = List.of("minecraft:weakness:1:200");
    public static final List<String> DEFAULT_CHAOS_MONARCH_PHASE_2_DEBUFFS = List.of("minecraft:slowness:1:200");
    public static final List<String> DEFAULT_CHAOS_MONARCH_PHASE_3_DEBUFFS = List.of("minecraft:poison:1:200");
    public static final List<String> DEFAULT_CHAOS_MONARCH_PHASE_4_DEBUFFS = List.of("minecraft:wither:1:200");
    public static final List<String> DEFAULT_CHAOS_MONARCH_PHASE_5_DEBUFFS = List.of("minecraft:weakness:1:200", "minecraft:poison:1:200", "minecraft:slowness:1:200", "minecraft:wither:1:200");
    public static final List<String> DEFAULT_CHAOS_MONARCH_PHASE_6_DEBUFFS = DEFAULT_CHAOS_MONARCH_PHASE_5_DEBUFFS;
    public static final List<String> DEFAULT_CHAOS_MONARCH_LIGHTNING_PHASE_1_HIT_EFFECTS = List.of("minecraft:weakness:1:200");
    public static final List<String> DEFAULT_CHAOS_MONARCH_LIGHTNING_PHASE_2_HIT_EFFECTS = List.of("goety:freezing:1:200");
    public static final List<String> DEFAULT_CHAOS_MONARCH_LIGHTNING_PHASE_3_HIT_EFFECTS = List.of("goety:spasms:10:200");
    public static final List<String> DEFAULT_CHAOS_MONARCH_LIGHTNING_PHASE_4_HIT_EFFECTS = List.of("goety:void_touched:1:100");
    public static final List<String> DEFAULT_CHAOS_MONARCH_LIGHTNING_PHASE_5_HIT_EFFECTS = List.of();
    public static final List<String> DEFAULT_CHAOS_MONARCH_LIGHTNING_PHASE_6_HIT_EFFECTS = List.of();
    public static final List<String> DEFAULT_NIGHT_PROWLER_PHASE_1_BUFFS = List.of("minecraft:regeneration:1", "minecraft:speed:10");
    public static final List<String> DEFAULT_NIGHT_PROWLER_PHASE_2_BUFFS = List.of("minecraft:regeneration:2", "minecraft:speed:10", "minecraft:resistance:2");
    public static final List<String> DEFAULT_DAY_STALKER_PHASE_1_BUFFS = List.of("minecraft:regeneration:2", "minecraft:speed:10");
    public static final List<String> DEFAULT_DAY_STALKER_PHASE_2_BUFFS = List.of("minecraft:regeneration:3", "minecraft:speed:10", "minecraft:resistance:2");
    public static final List<String> DEFAULT_TWIN_BOSS_NEARBY_PLAYER_DEBUFFS = List.of("minecraft:weakness:1:200", "minecraft:hunger:1:200");
    public static final List<String> DEFAULT_TWIN_BOSS_METEOR_HIT_EFFECTS = List.of("minecraft:slowness:1:200");
    public static final List<String> DEFAULT_DAY_STALKER_REACTION_TRAP_HIT_EFFECTS = List.of("minecraft:wither:1:200");
    public static final List<String> DEFAULT_NIGHT_PROWLER_REACTION_AOE_HIT_EFFECTS = List.of("minecraft:wither:1:200");
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.IntValue CHAOS_MONARCH_NEARBY_PLAYER_DEBUFF_RADIUS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CHAOS_MONARCH_PHASE_1_BUFFS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CHAOS_MONARCH_PHASE_2_BUFFS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CHAOS_MONARCH_PHASE_3_BUFFS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CHAOS_MONARCH_PHASE_4_BUFFS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CHAOS_MONARCH_PHASE_5_BUFFS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CHAOS_MONARCH_PHASE_6_BUFFS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CHAOS_MONARCH_PHASE_1_DEBUFFS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CHAOS_MONARCH_PHASE_2_DEBUFFS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CHAOS_MONARCH_PHASE_3_DEBUFFS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CHAOS_MONARCH_PHASE_4_DEBUFFS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CHAOS_MONARCH_PHASE_5_DEBUFFS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CHAOS_MONARCH_PHASE_6_DEBUFFS;
    private static final ForgeConfigSpec.IntValue CHAOS_MONARCH_GUARD_BREAK_REQUIRED_GUARDS_PHASE_1_TO_5;
    private static final ForgeConfigSpec.IntValue CHAOS_MONARCH_GUARD_BREAK_REQUIRED_GUARDS_PHASE_6;
    private static final ForgeConfigSpec.DoubleValue CHAOS_MONARCH_NORMAL_DAMAGE_MULTIPLIER;
    private static final ForgeConfigSpec.DoubleValue CHAOS_MONARCH_PHASE_6_HEAL_PER_SECOND;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CHAOS_MONARCH_LIGHTNING_PHASE_1_HIT_EFFECTS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CHAOS_MONARCH_LIGHTNING_PHASE_2_HIT_EFFECTS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CHAOS_MONARCH_LIGHTNING_PHASE_3_HIT_EFFECTS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CHAOS_MONARCH_LIGHTNING_PHASE_4_HIT_EFFECTS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CHAOS_MONARCH_LIGHTNING_PHASE_5_HIT_EFFECTS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CHAOS_MONARCH_LIGHTNING_PHASE_6_HIT_EFFECTS;
    private static final ForgeConfigSpec.IntValue DRAUGR_BOSS_GUARD_BREAK_REQUIRED_GUARDS;
    private static final ForgeConfigSpec.DoubleValue DRAUGR_BOSS_NORMAL_DAMAGE_MULTIPLIER;
    private static final ForgeConfigSpec.IntValue NIGHT_SHADE_GUARD_BREAK_REQUIRED_GUARDS;
    private static final ForgeConfigSpec.IntValue MOONKNIGHT_GUARD_BREAK_REQUIRED_GUARDS;
    private static final ForgeConfigSpec.DoubleValue MOONKNIGHT_PHASE_2_NORMAL_DAMAGE_MULTIPLIER;
    private static final ForgeConfigSpec.IntValue NIGHT_PROWLER_GUARD_BREAK_REQUIRED_GUARDS;
    private static final ForgeConfigSpec.DoubleValue NIGHT_PROWLER_PHASE_1_DAMAGE_MULTIPLIER;
    private static final ForgeConfigSpec.DoubleValue NIGHT_PROWLER_PHASE_2_NORMAL_DAMAGE_MULTIPLIER;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> NIGHT_PROWLER_PHASE_1_BUFFS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> NIGHT_PROWLER_PHASE_2_BUFFS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> NIGHT_PROWLER_NEARBY_PLAYER_DEBUFFS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> NIGHT_PROWLER_METEOR_HIT_EFFECTS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> NIGHT_PROWLER_REACTION_AOE_HIT_EFFECTS;
    private static final ForgeConfigSpec.IntValue DAY_STALKER_GUARD_BREAK_REQUIRED_GUARDS;
    private static final ForgeConfigSpec.DoubleValue DAY_STALKER_PHASE_1_DAMAGE_MULTIPLIER;
    private static final ForgeConfigSpec.DoubleValue DAY_STALKER_PHASE_2_NORMAL_DAMAGE_MULTIPLIER;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> DAY_STALKER_PHASE_1_BUFFS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> DAY_STALKER_PHASE_2_BUFFS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> DAY_STALKER_NEARBY_PLAYER_DEBUFFS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> DAY_STALKER_METEOR_HIT_EFFECTS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> DAY_STALKER_REACTION_TRAP_HIT_EFFECTS;
    private static final ForgeConfigSpec.IntValue TWIN_BOSS_NEARBY_PLAYER_DEBUFF_RADIUS;
    private static final ForgeConfigSpec.IntValue TWIN_BOSS_METEOR_WARNING_TICKS;
    private static final ForgeConfigSpec.IntValue TWIN_BOSS_METEOR_TARGET_RADIUS;
    private static final ForgeConfigSpec.IntValue TWIN_BOSS_METEOR_SPAWN_Y_OFFSET;

    private ChaosMonarchConfig() {
    }

    public static int getChaosMonarchNearbyPlayerDebuffRadius() {
        return CHAOS_MONARCH_NEARBY_PLAYER_DEBUFF_RADIUS.get();
    }

    public static List<String> getChaosMonarchPhaseBuffs(int phase) {
        return switch (phase) {
            case 2 -> ChaosMonarchConfig.castList((List) CHAOS_MONARCH_PHASE_2_BUFFS.get());
            case 3 -> ChaosMonarchConfig.castList((List) CHAOS_MONARCH_PHASE_3_BUFFS.get());
            case 4 -> ChaosMonarchConfig.castList((List) CHAOS_MONARCH_PHASE_4_BUFFS.get());
            case 5 -> ChaosMonarchConfig.castList((List) CHAOS_MONARCH_PHASE_5_BUFFS.get());
            case 6 -> ChaosMonarchConfig.castList((List) CHAOS_MONARCH_PHASE_6_BUFFS.get());
            default -> ChaosMonarchConfig.castList((List) CHAOS_MONARCH_PHASE_1_BUFFS.get());
        };
    }

    public static List<String> getChaosMonarchPhaseDebuffs(int phase) {
        return switch (phase) {
            case 2 -> ChaosMonarchConfig.castList((List) CHAOS_MONARCH_PHASE_2_DEBUFFS.get());
            case 3 -> ChaosMonarchConfig.castList((List) CHAOS_MONARCH_PHASE_3_DEBUFFS.get());
            case 4 -> ChaosMonarchConfig.castList((List) CHAOS_MONARCH_PHASE_4_DEBUFFS.get());
            case 5 -> ChaosMonarchConfig.castList((List) CHAOS_MONARCH_PHASE_5_DEBUFFS.get());
            case 6 -> ChaosMonarchConfig.castList((List) CHAOS_MONARCH_PHASE_6_DEBUFFS.get());
            default -> ChaosMonarchConfig.castList((List) CHAOS_MONARCH_PHASE_1_DEBUFFS.get());
        };
    }

    public static int getChaosMonarchGuardBreakRequiredGuards() {
        return getChaosMonarchGuardBreakRequiredGuards(1);
    }

    public static int getChaosMonarchGuardBreakRequiredGuards(int phase) {
        return phase >= 6
                ? CHAOS_MONARCH_GUARD_BREAK_REQUIRED_GUARDS_PHASE_6.get()
                : CHAOS_MONARCH_GUARD_BREAK_REQUIRED_GUARDS_PHASE_1_TO_5.get();
    }

    public static float getChaosMonarchNormalDamageMultiplier() {
        return CHAOS_MONARCH_NORMAL_DAMAGE_MULTIPLIER.get().floatValue();
    }

    public static float getChaosMonarchPhaseSixHealPerSecond() {
        return CHAOS_MONARCH_PHASE_6_HEAL_PER_SECOND.get().floatValue();
    }

    public static List<String> getChaosMonarchLightningHitEffects(int phase) {
        return switch (phase) {
            case 2 -> ChaosMonarchConfig.castList((List) CHAOS_MONARCH_LIGHTNING_PHASE_2_HIT_EFFECTS.get());
            case 3 -> ChaosMonarchConfig.castList((List) CHAOS_MONARCH_LIGHTNING_PHASE_3_HIT_EFFECTS.get());
            case 4 -> ChaosMonarchConfig.castList((List) CHAOS_MONARCH_LIGHTNING_PHASE_4_HIT_EFFECTS.get());
            case 5 -> ChaosMonarchConfig.castList((List) CHAOS_MONARCH_LIGHTNING_PHASE_5_HIT_EFFECTS.get());
            case 6 -> ChaosMonarchConfig.castList((List) CHAOS_MONARCH_LIGHTNING_PHASE_6_HIT_EFFECTS.get());
            default -> ChaosMonarchConfig.castList((List) CHAOS_MONARCH_LIGHTNING_PHASE_1_HIT_EFFECTS.get());
        };
    }

    public static int getDraugrBossGuardBreakRequiredGuards() {
        return DRAUGR_BOSS_GUARD_BREAK_REQUIRED_GUARDS.get();
    }

    public static float getDraugrBossNormalDamageMultiplier() {
        return DRAUGR_BOSS_NORMAL_DAMAGE_MULTIPLIER.get().floatValue();
    }

    public static int getNightShadeGuardBreakRequiredGuards() {
        return NIGHT_SHADE_GUARD_BREAK_REQUIRED_GUARDS.get();
    }

    public static int getMoonknightGuardBreakRequiredGuards() {
        return MOONKNIGHT_GUARD_BREAK_REQUIRED_GUARDS.get();
    }

    public static float getMoonknightPhaseTwoNormalDamageMultiplier() {
        return MOONKNIGHT_PHASE_2_NORMAL_DAMAGE_MULTIPLIER.get().floatValue();
    }

    public static int getNightProwlerGuardBreakRequiredGuards() {
        return NIGHT_PROWLER_GUARD_BREAK_REQUIRED_GUARDS.get();
    }

    public static float getNightProwlerPhaseOneDamageMultiplier() {
        return NIGHT_PROWLER_PHASE_1_DAMAGE_MULTIPLIER.get().floatValue();
    }

    public static float getNightProwlerPhaseTwoNormalDamageMultiplier() {
        return NIGHT_PROWLER_PHASE_2_NORMAL_DAMAGE_MULTIPLIER.get().floatValue();
    }

    public static List<String> getNightProwlerPhaseOneBuffs() {
        return ChaosMonarchConfig.castList((List)NIGHT_PROWLER_PHASE_1_BUFFS.get());
    }

    public static List<String> getNightProwlerPhaseTwoBuffs() {
        return ChaosMonarchConfig.castList((List)NIGHT_PROWLER_PHASE_2_BUFFS.get());
    }

    public static List<String> getNightProwlerNearbyPlayerDebuffs() {
        return ChaosMonarchConfig.castList((List)NIGHT_PROWLER_NEARBY_PLAYER_DEBUFFS.get());
    }

    public static List<String> getNightProwlerMeteorHitEffects() {
        return ChaosMonarchConfig.castList((List)NIGHT_PROWLER_METEOR_HIT_EFFECTS.get());
    }

    public static List<String> getNightProwlerReactionAoeHitEffects() {
        return ChaosMonarchConfig.castList((List)NIGHT_PROWLER_REACTION_AOE_HIT_EFFECTS.get());
    }

    public static int getDayStalkerGuardBreakRequiredGuards() {
        return DAY_STALKER_GUARD_BREAK_REQUIRED_GUARDS.get();
    }

    public static float getDayStalkerPhaseOneDamageMultiplier() {
        return DAY_STALKER_PHASE_1_DAMAGE_MULTIPLIER.get().floatValue();
    }

    public static float getDayStalkerPhaseTwoNormalDamageMultiplier() {
        return DAY_STALKER_PHASE_2_NORMAL_DAMAGE_MULTIPLIER.get().floatValue();
    }

    public static List<String> getDayStalkerPhaseOneBuffs() {
        return ChaosMonarchConfig.castList((List)DAY_STALKER_PHASE_1_BUFFS.get());
    }

    public static List<String> getDayStalkerPhaseTwoBuffs() {
        return ChaosMonarchConfig.castList((List)DAY_STALKER_PHASE_2_BUFFS.get());
    }

    public static List<String> getDayStalkerNearbyPlayerDebuffs() {
        return ChaosMonarchConfig.castList((List)DAY_STALKER_NEARBY_PLAYER_DEBUFFS.get());
    }

    public static List<String> getDayStalkerMeteorHitEffects() {
        return ChaosMonarchConfig.castList((List)DAY_STALKER_METEOR_HIT_EFFECTS.get());
    }

    public static List<String> getDayStalkerReactionTrapHitEffects() {
        return ChaosMonarchConfig.castList((List)DAY_STALKER_REACTION_TRAP_HIT_EFFECTS.get());
    }

    public static int getTwinBossNearbyPlayerDebuffRadius() {
        return TWIN_BOSS_NEARBY_PLAYER_DEBUFF_RADIUS.get();
    }

    public static int getTwinBossMeteorWarningTicks() {
        return TWIN_BOSS_METEOR_WARNING_TICKS.get();
    }

    public static int getTwinBossMeteorTargetRadius() {
        return TWIN_BOSS_METEOR_TARGET_RADIUS.get();
    }

    public static int getTwinBossMeteorSpawnYOffset() {
        return TWIN_BOSS_METEOR_SPAWN_Y_OFFSET.get();
    }

    private static boolean isValidString(Object value) {
        String string;
        return value instanceof String && !(string = (String)value).isBlank();
    }

    private static List<String> castList(List<? extends String> list) {
        return List.copyOf(list);
    }

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("chaos_monarch");
        CHAOS_MONARCH_NEARBY_PLAYER_DEBUFF_RADIUS = builder.comment("Radius for applying phase debuffs to players fighting Chaos Monarch.").defineInRange("nearby_player_debuff_radius", 64, 1, 256);
        CHAOS_MONARCH_PHASE_1_BUFFS = builder.comment("Permanent phase 1 buffs for Chaos Monarch.").defineListAllowEmpty(List.of("phase_1_buffs"), DEFAULT_CHAOS_MONARCH_PHASE_1_BUFFS, ChaosMonarchConfig::isValidString);
        CHAOS_MONARCH_PHASE_2_BUFFS = builder.comment("Permanent phase 2 buffs for Chaos Monarch.").defineListAllowEmpty(List.of("phase_2_buffs"), DEFAULT_CHAOS_MONARCH_PHASE_2_BUFFS, ChaosMonarchConfig::isValidString);
        CHAOS_MONARCH_PHASE_3_BUFFS = builder.comment("Permanent phase 3 buffs for Chaos Monarch.").defineListAllowEmpty(List.of("phase_3_buffs"), DEFAULT_CHAOS_MONARCH_PHASE_3_BUFFS, ChaosMonarchConfig::isValidString);
        CHAOS_MONARCH_PHASE_4_BUFFS = builder.comment("Permanent phase 4 buffs for Chaos Monarch.").defineListAllowEmpty(List.of("phase_4_buffs"), DEFAULT_CHAOS_MONARCH_PHASE_4_BUFFS, ChaosMonarchConfig::isValidString);
        CHAOS_MONARCH_PHASE_5_BUFFS = builder.comment("Permanent phase 5 buffs for Chaos Monarch.").defineListAllowEmpty(List.of("phase_5_buffs"), DEFAULT_CHAOS_MONARCH_PHASE_5_BUFFS, ChaosMonarchConfig::isValidString);
        CHAOS_MONARCH_PHASE_6_BUFFS = builder.comment("Permanent phase 6 buffs for Chaos Monarch.").defineListAllowEmpty(List.of("phase_6_buffs"), DEFAULT_CHAOS_MONARCH_PHASE_6_BUFFS, ChaosMonarchConfig::isValidString);
        CHAOS_MONARCH_PHASE_1_DEBUFFS = builder.comment("Phase 1 debuffs applied to players fighting Chaos Monarch.").defineListAllowEmpty(List.of("phase_1_debuffs"), DEFAULT_CHAOS_MONARCH_PHASE_1_DEBUFFS, ChaosMonarchConfig::isValidString);
        CHAOS_MONARCH_PHASE_2_DEBUFFS = builder.comment("Phase 2 debuffs applied to players fighting Chaos Monarch.").defineListAllowEmpty(List.of("phase_2_debuffs"), DEFAULT_CHAOS_MONARCH_PHASE_2_DEBUFFS, ChaosMonarchConfig::isValidString);
        CHAOS_MONARCH_PHASE_3_DEBUFFS = builder.comment("Phase 3 debuffs applied to players fighting Chaos Monarch.").defineListAllowEmpty(List.of("phase_3_debuffs"), DEFAULT_CHAOS_MONARCH_PHASE_3_DEBUFFS, ChaosMonarchConfig::isValidString);
        CHAOS_MONARCH_PHASE_4_DEBUFFS = builder.comment("Phase 4 debuffs applied to players fighting Chaos Monarch.").defineListAllowEmpty(List.of("phase_4_debuffs"), DEFAULT_CHAOS_MONARCH_PHASE_4_DEBUFFS, ChaosMonarchConfig::isValidString);
        CHAOS_MONARCH_PHASE_5_DEBUFFS = builder.comment("Phase 5 debuffs applied to players fighting Chaos Monarch.").defineListAllowEmpty(List.of("phase_5_debuffs"), DEFAULT_CHAOS_MONARCH_PHASE_5_DEBUFFS, ChaosMonarchConfig::isValidString);
        CHAOS_MONARCH_PHASE_6_DEBUFFS = builder.comment("Phase 6 debuffs applied to players fighting Chaos Monarch.").defineListAllowEmpty(List.of("phase_6_debuffs"), DEFAULT_CHAOS_MONARCH_PHASE_6_DEBUFFS, ChaosMonarchConfig::isValidString);
        CHAOS_MONARCH_GUARD_BREAK_REQUIRED_GUARDS_PHASE_1_TO_5 = builder.comment("Perfect guards required to stun Chaos Monarch in phases 1-5.").defineInRange("guard_break_required_guards_phase_1_to_5", 4, 1, 100);
        CHAOS_MONARCH_GUARD_BREAK_REQUIRED_GUARDS_PHASE_6 = builder.comment("Perfect guards required to stun Chaos Monarch in phase 6.").defineInRange("guard_break_required_guards_phase_6", 8, 1, 100);
        CHAOS_MONARCH_NORMAL_DAMAGE_MULTIPLIER = builder.comment("Damage multiplier while Chaos Monarch is not stunned.").defineInRange("normal_damage_multiplier", 0.5D, 0.0D, 10.0D);
        CHAOS_MONARCH_PHASE_6_HEAL_PER_SECOND = builder.comment("Health restored each second in phase 6.").defineInRange("phase_6_heal_per_second", 2.0D, 0.0D, 1000.0D);
        CHAOS_MONARCH_LIGHTNING_PHASE_1_HIT_EFFECTS = builder.comment("Phase 1 LIGHTNING hit effects.").defineListAllowEmpty(List.of("lightning_phase_1_hit_effects"), DEFAULT_CHAOS_MONARCH_LIGHTNING_PHASE_1_HIT_EFFECTS, ChaosMonarchConfig::isValidString);
        CHAOS_MONARCH_LIGHTNING_PHASE_2_HIT_EFFECTS = builder.comment("Phase 2 LIGHTNING hit effects.").defineListAllowEmpty(List.of("lightning_phase_2_hit_effects"), DEFAULT_CHAOS_MONARCH_LIGHTNING_PHASE_2_HIT_EFFECTS, ChaosMonarchConfig::isValidString);
        CHAOS_MONARCH_LIGHTNING_PHASE_3_HIT_EFFECTS = builder.comment("Phase 3 LIGHTNING hit effects.").defineListAllowEmpty(List.of("lightning_phase_3_hit_effects"), DEFAULT_CHAOS_MONARCH_LIGHTNING_PHASE_3_HIT_EFFECTS, ChaosMonarchConfig::isValidString);
        CHAOS_MONARCH_LIGHTNING_PHASE_4_HIT_EFFECTS = builder.comment("Phase 4 LIGHTNING hit effects.").defineListAllowEmpty(List.of("lightning_phase_4_hit_effects"), DEFAULT_CHAOS_MONARCH_LIGHTNING_PHASE_4_HIT_EFFECTS, ChaosMonarchConfig::isValidString);
        CHAOS_MONARCH_LIGHTNING_PHASE_5_HIT_EFFECTS = builder.comment("Phase 5 LIGHTNING hit effects.").defineListAllowEmpty(List.of("lightning_phase_5_hit_effects"), DEFAULT_CHAOS_MONARCH_LIGHTNING_PHASE_5_HIT_EFFECTS, ChaosMonarchConfig::isValidString);
        CHAOS_MONARCH_LIGHTNING_PHASE_6_HIT_EFFECTS = builder.comment("Phase 6 LIGHTNING hit effects.").defineListAllowEmpty(List.of("lightning_phase_6_hit_effects"), DEFAULT_CHAOS_MONARCH_LIGHTNING_PHASE_6_HIT_EFFECTS, ChaosMonarchConfig::isValidString);
        builder.pop();
        builder.push("draugr_boss");
        DRAUGR_BOSS_GUARD_BREAK_REQUIRED_GUARDS = builder.comment("Perfect SlashBlade guards required to posture-break Draugr Boss.").defineInRange("guard_break_required_guards", 6, 1, 100);
        DRAUGR_BOSS_NORMAL_DAMAGE_MULTIPLIER = builder.comment("Damage multiplier while Draugr Boss is not posture-broken.").defineInRange("normal_damage_multiplier", 0.5D, 0.0D, 10.0D);
        builder.pop();
        builder.push("night_shade");
        NIGHT_SHADE_GUARD_BREAK_REQUIRED_GUARDS = builder.comment("Perfect SlashBlade guards required to execute Night Shade.").defineInRange("guard_break_required_guards", 3, 1, 100);
        builder.pop();
        builder.push("moonknight");
        MOONKNIGHT_GUARD_BREAK_REQUIRED_GUARDS = builder.comment("Perfect guards required to force Moonknight's Core Beam.").defineInRange("guard_break_required_guards", 8, 1, 100);
        MOONKNIGHT_PHASE_2_NORMAL_DAMAGE_MULTIPLIER = builder.comment("Phase 2 damage multiplier before Core Beam is forced.").defineInRange("phase_2_normal_damage_multiplier", 0.7D, 0.0D, 10.0D);
        builder.pop();
        builder.push("chaos_twins");
        TWIN_BOSS_NEARBY_PLAYER_DEBUFF_RADIUS = builder.comment("Radius for applying combat debuffs to players fighting Day Stalker or Night Prowler.").defineInRange("nearby_player_debuff_radius", 64, 1, 256);
        TWIN_BOSS_METEOR_WARNING_TICKS = builder.comment("Red ground warning duration before ambience meteors land.").defineInRange("meteor_warning_ticks", 40, 1, 200);
        TWIN_BOSS_METEOR_TARGET_RADIUS = builder.comment("Radius for keeping ambience meteors active while players are fighting a twin boss.").defineInRange("meteor_target_radius", 64, 1, 256);
        TWIN_BOSS_METEOR_SPAWN_Y_OFFSET = builder.comment("Vertical offset above the impact point where ambience meteors spawn.").defineInRange("meteor_spawn_y_offset", 30, 4, 128);
        builder.pop();
        builder.push("night_prowler");
        NIGHT_PROWLER_GUARD_BREAK_REQUIRED_GUARDS = builder.comment("Perfect guards required to stun Night Prowler.").defineInRange("guard_break_required_guards", 12, 1, 100);
        NIGHT_PROWLER_PHASE_1_DAMAGE_MULTIPLIER = builder.comment("Phase 1 damage multiplier for Night Prowler.").defineInRange("phase_1_damage_multiplier", 0.7D, 0.0D, 10.0D);
        NIGHT_PROWLER_PHASE_2_NORMAL_DAMAGE_MULTIPLIER = builder.comment("Phase 2 normal damage multiplier for Night Prowler.").defineInRange("phase_2_normal_damage_multiplier", 0.5D, 0.0D, 10.0D);
        NIGHT_PROWLER_PHASE_1_BUFFS = builder.comment("Permanent phase 1 buffs for Night Prowler.").defineListAllowEmpty(List.of("phase_1_buffs"), DEFAULT_NIGHT_PROWLER_PHASE_1_BUFFS, ChaosMonarchConfig::isValidString);
        NIGHT_PROWLER_PHASE_2_BUFFS = builder.comment("Permanent phase 2 buffs for Night Prowler.").defineListAllowEmpty(List.of("phase_2_buffs"), DEFAULT_NIGHT_PROWLER_PHASE_2_BUFFS, ChaosMonarchConfig::isValidString);
        NIGHT_PROWLER_NEARBY_PLAYER_DEBUFFS = builder.comment("Debuffs applied to players in combat with Night Prowler.").defineListAllowEmpty(List.of("nearby_player_debuffs"), DEFAULT_TWIN_BOSS_NEARBY_PLAYER_DEBUFFS, ChaosMonarchConfig::isValidString);
        NIGHT_PROWLER_METEOR_HIT_EFFECTS = builder.comment("Effects applied when a Night Prowler ambience meteor damages a target.").defineListAllowEmpty(List.of("meteor_hit_effects"), DEFAULT_TWIN_BOSS_METEOR_HIT_EFFECTS, ChaosMonarchConfig::isValidString);
        NIGHT_PROWLER_REACTION_AOE_HIT_EFFECTS = builder.comment("Effects applied when Night Prowler's reaction lightning damages a target.").defineListAllowEmpty(List.of("reaction_aoe_hit_effects"), DEFAULT_NIGHT_PROWLER_REACTION_AOE_HIT_EFFECTS, ChaosMonarchConfig::isValidString);
        builder.pop();
        builder.push("day_stalker");
        DAY_STALKER_GUARD_BREAK_REQUIRED_GUARDS = builder.comment("Perfect guards required to stun Day Stalker.").defineInRange("guard_break_required_guards", 12, 1, 100);
        DAY_STALKER_PHASE_1_DAMAGE_MULTIPLIER = builder.comment("Phase 1 damage multiplier for Day Stalker.").defineInRange("phase_1_damage_multiplier", 0.7D, 0.0D, 10.0D);
        DAY_STALKER_PHASE_2_NORMAL_DAMAGE_MULTIPLIER = builder.comment("Phase 2 normal damage multiplier for Day Stalker.").defineInRange("phase_2_normal_damage_multiplier", 0.5D, 0.0D, 10.0D);
        DAY_STALKER_PHASE_1_BUFFS = builder.comment("Permanent phase 1 buffs for Day Stalker.").defineListAllowEmpty(List.of("phase_1_buffs"), DEFAULT_DAY_STALKER_PHASE_1_BUFFS, ChaosMonarchConfig::isValidString);
        DAY_STALKER_PHASE_2_BUFFS = builder.comment("Permanent phase 2 buffs for Day Stalker.").defineListAllowEmpty(List.of("phase_2_buffs"), DEFAULT_DAY_STALKER_PHASE_2_BUFFS, ChaosMonarchConfig::isValidString);
        DAY_STALKER_NEARBY_PLAYER_DEBUFFS = builder.comment("Debuffs applied to players in combat with Day Stalker.").defineListAllowEmpty(List.of("nearby_player_debuffs"), DEFAULT_TWIN_BOSS_NEARBY_PLAYER_DEBUFFS, ChaosMonarchConfig::isValidString);
        DAY_STALKER_METEOR_HIT_EFFECTS = builder.comment("Effects applied when a Day Stalker ambience meteor damages a target.").defineListAllowEmpty(List.of("meteor_hit_effects"), DEFAULT_TWIN_BOSS_METEOR_HIT_EFFECTS, ChaosMonarchConfig::isValidString);
        DAY_STALKER_REACTION_TRAP_HIT_EFFECTS = builder.comment("Effects applied when a Day Stalker reaction trap damages a target.").defineListAllowEmpty(List.of("reaction_trap_hit_effects"), DEFAULT_DAY_STALKER_REACTION_TRAP_HIT_EFFECTS, ChaosMonarchConfig::isValidString);
        builder.pop();
        SPEC = builder.build();
    }
}
