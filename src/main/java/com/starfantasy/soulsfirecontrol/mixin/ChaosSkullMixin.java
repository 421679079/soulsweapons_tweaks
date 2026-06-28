/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.HitResult
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.starfantasy.soulsfirecontrol.mixin;

import com.starfantasy.soulsfirecontrol.util.ChaosMonarchHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets={"net.soulsweaponry.entity.projectile.ChaosSkull"}, remap=false)
public abstract class ChaosSkullMixin
extends Projectile {
    protected ChaosSkullMixin(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method={"m_6532_"}, at={@At(value="HEAD")}, cancellable=true, remap=false)
    private void starfantasy$simplifyChaosSkullImpact(HitResult hitResult, CallbackInfo ci) {
        ChaosMonarchHelper.handleSimplifiedChaosSkullImpact(this, hitResult);
        ci.cancel();
    }

    @Inject(method={"randomHostile"}, at={@At(value="HEAD")}, cancellable=true, remap=false)
    private void starfantasy$useConfiguredHostileSummons(CallbackInfoReturnable<EntityType<?>> cir) {
        EntityType<?> entityType = ChaosMonarchHelper.pickRandomHostileSummon(this.level().getRandom());
        if (entityType != null) {
            cir.setReturnValue(entityType);
        }
    }

    @Inject(method={"randomPassive"}, at={@At(value="HEAD")}, cancellable=true, remap=false)
    private void starfantasy$useConfiguredPassiveSummons(CallbackInfoReturnable<EntityType<?>> cir) {
        EntityType<?> entityType = ChaosMonarchHelper.pickRandomPassiveSummon(this.level().getRandom());
        if (entityType != null) {
            cir.setReturnValue(entityType);
        }
    }
}
