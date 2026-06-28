package com.starfantasy.soulsfirecontrol.mixin;

import com.starfantasy.soulsfirecontrol.util.NightShadeTweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.soulsweaponry.entity.ai.goal.DraugrBossGoal;
import net.soulsweaponry.entity.mobs.NightShade;
import net.soulsweaponry.entity.projectile.MoonlightProjectile;
import net.soulsweaponry.entity.projectile.ShadowOrb;
import net.soulsweaponry.particles.ParticleHandler;
import net.soulsweaponry.registry.EntityRegistry;
import net.soulsweaponry.registry.SoundRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

@Mixin(targets = "net.soulsweaponry.entity.ai.goal.NightShadeGoal", remap = false)
public abstract class NightShadeGoalMixin {
    @Shadow
    @Final
    private NightShade boss;

    @Shadow
    private int attackStatus;

    @Shadow
    private void reset(float cooldownModifier) {
        throw new AssertionError();
    }

    @Shadow
    private void damageTarget(LivingEntity target, float damage) {
        throw new AssertionError();
    }

    @Shadow
    private float getModifiedDamage(float damage) {
        throw new AssertionError();
    }

    @Shadow
    private void moveRandomSpot(Vec3 vec3d) {
        throw new AssertionError();
    }

    @Inject(method = "bigSwipes", at = @At("HEAD"), cancellable = true)
    private void starfantasy$replaceBigSwipesWarnings(LivingEntity target, CallbackInfo ci) {
        NightShadeTweaks.warnBigSwipesOpening(this.boss, this.attackStatus, target.blockPosition());
        ++this.attackStatus;
        if (this.attackStatus == 1 && target.blockPosition() != null) {
            this.boss.setTargetPos(target.blockPosition());
        }
        if (DraugrBossGoal.isPosNotNullish(this.boss.getTargetPos())) {
            BlockPos pos = this.boss.getTargetPos();
            this.boss.getLookControl().setLookAt(pos.getX(), pos.getY(), pos.getZ());
            this.boss.getMoveControl().setWantedPosition(pos.getX() + 2.5D, pos.getY() + 2.0D,
                    pos.getZ() + 0.5D, 3.0D);
            if (this.attackStatus == 9 || this.attackStatus == 16) {
                playBigSwipeHit(target, pos);
            }
            NightShadeTweaks.warnBigSwipesSecondHit(this.boss, this.attackStatus);
        }
        if (this.attackStatus >= 22) {
            this.reset(1.0F);
        }
        ci.cancel();
    }

    @Inject(method = "genericCharge", at = @At("HEAD"), cancellable = true)
    private void starfantasy$replaceGenericChargeSingleHit(LivingEntity target, CallbackInfo ci) {
        NightShadeTweaks.warnGenericCharge(this.boss, this.attackStatus);
        ++this.attackStatus;
        Vec3 vec3d = target.getEyePosition();
        if (this.attackStatus == 1) {
            this.moveRandomSpot(vec3d);
        } else {
            this.boss.getMoveControl().setWantedPosition(vec3d.x, vec3d.y, vec3d.z, 1.0D);
        }
        if (this.attackStatus == 14) {
            for (Entity entity : this.boss.level().getEntities(this.boss, this.boss.getBoundingBox().inflate(2.0D))) {
                if (entity instanceof LivingEntity living) {
                    this.damageTarget(living, 18.0F);
                }
            }
        }
        if (this.attackStatus >= 20) {
            this.reset(1.0F);
        }
        ci.cancel();
    }

    @Inject(method = "aoe", at = @At("HEAD"), cancellable = true)
    private void starfantasy$replaceAoeWarning(LivingEntity target, CallbackInfo ci) {
        NightShadeTweaks.warnAoe(this.boss, this.attackStatus, target.blockPosition());
        ++this.attackStatus;
        if (this.attackStatus == 1 && target.blockPosition() != null) {
            this.boss.setTargetPos(target.blockPosition());
        }
        if (DraugrBossGoal.isPosNotNullish(this.boss.getTargetPos())) {
            BlockPos pos = this.boss.getTargetPos();
            this.boss.getLookControl().setLookAt(pos.getX(), pos.getY(), pos.getZ());
            if (this.attackStatus < 10) {
                this.boss.getMoveControl().setWantedPosition(pos.getX(), pos.getY() + 10.0D, pos.getZ(), 3.0D);
            }
            if (this.attackStatus > 10) {
                this.boss.getMoveControl().setWantedPosition(pos.getX(), pos.getY(), pos.getZ(), 4.0D);
                if (this.attackStatus == 16) {
                    playAoeHit(pos);
                }
            }
        }
        if (this.attackStatus >= 22) {
            this.reset(1.0F);
        }
        ci.cancel();
    }

