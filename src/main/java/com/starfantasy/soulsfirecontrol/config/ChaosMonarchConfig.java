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
    public static final List<String> DEFAULT_CONTROLLED_PROJECTILES = List.of("minecraft:spectral_arrow", "minecraft:shulker_bullet", "minecraft:experience_bottle", "starfantasy:fire_combo", "minecraft:snowball");
    public static final List<String> DEFAULT_RANDOM_PROJECTILES = List.of("minecraft:arrow", "minecraft:dragon_fireball", "minecraft:fireball", "minecraft:llama_spit", "minecraft:small_fireball", "minecraft:spectral_arrow", "minecraft:wither_skull", "minecraft:egg", "minecraft:experience_bottle", "minecraft:snowball", "minecraft:trident", "soulsweapons:cannonball_entity_type", "soulsweapons:charged_arrow_entity", "soulsweapons:comet_spear_entity", "soulsweapons:swordspear_entity", "soulsweapons:big_moonlight_projectile", "soulsweapons:silver_bullet_entity");
    public static final List<String> DEFAULT_RANDOM_HOSTILE_SUMMONS = List.of("minecraft:blaze", "minecraft:creeper", "minecraft:drowned", "minecraft:enderman", "minecraft:silverfish", "minecraft:skeleton", "minecraft:slime", "minecraft:spider", "minecraft:witch", "minecraft:wither_skeleton", "minecraft:zombie");
    public static final List<String> DEFAULT_RANDOM_PASSIVE_SUMMONS = List.of("minecraft:bat", "minecraft:bee", "minecraft:chicken", "minecraft:cod", "minecraft:cow", "minecraft:glow_squid", "minecraft:horse", "minecraft:llama", "minecraft:wandering_trader", "minecraft:mooshroom", "minecraft:pig", "minecraft:polar_bear", "minecraft:pufferfish", "minecraft:rabbit", "minecraft:salmon");
    public static final List<String> DEFAULT_NIGHT_PROWLER_PHASE_1_BUFFS = List.of("minecraft:regeneration:1", "minecraft:speed:10");
    public static final List<String> DEFAULT_NIGHT_PROWLER_PHASE_2_BUFFS = List.of("minecraft:regeneration:2", "minecraft:speed:10", "minecraft:resistance:2");
    public static final List<String> DEFAULT_DAY_STALKER_PHASE_1_BUFFS = List.of("minecraft:regeneration:2", "minecraft:speed:10");
    public static final List<String> DEFAULT_DAY_STALKER_PHASE_2_BUFFS = List.of("minecraft:regeneration:3", "minecraft:speed:10", "minecraft:resistance:2");
    public static final List<String> DEFAULT_TWIN_BOSS_NEARBY_PLAYER_DEBUFFS = List.of("minecraft:weakness:1:200", "minecraft:hunger:1:200");
    public static final List<String> DEFAULT_TWIN_BOSS_METEOR_HIT_EFFECTS = List.of("minecraft:slowness:1:200");
    public static final List<String> DEFAULT_DAY_STALKER_REACTION_TRAP_HIT_EFFECTS = List.of("minecraft:wither:1:200");
    public static final List<String> DEFAULT_NIGHT_PROWLER_REACTION_AOE_HIT_EFFECTS = List.of("minecraft:wither:1:200");
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.IntValue CHAOS_MONARCH_PROJECTILE_LIFETIME_TICKS;
    private static final ForgeConfigSpec.IntValue CHAOS_MONARCH_CHAOS_SKULL_COOLDOWN_TICKS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CHAOS_MONARCH_CONTROLLED_PROJECTILES;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CHAOS_MONARCH_RANDOM_PROJECTILES;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CHAOS_MONARCH_RANDOM_HOSTILE_SUMMONS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CHAOS_MONARCH_RANDOM_PASSIVE_SUMMONS;
    private static final ForgeConfigSpec.IntValue DRAUGR_BOSS_GUARD_BREAK_REQUIRED_GUARDS;
    private static final ForgeConfigSpec.DoubleValue DRAUGR_BOSS_NORMAL_DAMAGE_MULTIPLIER;
    private static final ForgeConfigSpec.IntValue NIGHT_SHADE_GUARD_BREAK_REQUIRED_GUARDS;
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
    private static final ForgeConfigSpec.ConfigValue<String> DAY_STALKER_REACTION_TRAP_ENTITY;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> DAY_STALKER_REACTION_TRAP_HIT_EFFECTS;
    private static final ForgeConfigSpec.IntValue TWIN_BOSS_NEARBY_PLAYER_DEBUFF_RADIUS;
    private static final ForgeConfigSpec.IntValue TWIN_BOSS_METEOR_WARNING_TICKS;
    private static final ForgeConfigSpec.IntValue TWIN_BOSS_METEOR_TARGET_RADIUS;
    private static final ForgeConfigSpec.IntValue TWIN_BOSS_METEOR_SPAWN_Y_OFFSET;

    private ChaosMonarchConfig() {
    }

    public static int getProjectileLifetimeTicks() {
        return (Integer)CHAOS_MONARCH_PROJECTILE_LIFETIME_TICKS.get();
    }

    public static List<String> getControlledProjectiles() {
        return ChaosMonarchConfig.castList((List)CHAOS_MONARCH_CONTROLLED_PROJECTILES.get());
    }

    public static int getChaosSkullCooldownTicks() {
        return (Integer)CHAOS_MONARCH_CHAOS_SKULL_COOLDOWN_TICKS.get();
    }

    public static List<String> getRandomProjectiles() {
        return ChaosMonarchConfig.castList((List)CHAOS_MONARCH_RANDOM_PROJECTILES.get());
    }

    public static List<String> getRandomHostileSummons() {
        return ChaosMonarchConfig.castList((List)CHAOS_MONARCH_RANDOM_HOSTILE_SUMMONS.get());
    }

    public static List<String> getRandomPassiveSummons() {
        return ChaosMonarchConfig.castList((List)CHAOS_MONARCH_RANDOM_PASSIVE_SUMMONS.get());
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

    public static String getDayStalkerReactionTrapEntity() {
        return DAY_STALKER_REACTION_TRAP_ENTITY.get();
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
        CHAOS_MONARCH_PROJECTILE_LIFETIME_TICKS = builder.comment("Hard lifetime for Chaos Monarch projectiles.").defineInRange("projectile_lifetime_ticks", 200, 0, 1200);
        CHAOS_MONARCH_CHAOS_SKULL_COOLDOWN_TICKS = builder.comment("Cooldown for the Chaos Skull summon attack.").defineInRange("chaos_skull_cooldown_ticks", 600, 0, 72000);
        CHAOS_MONARCH_CONTROLLED_PROJECTILES = builder.comment("Projectile pool for the controlled-projectile attack.").defineListAllowEmpty(List.of("controlled_projectiles"), DEFAULT_CONTROLLED_PROJECTILES, ChaosMonarchConfig::isValidString);
        CHAOS_MONARCH_RANDOM_PROJECTILES = builder.comment("Projectile pool for the random projectile barrage attack.").defineListAllowEmpty(List.of("random_projectiles"), DEFAULT_RANDOM_PROJECTILES, ChaosMonarchConfig::isValidString);
        CHAOS_MONARCH_RANDOM_HOSTILE_SUMMONS = builder.comment("Random hostile summon pool used by Chaos Skull.").defineListAllowEmpty(List.of("random_hostile_summons"), DEFAULT_RANDOM_HOSTILE_SUMMONS, ChaosMonarchConfig::isValidString);
        CHAOS_MONARCH_RANDOM_PASSIVE_SUMMONS = builder.comment("Random passive summon pool used by Chaos Skull.").defineListAllowEmpty(List.of("random_passive_summons"), DEFAULT_RANDOM_PASSIVE_SUMMONS, ChaosMonarchConfig::isValidString);
        builder.pop();
        builder.push("draugr_boss");
        DRAUGR_BOSS_GUARD_BREAK_REQUIRED_GUARDS = builder.comment("Perfect SlashBlade guards required to posture-break Draugr Boss.").defineInRange("guard_break_required_guards", 6, 1, 100);
        DRAUGR_BOSS_NORMAL_DAMAGE_MULTIPLIER = builder.comment("Damage multiplier while Draugr Boss is not posture-broken.").defineInRange("normal_damage_multiplier", 0.5D, 0.0D, 10.0D);
        builder.pop();
        builder.push("night_shade");
        NIGHT_SHADE_GUARD_BREAK_REQUIRED_GUARDS = builder.comment("Perfect SlashBlade guards required to execute Night Shade.").defineInRange("guard_break_required_guards", 3, 1, 100);
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
        DAY_STALKER_REACTION_TRAP_ENTITY = builder.comment("Visual trap entity spawned by Day Stalker's phase 2 reaction trap pattern.").define("reaction_trap_entity", "soulsweapons:flame_pillar");
        DAY_STALKER_REACTION_TRAP_HIT_EFFECTS = builder.comment("Effects applied when a Day Stalker reaction trap damages a target.").defineListAllowEmpty(List.of("reaction_trap_hit_effects"), DEFAULT_DAY_STALKER_REACTION_TRAP_HIT_EFFECTS, ChaosMonarchConfig::isValidString);
        builder.pop();
        SPEC = builder.build();
    }
}
