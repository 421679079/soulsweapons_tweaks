package com.starfantasy.soulsfirecontrol.combat.chaosmonarch;

import com.starfantasy.soulsfirecontrol.StarFantasySoulsFireControl;
import com.starfantasy.soulsfirecontrol.config.ChaosMonarchConfig;
import com.starfantasy.soulsfirecontrol.entity.ChaosBarrageProjectileEntity;
import com.starfantasy.soulsfirecontrol.entity.ChaosWitherSkullProjectileEntity;
import com.starfantasy.soulsfirecontrol.entity.TwinMeteorEntityRegistry;
import com.starfantasy.soulsfirecontrol.util.ChaosMonarchTweaks;
import com.starfantasy.soulsfirecontrol.vfx.telegraph.TelegraphVfx;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.soulsweaponry.entity.mobs.ChaosMonarch;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = StarFantasySoulsFireControl.MOD_ID)
public final class ChaosMonarchBarrageManager {
    private static final int WARNING_TICKS = 20;
    private static final double WARNING_RADIUS = 5.5D;
    private static final double WARNING_HEIGHT = 2.8D;
    private static final int[] FAN_COUNTS = {5, 4, 5, 4, 5};
    private static final int FAN_INTERVAL_TICKS = 5;
    private static final int STREAM_COUNT = 10;
    private static final int STREAM_INTERVAL_TICKS = 2;
    private static final double PROJECTILE_SPEED = 0.82D;
    private static final double STREAM_PROJECTILE_SPEED = PROJECTILE_SPEED * 2.0D;
    private static final float MAGIC_DAMAGE = 20.0F;
    private static final float WITHER_SKULL_DAMAGE = 10.0F;
    private static final float PHASE_SIX_VOID_DAMAGE = 15.0F;

    private static final List<PendingShot> PENDING_SHOTS = new ArrayList<>();

    private ChaosMonarchBarrageManager() {
    }

    public static int startBarrage(ChaosMonarch boss, LivingEntity target, int phase) {
        if (boss == null || boss.level().isClientSide() || target == null || !target.isAlive()) {
            return WARNING_TICKS;
        }
        if (phase >= 5) {
            TelegraphVfx.purpleAttackWarningRing(boss, WARNING_TICKS, WARNING_RADIUS, WARNING_HEIGHT);
        } else {
            TelegraphVfx.redAttackWarningRing(boss, WARNING_TICKS, WARNING_RADIUS, WARNING_HEIGHT);
        }
        boolean fanBranch = boss.getRandom().nextBoolean();
        if (fanBranch) {
            scheduleFanBranch(boss, target, phase);
            return WARNING_TICKS + (FAN_COUNTS.length - 1) * FAN_INTERVAL_TICKS + 12;
        }
        scheduleStreamBranch(boss, target, phase);
        return WARNING_TICKS + (STREAM_COUNT - 1) * STREAM_INTERVAL_TICKS + 12;
    }

    public static void spawnWitherSkullMeteorVisual(ChaosMonarch boss, Vec3 impact, int travelTicks) {
        if (!(boss.level() instanceof ServerLevel level)) {
            return;
        }
        ChaosWitherSkullProjectileEntity visual = TwinMeteorEntityRegistry.CHAOS_WITHER_SKULL.get().create(level);
        if (visual == null) {
            return;
        }
        Vec3 start = impact.add(0.0D, ChaosMonarchConfig.getTwinBossMeteorSpawnYOffset(), 0.0D);
        visual.moveTo(start.x, start.y, start.z, boss.getYRot(), boss.getXRot());
        visual.configureVisual(boss, impact, travelTicks);
        level.addFreshEntity(visual);
    }

