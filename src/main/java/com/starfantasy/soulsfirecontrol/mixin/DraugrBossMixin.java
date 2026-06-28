package com.starfantasy.soulsfirecontrol.mixin;

import com.starfantasy.soulsfirecontrol.combat.draugr.DraugrActionLockTracker;
import com.starfantasy.soulsfirecontrol.client.sync.DraugrAnimationSyncState;
import com.starfantasy.soulsfirecontrol.combat.guard.GuardBreakTracker;
import com.starfantasy.soulsfirecontrol.config.ChaosMonarchConfig;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.soulsweaponry.entity.mobs.DraugrBoss;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;

@Mixin(value = DraugrBoss.class, remap = false)
public abstract class DraugrBossMixin {
    @Unique
    private boolean starfantasy$deathShadeSpawned;

    @Shadow
    private int projectileCount;

    @Shadow
    private void setSameWeaponCount(int amount) {
    }

    @Inject(method = "m_6469_", at = @At("HEAD"))
    private void starfantasy$disableGrowingResistanceAndProjectileCounter(DamageSource source, float amount,
                                                                          CallbackInfoReturnable<Boolean> cir) {
        this.setSameWeaponCount(0);
        if (source.getDirectEntity() instanceof Projectile) {
            this.projectileCount = Integer.MAX_VALUE;
        }
    }

    @ModifyVariable(method = "m_6469_", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float starfantasy$reduceNormalDamage(float amount) {
        DraugrBoss boss = (DraugrBoss) (Object) this;
        if (amount <= 0.0F || boss.isPostureBroken() || GuardBreakTracker.isStunned(boss)) {
            return amount;
        }
        return amount * ChaosMonarchConfig.getDraugrBossNormalDamageMultiplier();
    }

    @Inject(method = "m_6667_", at = @At("HEAD"))
    private void starfantasy$clearTweakedStatesForDeath(DamageSource source, CallbackInfo ci) {
        DraugrBoss boss = (DraugrBoss) (Object) this;
        boss.setPostureBroken(false);
        GuardBreakTracker.clear(boss);
        DraugrActionLockTracker.clear(boss);
    }

    @Redirect(method = "m_6667_",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;m_7967_(Lnet/minecraft/world/entity/Entity;)Z",
                    remap = false),
            remap = false)
    private boolean starfantasy$spawnDeathShadeOnce(Level level, Entity entity) {
        if (this.starfantasy$deathShadeSpawned) {
            return false;
        }
        this.starfantasy$deathShadeSpawned = true;
        if (level.isClientSide()) {
            return false;
        }
        return level.addFreshEntity(entity);
    }

    @Inject(method = "attackAnimations", at = @At("HEAD"), cancellable = true)
    private void starfantasy$playSyncedDraugrAnimation(AnimationState<?> event,
                                                       CallbackInfoReturnable<PlayState> cir) {
        DraugrBoss boss = (DraugrBoss) (Object) this;
        DraugrAnimationSyncState.SyncedAnimation synced = DraugrAnimationSyncState.getActiveAnimation(boss);
        if (synced == null) {
            return;
        }
        if (DraugrAnimationSyncState.consumeReset(boss, synced.sequence())) {
            event.getController().forceAnimationReset();
        }
        event.getController().setAnimation(RawAnimation.begin().then(synced.animationName(), synced.loopType()));
        cir.setReturnValue(PlayState.CONTINUE);
    }

    @Inject(method = "setState", at = @At("HEAD"), cancellable = true)
    private void starfantasy$keepLockedDraugrAction(DraugrBoss.States state, CallbackInfo ci) {
        DraugrBoss boss = (DraugrBoss) (Object) this;
        if (GuardBreakTracker.isStunned(boss)) {
            if (state != DraugrBoss.States.IDLE && !DraugrActionLockTracker.isUninterruptibleOverride(state)) {
                ci.cancel();
            }
            return;
        }
        if (!DraugrActionLockTracker.canChangeState(boss, state)) {
            ci.cancel();
        }
    }

    @Inject(method = "m_8107_", at = @At("TAIL"))
    private void starfantasy$tickTweakedDraugrStates(CallbackInfo ci) {
        DraugrBoss boss = (DraugrBoss) (Object) this;
        DraugrActionLockTracker.tick(boss);
        GuardBreakTracker.tick(boss);
        if (boss.isShielding()) {
            boss.removeEffect(MobEffects.DAMAGE_RESISTANCE);
        }
        boss.removeEffect(MobEffects.WEAKNESS);
        boss.removeEffect(MobEffects.DAMAGE_BOOST);
        if (!boss.getState().equals(DraugrBoss.States.RUN_THRUST)) {
            boss.removeEffect(MobEffects.MOVEMENT_SPEED);
        }
        if (!boss.isSpawning()
                && !GuardBreakTracker.isStunned(boss)
                && boss.getState().equals(DraugrBoss.States.IDLE)) {
            boss.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        }
    }
}
