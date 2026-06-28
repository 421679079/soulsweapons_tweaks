package com.starfantasy.soulsfirecontrol.combat.guard;

import com.starfantasy.soulsfirecontrol.util.AccursedLordTweaks;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.soulsweaponry.entity.mobs.AccursedLordBoss;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class AccursedLordGuardBreakTracker {
    private static final int STANCE_DECREASE_COOLDOWN_TICKS = 30;
    private static final Map<UUID, GuardBreakState> STATES = new HashMap<>();

    private AccursedLordGuardBreakTracker() {
    }

    public static void recordPerfectGuard(AccursedLordBoss boss, ServerPlayer player) {
        if (boss.level().isClientSide() || boss.isDeadOrDying()
                || !AccursedLordTweaks.rewardsGuardBreak(boss)) {
            return;
        }
        int required = AccursedLordTweaks.REQUIRED_GUARDS;
        GuardBreakState state = STATES.computeIfAbsent(boss.getUUID(), uuid -> new GuardBreakState());
        if (state.stunTicks > 0 || isOnCooldown(boss.tickCount, state.lastGuardTick)) {
            return;
        }
        state.lastGuardTick = boss.tickCount;
        state.guardCount = Math.min(required, state.guardCount + 1);
        if (state.guardCount >= required) {
            GuardBreakHudSync.syncTriggered(boss, required);
            startStun(boss, state, required, player);
        } else {
            GuardBreakHudSync.syncGuarded(boss, state.guardCount, required);
        }
    }

    public static void tick(AccursedLordBoss boss) {
        if (boss.level().isClientSide()) {
            return;
        }
        int required = AccursedLordTweaks.REQUIRED_GUARDS;
        GuardBreakState state = STATES.get(boss.getUUID());
        if (boss.isDeadOrDying()) {
            STATES.remove(boss.getUUID());
            GuardBreakHudSync.clearOrReset(boss, required);
            return;
        }
        if (state == null) {
            if (boss.tickCount % 10 == 0) {
                GuardBreakHudSync.syncIdle(boss, required);
            }
            return;
        }
        if (state.stunTicks > 0) {
            pinStunnedBoss(boss);
            spawnStunParticles(boss);
            --state.stunTicks;
            if (state.stunTicks <= 0) {
                clearStun(boss, state, required);
                return;
            }
        }
        if (boss.tickCount % 20 == 0) {
            int hudGuardCount = state.stunTicks > 0 ? required : state.guardCount;
            GuardBreakHudSync.refresh(boss, hudGuardCount, required);
        }
        pruneEmptyStates();
    }

    public static boolean isStunned(AccursedLordBoss boss) {
        GuardBreakState state = STATES.get(boss.getUUID());
        return state != null && state.stunTicks > 0;
    }

    public static void clear(AccursedLordBoss boss) {
        STATES.remove(boss.getUUID());
        GuardBreakHudSync.hide(boss, AccursedLordTweaks.REQUIRED_GUARDS);
    }

    private static boolean isOnCooldown(int currentTick, int lastTick) {
        return lastTick != Integer.MIN_VALUE && currentTick - lastTick < STANCE_DECREASE_COOLDOWN_TICKS;
    }

    private static void startStun(AccursedLordBoss boss, GuardBreakState state, int required, ServerPlayer player) {
        state.guardCount = required;
        state.stunTicks = AccursedLordTweaks.STUN_TICKS;
        boss.setAttackAnimation(AccursedLordBoss.AccursedLordAnimations.IDLE);
        boss.setAggressive(false);
        boss.removePlacedLava();
        pinStunnedBoss(boss);
        knockBossAwayFromPlayer(boss, player);
        playStunBurst(boss);
    }

    private static void clearStun(AccursedLordBoss boss, GuardBreakState state, int required) {
        state.guardCount = 0;
        state.stunTicks = 0;
        boss.setAggressive(true);
        GuardBreakHudSync.syncIdle(boss, required);
    }

    private static void pinStunnedBoss(AccursedLordBoss boss) {
        boss.getNavigation().stop();
        boss.setAttackAnimation(AccursedLordBoss.AccursedLordAnimations.IDLE);
        boss.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 20, false, true));
        boss.setDeltaMovement(0.0D, boss.getDeltaMovement().y, 0.0D);
        boss.hurtMarked = true;
    }

    private static void knockBossAwayFromPlayer(AccursedLordBoss boss, ServerPlayer player) {
        Vec3 direction = boss.position().subtract(player.position());
        direction = new Vec3(direction.x, 0.0D, direction.z);
        if (direction.lengthSqr() < 1.0E-4D) {
            direction = player.getLookAngle().multiply(-1.0D, 0.0D, -1.0D);
        }
        if (direction.lengthSqr() < 1.0E-4D) {
            direction = new Vec3(1.0D, 0.0D, 0.0D);
        }
        boss.setDeltaMovement(direction.normalize().scale(0.35D).add(0.0D, 0.08D, 0.0D));
        boss.hurtMarked = true;
    }

    private static void spawnStunParticles(AccursedLordBoss boss) {
        if (!(boss.level() instanceof ServerLevel level)) {
            return;
        }
        double centerY = boss.getY() + boss.getBbHeight() + 0.45D;
        double radius = 1.25D;
        double baseAngle = boss.tickCount * 0.38D;
        for (int i = 0; i < 10; ++i) {
            double angle = baseAngle + i * (Math.PI * 2.0D / 10.0D);
            double x = boss.getX() + Math.cos(angle) * radius;
            double z = boss.getZ() + Math.sin(angle) * radius;
            level.sendParticles(ParticleTypes.END_ROD, x, centerY, z,
                    1, 0.02D, 0.06D, 0.02D, 0.025D);
            level.sendParticles(ParticleTypes.SMOKE, x, centerY - 0.1D, z,
                    1, 0.05D, 0.05D, 0.05D, 0.04D);
        }
    }

    private static void playStunBurst(AccursedLordBoss boss) {
        if (!(boss.level() instanceof ServerLevel level)) {
            return;
        }
        double x = boss.getX();
        double y = boss.getY() + boss.getBbHeight() * 0.65D;
        double z = boss.getZ();
        level.playSound(null, x, y, z,
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.HOSTILE,
                1.35F,
                0.62F);
        level.playSound(null, x, y, z,
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.HOSTILE,
                1.0F,
                0.9F);
        level.sendParticles(ParticleTypes.FLASH, x, y, z,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.END_ROD, x, y + 0.5D, z,
                90, 1.5D, 1.0D, 1.5D, 0.18D);
    }

    private static void pruneEmptyStates() {
        Iterator<Map.Entry<UUID, GuardBreakState>> iterator = STATES.entrySet().iterator();
        while (iterator.hasNext()) {
            GuardBreakState state = iterator.next().getValue();
            if (state.stunTicks <= 0 && state.guardCount <= 0) {
                iterator.remove();
            }
        }
    }

    private static final class GuardBreakState {
        private int guardCount;
        private int stunTicks;
        private int lastGuardTick = Integer.MIN_VALUE;
    }
}
