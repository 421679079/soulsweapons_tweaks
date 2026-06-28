/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.Explosion
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.Level$ExplosionInteraction
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package com.starfantasy.soulsfirecontrol.mixin;

import com.starfantasy.soulsfirecontrol.combat.daystalker.DayStalkerReactionTrapManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.soulsweaponry.entity.mobs.DayStalker;
import net.soulsweaponry.entity.projectile.GrowingFireball;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets={"net.soulsweaponry.entity.projectile.GrowingFireball"}, remap=false)
public abstract class GrowingFireballMixin {
    @Redirect(method={"detonate"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/level/Level;m_255391_(Lnet/minecraft/world/entity/Entity;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;)Lnet/minecraft/world/level/Explosion;", remap=false), remap=false)
    private Explosion starfantasy$disableGrowingFireballGroundFire(Level level, Entity source, double x, double y, double z, float power, boolean causesFire, Level.ExplosionInteraction interaction) {
        if (source instanceof GrowingFireball fireball && fireball.getOwner() instanceof DayStalker boss) {
            DayStalkerReactionTrapManager.trigger(boss);
        }
        return level.explode(source, x, y, z, power, false, interaction);
    }

    @Redirect(method = "m_8119_",
            at = @At(value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/projectile/GrowingFireball;m_6686_(DDDFF)V",
                    remap = false),
            remap = false)
    private void starfantasy$speedUpDayStalkerConflagration(GrowingFireball fireball,
                                                            double x, double y, double z,
                                                            float velocity, float inaccuracy) {
        if (fireball.getOwner() instanceof DayStalker) {
            fireball.shoot(x, y, z, velocity * 2.0F, inaccuracy);
            return;
        }
        fireball.shoot(x, y, z, velocity, inaccuracy);
    }
}
