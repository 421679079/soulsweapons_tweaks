package com.starfantasy.soulsfirecontrol.util;

import com.starfantasy.soulsfirecontrol.vfx.telegraph.TelegraphVfx;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.soulsweaponry.entity.mobs.Moonknight;

public final class MoonknightTweaks {
    public static final String NO_LOOT_SUMMON_TAG = "starfantasy_moonknight_no_loot_summon";

    private static final int DEFAULT_GOAL_TICK_INTERVAL = 2;
    private static final int MACE_FIRST_HIT_FRAME = 7;
    private static final int MACE_SECOND_TARGET_FRAME = 13;
    private static final int MACE_SECOND_HIT_FRAME = 21;
    private static final int OBLITERATE_HIT_FRAME = 13;
    private static final int BLIND_HIT_FRAME = 9;
    private static final int RUPTURE_WARNING_FRAME = 32;
    private static final int RUPTURE_HIT_FRAME = 52;
    private static final int SWORD_LIGHT_FIRST_HIT_FRAME = 10;
    private static final int SWORD_LIGHT_SECOND_HIT_FRAME = 16;
    private static final int SWORD_LIGHT_THIRD_HIT_FRAME = 22;
    private static final int SWORD_LIGHT_FOURTH_HIT_FRAME = 27;
    private static final int SWORD_LIGHT_SMASH_HIT_FRAME = 43;
    private static final int MOONFALL_LOCK_FRAME = 15;
    private static final int MOONFALL_HIT_FRAME = 25;
    private static final int MOONVEIL_LANDING_FRAME = 24;
    private static final int MOONVEIL_FIRST_HIT_FRAME = 38;
    private static final int MOONVEIL_SECOND_HIT_FRAME = 63;
    private static final int THRUST_FIRST_HIT_FRAME = 17;
    private static final int THRUST_SECOND_HIT_FRAME = 29;
    private static final int THRUST_THIRD_HIT_FRAME = 39;
    private static final int THRUST_BLINDING_LIGHT_FRAME = 48;
    private static final int CORE_BEAM_LOCK_FRAME = 19;
    private static final int PHASE_TWO_RUPTURE_SMASH_FRAME = 29;
    private static final int HEAVY_SWING_PROJECTILE_FRAME = 22;
    private static final int WARMUP_ENTITY_DAMAGE_DELAY = 7;

    private static final double WARNING_HEIGHT_FRACTION = 0.5D;
    private static final double MIN_WARNING_HEIGHT = 2.5D;
    private static final double MAX_WARNING_HEIGHT = 5.0D;
    private static final double MACE_FIRST_BODY_WARNING_RADIUS = 10.0D;
    private static final double MACE_SECOND_BODY_WARNING_RADIUS = 6.0D;
    private static final double MACE_SECOND_GROUND_WARNING_RADIUS = 3.5D;
    private static final double OBLITERATE_BODY_WARNING_RADIUS = 7.5D;
    private static final double OBLITERATE_GROUND_WARNING_RADIUS = 3.5D;
    private static final double BLIND_RADIUS = 5.0D;
    private static final double RUPTURE_PHASE_ONE_RADIUS = 14.0D;
    private static final double SWORD_LIGHT_RADIUS = 7.5D;
    private static final double MOONFALL_BODY_RADIUS = 8.0D;
    private static final double MOONFALL_GROUND_RADIUS = 3.5D;
    private static final double MOONVEIL_RADIUS = 7.5D;
    private static final double THRUST_BODY_RADIUS = 6.0D;
    private static final double CORE_BEAM_GROUND_RADIUS = 4.0D;
    private static final double PHASE_TWO_RUPTURE_RADIUS = 3.5D;
    private static final double HEAVY_SWING_BODY_RADIUS = 8.0D;
    private static final double HOLY_MOONLIGHT_PILLAR_RADIUS = 3.5D;

    private MoonknightTweaks() {
    }

