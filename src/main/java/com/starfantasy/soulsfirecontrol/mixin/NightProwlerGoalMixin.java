package com.starfantasy.soulsfirecontrol.mixin;

import com.starfantasy.soulsfirecontrol.combat.guard.NightProwlerGuardBreakTracker;
import com.starfantasy.soulsfirecontrol.combat.nightprowler.NightProwlerReactionLightningManager;
import com.starfantasy.soulsfirecontrol.util.NightProwlerTweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.soulsweaponry.config.ConfigConstructor;
import net.soulsweaponry.entity.mobs.NightProwler;
import net.soulsweaponry.entity.mobs.Soulmass;
import net.soulsweaponry.entity.projectile.MoonlightProjectile;
import net.soulsweaponry.entity.projectile.NightSkull;
import net.soulsweaponry.entity.projectile.noclip.BlackflameExplosionEntity;
import net.soulsweaponry.entity.projectile.noclip.BlackflameSnakeEntity;
import net.soulsweaponry.entity.util.BlackflameSnakeUtil;
import net.soulsweaponry.particles.ParticleEvents;
import net.soulsweaponry.particles.ParticleHandler;
import net.soulsweaponry.registry.EntityRegistry;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(targets = "net.soulsweaponry.entity.ai.goal.NightProwlerGoal", remap = false)
public abstract class NightProwlerGoalMixin {
    @Shadow
    @Final
    private NightProwler boss;

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
    private int bonusDmg;

    @Shadow
    private int flipCounter;

    @Unique
    private Vec3 starfantasy$blackflameLandingSyncPos;

    @Unique
    private int starfantasy$blackflameLandingSyncTicks;

    @Unique
    private boolean starfantasy$usedSoulmassSummon;

    @Shadow
    private void checkAndReset(int attackCooldown, int specialCooldown) {
        throw new AssertionError();
    }

    @Shadow
    public abstract void aoe(AABB box, float damage, float knockback, boolean knockbackAway);

    @Shadow
    private float getModifiedDamage(float damage) {
        throw new AssertionError();
    }

    @Shadow
    private void shootSplitSkulls(Vec3 target, int amount, float velocity) {
        throw new AssertionError();
    }

    @Shadow
    private void shootSplitMoonlight(Vec3 target, int amount) {
        throw new AssertionError();
    }

    @Shadow
    protected abstract boolean isInMeleeRange(LivingEntity target);

    @Shadow
    private boolean isSummonsAlive() {
        throw new AssertionError();
    }

    @Inject(method = "spawnNightsEdge", at = @At("HEAD"))
    private void starfantasy$warnNightsEdgeGround(Vec3 position, Integer warmup, Float yaw, CallbackInfo ci) {
        NightProwlerTweaks.warnNightsEdgeGround(this.boss, position, warmup);
    }

    @Inject(method = "shootSplitSkulls", at = @At("HEAD"), cancellable = true)
    private void starfantasy$replaceSplitSkullsWithNightSkulls(Vec3 target, int amount, float velocity, CallbackInfo ci) {
        this.boss.level().playSound(null, this.boss.blockPosition(),
                SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 1.0F, 1.0F);
        starfantasy$shootNightSkullFan(target, amount, velocity, 5.0F);
        ci.cancel();
    }