    @Inject(method = "throwMoonlight", at = @At("HEAD"))
    private void starfantasy$warnThrowMoonlight(LivingEntity target, CallbackInfo ci) {
        NightShadeTweaks.warnThrowMoonlight(this.boss, this.attackStatus);
    }

    @Inject(method = "shadowBall", at = @At("HEAD"), cancellable = true)
    private void starfantasy$replaceShadowOrbsWithoutEffects(LivingEntity target, CallbackInfo ci) {
        NightShadeTweaks.warnShadowOrbs(this.boss, this.attackStatus);
        ++this.attackStatus;
        this.boss.getLookControl().setLookAt(target);
        this.boss.getNavigation().stop();
        double e = target.getX() - this.boss.getX();
        double f = target.getY(0.5D) - this.boss.getY(1.0D);
        double g = target.getZ() - this.boss.getZ();
        if (this.attackStatus >= 6 && this.attackStatus <= 15) {
            this.boss.level().playSound(null, this.boss.blockPosition(), SoundEvents.BLAZE_SHOOT,
                    SoundSource.HOSTILE, 1.0F, 1.0F);
            ShadowOrb orb = new ShadowOrb(this.boss.level(), this.boss, e, f, g, new MobEffect[0]);
            orb.setPos(this.boss.getX(), this.boss.getEyeY(), this.boss.getZ());
            orb.shoot(e, f, g, 2.0F, 1.0F);
            this.boss.level().addFreshEntity(orb);
        }
        if (this.attackStatus == 16) {
            this.boss.level().playSound(null, this.boss.blockPosition(), (SoundEvent) SoundRegistry.MOONLIGHT_BIG_EVENT.get(),
                    SoundSource.HOSTILE, 1.0F, 1.0F);
            MoonlightProjectile projectile = new MoonlightProjectile(
                    (EntityType<? extends AbstractArrow>) (EntityType<?>) EntityRegistry.MOONLIGHT_BIG_ENTITY_TYPE.get(),
                    this.boss.level(),
                    this.boss
            );
            projectile.setPos(this.boss.getX(), this.boss.getEyeY(), this.boss.getZ());
            projectile.shoot(e, f, g, 2.0F, 1.0F);
            projectile.setAgeAndPoints(30, 75, 4);
            projectile.setBaseDamage(this.getModifiedDamage(18.0F));
            this.boss.level().addFreshEntity(projectile);
        }
        if (this.attackStatus >= 25) {
            this.reset(1.0F);
        }
        ci.cancel();
    }

    private void playBigSwipeHit(LivingEntity target, BlockPos pos) {
        this.boss.level().playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                SoundSource.HOSTILE, 1.0F, 1.0F);
        if (this.boss.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 3; ++i) {
                for (int j = -10; j <= 10; ++j) {
                    serverLevel.sendParticles(ParticleTypes.GLOW,
                            pos.getX(),
                            pos.getY() + 0.3D + (double) i / 1.5D,
                            pos.getZ() + (double) j / 10.0D,
                            1,
                            0.0D,
                            0.0D,
                            0.0D,
                            0.0D);
                }
            }
        }
        for (Entity entity : this.boss.level().getEntities(this.boss, new AABB(pos).inflate(2.0D))) {
            if (entity instanceof LivingEntity living) {
                this.damageTarget(living, 20.0F);
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 3));
            }
        }
    }

    private void playAoeHit(BlockPos pos) {
        if (this.boss.level() instanceof ServerLevel) {
            HashMap<ParticleOptions, Vec3> map = new HashMap<>();
            map.put(ParticleTypes.LARGE_SMOKE, new Vec3(1.0D, 1.0D, 1.0D));
            map.put(ParticleTypes.SOUL_FIRE_FLAME, new Vec3(1.0D, 1.0D, 1.0D));
            ParticleHandler.particleOutburstMap(this.boss.level(), 600, this.boss.getX(), this.boss.getY(),
                    this.boss.getZ(), map, 1.0F);
        }
        this.boss.level().playSound(null, this.boss.blockPosition(), SoundEvents.GENERIC_EXPLODE,
                SoundSource.HOSTILE, 0.8F, 1.0F);
        for (Entity entity : this.boss.level().getEntities(this.boss, new AABB(pos).inflate(3.0D))) {
            if (entity instanceof LivingEntity living) {
                this.damageTarget(living, 25.0F);
                double x = living.getX() - this.boss.getX();
                double z = living.getZ() - this.boss.getZ();
                living.knockback(2.0D, -x, -z);
            }
        }
    }
}
