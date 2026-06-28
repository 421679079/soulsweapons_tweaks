package com.starfantasy.soulsfirecontrol.util;

import com.starfantasy.soulsfirecontrol.vfx.telegraph.TelegraphVfx;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.soulsweaponry.entity.mobs.ReturningKnight;

public final class ReturningKnightTweaks {
    public static final String NO_LOOT_SUMMON_TAG = "starfantasy_returning_knight_no_loot_summon";

    private static final int DEFAULT_GOAL_TICK_INTERVAL = 2;
    private static final int MACE_FIRST_HIT_FRAME = 7;
    private static final int MACE_SECOND_TARGET_FRAME = 13;
    private static final int MACE_SECOND_HIT_FRAME = 21;
    private static final int OBLITERATE_HIT_FRAME = 18;
    private static final int BLIND_HIT_FRAME = 12;
    private static final int RUPTURE_WARNING_FRAME = 32;
    private static final int RUPTURE_HIT_FRAME = 52;

    private static final double WARNING_HEIGHT_FRACTION = 0.5D;
    private static final double MIN_WARNING_HEIGHT = 2.5D;
    private static final double MAX_WARNING_HEIGHT = 5.0D;
    private static final double MACE_FIRST_BODY_WARNING_RADIUS = 10.0D;
    private static final double MACE_SECOND_BODY_WARNING_RADIUS = 6.0D;
    private static final double MACE_SECOND_GROUND_WARNING_RADIUS = 3.0D;
    private static final double OBLITERATE_BODY_WARNING_RADIUS = 7.5D;
    private static final double OBLITERATE_GROUND_WARNING_RADIUS = 3.0D;
    private static final double BLIND_RADIUS = 5.0D;
    private static final double RUPTURE_RADIUS = 18.0D;

    private ReturningKnightTweaks() {
    }

    public static void warnMaceOpening(ReturningKnight boss, BlockPos targetPos) {
        warnBossBody(boss, warningTicksForInitialGoalFrame(MACE_FIRST_HIT_FRAME), MACE_FIRST_BODY_WARNING_RADIUS);
    }

    public static void warnMaceSecond(ReturningKnight boss, BlockPos targetPos) {
        int ticks = warningTicksBetweenGoalFrames(MACE_SECOND_TARGET_FRAME, MACE_SECOND_HIT_FRAME);
        warnBossBody(boss, ticks, MACE_SECOND_BODY_WARNING_RADIUS);
        warnGround(boss, targetPos, ticks, MACE_SECOND_GROUND_WARNING_RADIUS);
    }

    public static void warnObliterate(ReturningKnight boss, BlockPos targetPos) {
        int ticks = warningTicksForInitialGoalFrame(OBLITERATE_HIT_FRAME);
        warnBossBody(boss, ticks, OBLITERATE_BODY_WARNING_RADIUS);
        warnGround(boss, targetPos, ticks, OBLITERATE_GROUND_WARNING_RADIUS);
    }

    public static void warnBlind(ReturningKnight boss, int attackStatus) {
        if (attackStatus != 0) {
            return;
        }
        warnBossBody(boss, warningTicksForInitialGoalFrame(BLIND_HIT_FRAME), BLIND_RADIUS);
    }

    public static void warnRupture(ReturningKnight boss, int attackStatus) {
        if (boss == null || !boss.getRupture() || attackStatus != RUPTURE_WARNING_FRAME) {
            return;
        }
        int ticks = warningTicksBetweenGoalFrames(RUPTURE_WARNING_FRAME, RUPTURE_HIT_FRAME);
        warnBossBody(boss, ticks, RUPTURE_RADIUS);
        TelegraphVfx.groundWarningCircleTrackingGround(boss, ticks, RUPTURE_RADIUS, true);
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

    private static void warnBossBody(ReturningKnight boss, int ticks, double radius) {
        if (boss == null || ticks <= 0 || radius <= 0.0D) {
            return;
        }
        double height = Math.max(MIN_WARNING_HEIGHT,
                Math.min(MAX_WARNING_HEIGHT, boss.getBbHeight() * WARNING_HEIGHT_FRACTION));
        TelegraphVfx.redAttackWarningRing(boss, ticks, radius, height);
    }

    private static void warnGround(ReturningKnight boss, BlockPos pos, int ticks, double radius) {
        if (boss == null || pos == null || ticks <= 0 || radius <= 0.0D) {
            return;
        }
        Vec3 center = new Vec3(pos.getX() + 0.5D, pos.getY() + 0.06D, pos.getZ() + 0.5D);
        TelegraphVfx.redGroundWarningCircle(boss, center, ticks, radius);
    }

    private static int warningTicksForInitialGoalFrame(int hitFrame) {
        return Math.max(1, hitFrame * DEFAULT_GOAL_TICK_INTERVAL);
    }

    private static int warningTicksBetweenGoalFrames(int startFrame, int hitFrame) {
        return Math.max(1, (hitFrame - startFrame) * DEFAULT_GOAL_TICK_INTERVAL);
    }
}