    @Inject(method = "shootSplitBoth", at = @At("HEAD"), cancellable = true)
    private void starfantasy$replaceSplitBothWithNightSkulls(Vec3 target, int amount, CallbackInfo ci) {
        this.boss.level().playSound(null, this.boss.blockPosition(),
                SoundRegistry.MOONLIGHT_BIG_EVENT.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
        this.boss.level().playSound(null, this.boss.blockPosition(),
                SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 1.0F, 1.0F);
        int middle = Mth.floor(amount / 2.0F);
        for (int i = -middle; i <= middle; ++i) {
            Vec3 vec = target.yRot((float) Math.toRadians(8.0F * i));
            boolean shootSkull = this.flipCounter % 2 == 0 == (i % 2 == 0);
            if (shootSkull) {
                starfantasy$shootNightSkull(vec, 1.75F);
                continue;
            }
            MoonlightProjectile moonlight = EntityRegistry.MOONLIGHT_BIG_ENTITY_TYPE.get().create(this.boss.level());
            if (moonlight == null) {
                continue;
            }
            moonlight.setAgeAndPoints(30, 150, 4);
            moonlight.setBaseDamage(this.getModifiedDamage(20.0F));
            moonlight.setPosRaw(this.boss.getX(), this.boss.getEyeY(), this.boss.getZ());
            moonlight.shoot(vec.x(), vec.y(), vec.z(), 1.75F, 1.0F);
            moonlight.setOwner(this.boss);
            this.boss.level().addFreshEntity(moonlight);
        }
        ++this.flipCounter;
        ci.cancel();
    }

    @Inject(method = "m_8036_", at = @At("HEAD"), cancellable = true)
    private void starfantasy$disableGoalWhileStunned(CallbackInfoReturnable<Boolean> cir) {
        if (NightProwlerGuardBreakTracker.isStunned(this.boss)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "checkAndSetAttack", at = @At("HEAD"), cancellable = true)
    private void starfantasy$replaceAttackSelection(LivingEntity target, CallbackInfo ci) {
        if (this.starfantasy$usedSoulmassSummon
                && this.boss.getHealth() >= this.boss.getMaxHealth() * 0.9F) {
            this.starfantasy$usedSoulmassSummon = false;
        }
        if (starfantasy$shouldPrioritizeSoulmassSummon()) {
            this.boss.setAttackAnimation(NightProwler.Attacks.NIGHTS_EMBRACE);
            ci.cancel();
            return;
        }
        double distanceToEntity = this.boss.distanceToSqr(target);
        int rand = this.boss.getRandom().nextInt(NightProwler.ATTACKS_LENGTH);
        NightProwler.Attacks attack = NightProwler.Attacks.values()[rand];
        switch (attack) {
            case TRINITY -> {
                if (!this.boss.isPhaseTwo()
                        && (this.isInMeleeRange(target) || this.boss.isFlying())) {
                    this.boss.setAttackAnimation(attack);
                }
            }
            case REAPING_SLASH -> {
                if (!this.boss.isPhaseTwo() && distanceToEntity < 300.0D) {
                    this.boss.setAttackAnimation(attack);
                }
            }
            case NIGHTS_EMBRACE -> {
                if (this.boss.isPhaseTwo()
                        && starfantasy$isLowHealthPhaseTwo()
                        && !this.starfantasy$usedSoulmassSummon
                        && !this.isSummonsAlive()
                        && !NightProwlerTweaks.hasActiveSummonAllies(this.boss)) {
                    this.boss.setAttackAnimation(attack);
                }
            }
            case RIPPLE_FANG -> {
                if (this.isInMeleeRange(target)) {
                    this.boss.setAttackAnimation(attack);
                }
            }
            case SOUL_REAPER -> {
                if (!this.isInMeleeRange(target)) {
                    starfantasy$snapSoulReaperStartPosition(target);
                }
                this.boss.setAttackAnimation(attack);
            }
            case DARKNESS_RISE -> {
                if (this.isInMeleeRange(target) && !this.boss.isFlying()) {
                    this.boss.setAttackAnimation(attack);
                }
            }
            case BLADES_REACH -> {
                if (distanceToEntity < 200.0D) {
                    this.boss.setAttackAnimation(attack);
                }
            }
            case DIMINISHING_LIGHT, ENGULF -> this.boss.setAttackAnimation(attack);
            case BLACKFLAME_SNAKE -> this.boss.setAttackAnimation(attack);
            case ECLIPSE -> {
                if (this.boss.isPhaseTwo() && this.specialCooldown <= 0) {
                    this.boss.setAttackAnimation(attack);
                }
            }
            case LUNAR_DISPLACEMENT -> {
                if (this.boss.isPhaseTwo()) {
                    this.boss.setAttackAnimation(attack);
                }
            }
            case DEATHBRINGERS_GRASP -> {
                if (distanceToEntity < 81.0D) {
                    this.boss.setAttackAnimation(attack);
                }
            }
            default -> this.boss.setAttackAnimation(NightProwler.Attacks.IDLE);
        }
        ci.cancel();
    }

    @Inject(method = "m_8037_", at = @At("HEAD"), cancellable = true)
    private void starfantasy$interruptGoalWhileStunned(CallbackInfo ci) {
        if (!NightProwlerGuardBreakTracker.isStunned(this.boss)) {
            return;
        }
        this.attackCooldown = 20;
        this.specialCooldown = Math.max(this.specialCooldown, 20);
        this.attackStatus = 0;
        this.attackLength = 0;
        this.hasExploded = false;
        this.changeFlightTargetTimer = 0;
        this.flightPosAdder = null;
        this.bonusDmg = 0;
        this.flipCounter = 0;
        this.boss.getNavigation().stop();
        this.boss.setAttackAnimation(NightProwler.Attacks.IDLE);
        this.boss.setFlying(false);
        this.boss.setChaseTarget(false);
        this.boss.setWaitAnimation(false);
        this.boss.setRemainingAniTicks(0);
        this.boss.setParticleState(0);
        ci.cancel();
    }

    @Inject(method = "checkAndReset", at = @At("HEAD"), cancellable = true)
    private void starfantasy$checkAndResetWithLowHealthCooldowns(int attackCooldown, int specialCooldown,
                                                                 CallbackInfo ci) {
        if (this.attackStatus > this.attackLength) {
            boolean lowHealth = this.boss.getHealth() <= this.boss.getMaxHealth() * 0.5F;
            boolean lowHealthPhaseTwo = this.boss.isPhaseTwo()
                    && lowHealth;
            this.attackStatus = 0;
            double attackModifier = this.boss.isPhaseTwo()
                    ? (lowHealthPhaseTwo ? 0.0D : ConfigConstructor.night_prowler_cooldown_modifier_phase_2)
                    : ConfigConstructor.night_prowler_cooldown_modifier_phase_1;
            this.attackCooldown = Mth.floor((double) attackCooldown * attackModifier);
            if (specialCooldown != 0) {
                double specialModifier;
                if (this.boss.isPhaseTwo()) {
                    specialModifier = lowHealthPhaseTwo
                            ? ConfigConstructor.night_prowler_special_cooldown_modifier_phase_2 * 0.5D
                            : ConfigConstructor.night_prowler_special_cooldown_modifier_phase_2;
                } else {
                    specialModifier = ConfigConstructor.night_prowler_special_cooldown_modifier_phase_1;
                    if (lowHealth) {
                        specialModifier *= 0.0D;
                    }
                }
                this.specialCooldown = Mth.floor((double) specialCooldown * specialModifier);
            }
            this.attackLength = 0;
            this.boss.setAttackAnimation(NightProwler.Attacks.IDLE);
            this.boss.setChaseTarget(true);
            this.hasExploded = false;
            this.boss.setParticleState(0);
            this.bonusDmg = 0;
            this.flipCounter = 0;
            if (this.boss.isPhaseTwo()) {
                this.boss.setFlying(false);
            }
        }
        ci.cancel();
    }

    @ModifyConstant(method = "m_8037_", constant = @Constant(intValue = 270))
    private int starfantasy$shortenEclipseAnimationLength(int original) {
        return 170;
    }

    @ModifyConstant(method = "eclipse", constant = @Constant(intValue = 220))
    private int starfantasy$shortenEclipseSkullWindow(int original) {
        return 120;
    }

    @ModifyConstant(method = "eclipse", constant = @Constant(intValue = 240))
    private int starfantasy$shortenEclipseLanding(int original) {
        return 140;
    }

    @Redirect(method = "eclipse",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;m_7967_(Lnet/minecraft/world/entity/Entity;)Z",
                    remap = false),
            remap = false)
    private boolean starfantasy$warnEclipseSkullLanding(Level level, Entity entity) {
        if (entity instanceof NightSkull) {
            NightProwlerTweaks.warnEclipseSkullLanding(this.boss, entity);
        }
        return level.addFreshEntity(entity);
    }

    @Redirect(method = "aoe",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;m_147240_(DDD)V",
                    remap = false),
            remap = false)
    private void starfantasy$deathbringersGraspIgnoresKnockbackResistance(LivingEntity target,
                                                                          double strength,
                                                                          double x,
                                                                          double z) {
        if (this.boss.getAttackAnimation() != NightProwler.Attacks.DEATHBRINGERS_GRASP) {
            target.knockback(strength, x, z);
            return;
        }
        Vec3 direction = new Vec3(x, 0.0D, z);
        if (direction.lengthSqr() < 1.0E-6D) {
            return;
        }
        direction = direction.normalize();
        Vec3 movement = target.getDeltaMovement();
        double y = target.onGround()
                ? Math.min(0.4D, movement.y / 2.0D + strength)
                : movement.y;
        target.setDeltaMovement(movement.x / 2.0D - direction.x * strength,
                y,
                movement.z / 2.0D - direction.z * strength);
        target.hasImpulse = true;
        target.hurtMarked = true;
    }

