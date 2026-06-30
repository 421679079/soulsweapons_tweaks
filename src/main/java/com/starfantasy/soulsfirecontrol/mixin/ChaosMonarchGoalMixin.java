package com.starfantasy.soulsfirecontrol.mixin;

import com.starfantasy.soulsfirecontrol.combat.chaosmonarch.ChaosMonarchBarrageManager;
import com.starfantasy.soulsfirecontrol.combat.chaosmonarch.ChaosMonarchLightningManager;
import com.starfantasy.soulsfirecontrol.combat.chaosmonarch.ChaosMonarchPhaseManager;
import com.starfantasy.soulsfirecontrol.combat.guard.ChaosMonarchGuardBreakTracker;
import com.starfantasy.soulsfirecontrol.util.ChaosMonarchTweaks;
import com.starfantasy.soulsfirecontrol.vfx.telegraph.TelegraphVfx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.soulsweaponry.entity.ai.goal.ChaosMonarchGoal;
import net.soulsweaponry.entity.mobs.ChaosMonarch;
import net.soulsweaponry.particles.ParticleEvents;
import net.soulsweaponry.particles.ParticleHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ChaosMonarchGoal.class, remap = false)
public abstract class ChaosMonarchGoalMixin {
    @Unique
    private static final int TELEPORT_COOLDOWN_TICKS = 300;
    @Unique
    private static final int TELEPORT_SUMMON_FRAME = 13;
    @Unique
    private static final int TELEPORT_MOVE_FRAME = 23;
    @Unique
    private static final int LIGHTNING_COOLDOWN_TICKS = 300;
    @Unique
    private static final int MELEE_COOLDOWN_TICKS = 240;
    @Unique
    private static final int[] MELEE_HIT_FRAMES = {8, 16, 27, 39};
    @Unique
    private static final int MELEE_GOAL_TICK_SCALE = 2;
    @Unique
    private static final int PHASE_SIX_STEP_LIGHTNING = 0;
    @Unique
    private static final int PHASE_SIX_STEP_BARRAGE_ONE = 1;
    @Unique
    private static final int PHASE_SIX_STEP_BARRAGE_TWO = 2;
    @Unique
    private static final int PHASE_SIX_STEP_BARRAGE_THREE = 3;
    @Unique
    private static final int PHASE_SIX_STEP_MELEE = 4;
    @Unique
    private static final int PHASE_SIX_STEP_TELEPORT = 5;
    @Unique
    private static final int PHASE_SIX_SEQUENCE_LENGTH = 6;
    @Unique
    private static final double MELEE_WARNING_RADIUS = 5.5D;
    @Unique
    private static final double MELEE_WARNING_HEIGHT = 2.8D;
    @Unique
    private static final double MELEE_CHASE_DISTANCE = 2.0D;

    @Unique
    private int starfantasy$teleportCooldown;
    @Unique
    private int starfantasy$lightningCooldown;
    @Unique
    private int starfantasy$meleeCooldown;
    @Unique
    private int starfantasy$barrageResetFrame = 30;
    @Unique
    private int starfantasy$phaseSixSequenceStep = PHASE_SIX_STEP_LIGHTNING;
    @Unique
    private int starfantasy$lastKnownPhase = 1;

    @Shadow
    @Final
    private ChaosMonarch boss;

    @Shadow
    private int attackCooldown;

    @Shadow
    private int attackStatus;

    @Shadow
    private boolean randomOrNot;

    @Shadow
    private int controlledProjectile;

    @Shadow
    private BlockPos blockPos;

    @Shadow
    public abstract float getModifiedDamage(float damage);

    @Shadow
    public abstract void resetAttack(float cooldownModifier);

    @Shadow
    private void randomProjectiles() {
        throw new AssertionError();
    }

    @Shadow
    private void controlledProjectiles(LivingEntity target) {
        throw new AssertionError();
    }

