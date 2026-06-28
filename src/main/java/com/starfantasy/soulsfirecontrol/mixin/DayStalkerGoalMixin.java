/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.entity.projectile.SmallFireball
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  net.soulsweaponry.entity.mobs.DayStalker
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package com.starfantasy.soulsfirecontrol.mixin;

import com.starfantasy.soulsfirecontrol.combat.guard.DayStalkerGuardBreakTracker;
import com.starfantasy.soulsfirecontrol.combat.daystalker.FlamesEdgeAftershock;
import com.starfantasy.soulsfirecontrol.combat.daystalker.DayStalkerReactionTrapManager;
import com.starfantasy.soulsfirecontrol.util.DayStalkerTweaks;
import com.starfantasy.soulsfirecontrol.util.SoulsFireControlHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.soulsweaponry.config.ConfigConstructor;
import net.soulsweaponry.entity.mobs.DayStalker;
import net.soulsweaponry.entity.mobs.WarmthEntity;
import net.soulsweaponry.entity.projectile.GrowingFireball;
import net.soulsweaponry.entity.projectile.noclip.FlamePillar;
import net.soulsweaponry.particles.ParticleEvents;
import net.soulsweaponry.particles.ParticleHandler;
import net.soulsweaponry.registry.SoundRegistry;
import net.soulsweaponry.util.WeaponUtil;
import org.apache.logging.log4j.util.TriConsumer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mixin(targets={"net.soulsweaponry.entity.ai.goal.DayStalkerGoal"}, remap=false)
public abstract class DayStalkerGoalMixin {
    @Unique
    private Vec3 starfantasy$skyHighImpactCenter;

    @Unique
    private boolean starfantasy$usedWarmthSummon;

    @Unique
    private final List<FlamesEdgeAftershock> starfantasy$flamesEdgeAftershocks = new ArrayList<>();

    @Shadow
    @Final
    private DayStalker boss;

    @Shadow
    private int attackCooldown;

    @Shadow
    private int specialCooldown;

    @Shadow
    private int attackStatus;

    @Shadow
    private int attackLength;

    @Shadow
    private boolean hasExploded;

    @Shadow
    private int changeFlightTargetTimer;

    @Shadow
    private Vec3 flightPosAdder;

    @Shadow
    private float flyY;

    @Shadow
    private float fallDistance;

    @Shadow
    private float attackRotation;

    @Shadow
    private void checkAndReset(int attackCooldown, int specialCooldown) {
    }

    @Shadow
    private boolean damageTarget(LivingEntity target, float damage) {
        return false;
    }

    @Shadow
    private void spawnFlamePillar(Vec3 vec, Integer warmup, Float yaw) {
    }

    @Inject(method = "checkAndSetAttack", at = @At("HEAD"), cancellable = true)
    private void starfantasy$prioritizeWarmthSummon(LivingEntity target, CallbackInfo ci) {
        if (this.starfantasy$usedWarmthSummon
                && this.boss.getHealth() >= this.boss.getMaxHealth() * 0.9F) {
            this.starfantasy$usedWarmthSummon = false;
        }
        if (DayStalkerTweaks.consumeWarmthSummonReset(this.boss)) {
            this.starfantasy$usedWarmthSummon = false;
        }
        if (starfantasy$shouldPrioritizeWarmthSummon()) {
            this.boss.setAttackAnimation(DayStalker.Attacks.WARMTH);
            ci.cancel();
        }
    }