    @Inject(method = "trinity", at = @At("HEAD"))
    private void starfantasy$warnTrinity(CallbackInfo ci) {
        NightProwlerTweaks.warnTrinity(this.boss, this.attackStatus);
    }

    @Inject(method = "reapingSlash", at = @At("HEAD"))
    private void starfantasy$warnReapingSlash(LivingEntity target, CallbackInfo ci) {
        NightProwlerTweaks.warnReapingSlash(this.boss, this.attackStatus);
    }

    @Inject(method = "nightsEmbrace", at = @At("HEAD"), cancellable = true)
    private void starfantasy$warnNightsEmbrace(CallbackInfo ci) {
        NightProwlerTweaks.warnNightsEmbrace(this.boss, this.attackStatus);
        if (this.boss.isPhaseTwo()) {
            starfantasy$tickPhaseTwoNightsEmbrace();
            ci.cancel();
        }
    }

    @Inject(method = "rippleFang", at = @At("HEAD"))
    private void starfantasy$warnRippleFang(LivingEntity target, CallbackInfo ci) {
        NightProwlerTweaks.warnRippleFang(this.boss, this.attackStatus);
    }

    @Inject(method = "bladesReach", at = @At("HEAD"))
    private void starfantasy$warnBladesReach(LivingEntity target, CallbackInfo ci) {
        NightProwlerTweaks.warnBladesReach(this.boss, this.attackStatus);
    }