    @Inject(method = "m_8036_", at = @At("HEAD"), cancellable = true)
    private void starfantasy$disableGoalDuringManagedStates(CallbackInfoReturnable<Boolean> cir) {
        if (ChaosMonarchPhaseManager.isTransitioning(this.boss)
                || ChaosMonarchGuardBreakTracker.isStunned(this.boss)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "m_8037_", at = @At("HEAD"), cancellable = true)
    private void starfantasy$replaceChaosMonarchGoalTick(CallbackInfo ci) {
        if (ChaosMonarchPhaseManager.consumeGoalReset(this.boss)) {
            starfantasy$resetGoalFields(0);
            ci.cancel();
            return;
        }
        if (ChaosMonarchPhaseManager.isTransitioning(this.boss)) {
            this.attackStatus = 0;
            this.attackCooldown = 20;
            this.blockPos = null;
            this.boss.setAttack(ChaosMonarch.Attack.TELEPORT.ordinal());
            this.boss.getNavigation().stop();
            ci.cancel();
            return;
        }
        if (ChaosMonarchGuardBreakTracker.isStunned(this.boss)) {
            starfantasy$resetPhaseSixSequence();
            starfantasy$resetGoalFields(20);
            this.boss.getNavigation().stop();
            ci.cancel();
            return;
        }

        int phase = ChaosMonarchPhaseManager.getCurrentPhase(this.boss);
        starfantasy$syncPhaseSixSequence(phase);

        if (this.starfantasy$teleportCooldown > 0) {
            --this.starfantasy$teleportCooldown;
        }
        if (this.starfantasy$lightningCooldown > 0) {
            --this.starfantasy$lightningCooldown;
        }
        if (this.starfantasy$meleeCooldown > 0) {
            --this.starfantasy$meleeCooldown;
        }
        --this.attackCooldown;
        LivingEntity target = this.boss.getTarget();
        if (target == null || !target.isAlive()) {
            starfantasy$resetPhaseSixSequence();
            if (phase >= 6) {
                starfantasy$resetGoalFields(0);
            }
            ci.cancel();
            return;
        }
        this.boss.setAggressive(true);
        this.boss.getLookControl().setLookAt(target.getX(), target.getEyeY(), target.getZ());
        if (this.attackCooldown < 0) {
            if (this.boss.getAttack() == ChaosMonarch.Attack.IDLE) {
                starfantasy$selectAttack(target);
            }
            switch (this.boss.getAttack()) {
                case TELEPORT -> starfantasy$tickTeleport(target);
                case MELEE -> starfantasy$tickMelee(target);
                case LIGHTNING -> starfantasy$tickLightning(target);
                case BARRAGE -> starfantasy$tickBarrage(target);
                default -> {
                }
            }
        }
        ci.cancel();
    }

    @Unique
    private void starfantasy$resetGoalFields(int cooldown) {
        ChaosMonarchTweaks.clearMeleeClashWindow(this.boss);
        this.attackStatus = 0;
        this.attackCooldown = cooldown;
        this.randomOrNot = this.boss.getRandom().nextBoolean();
        this.controlledProjectile = this.boss.getRandom().nextInt(5);
        this.blockPos = null;
        this.starfantasy$barrageResetFrame = 30;
        this.boss.setAttack(ChaosMonarch.Attack.IDLE.ordinal());
        this.boss.setAggressive(false);
    }

    @Unique
    private void starfantasy$selectAttack(LivingEntity target) {
        this.attackStatus = 0;
        this.blockPos = null;
        int phase = ChaosMonarchPhaseManager.getCurrentPhase(this.boss);
        if (phase >= 6) {
            if (ChaosMonarchPhaseManager.consumeForcedLightning(this.boss)) {
                this.starfantasy$phaseSixSequenceStep = PHASE_SIX_STEP_LIGHTNING;
            }
            starfantasy$startPhaseSixSequenceAttack(target);
            return;
        }
        if (ChaosMonarchPhaseManager.consumeForcedLightning(this.boss)) {
            starfantasy$startLightning();
            return;
        }
        if (this.starfantasy$teleportCooldown <= 0) {
            this.boss.setAttack(ChaosMonarch.Attack.TELEPORT.ordinal());
            return;
        }
        if (this.starfantasy$lightningCooldown <= 0) {
            starfantasy$startLightning();
            return;
        }
        if (this.starfantasy$meleeCooldown <= 0) {
            starfantasy$startMelee(target);
            return;
        }
        starfantasy$startBarrage(target);
    }

    @Unique
    private void starfantasy$startPhaseSixSequenceAttack(LivingEntity target) {
        switch (this.starfantasy$phaseSixSequenceStep) {
            case PHASE_SIX_STEP_LIGHTNING -> starfantasy$startLightning();
            case PHASE_SIX_STEP_BARRAGE_ONE, PHASE_SIX_STEP_BARRAGE_TWO, PHASE_SIX_STEP_BARRAGE_THREE ->
                    starfantasy$startBarrage(target);
            case PHASE_SIX_STEP_MELEE -> starfantasy$startMelee(target);
            case PHASE_SIX_STEP_TELEPORT -> this.boss.setAttack(ChaosMonarch.Attack.TELEPORT.ordinal());
            default -> {
                this.starfantasy$phaseSixSequenceStep = PHASE_SIX_STEP_LIGHTNING;
                starfantasy$startLightning();
            }
        }
    }

    @Unique
    private void starfantasy$finishAttack(float cooldownModifier) {
        if (ChaosMonarchPhaseManager.getCurrentPhase(this.boss) >= 6) {
            this.starfantasy$phaseSixSequenceStep =
                    (this.starfantasy$phaseSixSequenceStep + 1) % PHASE_SIX_SEQUENCE_LENGTH;
        }
        this.resetAttack(cooldownModifier);
    }

    @Unique
    private void starfantasy$syncPhaseSixSequence(int phase) {
        if ((phase >= 6 && this.starfantasy$lastKnownPhase < 6)
                || (phase < 6 && this.starfantasy$lastKnownPhase >= 6)) {
            starfantasy$resetPhaseSixSequence();
        }
        this.starfantasy$lastKnownPhase = phase;
    }

    @Unique
    private void starfantasy$resetPhaseSixSequence() {
        this.starfantasy$phaseSixSequenceStep = PHASE_SIX_STEP_LIGHTNING;
    }

    @Unique
    private void starfantasy$startMelee(LivingEntity target) {
        ChaosMonarchTweaks.clearMeleeClashWindow(this.boss);
        ChaosMonarchTweaks.teleportToMeleeStart(this.boss, target);
        this.starfantasy$meleeCooldown = starfantasy$goalCooldownTicks(MELEE_COOLDOWN_TICKS);
        this.boss.setAttack(ChaosMonarch.Attack.MELEE.ordinal());
    }

    @Unique
    private void starfantasy$startLightning() {
        this.starfantasy$lightningCooldown = starfantasy$goalCooldownTicks(LIGHTNING_COOLDOWN_TICKS);
        this.boss.setAttack(ChaosMonarch.Attack.LIGHTNING.ordinal());
    }

    @Unique
    private void starfantasy$startBarrage(LivingEntity target) {
        int durationTicks = ChaosMonarchBarrageManager.startBarrage(this.boss, target,
                ChaosMonarchPhaseManager.getCurrentPhase(this.boss));
        this.starfantasy$barrageResetFrame = starfantasy$goalCooldownTicks(durationTicks) + 2;
        this.boss.setAttack(ChaosMonarch.Attack.BARRAGE.ordinal());
    }

    @Unique
    private static int starfantasy$goalCooldownTicks(int serverTicks) {
        return Math.max(1, (serverTicks + MELEE_GOAL_TICK_SCALE - 1) / MELEE_GOAL_TICK_SCALE);
    }

    @Unique
    private void starfantasy$tickTeleport(LivingEntity target) {
        ++this.attackStatus;
        if (this.attackStatus % 2 == 0 && this.attackStatus < 10 && !this.boss.level().isClientSide) {
            ParticleHandler.particleSphere(this.boss.level(), 1000,
                    this.boss.getX(), this.boss.getY(), this.boss.getZ(),
                    (ParticleOptions) ParticleTypes.PORTAL, 6.0F);
        }
        if (this.attackStatus == TELEPORT_SUMMON_FRAME) {
            ChaosMonarchTweaks.summonTeleportMobs(this.boss, this.boss.position());
        }
        if (this.attackStatus == TELEPORT_MOVE_FRAME) {
            this.boss.level().playSound(null, this.boss.getX(), this.boss.getY(), this.boss.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 1.0F);
            boolean teleported = ChaosMonarchTweaks.teleportForTeleportAttack(this.boss, target);
            if (teleported) {
                if (!this.boss.level().isClientSide) {
                    ParticleHandler.particleSphereList(this.boss.level(), 1000,
                            this.boss.getX(), this.boss.getY(), this.boss.getZ(),
                            1.0F,
                            new ParticleOptions[]{ParticleTypes.DRAGON_BREATH, ParticleTypes.DRAGON_BREATH});
                }
            }
        }
        if (this.attackStatus >= 30) {
            this.starfantasy$teleportCooldown = TELEPORT_COOLDOWN_TICKS;
            starfantasy$finishAttack(0.5F);
        }
    }

    @Unique
    private void starfantasy$tickMelee(LivingEntity target) {
        ChaosMonarchTweaks.updateMeleeClashFrame(this.boss, this.attackStatus);
        starfantasy$warnMelee(this.attackStatus);
        ++this.attackStatus;
        ChaosMonarchTweaks.updateMeleeClashFrame(this.boss, this.attackStatus);
        ChaosMonarchTweaks.faceTarget(this.boss, target);
        for (int i = 0; i < MELEE_HIT_FRAMES.length; ++i) {
            if (this.attackStatus == MELEE_HIT_FRAMES[i]) {
                ChaosMonarchTweaks.chaseBeforeMeleeHit(this.boss, target, MELEE_CHASE_DISTANCE);
                if (ChaosMonarchTweaks.consumeMeleeClashParry(this.boss, i)) {
                    ServerPlayer player = ChaosMonarchTweaks.resolveMeleeClashPlayer(this.boss);
                    ChaosMonarchTweaks.playMeleeClashParryEffects(this.boss, player);
                    if (player != null) {
                        ChaosMonarchGuardBreakTracker.recordPerfectGuard(this.boss, player);
                    }
                } else {
                    starfantasy$hitMelee(target, i);
                }
                break;
            }
        }
        if (this.attackStatus >= 45) {
            starfantasy$finishAttack(1.0F);
        }
    }

    @Unique
    private void starfantasy$warnMelee(int currentFrame) {
        int previousFrame = 0;
        for (int i = 0; i < MELEE_HIT_FRAMES.length; ++i) {
            int hitFrame = MELEE_HIT_FRAMES[i];
            if (currentFrame == previousFrame) {
                int warningTicks = Math.max(1, (hitFrame - previousFrame) * MELEE_GOAL_TICK_SCALE);
                ChaosMonarchTweaks.beginMeleeClashWindow(this.boss, i, currentFrame, hitFrame,
                        MELEE_WARNING_RADIUS, MELEE_WARNING_HEIGHT);
                TelegraphVfx.attackWarningRing(this.boss, warningTicks,
                        MELEE_WARNING_RADIUS, MELEE_WARNING_HEIGHT);
            }
            previousFrame = hitFrame;
        }
    }

    @Unique
    private void starfantasy$hitMelee(LivingEntity target, int hitIndex) {
        Vec3 look = this.boss.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);
        if (horizontal.lengthSqr() < 1.0E-4D) {
            horizontal = target.position().subtract(this.boss.position()).multiply(1.0D, 0.0D, 1.0D);
        }
        if (horizontal.lengthSqr() < 1.0E-4D) {
            horizontal = new Vec3(1.0D, 0.0D, 0.0D);
        }
        Vec3 center = this.boss.position().add(horizontal.normalize().scale(3.0D));
        BlockPos hitPos = BlockPos.containing(center.x, target.getY(), center.z);
        double expand = hitIndex < 2 ? 3.0D : 2.0D;
        float damage = switch (hitIndex) {
            case 0, 1 -> 15.0F;
            case 2 -> 20.0F;
            default -> 30.0F;
        };
        if (hitIndex < 2) {
            this.boss.level().playSound(null, hitPos, SoundEvents.PLAYER_ATTACK_SWEEP,
                    SoundSource.HOSTILE, 1.0F, 1.0F);
        } else if (hitIndex == 2) {
            this.boss.level().playSound(null, hitPos, SoundEvents.PLAYER_ATTACK_STRONG,
                    SoundSource.HOSTILE, 1.0F, 1.0F);
        } else {
            this.boss.level().playSound(null, hitPos, SoundEvents.WITHER_BREAK_BLOCK,
                    SoundSource.HOSTILE, 1.0F, 1.0F);
            if (!this.boss.level().isClientSide) {
                ParticleHandler.particleSphereList(this.boss.level(), 100,
                        hitPos.getX(), hitPos.getY(), hitPos.getZ(),
                        ParticleEvents.DARK_EXPLOSION_LIST, 0.3F);
            }
        }
        AABB box = new AABB(hitPos).inflate(expand);
        for (Entity entity : this.boss.level().getEntities(this.boss, box)) {
            if (!(entity instanceof LivingEntity living) || ChaosMonarchTweaks.shouldSkipTarget(this.boss, living)) {
                continue;
            }
            if (living instanceof Player player && (player.isCreative() || player.isSpectator())) {
                continue;
            }
            living.hurt(this.boss.level().damageSources().mobAttack(this.boss),
                    this.getModifiedDamage(damage));
            living.knockback(this.boss.getRandom().nextDouble(),
                    this.boss.getX() - living.getX(),
                    this.boss.getZ() - living.getZ());
        }
    }

    @Unique
    private void starfantasy$tickLightning(LivingEntity target) {
        ++this.attackStatus;
        if (this.attackStatus == 5) {
            this.boss.level().playSound(null, this.boss.getX(), this.boss.getY(), this.boss.getZ(),
                    SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.HOSTILE, 1.0F, 1.5F);
        }
        if (this.attackStatus == 15) {
            ChaosMonarchLightningManager.startLightning(this.boss,
                    ChaosMonarchPhaseManager.getCurrentPhase(this.boss), target);
        }
        if (this.attackStatus >= 30) {
            starfantasy$finishAttack(1.0F);
        }
    }

    @Unique
    private void starfantasy$tickBarrage(LivingEntity target) {
        ++this.attackStatus;
        this.boss.getLookControl().setLookAt(target.getX(), target.getEyeY(), target.getZ());
        if (this.attackStatus >= this.starfantasy$barrageResetFrame) {
            starfantasy$finishAttack(1.0F);
        }
    }
}