    @Redirect(method = "checkAndSetAttack",
            at = @At(value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/mobs/DayStalker;setAttackAnimation(Lnet/soulsweaponry/entity/mobs/DayStalker$Attacks;)V"),
            remap = false)
    private void starfantasy$disableAirCombustionSelection(DayStalker boss, DayStalker.Attacks attack) {
        if (attack == DayStalker.Attacks.AIR_COMBUSTION) {
            return;
        }
        if (attack == DayStalker.Attacks.WARMTH && !starfantasy$shouldPrioritizeWarmthSummon()) {
            return;
        }
        boss.setAttackAnimation(attack);
    }

    @Redirect(method={"lambda$decimate$0"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/level/Level;m_46597_(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z", remap=false), remap=false)
    private boolean starfantasy$disableDecimateGroundFire(Level level, BlockPos pos, BlockState state) {
        return SoulsFireControlHelper.placeBlockUnlessSoulsFire(level, pos, state);
    }

    @Redirect(method = "decimate",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;m_5997_(DDD)V",
                    remap = false))
    private void starfantasy$forceDecimateKnockup(LivingEntity target, double x, double y, double z) {
        DayStalkerTweaks.forceVerticalKnockup(target, y);
    }

    @Redirect(method = "flamethrower",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
                    remap = false))
    private boolean starfantasy$disablePhaseTwoFlamethrowerDamage(LivingEntity target, DamageSource source, float amount) {
        return !this.boss.isPhaseTwo() && DayStalkerTweaks.hurtWithBossMagic(this.boss, target, amount);
    }

    @Redirect(method = "flamethrower",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;m_20254_(I)V",
                    remap = false))
    private void starfantasy$disablePhaseTwoFlamethrowerIgnite(LivingEntity target, int seconds) {
        if (!this.boss.isPhaseTwo()) {
            target.setSecondsOnFire(seconds);
        }
    }

    @ModifyConstant(method = "flamethrower", constant = @Constant(doubleValue = 2.0D), remap = false)
    private double starfantasy$expandPhaseTwoFlamethrowerMelee(double original) {
        return this.boss.isPhaseTwo() ? original + 2.0D : original;
    }

    @Redirect(method={"chaosStorm"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/level/Level;m_7967_(Lnet/minecraft/world/entity/Entity;)Z", remap=false), remap=false)
    private boolean starfantasy$attachOwnerToDayStalkerFireball(Level level, Entity entity) {
        Projectile projectile;
        if (entity instanceof FlamePillar pillar) {
            DayStalkerTweaks.warnFlamePillarGround(this.boss, pillar.position(), pillar.getWarmup());
            if (this.boss.isPhaseTwo()) {
                pillar.setEventId(-1);
            }
        }
        if (entity instanceof SmallFireball && entity instanceof Projectile && (projectile = (Projectile)entity).getOwner() == null) {
            projectile.setOwner((Entity)this.boss);
        }
        return level.addFreshEntity(entity);
    }

    @Redirect(method={"blazeBarrage"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/level/Level;m_7967_(Lnet/minecraft/world/entity/Entity;)Z", remap=false), remap=false)
    private boolean starfantasy$markBlazeBarrageFireball(Level level, Entity entity) {
        DayStalkerTweaks.attachBlazeBarrageOwner(this.boss, entity);
        return level.addFreshEntity(entity);
    }

    @Redirect(method = "conflagration",
            at = @At(value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/projectile/GrowingFireball;getMaxAge()I"),
            remap = false)
    private int starfantasy$shortenConflagrationGrowth(GrowingFireball fireball) {
        fireball.setMaxAge(DayStalkerTweaks.CONFLAGRATION_GROWING_FIREBALL_GROWTH_TICKS);
        return DayStalkerTweaks.CONFLAGRATION_GROWING_FIREBALL_GROWTH_TICKS;
    }

    @Inject(method = "m_8036_", at = @At("HEAD"), cancellable = true)
    private void starfantasy$disableGoalWhileStunned(CallbackInfoReturnable<Boolean> cir) {
        if (DayStalkerGuardBreakTracker.isStunned(this.boss)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "m_8037_", at = @At("HEAD"), cancellable = true)
    private void starfantasy$interruptGoalWhileStunned(CallbackInfo ci) {
        if (!DayStalkerGuardBreakTracker.isStunned(this.boss)) {
            return;
        }
        this.attackCooldown = 20;
        this.specialCooldown = Math.max(this.specialCooldown, 20);
        this.attackStatus = 0;
        this.attackLength = 0;
        this.hasExploded = false;
        this.changeFlightTargetTimer = 0;
        this.flightPosAdder = null;
        this.flyY = 6.0F;
        this.fallDistance = 0.0F;
        this.boss.getNavigation().stop();
        this.boss.setAttackAnimation(DayStalker.Attacks.IDLE);
        this.boss.setFlying(false);
        this.boss.setChaseTarget(false);
        this.boss.setWaitAnimation(false);
        this.boss.setRemainingAniTicks(0);
        this.boss.setParticleState(0);
        this.starfantasy$skyHighImpactCenter = null;
        this.starfantasy$flamesEdgeAftershocks.clear();
        ci.cancel();
    }

    @Inject(method = "checkAndReset", at = @At("HEAD"), cancellable = true)
    private void starfantasy$checkAndResetWithLowHealthSpecialCooldowns(int attackCooldown, int specialCooldown,
                                                                        CallbackInfo ci) {
        if (this.attackStatus > this.attackLength) {
            boolean lowHealth = this.boss.getHealth() <= this.boss.getMaxHealth() * 0.5F;
            this.attackStatus = 0;
            double attackModifier = this.boss.isPhaseTwo()
                    ? ConfigConstructor.day_stalker_cooldown_modifier_phase_2
                    : ConfigConstructor.day_stalker_cooldown_modifier_phase_1;
            this.attackCooldown = Mth.floor((double) attackCooldown * attackModifier);
            if (specialCooldown != 0) {
                double specialModifier = this.boss.isPhaseTwo()
                        ? ConfigConstructor.day_stalker_special_cooldown_modifier_phase_2
                        : ConfigConstructor.day_stalker_special_cooldown_modifier_phase_1;
                if (lowHealth) {
                    specialModifier *= 0.0D;
                }
                this.specialCooldown = Mth.floor((double) specialCooldown * specialModifier);
            }
            this.attackLength = 0;
            this.boss.setAttackAnimation(DayStalker.Attacks.IDLE);
            this.boss.setChaseTarget(true);
            this.hasExploded = false;
            this.flyY = 6.0F;
            this.fallDistance = 0.0F;
            this.starfantasy$skyHighImpactCenter = null;
            this.starfantasy$flamesEdgeAftershocks.clear();
        }
        ci.cancel();
    }

    @Inject(method = "airCombustion", at = @At("HEAD"))
    private void starfantasy$warnAirCombustion(LivingEntity target, CallbackInfo ci) {
        DayStalkerTweaks.warnAirCombustion(this.boss, this.attackStatus);
    }

    @Inject(method = "decimate", at = @At("HEAD"))
    private void starfantasy$warnDecimate(LivingEntity target, CallbackInfo ci) {
        DayStalkerTweaks.warnDecimate(this.boss, this.attackStatus);
    }

    @Inject(method = "decimate", at = @At("TAIL"))
    private void starfantasy$triggerDecimateReactionTraps(LivingEntity target, CallbackInfo ci) {
        if (this.attackStatus == 42) {
            DayStalkerReactionTrapManager.trigger(this.boss);
        }
    }

    @Inject(method = "dawnbreaker", at = @At("HEAD"))
    private void starfantasy$warnDawnbreaker(LivingEntity target, CallbackInfo ci) {
        DayStalkerTweaks.warnDawnbreaker(this.boss, this.attackStatus);
    }

    @Inject(method = "dawnbreaker", at = @At("TAIL"))
    private void starfantasy$triggerDawnbreakerReactionTraps(LivingEntity target, CallbackInfo ci) {
        if (this.attackStatus == 32) {
            DayStalkerReactionTrapManager.trigger(this.boss);
        }
    }

    @Inject(method = "chaosStorm", at = @At("HEAD"))
    private void starfantasy$warnChaosStorm(CallbackInfo ci) {
        DayStalkerTweaks.warnChaosStorm(this.boss, this.attackStatus);
    }

    @Inject(method = "flamethrower", at = @At("HEAD"))
    private void starfantasy$warnFlamethrower(LivingEntity target, double distance, CallbackInfo ci) {
        if (this.boss.isPhaseTwo() && this.attackStatus == 0) {
            DayStalkerTweaks.teleportNearTarget(this.boss, target, 4.0D);
        }
        DayStalkerTweaks.warnFlamethrower(this.boss, this.attackStatus);
    }

    @Inject(method = "flamethrower", at = @At("TAIL"))
    private void starfantasy$triggerFlamethrowerReactionTraps(LivingEntity target, double distance, CallbackInfo ci) {
        if (this.attackStatus == 63) {
            DayStalkerReactionTrapManager.trigger(this.boss);
        }
    }

    @Inject(method = "sunfireRush", at = @At("HEAD"))
    private void starfantasy$warnSunfireRush(LivingEntity target, CallbackInfo ci) {
        DayStalkerTweaks.warnSunfireRush(this.boss, this.attackStatus);
    }

    @Inject(method = "sunfireRush", at = @At("TAIL"))
    private void starfantasy$triggerSunfireRushReactionTraps(LivingEntity target, CallbackInfo ci) {
        if (this.attackStatus == 96) {
            DayStalkerReactionTrapManager.trigger(this.boss);
        }
    }

    @Inject(method = "conflagration", at = @At("HEAD"))
    private void starfantasy$warnConflagration(LivingEntity target, CallbackInfo ci) {
        if (this.boss.isPhaseTwo()) {
            this.attackLength = DayStalkerTweaks.CONFLAGRATION_PHASE_TWO_TOTAL_TICKS;
        }
        DayStalkerTweaks.warnConflagration(this.boss, this.attackStatus);
    }

    @Inject(method = "blazeBarrage", at = @At("HEAD"))
    private void starfantasy$warnBlazeBarrage(LivingEntity target, CallbackInfo ci) {
        DayStalkerTweaks.warnBlazeBarrage(this.boss, this.attackStatus);
    }

    @Inject(method = "flamesEdge", at = @At("HEAD"))
    private void starfantasy$warnFlamesEdge(CallbackInfo ci) {
        this.starfantasy$tickFlamesEdgeAftershocks();
        DayStalkerTweaks.warnFlamesEdge(this.boss, this.attackStatus);
    }

    @Inject(method = "flamesEdge", at = @At("TAIL"))
    private void starfantasy$scheduleFlamesEdgeAftershocks(CallbackInfo ci) {
        int hitFrame = this.boss.isPhaseTwo() ? 41 : 22;
        if (this.attackStatus == hitFrame) {
            DayStalkerReactionTrapManager.triggerDelayed(this.boss, 40);
            this.starfantasy$scheduleFlamesEdgeAftershocks();
        }
    }

    @Inject(method = "radiance", at = @At("HEAD"))
    private void starfantasy$warnRadiance(CallbackInfo ci) {
        if (this.attackStatus == 74) {
            DayStalkerTweaks.teleportBehindTarget(this.boss, this.boss.getTarget(), 4.0D);
        }
        DayStalkerTweaks.warnRadiance(this.boss, this.attackStatus);
    }

    @Inject(method = "radiance", at = @At("TAIL"))
    private void starfantasy$triggerRadianceReactionTraps(CallbackInfo ci) {
        if (this.attackStatus == 80) {
            DayStalkerReactionTrapManager.trigger(this.boss);
        }
    }

    @Inject(method = "warmth", at = @At("HEAD"))
    private void starfantasy$warnWarmth(CallbackInfo ci) {
        DayStalkerTweaks.warnWarmth(this.boss, this.attackStatus);
    }

    @Redirect(method = "warmth",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;m_7967_(Lnet/minecraft/world/entity/Entity;)Z",
                    remap = false),
            remap = false)
    private boolean starfantasy$tagWarmthSummon(Level level, Entity entity) {
        if (entity instanceof WarmthEntity warmth) {
            DayStalkerTweaks.addWarmthSummonTags(this.boss, warmth);
            warmth.setTarget(this.boss.getTarget());
            this.starfantasy$usedWarmthSummon = true;
        }
        return level.addFreshEntity(entity);
    }

    @Inject(method = "overheat", at = @At("HEAD"))
    private void starfantasy$warnOverheat(LivingEntity target, CallbackInfo ci) {
        DayStalkerTweaks.warnOverheat(this.boss, this.attackStatus);
    }

    @Inject(method = "overheat", at = @At("TAIL"))
    private void starfantasy$warnOverheatPillars(LivingEntity target, CallbackInfo ci) {
        if (this.attackStatus == 57 && this.attackRotation != 0.0F) {
            DayStalkerTweaks.warnOverheatPillars(this.boss, this.attackRotation);
        }
        if (this.attackStatus == DayStalkerTweaks.OVERHEAT_FIRST_PILLAR_HIT_FRAME) {
            DayStalkerReactionTrapManager.triggerDelayed(this.boss, 20);
        }
    }

    @Inject(method = "inferno", at = @At("HEAD"), cancellable = true)
    private void starfantasy$replaceInferno(CallbackInfo ci) {
        DayStalkerTweaks.warnInferno(this.boss, this.attackStatus);
        ++this.attackStatus;
        this.boss.getNavigation().stop();
        if (this.attackStatus == 23) {
            this.boss.playSound(SoundEvents.BLAZE_SHOOT, 1.0F, 1.0F);
            this.boss.setFlying(true);
            this.boss.push(0.0D, 0.75D, 0.0D);
        }
        if (this.attackStatus == 69) {
            this.boss.setFlying(false);
            this.boss.setDeltaMovement(0.0D, -2.0D, 0.0D);
        }
        if (this.attackStatus == 70 && !this.hasExploded) {
            starfantasy$snapInfernoLanding();
            DayStalkerTweaks.detonateInferno(this.boss, false);
            this.hasExploded = true;
        }
        if (this.attackStatus == 125) {
            DayStalkerTweaks.detonateInferno(this.boss, true);
            DayStalkerReactionTrapManager.trigger(this.boss);
        }
        this.checkAndReset(30, 0);
        ci.cancel();
    }

    @Inject(method = "skyHigh", at = @At("HEAD"), cancellable = true)
    private void starfantasy$replaceSkyHigh(CallbackInfo ci) {
        ++this.attackStatus;
        this.boss.getNavigation().stop();
        LivingEntity target = this.boss.getTarget();
        if (this.attackStatus == 1) {
            this.starfantasy$skyHighImpactCenter = null;
        }
        if (this.attackStatus == DayStalkerTweaks.SKY_HIGH_WARNING_START) {
            DayStalkerTweaks.warnSkyHighStart(this.boss, target);
        }
        if (this.attackStatus == 23) {
            this.boss.level().playSound(null, this.boss.blockPosition(), SoundEvents.BLAZE_SHOOT,
                    SoundSource.HOSTILE, 1.0F, 1.0F);
            this.boss.level().playSound(null, this.boss.blockPosition(), SoundRegistry.NIGHT_PROWLER_SCREAM.get(),
                    SoundSource.HOSTILE, 1.0F, 1.0F);
            this.boss.setFlying(true);
            this.flyY = 30.0F;
            this.boss.push(0.0D, 1.5D, 0.0D);
        }
        if (this.attackStatus == DayStalkerTweaks.SKY_HIGH_LOCK_FRAME) {
            this.starfantasy$skyHighImpactCenter = DayStalkerTweaks.groundCenterBelow(target == null ? this.boss : target);
        }
        if (this.attackStatus >= DayStalkerTweaks.SKY_HIGH_LANDING_FRAME
                && this.attackStatus <= DayStalkerTweaks.SKY_HIGH_HIT_FRAME) {
            if (this.starfantasy$skyHighImpactCenter == null) {
                this.starfantasy$skyHighImpactCenter = DayStalkerTweaks.groundCenterBelow(this.boss);
            }
            DayStalkerTweaks.snapToGroundAndSync(this.boss, this.starfantasy$skyHighImpactCenter);
        }
        if (this.attackStatus == DayStalkerTweaks.SKY_HIGH_HIT_FRAME && !this.hasExploded) {
            this.starfantasy$detonateSkyHigh();
            DayStalkerReactionTrapManager.triggerDelayed(this.boss, 20);
            this.hasExploded = true;
        }
        this.checkAndReset(5, 80);
        ci.cancel();
    }

    @Inject(method = "flamesReach", at = @At("HEAD"))
    private void starfantasy$warnFlamesReach(LivingEntity target, double distance, CallbackInfo ci) {
        DayStalkerTweaks.warnFlamesReach(this.boss, this.attackStatus);
    }

    @Inject(method = "flamesReach", at = @At("TAIL"))
    private void starfantasy$triggerFlamesReachReactionTraps(LivingEntity target, double distance, CallbackInfo ci) {
        if (this.attackStatus == 43) {
            DayStalkerReactionTrapManager.trigger(this.boss);
        }
    }

    @Inject(method = "spawnFlamePillar", at = @At("HEAD"))
    private void starfantasy$warnSpawnedFlamePillar(Vec3 vec, Integer warmup, Float yaw, CallbackInfo ci) {
        if (this.boss.getAttackAnimation() == DayStalker.Attacks.OVERHEAT && this.attackStatus >= 57) {
            return;
        }
        DayStalkerTweaks.warnFlamePillarGround(this.boss, vec, warmup == null ? 0 : warmup);
    }

    private void starfantasy$snapInfernoLanding() {
        Vec3 ground = DayStalkerTweaks.groundCenterBelow(this.boss);
        double y = ground.y() - 0.06D;
        this.boss.moveTo(ground.x(), y, ground.z(), this.boss.getYRot(), this.boss.getXRot());
        this.boss.setFlying(false);
        this.boss.setDeltaMovement(Vec3.ZERO);
        this.boss.fallDistance = 0.0F;
        this.boss.hurtMarked = true;
        if (this.boss.level() instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().broadcast(this.boss, new ClientboundTeleportEntityPacket(this.boss));
        }
    }

    @Unique
    private void starfantasy$detonateSkyHigh() {
        Vec3 center = this.starfantasy$skyHighImpactCenter;
        if (center == null) {
            center = DayStalkerTweaks.groundCenterBelow(this.boss);
            this.starfantasy$skyHighImpactCenter = center;
        }
        double radius = DayStalkerTweaks.skyHighDamageRadius();
        float damage = DayStalkerTweaks.skyHighDamage();
        AABB box = new AABB(center, center).inflate(radius);
        for (Entity entity : this.boss.level().getEntities(this.boss, box)) {
            if (entity instanceof LivingEntity livingEntity) {
                this.damageTarget(livingEntity, damage);
            }
        }
        WeaponUtil.doConsumerOnCircle(this.boss.level(), this.boss.getYRot(), center, 10.0D, 10,
                new Vec2(1.5F, 1.75F),
                (TriConsumer<Vec3, Integer, Float>) (vec, warmup, yaw) ->
                        this.spawnFlamePillar(vec, warmup - 6, yaw));
        this.boss.level().playSound(null, BlockPos.containing(center), SoundEvents.GENERIC_EXPLODE,
                SoundSource.PLAYERS, 1.0F, 1.0F);
        if (!this.boss.level().isClientSide) {
            float particleScale = Math.max(1.0F, DayStalkerTweaks.skyHighParticleScale());
            ParticleHandler.particleOutburstMap(this.boss.level(), Mth.floor(200.0F * particleScale),
                    center.x(), center.y(), center.z(), ParticleEvents.DEFAULT_GRAND_SKYFALL_MAP, particleScale);
        }
    }

    @Unique
    private boolean starfantasy$shouldPrioritizeWarmthSummon() {
        return this.boss.isPhaseTwo()
                && this.boss.getHealth() <= this.boss.getMaxHealth() * 0.5F
                && !this.starfantasy$usedWarmthSummon
                && !DayStalkerTweaks.hasActiveWarmthSummons(this.boss);
    }

    @Unique
    private void starfantasy$scheduleFlamesEdgeAftershocks() {
        this.starfantasy$flamesEdgeAftershocks.clear();
        for (int i = 0; i < DayStalkerTweaks.FLAMES_EDGE_AFTERSHOCK_COUNT; ++i) {
            Vec3 center = DayStalkerTweaks.flamesEdgeAftershockCenter(this.boss, i);
            int warningDelay = i * DayStalkerTweaks.FLAMES_EDGE_AFTERSHOCK_INTERVAL_TICKS;
            FlamesEdgeAftershock aftershock = new FlamesEdgeAftershock(
                    center,
                    warningDelay,
                    warningDelay + DayStalkerTweaks.FLAMES_EDGE_AFTERSHOCK_WARNING_TICKS);
            if (warningDelay == 0) {
                DayStalkerTweaks.warnFlamesEdgeAftershock(this.boss, center);
                aftershock.warningShown = true;
            }
            this.starfantasy$flamesEdgeAftershocks.add(aftershock);
        }
    }

    @Unique
    private void starfantasy$tickFlamesEdgeAftershocks() {
        Iterator<FlamesEdgeAftershock> iterator = this.starfantasy$flamesEdgeAftershocks.iterator();
        while (iterator.hasNext()) {
            FlamesEdgeAftershock aftershock = iterator.next();
            --aftershock.warningDelayTicks;
            --aftershock.explosionDelayTicks;
            if (!aftershock.warningShown && aftershock.warningDelayTicks <= 0) {
                DayStalkerTweaks.warnFlamesEdgeAftershock(this.boss, aftershock.center);
                aftershock.warningShown = true;
            }
            if (aftershock.explosionDelayTicks <= 0) {
                DayStalkerTweaks.detonateFlamesEdgeAftershock(this.boss, aftershock.center);
                iterator.remove();
            }
        }
    }

}