    @Inject(method = "bladesReach", at = @At("TAIL"))
    private void starfantasy$triggerBladesReachReactionLightning(LivingEntity target, CallbackInfo ci) {
        if (this.boss.isPhaseTwo() && this.attackStatus == 43) {
            NightProwlerReactionLightningManager.trigger(this.boss);
        }
    }

    @Inject(method = "soulReaper", at = @At("HEAD"), cancellable = true)
    private void starfantasy$warnSoulReaper(LivingEntity target, CallbackInfo ci) {
        NightProwlerTweaks.warnSoulReaper(this.boss, this.attackStatus);
        starfantasy$tickSoulReaperWithLargerHitboxes(target);
        ci.cancel();
    }

    @Inject(method = "diminishingLight", at = @At("HEAD"))
    private void starfantasy$warnDiminishingLight(LivingEntity target, CallbackInfo ci) {
        NightProwlerTweaks.warnDiminishingLight(this.boss, this.attackStatus);
    }

    @Inject(method = "diminishingLight", at = @At("TAIL"))
    private void starfantasy$triggerDiminishingLightReactionLightning(LivingEntity target, CallbackInfo ci) {
        if (this.boss.isPhaseTwo() && this.attackStatus == 40) {
            NightProwlerReactionLightningManager.trigger(this.boss);
        }
    }

    @Inject(method = "darknessRise", at = @At("HEAD"), cancellable = true)
    private void starfantasy$warnDarknessRise(LivingEntity target, CallbackInfo ci) {
        NightProwlerTweaks.warnDarknessRise(this.boss, this.attackStatus);
        starfantasy$tickDarknessRiseWithGroundWarnings(target);
        ci.cancel();
    }

    @Inject(method = "eclipse", at = @At("HEAD"))
    private void starfantasy$warnEclipse(CallbackInfo ci) {
        NightProwlerTweaks.warnEclipse(this.boss, this.attackStatus);
    }

    @Inject(method = "engulf", at = @At("HEAD"))
    private void starfantasy$warnEngulf(LivingEntity target, CallbackInfo ci) {
        NightProwlerTweaks.warnEngulf(this.boss, this.attackStatus);
    }

    @Inject(method = "engulf", at = @At("TAIL"))
    private void starfantasy$triggerEngulfReactionLightning(LivingEntity target, CallbackInfo ci) {
        if (this.boss.isPhaseTwo() && this.attackStatus == 54) {
            NightProwlerReactionLightningManager.trigger(this.boss);
        }
    }

    @Inject(method = "blackflameSnake", at = @At("HEAD"), cancellable = true)
    private void starfantasy$warnBlackflameSnake(LivingEntity target, CallbackInfo ci) {
        NightProwlerTweaks.warnBlackflameSnake(this.boss, this.attackStatus);
        NightProwlerTweaks.warnBlackflameSnakeLanding(this.boss, target, this.attackStatus);
        if (this.boss.isPhaseTwo()) {
            starfantasy$tickPhaseTwoBlackflameSnake(target);
            ci.cancel();
        }
    }

    @Inject(method = "lunarDisplacement", at = @At("HEAD"))
    private void starfantasy$warnLunarDisplacement(LivingEntity target, CallbackInfo ci) {
        NightProwlerTweaks.warnLunarDisplacement(this.boss, this.attackStatus);
    }

    @Inject(method = "lunarDisplacement", at = @At("TAIL"))
    private void starfantasy$triggerLunarDisplacementReactionLightning(LivingEntity target, CallbackInfo ci) {
        if (this.boss.isPhaseTwo() && this.attackStatus == 42) {
            NightProwlerReactionLightningManager.trigger(this.boss);
        }
    }

    @Inject(method = "deathsGrasp", at = @At("HEAD"))
    private void starfantasy$warnDeathsGrasp(LivingEntity target, CallbackInfo ci) {
        NightProwlerTweaks.warnDeathsGrasp(this.boss, this.attackStatus);
    }

    @Inject(method = "deathsGrasp", at = @At("TAIL"))
    private void starfantasy$triggerDeathsGraspReactionLightning(LivingEntity target, CallbackInfo ci) {
        if (this.boss.isPhaseTwo() && this.attackStatus == 43) {
            NightProwlerReactionLightningManager.trigger(this.boss);
        }
    }