    public static void warnMaceOpening(Moonknight boss, int attackStatus) {
        if (!isPhaseOne(boss) || attackStatus != 0) {
            return;
        }
        warnBossBody(boss, warningTicksForInitialGoalFrame(MACE_FIRST_HIT_FRAME),
                MACE_FIRST_BODY_WARNING_RADIUS, true);
    }

    public static void warnMaceSecond(Moonknight boss, int attackStatus, BlockPos targetPos) {
        if (!isPhaseOne(boss) || attackStatus != MACE_SECOND_TARGET_FRAME - 1) {
            return;
        }
        int ticks = warningTicksBetweenGoalFrames(MACE_SECOND_TARGET_FRAME, MACE_SECOND_HIT_FRAME);
        warnBossBody(boss, ticks, MACE_SECOND_BODY_WARNING_RADIUS, true);
        warnGround(boss, targetPos, ticks, MACE_SECOND_GROUND_WARNING_RADIUS, true);
    }

    public static void warnObliterate(Moonknight boss, int attackStatus, BlockPos targetPos) {
        if (boss == null) {
            return;
        }
        if (isPhaseOne(boss)) {
            if (attackStatus != 0) {
                return;
            }
            int ticks = warningTicksForInitialGoalFrame(OBLITERATE_HIT_FRAME);
            warnBossBody(boss, ticks, OBLITERATE_BODY_WARNING_RADIUS, true);
            warnGround(boss, targetPos, ticks, OBLITERATE_GROUND_WARNING_RADIUS, true);
            return;
        }
        if (!boss.getPhaseTwoAttack().equals(Moonknight.MoonknightPhaseTwo.MOONFALL)) {
            return;
        }
        if (attackStatus == 0) {
            warnBossBody(boss, warningTicksForInitialGoalFrame(MOONFALL_HIT_FRAME),
                    MOONFALL_BODY_RADIUS, false);
        }
        if (attackStatus == MOONFALL_LOCK_FRAME - 1) {
            warnGround(boss, targetPos, warningTicksBetweenGoalFrames(MOONFALL_LOCK_FRAME, MOONFALL_HIT_FRAME),
                    MOONFALL_GROUND_RADIUS, false);
        }
    }

    public static void warnBlind(Moonknight boss, int attackStatus) {
        if (boss == null || attackStatus != 0) {
            return;
        }
        warnBossBody(boss, warningTicksForInitialGoalFrame(BLIND_HIT_FRAME), BLIND_RADIUS, isPhaseOne(boss));
    }

    public static void warnRupture(Moonknight boss, int attackStatus) {
        if (boss == null) {
            return;
        }
        if (isPhaseOne(boss)) {
            if (attackStatus != RUPTURE_WARNING_FRAME) {
                return;
            }
            int ticks = warningTicksBetweenGoalFrames(RUPTURE_WARNING_FRAME, RUPTURE_HIT_FRAME);
            warnBossBody(boss, ticks, RUPTURE_PHASE_ONE_RADIUS, true);
            TelegraphVfx.redGroundWarningCircle(boss, boss.position().add(0.0D, 0.06D, 0.0D), ticks,
                    RUPTURE_PHASE_ONE_RADIUS);
            return;
        }
        if (attackStatus == 0 && boss.getPhaseTwoAttack().equals(Moonknight.MoonknightPhaseTwo.RUPTURE)) {
            int ticks = warningTicksForInitialGoalFrame(PHASE_TWO_RUPTURE_SMASH_FRAME);
            warnBossBody(boss, ticks, PHASE_TWO_RUPTURE_RADIUS, false);
            TelegraphVfx.groundWarningCircleTrackingGround(boss, ticks, PHASE_TWO_RUPTURE_RADIUS, false);
        }
    }