    @SubscribeEvent
    public static void tickPending(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) {
            return;
        }
        List<PendingShot> dueShots = new ArrayList<>();
        Iterator<PendingShot> iterator = PENDING_SHOTS.iterator();
        while (iterator.hasNext()) {
            PendingShot shot = iterator.next();
            if (!shot.dimension.equals(level.dimension())) {
                continue;
            }
            --shot.delayTicks;
            if (shot.delayTicks > 0) {
                continue;
            }
            dueShots.add(shot);
            iterator.remove();
        }
        for (PendingShot shot : dueShots) {
            runShot(level, shot);
        }
    }

    private static void scheduleFanBranch(ChaosMonarch boss, LivingEntity target, int phase) {
        if (phase >= 6) {
            int[] styles = shuffledStyles(boss.getRandom());
            for (int i = 0; i < FAN_COUNTS.length; ++i) {
                scheduleShot(boss, target, styles[i], 6, FAN_COUNTS[i], true,
                        PROJECTILE_SPEED, WARNING_TICKS + i * FAN_INTERVAL_TICKS);
            }
            return;
        }
        int style = styleForPhase(phase);
        for (int i = 0; i < FAN_COUNTS.length; ++i) {
            scheduleShot(boss, target, style, phase, FAN_COUNTS[i], true,
                    PROJECTILE_SPEED, WARNING_TICKS + i * FAN_INTERVAL_TICKS);
        }
    }

    private static void scheduleStreamBranch(ChaosMonarch boss, LivingEntity target, int phase) {
        if (phase >= 6) {
            int[] styles = shuffledPhaseSixStream(boss.getRandom());
            for (int i = 0; i < styles.length; ++i) {
                scheduleShot(boss, target, styles[i], 6, 1, false,
                        STREAM_PROJECTILE_SPEED, WARNING_TICKS + i * STREAM_INTERVAL_TICKS);
            }
            return;
        }
        int style = styleForPhase(phase);
        for (int i = 0; i < STREAM_COUNT; ++i) {
            scheduleShot(boss, target, style, phase, 1, false,
                    STREAM_PROJECTILE_SPEED, WARNING_TICKS + i * STREAM_INTERVAL_TICKS);
        }
    }

    private static void scheduleShot(ChaosMonarch boss, LivingEntity target, int style, int effectPhase,
                                     int count, boolean fan, double projectileSpeed, int delayTicks) {
        PENDING_SHOTS.add(new PendingShot(boss.level().dimension(), boss.getUUID(), target.getUUID(),
                style, effectPhase, count, fan, projectileSpeed, delayTicks));
    }

    private static void runShot(ServerLevel level, PendingShot shot) {
        Entity bossEntity = level.getEntity(shot.bossUuid);
        Entity targetEntity = shot.targetUuid == null ? null : level.getEntity(shot.targetUuid);
        if (!(bossEntity instanceof ChaosMonarch boss) || !boss.isAlive()
                || !(targetEntity instanceof LivingEntity target) || !target.isAlive()
                || ChaosMonarchPhaseManager.isTransitioning(boss)) {
            return;
        }
        level.playSound(null, boss.getX(), boss.getEyeY(), boss.getZ(),
                SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.2F, 1.0F);
        if (shot.fan) {
            spawnFan(level, boss, target, shot.style, shot.effectPhase, shot.count, shot.projectileSpeed);
        } else {
            spawnProjectile(level, boss, target, shot.style, shot.effectPhase, 0.0D, shot.projectileSpeed);
        }
    }

    private static void spawnFan(ServerLevel level, ChaosMonarch boss, LivingEntity target,
                                 int style, int effectPhase, int count, double projectileSpeed) {
        for (int i = 0; i < count; ++i) {
            double offset = count == 4 ? -60.0D + i * 40.0D : -80.0D + i * 40.0D;
            spawnProjectile(level, boss, target, style, effectPhase, offset, projectileSpeed);
        }
    }

    private static void spawnProjectile(ServerLevel level, ChaosMonarch boss, LivingEntity target,
                                        int style, int effectPhase, double yawOffsetDegrees, double projectileSpeed) {
        if (style == ChaosBarrageProjectileEntity.STYLE_WITHER_SKULL) {
            spawnWitherSkullProjectile(level, boss, target, effectPhase, yawOffsetDegrees, projectileSpeed);
            return;
        }
        ChaosBarrageProjectileEntity projectile = TwinMeteorEntityRegistry.CHAOS_BARRAGE_PROJECTILE.get().create(level);
        if (projectile == null) {
            return;
        }
        Vec3 start = boss.getEyePosition().add(0.0D, -0.1D, 0.0D);
        Vec3 direction = aimedDirection(start, target, yawOffsetDegrees);
        projectile.moveTo(start.x, start.y, start.z, boss.getYRot(), boss.getXRot());
        projectile.configureBarrage(boss, style, effectPhase,
                direction.scale(projectileSpeed), damageForStyle(style, effectPhase));
        level.addFreshEntity(projectile);
    }

    private static void spawnWitherSkullProjectile(ServerLevel level, ChaosMonarch boss, LivingEntity target,
                                                   int effectPhase, double yawOffsetDegrees, double projectileSpeed) {
        ChaosWitherSkullProjectileEntity skull = TwinMeteorEntityRegistry.CHAOS_WITHER_SKULL.get().create(level);
        if (skull == null) {
            return;
        }
        Vec3 start = boss.getEyePosition().add(0.0D, -0.1D, 0.0D);
        Vec3 direction = aimedDirection(start, target, yawOffsetDegrees);
        skull.moveTo(start.x, start.y, start.z, boss.getYRot(), boss.getXRot());
        skull.configureBarrage(boss, effectPhase, direction.scale(projectileSpeed),
                damageForStyle(ChaosBarrageProjectileEntity.STYLE_WITHER_SKULL, effectPhase));
        level.addFreshEntity(skull);
    }

    private static Vec3 aimedDirection(Vec3 start, LivingEntity target, double yawOffsetDegrees) {
        Vec3 toTarget = target.position().subtract(start);
        Vec3 horizontal = new Vec3(toTarget.x, 0.0D, toTarget.z);
        if (horizontal.lengthSqr() < 1.0E-4D) {
            horizontal = new Vec3(1.0D, 0.0D, 0.0D);
        }
        double radians = Math.toRadians(yawOffsetDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        Vec3 base = horizontal.normalize();
        Vec3 rotated = new Vec3(base.x * cos - base.z * sin, 0.0D, base.x * sin + base.z * cos);
        double y = toTarget.y / Math.max(1.0E-4D, horizontal.length());
        return new Vec3(rotated.x, y, rotated.z).normalize();
    }

    private static int styleForPhase(int phase) {
        return switch (Math.max(1, Math.min(5, phase))) {
            case 2 -> ChaosBarrageProjectileEntity.STYLE_FROST;
            case 3 -> ChaosBarrageProjectileEntity.STYLE_MOONLIGHT;
            case 4 -> ChaosBarrageProjectileEntity.STYLE_NIGHT;
            case 5 -> ChaosBarrageProjectileEntity.STYLE_WITHER_SKULL;
            default -> ChaosBarrageProjectileEntity.STYLE_FIRE;
        };
    }

    private static float damageForStyle(int style, int effectPhase) {
        float damage = effectPhase >= 6
                ? PHASE_SIX_VOID_DAMAGE
                : style == ChaosBarrageProjectileEntity.STYLE_WITHER_SKULL ? WITHER_SKULL_DAMAGE : MAGIC_DAMAGE;
        return ChaosMonarchTweaks.modifiedDamage(damage);
    }

    private static int[] shuffledStyles(RandomSource random) {
        int[] styles = {
                ChaosBarrageProjectileEntity.STYLE_FIRE,
                ChaosBarrageProjectileEntity.STYLE_FROST,
                ChaosBarrageProjectileEntity.STYLE_MOONLIGHT,
                ChaosBarrageProjectileEntity.STYLE_NIGHT,
                ChaosBarrageProjectileEntity.STYLE_WITHER_SKULL
        };
        shuffle(styles, random);
        return styles;
    }

    private static int[] shuffledPhaseSixStream(RandomSource random) {
        int[] styles = {
                ChaosBarrageProjectileEntity.STYLE_FIRE,
                ChaosBarrageProjectileEntity.STYLE_FIRE,
                ChaosBarrageProjectileEntity.STYLE_FROST,
                ChaosBarrageProjectileEntity.STYLE_FROST,
                ChaosBarrageProjectileEntity.STYLE_MOONLIGHT,
                ChaosBarrageProjectileEntity.STYLE_MOONLIGHT,
                ChaosBarrageProjectileEntity.STYLE_NIGHT,
                ChaosBarrageProjectileEntity.STYLE_NIGHT,
                ChaosBarrageProjectileEntity.STYLE_WITHER_SKULL,
                ChaosBarrageProjectileEntity.STYLE_WITHER_SKULL
        };
        shuffle(styles, random);
        return styles;
    }

    private static void shuffle(int[] values, RandomSource random) {
        for (int i = values.length - 1; i > 0; --i) {
            int j = random.nextInt(i + 1);
            int value = values[i];
            values[i] = values[j];
            values[j] = value;
        }
    }

    private static final class PendingShot {
        private final ResourceKey<Level> dimension;
        private final UUID bossUuid;
        @Nullable
        private final UUID targetUuid;
        private final int style;
        private final int effectPhase;
        private final int count;
        private final boolean fan;
        private final double projectileSpeed;
        private int delayTicks;

        private PendingShot(ResourceKey<Level> dimension, UUID bossUuid, @Nullable UUID targetUuid,
                            int style, int effectPhase, int count, boolean fan,
                            double projectileSpeed, int delayTicks) {
            this.dimension = dimension;
            this.bossUuid = bossUuid;
            this.targetUuid = targetUuid;
            this.style = style;
            this.effectPhase = effectPhase;
            this.count = Math.max(1, count);
            this.fan = fan;
            this.projectileSpeed = Math.max(0.01D, projectileSpeed);
            this.delayTicks = Math.max(1, delayTicks);
        }
    }
}
