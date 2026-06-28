package com.starfantasy.soulsfirecontrol.util;

import com.starfantasy.soulsfirecontrol.vfx.telegraph.TelegraphVfx;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.soulsweaponry.entity.mobs.NightShade;

public final class NightShadeTweaks {
    private static final int BIG_SWIPES_FIRST_WARNING_TICKS = 16;
    private static final int BIG_SWIPES_SECOND_WARNING_TICKS = 14;
    private static final int GENERIC_CHARGE_WARNING_TICKS = 26;
    private static final int AOE_WARNING_TICKS = 30;
    private static final int RANGED_WARNING_TICKS = 10;
    private static final double WARNING_HEIGHT = 1.75D;
    private static final double BOSS_RING_RADIUS_SCALE = 1.5D;
    private static final double BIG_SWIPES_RADIUS = 2.8D;
    private static final double GENERIC_CHARGE_RADIUS = 3.0D;
    private static final double AOE_RADIUS = 3.8D;
    private static final double RANGED_RADIUS = 2.7D;
    private static final double BIG_SWIPES_HALF_SIZE = 2.5D;
    private static final double AOE_HALF_SIZE = 3.5D;

    private NightShadeTweaks() {
    }

    public static void warnBigSwipesOpening(NightShade boss, int attackStatus, BlockPos targetPos) {
        if (attackStatus == 0) {
            TelegraphVfx.attackWarningRing(boss, BIG_SWIPES_FIRST_WARNING_TICKS, scaled(BIG_SWIPES_RADIUS), WARNING_HEIGHT);
            groundSquare(boss, targetPos, BIG_SWIPES_FIRST_WARNING_TICKS, BIG_SWIPES_HALF_SIZE);
        }
    }

    public static void warnBigSwipesSecondHit(NightShade boss, int attackStatus) {
        if (attackStatus == 9) {
            TelegraphVfx.attackWarningRing(boss, BIG_SWIPES_SECOND_WARNING_TICKS, scaled(BIG_SWIPES_RADIUS), WARNING_HEIGHT);
            groundSquare(boss, boss.getTargetPos(), BIG_SWIPES_SECOND_WARNING_TICKS, BIG_SWIPES_HALF_SIZE);
        }
    }

    public static void warnGenericCharge(NightShade boss, int attackStatus) {
        if (attackStatus == 0) {
            TelegraphVfx.attackWarningRing(boss, GENERIC_CHARGE_WARNING_TICKS, scaled(GENERIC_CHARGE_RADIUS), WARNING_HEIGHT);
        }
    }

    public static void warnAoe(NightShade boss, int attackStatus, BlockPos targetPos) {
        if (attackStatus == 0) {
            TelegraphVfx.attackWarningRing(boss, AOE_WARNING_TICKS, scaled(AOE_RADIUS), WARNING_HEIGHT);
            groundSquare(boss, targetPos, AOE_WARNING_TICKS, AOE_HALF_SIZE);
        }
    }

    public static void warnThrowMoonlight(NightShade boss, int attackStatus) {
        if (attackStatus == 0) {
            TelegraphVfx.redAttackWarningRing(boss, RANGED_WARNING_TICKS, scaled(RANGED_RADIUS), WARNING_HEIGHT);
        }
    }

    public static void warnShadowOrbs(NightShade boss, int attackStatus) {
        if (attackStatus == 0) {
            TelegraphVfx.redAttackWarningRing(boss, RANGED_WARNING_TICKS, scaled(RANGED_RADIUS), WARNING_HEIGHT);
        }
    }

    public static boolean rewardsGuardBreak(NightShade boss) {
        NightShade.AttackStates state = boss.getAttackState();
        return state == NightShade.AttackStates.BIG_SWIPES
                || state == NightShade.AttackStates.GENERIC_CHARGE
                || state == NightShade.AttackStates.AOE;
    }

    private static void groundSquare(NightShade boss, BlockPos pos, int ticks, double halfSize) {
        if (pos == null) {
            return;
        }
        Vec3 center = new Vec3(pos.getX() + 0.5D, pos.getY() + 0.06D, pos.getZ() + 0.5D);
        TelegraphVfx.groundWarningRectangle(boss, center, ticks, halfSize, halfSize);
    }

    private static double scaled(double radius) {
        return radius * BOSS_RING_RADIUS_SCALE;
    }
}