    public static void warnSwordOfLight(Moonknight boss, int attackStatus, LivingEntity target) {
        if (!isPhaseTwoAttack(boss, Moonknight.MoonknightPhaseTwo.SWORD_OF_LIGHT)) {
            return;
        }
        switch (attackStatus) {
            case 0 -> warnBossBody(boss, warningTicksForInitialGoalFrame(SWORD_LIGHT_FIRST_HIT_FRAME),
                    SWORD_LIGHT_RADIUS, false);
            case SWORD_LIGHT_FIRST_HIT_FRAME -> warnBossBody(boss,
                    warningTicksBetweenGoalFrames(SWORD_LIGHT_FIRST_HIT_FRAME, SWORD_LIGHT_SECOND_HIT_FRAME),
                    SWORD_LIGHT_RADIUS, false);
            case SWORD_LIGHT_SECOND_HIT_FRAME -> warnBossBody(boss,
                    warningTicksBetweenGoalFrames(SWORD_LIGHT_SECOND_HIT_FRAME, SWORD_LIGHT_THIRD_HIT_FRAME),
                    SWORD_LIGHT_RADIUS, false);
            case SWORD_LIGHT_THIRD_HIT_FRAME -> warnBossBody(boss,
                    warningTicksBetweenGoalFrames(SWORD_LIGHT_THIRD_HIT_FRAME, SWORD_LIGHT_FOURTH_HIT_FRAME),
                    SWORD_LIGHT_RADIUS, false);
            case SWORD_LIGHT_FOURTH_HIT_FRAME -> {
                int ticks = warningTicksBetweenGoalFrames(SWORD_LIGHT_FOURTH_HIT_FRAME,
                        SWORD_LIGHT_SMASH_HIT_FRAME);
                warnBossBody(boss, ticks, SWORD_LIGHT_RADIUS, false);
            }
            default -> {
            }
        }
    }

    public static void warnMoonveil(Moonknight boss, int attackStatus) {
        if (!isPhaseTwoAttack(boss, Moonknight.MoonknightPhaseTwo.MOONVEIL)) {
            return;
        }
        if (attackStatus == 0) {
            warnBossBody(boss, warningTicksForInitialGoalFrame(MOONVEIL_FIRST_HIT_FRAME),
                    MOONVEIL_RADIUS, false);
        }
        if (attackStatus == MOONVEIL_LANDING_FRAME) {
            TelegraphVfx.groundWarningCircleTrackingGround(boss,
                    warningTicksBetweenGoalFrames(MOONVEIL_LANDING_FRAME, MOONVEIL_FIRST_HIT_FRAME),
                    MOONVEIL_RADIUS, false);
        }
        if (attackStatus == MOONVEIL_FIRST_HIT_FRAME) {
            int ticks = warningTicksBetweenGoalFrames(MOONVEIL_FIRST_HIT_FRAME, MOONVEIL_SECOND_HIT_FRAME);
            warnBossBody(boss, ticks, MOONVEIL_RADIUS, false);
            TelegraphVfx.groundWarningCircleTrackingGround(boss, ticks, MOONVEIL_RADIUS, false);
        }
    }

    public static void warnThrust(Moonknight boss, int attackStatus, LivingEntity target) {
        if (!isPhaseTwoAttack(boss, Moonknight.MoonknightPhaseTwo.THRUST)) {
            return;
        }
        switch (attackStatus) {
            case 0 -> {
                int ticks = warningTicksForInitialGoalFrame(THRUST_FIRST_HIT_FRAME);
                warnBossBody(boss, ticks, THRUST_BODY_RADIUS, false);
            }
            case THRUST_FIRST_HIT_FRAME -> warnBossBody(boss,
                    warningTicksBetweenGoalFrames(THRUST_FIRST_HIT_FRAME, THRUST_SECOND_HIT_FRAME),
                    THRUST_BODY_RADIUS, false);
            case THRUST_SECOND_HIT_FRAME -> warnBossBody(boss,
                    warningTicksBetweenGoalFrames(THRUST_SECOND_HIT_FRAME, THRUST_THIRD_HIT_FRAME),
                    THRUST_BODY_RADIUS, false);
            case THRUST_THIRD_HIT_FRAME -> warnBossBody(boss,
                    warningTicksBetweenGoalFrames(THRUST_THIRD_HIT_FRAME, THRUST_BLINDING_LIGHT_FRAME),
                    BLIND_RADIUS, false);
            default -> {
            }
        }
    }