    private void starfantasy$tickDarknessRiseWithGroundWarnings(LivingEntity target) {
        ++this.attackStatus;
        this.boss.getNavigation().stop();
        if (this.attackStatus == 24) {
            this.boss.level().playSound(null, this.boss.blockPosition(),
                    SoundRegistry.DARKNESS_RISE.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
            this.boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 0));
            this.boss.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 1));
            float yawRad = (float) Mth.atan2(target.getZ() - this.boss.getZ(), target.getX() - this.boss.getX());
            float yawDeg = (float) Math.toDegrees(yawRad);
            double heightDiff = Math.abs(target.getY() - this.boss.getY());
            double maxYOffset = heightDiff + 2.0D;
            WeaponUtil.doConsumerOnCircle(this.boss.level(), yawDeg, this.boss.position(), maxYOffset,
                    this.boss.isPhaseTwo() ? 10 : 4,
                    new Vec2(1.5F, 1.75F),
                    (TriConsumer<Vec3, Integer, Float>) (pos, delay, yaw) -> {
                        BlackflameExplosionEntity entity = new BlackflameExplosionEntity(this.boss.level());
                        entity.setOwner(this.boss);
                        entity.setRadius(2.0F);
                        entity.setBaseDamage(this.getModifiedDamage(30.0F));
                        entity.setWarmup(delay + 15);
                        entity.setPos(pos.x(), pos.y(), pos.z());
                        NightProwlerTweaks.warnBlackflameGround(this.boss, pos, delay + 15);
                        this.boss.level().addFreshEntity(entity);
                        if (this.boss.level() instanceof ServerLevel serverWorld) {
                            float spread = 0.01F;
                            serverWorld.sendParticles(ParticleTypes.LARGE_SMOKE,
                                    pos.x(), pos.y(), pos.z(),
                                    4,
                                    this.boss.getRandom().nextFloat() * spread - spread / 2.0F,
                                    this.boss.getRandom().nextFloat() * spread - spread / 2.0F,
                                    this.boss.getRandom().nextFloat() * spread - spread / 2.0F,
                                    0.0D);
                        }
                    });
        }
        this.checkAndReset(10, 0);
    }

    private void starfantasy$tickPhaseTwoNightsEmbrace() {
        if (!starfantasy$isLowHealthPhaseTwo()
                || this.starfantasy$usedSoulmassSummon
                || this.isSummonsAlive()
                || NightProwlerTweaks.hasActiveSummonAllies(this.boss)) {
            this.attackStatus = this.attackLength + 1;
            this.checkAndReset(10, 0);
            return;
        }
        ++this.attackStatus;
        this.boss.getNavigation().stop();
        if (this.attackStatus >= 18 && this.attackStatus <= 46 && this.attackStatus % 3 == 0) {
            this.boss.playSound(SoundRegistry.SCYTHE_SWIPE.get(), 1.0F,
                    (float) this.boss.getRandom().nextIntBetweenInclusive(6, 10) / 10.0F);
        }
        if (!this.boss.level().isClientSide && this.attackStatus == 75) {
            Vec3 center = this.boss.position();
            Vec3 side = starfantasy$summonSideVector();
            String groupTag = NightProwlerTweaks.summonAllyGroupTag(this.boss);
            NightProwlerTweaks.addSummonAllyTags(this.boss, groupTag);
            int[] summons = new int[2];
            for (int i = 0; i < 2; ++i) {
                double sign = i == 0 ? -1.0D : 1.0D;
                Vec3 wanted = center.add(side.scale(5.0D * sign));
                Vec3 landing = starfantasy$findGroundedPosition(wanted.x(), center.y(), wanted.z());
                Soulmass entity = EntityRegistry.SOULMASS.get().create(this.boss.level());
                if (entity == null) {
                    continue;
                }
                entity.setPos(landing.x(), landing.y() + 0.1D, landing.z());
                entity.setTarget(this.boss.getTarget());
                entity.addTag(NightProwlerTweaks.NO_LOOT_SOULMASS_TAG);
                NightProwlerTweaks.addSummonAllyTags(entity, groupTag);
                ParticleHandler.particleOutburstMap(this.boss.level(), 100,
                        landing.x(), landing.y(), landing.z(),
                        ParticleEvents.CONJURE_ENTITY_MAP, 1.0F);
                this.boss.level().playSound(null, BlockPos.containing(landing),
                        SoundRegistry.NIGHTFALL_SPAWN_EVENT.get(),
                        SoundSource.HOSTILE, 0.7F, 1.0F);
                this.boss.level().addFreshEntity(entity);
                summons[i] = entity.getId();
            }
            this.boss.setAliveSummons(summons);
            this.starfantasy$usedSoulmassSummon = true;
            this.boss.playSound(SoundRegistry.SCYTHE_SWIPE.get(), 1.0F, 0.75F);
        }
        this.checkAndReset(10, 120);
    }

    private boolean starfantasy$isLowHealthPhaseTwo() {
        return this.boss.isPhaseTwo()
                && this.boss.getHealth() <= this.boss.getMaxHealth() * 0.5F;
    }

    private boolean starfantasy$shouldPrioritizeSoulmassSummon() {
        return starfantasy$isLowHealthPhaseTwo()
                && !this.starfantasy$usedSoulmassSummon
                && !this.isSummonsAlive()
                && !NightProwlerTweaks.hasActiveSummonAllies(this.boss);
    }

    private Vec3 starfantasy$summonSideVector() {
        LivingEntity target = this.boss.getTarget();
        Vec3 forward = target == null
                ? this.boss.getLookAngle()
                : target.position().subtract(this.boss.position());
        forward = new Vec3(forward.x, 0.0D, forward.z);
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = new Vec3(1.0D, 0.0D, 0.0D);
        }
        forward = forward.normalize();
        return new Vec3(-forward.z, 0.0D, forward.x);
    }

    private void starfantasy$tickPhaseTwoBlackflameSnake(LivingEntity target) {
        this.attackLength = 140;
        ++this.attackStatus;
        this.boss.getNavigation().stop();
        starfantasy$syncBlackflameLandingPosition();
        Vec3 targetPos = target.position();
        this.boss.getLookControl().setLookAt(target);
        this.boss.lookAt(target, this.boss.getHeadRotSpeed() * 2, this.boss.getMaxHeadXRot() * 2);
        if (this.attackStatus <= 1) {
            this.boss.playSound(SoundRegistry.NIGHT_SKULL_DIE.get(), 1.0F, 0.75F);
            this.boss.setFlying(true);
            this.boss.push(0.0D, 0.5D, 0.0D);
        }
        if (this.attackStatus >= 84 && !this.hasExploded) {
            this.boss.setFlying(false);
            starfantasy$snapBlackflameSnakeLanding(targetPos);
            starfantasy$triggerBlackflameSnakeCrash(targetPos);
        } else if (this.attackStatus >= 84) {
            this.boss.setFlying(false);
            this.boss.setParticleState(0);
        }
        this.checkAndReset(this.boss.isFlying() ? 60 : 5, 0);
    }

    private void starfantasy$snapSoulReaperStartPosition(LivingEntity target) {
        Vec3 offset = this.boss.position().subtract(target.position());
        offset = new Vec3(offset.x, 0.0D, offset.z);
        if (offset.lengthSqr() < 1.0E-4D) {
            Vec3 look = target.getLookAngle();
            offset = new Vec3(-look.x, 0.0D, -look.z);
        }
        if (offset.lengthSqr() < 1.0E-4D) {
            offset = new Vec3(1.0D, 0.0D, 0.0D);
        }
        Vec3 desired = target.position().add(offset.normalize().scale(3.0D));
        Vec3 landing = starfantasy$findGroundedPosition(desired.x(), target.getY(), desired.z());
        if (!this.boss.teleportTo(landing.x(), landing.y(), landing.z())) {
            this.boss.moveTo(landing.x(), landing.y(), landing.z(), this.boss.getYRot(), this.boss.getXRot());
        }
        this.boss.lookAt(target, 180.0F, 180.0F);
        this.boss.setDeltaMovement(Vec3.ZERO);
        this.boss.fallDistance = 0.0F;
        this.boss.hurtMarked = true;
    }

    private void starfantasy$snapBlackflameSnakeLanding(LivingEntity target) {
        starfantasy$snapBlackflameSnakeLanding(target.position());
    }

    private void starfantasy$snapBlackflameSnakeLanding(Vec3 targetPos) {
        Vec3 landing = starfantasy$findGroundedPosition(targetPos.x(), targetPos.y(), targetPos.z());
        if (!this.boss.teleportTo(landing.x(), landing.y(), landing.z())) {
            this.boss.moveTo(landing.x(), landing.y(), landing.z(), this.boss.getYRot(), this.boss.getXRot());
        }
        this.boss.getLookControl().setLookAt(targetPos.x(), targetPos.y(), targetPos.z());
        this.boss.setDeltaMovement(Vec3.ZERO);
        this.boss.fallDistance = 0.0F;
        this.boss.hurtMarked = true;
        this.starfantasy$blackflameLandingSyncPos = landing;
        this.starfantasy$blackflameLandingSyncTicks = 6;
        starfantasy$syncBlackflameLandingPosition();
    }

    private void starfantasy$syncBlackflameLandingPosition() {
        if (this.starfantasy$blackflameLandingSyncTicks <= 0
                || this.starfantasy$blackflameLandingSyncPos == null
                || this.boss.level().isClientSide) {
            return;
        }
        --this.starfantasy$blackflameLandingSyncTicks;
        Vec3 landing = this.starfantasy$blackflameLandingSyncPos;
        this.boss.moveTo(landing.x(), landing.y(), landing.z(), this.boss.getYRot(), this.boss.getXRot());
        this.boss.setDeltaMovement(Vec3.ZERO);
        this.boss.fallDistance = 0.0F;
        this.boss.hurtMarked = true;
        if (this.boss.level() instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().broadcast(this.boss, new ClientboundTeleportEntityPacket(this.boss));
        }
    }

    private Vec3 starfantasy$findGroundedPosition(LivingEntity target) {
        return starfantasy$findGroundedPosition(target.getX(), target.getY(), target.getZ());
    }

    private Vec3 starfantasy$findGroundedPosition(double x, double yPosition, double z) {
        Level level = this.boss.level();
        int startY = Math.min(level.getMaxBuildHeight() - 2, Mth.floor(yPosition) + 10);
        int minY = level.getMinBuildHeight() + 1;
        int baseX = Mth.floor(x);
        int baseZ = Mth.floor(z);
        for (int radius = 0; radius <= 4; ++radius) {
            for (int dx = -radius; dx <= radius; ++dx) {
                for (int dz = -radius; dz <= radius; ++dz) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }
                    for (int y = startY; y >= minY; --y) {
                        BlockPos feet = new BlockPos(baseX + dx, y, baseZ + dz);
                        if (starfantasy$canStandAt(level, feet)) {
                            return Vec3.atBottomCenterOf(feet);
                        }
                    }
                }
            }
        }
        return new Vec3(x, yPosition, z);
    }

    private boolean starfantasy$canStandAt(Level level, BlockPos feet) {
        BlockState below = level.getBlockState(feet.below());
        BlockState body = level.getBlockState(feet);
        BlockState head = level.getBlockState(feet.above());
        return below.isFaceSturdy(level, feet.below(), Direction.UP)
                && !body.blocksMotion()
                && !head.blocksMotion();
    }

    private void starfantasy$triggerBlackflameSnakeCrash(LivingEntity target) {
        starfantasy$triggerBlackflameSnakeCrash(target.position());
    }

    private void starfantasy$triggerBlackflameSnakeCrash(Vec3 targetPos) {
        this.hasExploded = true;
        this.boss.setTargetPos(this.boss.blockPosition());
        this.boss.setParticleState(2);
        this.aoe(this.boss.getBoundingBox().inflate(2.0D), 35.0F, 2.0F, true);
        this.boss.level().playSound(null, this.boss.blockPosition(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0F, 0.85F);

        List<List<Vec3>> positions = BlackflameSnakeUtil.getCurvedPositions(
                this.boss.getYRot(), 10.0F, this.boss.position(), targetPos);
        for (List<Vec3> list : positions) {
            for (int i = 0; i < list.size(); ++i) {
                Vec3 pos = list.get(i);
                BlackflameExplosionEntity explosion = new BlackflameExplosionEntity(this.boss.level());
                explosion.setOwner(this.boss);
                explosion.setRadius(2.0F);
                explosion.setBaseDamage(this.getModifiedDamage(35.0F));
                explosion.setWarmup(i);
                explosion.setPos(pos.x(), pos.y(), pos.z());
                NightProwlerTweaks.warnBlackflameGround(this.boss, pos, i);
                this.boss.level().addFreshEntity(explosion);
            }
        }

        BlackflameSnakeEntity snake = new BlackflameSnakeEntity(this.boss.level());
        snake.setPos(this.boss.getX(), this.boss.getY(), this.boss.getZ());
        snake.setBaseDamage(this.getModifiedDamage(30.0F));
        snake.setDeltaMovement(new Vec3(
                targetPos.x() - this.boss.getX(),
                targetPos.y() - this.boss.getY(),
                targetPos.z() - this.boss.getZ()).scale(0.2F));
        LivingEntity target = this.boss.getTarget();
        if (target != null) {
            snake.setTargetUuid(target.getUUID());
        }
        this.boss.level().addFreshEntity(snake);
        NightProwlerReactionLightningManager.triggerDelayed(this.boss, 20);
    }

    private void starfantasy$tickSoulReaperWithLargerHitboxes(LivingEntity target) {
        ++this.attackStatus;
        this.boss.getLookControl().setLookAt(target);
        this.boss.lookAt(target, this.boss.getHeadRotSpeed(), this.boss.getMaxHeadXRot());
        this.boss.getNavigation().stop();
        Vec3 vel = new Vec3(
                target.getX() - this.boss.getX(),
                target.getEyeY() - this.boss.getY(1.0D),
                target.getZ() - this.boss.getZ());
        boolean phase2 = this.boss.isPhaseTwo();
        int[] hitFrames = phase2
                ? new int[]{17, 28, 50, 61, 74, 96, 109, 128}
                : new int[]{14, 29, 49, 72};
        if (starfantasy$isSoulReaperChaseFrame(hitFrames)) {
            this.boss.setDeltaMovement(starfantasy$soulReaperChaseMovement(target, vel));
        }
        Vec3 vec = this.boss.getLookAngle().scale(4.0D).add(this.boss.position());
        vec = new Vec3(vec.x(), target.getY(), vec.z());
        this.boss.setTargetPos(BlockPos.containing(vec).atY(this.boss.getBlockY()));

        Map<Integer, AABB> hitboxes = new HashMap<>();
        if (!phase2) {
            hitboxes.put(14, new AABB(BlockPos.containing(vec)).inflate(3.0D));
            hitboxes.put(29, new AABB(BlockPos.containing(vec)).inflate(3.0D));
            hitboxes.put(49, this.boss.getBoundingBox().inflate(3.0D));
            hitboxes.put(72, this.boss.getBoundingBox().inflate(3.0D));
        } else {
            hitboxes.put(17, new AABB(BlockPos.containing(vec)).inflate(3.0D));
            hitboxes.put(28, new AABB(BlockPos.containing(vec)).inflate(3.0D));
            hitboxes.put(50, this.boss.getBoundingBox().inflate(3.0D));
            hitboxes.put(61, new AABB(BlockPos.containing(vec)).inflate(3.0D));
            hitboxes.put(74, new AABB(BlockPos.containing(vec)).inflate(3.0D));
            hitboxes.put(96, this.boss.getBoundingBox().inflate(3.0D));
            hitboxes.put(109, new AABB(BlockPos.containing(vec)).inflate(3.0D));
            hitboxes.put(128, this.boss.getBoundingBox().inflate(4.0D));
        }

        for (Map.Entry<Integer, AABB> entry : hitboxes.entrySet()) {
            int frame = entry.getKey();
            if (this.attackStatus != frame) {
                continue;
            }
            this.boss.level().playSound(null, this.boss.blockPosition(),
                    SoundRegistry.SCYTHE_SWIPE.get(),
                    SoundSource.HOSTILE,
                    1.0F,
                    (float) this.boss.getRandom().nextIntBetweenInclusive(6, 10) / 10.0F);
            this.aoe(entry.getValue(), 20.0F, 0.4F, true);
            this.bonusDmg += phase2 ? 3 : 5;
            if (this.attackStatus == 61) {
                this.shootSplitSkulls(vel, 3, 1.5F);
            }
            if (this.attackStatus == 74) {
                this.boss.setParticleState(2);
                this.boss.playSound(SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, 1.0F, 1.0F);
            }
            if (this.attackStatus == 109) {
                this.shootSplitMoonlight(vel, 3);
            }
            if (this.attackStatus == 128) {
                this.boss.setParticleState(3);
                NightProwlerReactionLightningManager.trigger(this.boss);
            }
        }
        if (this.attackStatus != 74 && this.attackStatus != 128) {
            this.boss.setParticleState(0);
        }
        this.checkAndReset(this.boss.isPhaseTwo() ? 5 : 40, 0);
    }

    private boolean starfantasy$isSoulReaperChaseFrame(int[] hitFrames) {
        for (int frame : hitFrames) {
            if (this.attackStatus == frame - 1) {
                return true;
            }
        }
        return false;
    }

    private Vec3 starfantasy$soulReaperChaseMovement(LivingEntity target, Vec3 originalVelocity) {
        if (this.isInMeleeRange(target)) {
            return originalVelocity.scale(0.1D);
        }
        Vec3 horizontal = new Vec3(originalVelocity.x, 0.0D, originalVelocity.z);
        if (horizontal.lengthSqr() < 1.0E-4D) {
            return Vec3.ZERO;
        }
        return horizontal.normalize().scale(Math.min(3.0D, horizontal.length()));
    }

    @Unique
    private void starfantasy$shootNightSkullFan(Vec3 target, int amount, float velocity, float spreadDegrees) {
        int middle = Mth.floor(amount / 2.0F);
        for (int i = -middle; i <= middle; ++i) {
            Vec3 vec = target.yRot((float) Math.toRadians(spreadDegrees * i));
            starfantasy$shootNightSkull(vec, velocity);
        }
    }

    @Unique
    private void starfantasy$shootNightSkull(Vec3 direction, float velocity) {
        NightSkull skull = EntityRegistry.NIGHT_SKULL.get().create(this.boss.level());
        if (skull == null) {
            return;
        }
        skull.setPosRaw(this.boss.getX(), this.boss.getEyeY(), this.boss.getZ());
        skull.shoot(direction.x(), direction.y(), direction.z(), velocity, 1.0F);
        skull.setOwner(this.boss);
        this.boss.level().addFreshEntity(skull);
    }
}
