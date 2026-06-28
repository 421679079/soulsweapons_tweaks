/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.soulsweaponry.entity.mobs.ChaosMonarch
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.starfantasy.soulsfirecontrol.mixin;

import com.starfantasy.soulsfirecontrol.util.ChaosMonarchHelper;
import net.minecraft.world.entity.LivingEntity;
import net.soulsweaponry.entity.mobs.ChaosMonarch;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets={"net.soulsweaponry.entity.ai.goal.ChaosMonarchGoal"}, remap=false)
public abstract class ChaosMonarchGoalMixin {
    @Shadow
    @Final
    private ChaosMonarch boss;
    @Shadow
    private int attackStatus;
    @Unique
    private String starfantasy$controlledProjectileId = "";

    @Inject(method={"resetAttack"}, at={@At(value="TAIL")}, remap=false)
    private void starfantasy$chooseConfiguredControlledProjectile(float scale, CallbackInfo ci) {
        this.starfantasy$controlledProjectileId = ChaosMonarchHelper.pickControlledProjectileId(this.boss.getRandom());
    }

    @Inject(method={"chaosSkull"}, at={@At(value="HEAD")}, cancellable=true, remap=false)
    private void starfantasy$gateChaosSkullByCooldown(LivingEntity target, CallbackInfo ci) {
        ChaosMonarchHelper.fireChaosSkullOrFallback(this.boss, target);
        ci.cancel();
    }

    @Inject(method={"controlledProjectiles"}, at={@At(value="HEAD")}, cancellable=true, remap=false)
    private void starfantasy$replaceControlledProjectiles(LivingEntity target, CallbackInfo ci) {
        ChaosMonarchHelper.fireControlledProjectile(this.boss, target, this.attackStatus, this.starfantasy$controlledProjectileId);
        ci.cancel();
    }

    @Inject(method={"randomProjectiles"}, at={@At(value="HEAD")}, cancellable=true, remap=false)
    private void starfantasy$replaceRandomProjectiles(CallbackInfo ci) {
        ChaosMonarchHelper.fireRandomProjectile(this.boss);
        ci.cancel();
    }

    @Inject(method={"spawnLightning"}, at={@At(value="HEAD")}, cancellable=true, remap=false)
    private void starfantasy$replaceLightningWithFlamePillars(int multiplier, CallbackInfo ci) {
        ChaosMonarchHelper.spawnLightningReplacement(this.boss, multiplier);
        ci.cancel();
    }
}