    public static void warnCoreBeam(Moonknight boss, int attackStatus, LivingEntity target) {
        if (!isPhaseTwoAttack(boss, Moonknight.MoonknightPhaseTwo.CORE_BEAM)) {
            return;
        }
        if (attackStatus == 0 && target != null) {
            TelegraphVfx.groundWarningCircleTrackingGround(target,
                    warningTicksForInitialGoalFrame(CORE_BEAM_LOCK_FRAME),
                    CORE_BEAM_GROUND_RADIUS, true);
        }
    }

    public static void warnHeavySwing(Moonknight boss, int attackStatus) {
        if (!isPhaseTwoAttack(boss, Moonknight.MoonknightPhaseTwo.HEAVY_SWING) || attackStatus != 0) {
            return;
        }
        warnBossBody(boss, warningTicksForInitialGoalFrame(HEAVY_SWING_PROJECTILE_FRAME),
                HEAVY_SWING_BODY_RADIUS, true);
    }

    public static void warnMoonlightPillarGround(Moonknight boss, Vec3 position, int warmup) {
        if (boss == null || position == null) {
            return;
        }
        int ticks = Math.max(1, warmup + WARMUP_ENTITY_DAMAGE_DELAY);
        TelegraphVfx.redGroundWarningCircle(boss, position.add(0.0D, 0.06D, 0.0D),
                ticks, HOLY_MOONLIGHT_PILLAR_RADIUS);
    }

    public static boolean rewardsGuardBreak(Moonknight boss) {
        if (boss == null || !boss.isPhaseTwo()) {
            return false;
        }
        return switch (boss.getPhaseTwoAttack()) {
            case SWORD_OF_LIGHT, MOONFALL, MOONVEIL, THRUST, BLINDING_LIGHT, RUPTURE -> true;
            default -> false;
        };
    }

    public static void prepareNoLootSummon(Entity entity) {
        if (entity == null) {
            return;
        }
        entity.addTag(NO_LOOT_SUMMON_TAG);
        if (entity instanceof Mob mob) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                mob.setDropChance(slot, 0.0F);
            }
        }
    }

    private static boolean isPhaseOne(Moonknight boss) {
        return boss != null && !boss.isPhaseTwo();
    }

    private static boolean isPhaseTwoAttack(Moonknight boss, Moonknight.MoonknightPhaseTwo attack) {
        return boss != null && boss.isPhaseTwo() && boss.getPhaseTwoAttack().equals(attack);
    }

    private static void warnBossBody(Moonknight boss, int ticks, double radius, boolean red) {
        if (boss == null || ticks <= 0 || radius <= 0.0D) {
            return;
        }
        double height = Math.max(MIN_WARNING_HEIGHT,
                Math.min(MAX_WARNING_HEIGHT, boss.getBbHeight() * WARNING_HEIGHT_FRACTION));
        if (red) {
            TelegraphVfx.redAttackWarningRing(boss, ticks, radius, height);
        } else {
            TelegraphVfx.attackWarningRing(boss, ticks, radius, height);
        }
    }

    private static void warnGround(Moonknight boss, BlockPos pos, int ticks, double radius, boolean red) {
        if (boss == null || pos == null || ticks <= 0 || radius <= 0.0D) {
            return;
        }
        Vec3 center = new Vec3(pos.getX() + 0.5D, pos.getY() + 0.06D, pos.getZ() + 0.5D);
        if (red) {
            TelegraphVfx.redGroundWarningCircle(boss, center, ticks, radius);
        } else {
            TelegraphVfx.groundWarningCircle(boss, center, ticks, radius);
        }
    }

    private static int warningTicksForInitialGoalFrame(int hitFrame) {
        return Math.max(1, hitFrame * DEFAULT_GOAL_TICK_INTERVAL);
    }

    private static int warningTicksBetweenGoalFrames(int startFrame, int hitFrame) {
        return Math.max(1, (hitFrame - startFrame) * DEFAULT_GOAL_TICK_INTERVAL);
    }
}
