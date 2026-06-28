/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package com.starfantasy.soulsfirecontrol.mixin;

import com.starfantasy.soulsfirecontrol.util.SoulsFireControlHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets={"net.soulsweaponry.entity.projectile.noclip.DamagingWarmupEntityEvents"}, remap=false)
public abstract class DamagingWarmupEntityEventsMixin {
    @Redirect(method={"lambda$static$0"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/level/Level;m_46597_(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z", remap=false), remap=false)
    private static boolean starfantasy$disableWarmupGroundFire(Level level, BlockPos pos, BlockState state) {
        return SoulsFireControlHelper.placeBlockUnlessSoulsFire(level, pos, state);
    }
}
