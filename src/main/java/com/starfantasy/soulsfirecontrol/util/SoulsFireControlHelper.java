/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.projectile.SmallFireball
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.starfantasy.soulsfirecontrol.util;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class SoulsFireControlHelper {
    private static final double OWNERLESS_SMALL_FIREBALL_SEARCH_RADIUS = 96.0;

    private SoulsFireControlHelper() {
    }

    public static boolean placeBlockUnlessSoulsFire(Level level, BlockPos pos, BlockState state) {
        return SoulsFireControlHelper.isGroundFire(state) ? false : level.setBlockAndUpdate(pos, state);
    }

    public static boolean placeBlockUnlessSoulsFireFromOwner(Level level, BlockPos pos, BlockState state, Entity owner, Entity sourceEntity) {
        if (SoulsFireControlHelper.isGroundFire(state) && SoulsFireControlHelper.shouldBlockSoulsFire(owner, sourceEntity)) {
            return false;
        }
        return level.setBlockAndUpdate(pos, state);
    }

    private static boolean shouldBlockSoulsFire(Entity owner, Entity sourceEntity) {
        SmallFireball smallFireball;
        block5: {
            block4: {
                if (SoulsFireControlHelper.isSoulsweaponsEntity(owner)) {
                    return true;
                }
                if (!(sourceEntity instanceof SmallFireball)) break block4;
                smallFireball = (SmallFireball)sourceEntity;
                if (owner == null) break block5;
            }
            return false;
        }
        Level level = smallFireball.level();
        List nearbySoulsEntities = level.getEntities((Entity)smallFireball, smallFireball.getBoundingBox().inflate(96.0), SoulsFireControlHelper::isSoulsweaponsEntity);
        return !nearbySoulsEntities.isEmpty();
    }

    private static boolean isGroundFire(BlockState state) {
        return state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE);
    }

    private static boolean isSoulsweaponsEntity(Entity owner) {
        return owner != null && owner.getClass().getName().startsWith("net.soulsweaponry.");
    }
}

