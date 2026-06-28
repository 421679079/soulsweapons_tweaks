package com.starfantasy.soulsfirecontrol.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.soulsweaponry.entity.projectile.noclip.BlackflameSnakeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = BlackflameSnakeEntity.class, remap = false)
public abstract class BlackflameSnakeEntityMixin {
    @Shadow
    private boolean hasHitPlayer;

    @Redirect(method = "m_8119_",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;m_5997_(DDD)V",
                    remap = false),
            remap = false)
    private void starfantasy$disableDirectHitLaunch(LivingEntity target, double x, double y, double z) {
        // Damage remains unchanged; the extra vertical launch is too punishing with perfect guards.
    }

    @Redirect(method = "m_8119_",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
                    remap = false),
            remap = false)
    private boolean starfantasy$markPlayerHitOnDamageAttempt(LivingEntity target, DamageSource source, float amount) {
        boolean damaged = target.hurt(source, amount);
        if (target instanceof Player) {
            this.hasHitPlayer = true;
        }
        return damaged;
    }

    @Redirect(method = "m_8119_",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;m_255391_(Lnet/minecraft/world/entity/Entity;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;)Lnet/minecraft/world/level/Explosion;",
                    remap = false),
            remap = false)
    private Explosion starfantasy$disableFinalTntExplosion(Level level, Entity source, double x, double y, double z,
                                                          float power, boolean fire,
                                                          Level.ExplosionInteraction interaction) {
        return null;
    }
}
