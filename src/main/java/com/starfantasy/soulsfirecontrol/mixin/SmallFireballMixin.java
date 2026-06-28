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
 *  net.minecraft.world.phys.BlockHitResult
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package com.starfantasy.soulsfirecontrol.mixin;

import com.starfantasy.soulsfirecontrol.util.DayStalkerTweaks;
import com.starfantasy.soulsfirecontrol.util.AccursedLordTweaks;
import com.starfantasy.soulsfirecontrol.util.SoulsFireControlHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={SmallFireball.class})
public abstract class SmallFireballMixin {
    @Redirect(method={"onHitBlock"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean starfantasy$disableSoulsweaponsSmallFireballFire(Level level, BlockPos pos, BlockState state, BlockHitResult hitResult) {
        Entity owner = ((Projectile)(Object)this).getOwner();
        return SoulsFireControlHelper.placeBlockUnlessSoulsFireFromOwner(level, pos, state, owner, (Entity)(Object)this);
    }

    @Inject(method = "onHitEntity", at = @At("TAIL"))
    private void starfantasy$explodeSummonedWarmthFireballOnEntity(EntityHitResult hitResult, CallbackInfo ci) {
        DayStalkerTweaks.detonateWarmthFireball((SmallFireball) (Object) this, hitResult.getLocation());
        DayStalkerTweaks.detonateBlazeBarrageFireball((SmallFireball) (Object) this, hitResult.getLocation());
        AccursedLordTweaks.detonateAccursedProjectile((SmallFireball) (Object) this, hitResult.getLocation());
    }

    @Inject(method = "onHitBlock", at = @At("TAIL"))
    private void starfantasy$explodeSummonedWarmthFireballOnBlock(BlockHitResult hitResult, CallbackInfo ci) {
        DayStalkerTweaks.detonateWarmthFireball((SmallFireball) (Object) this, hitResult.getLocation());
        DayStalkerTweaks.detonateBlazeBarrageFireball((SmallFireball) (Object) this, hitResult.getLocation());
        AccursedLordTweaks.detonateAccursedProjectile((SmallFireball) (Object) this, hitResult.getLocation());
    }
}
