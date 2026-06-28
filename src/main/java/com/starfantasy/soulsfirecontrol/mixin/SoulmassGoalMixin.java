package com.starfantasy.soulsfirecontrol.mixin;

import com.starfantasy.soulsfirecontrol.util.NightProwlerTweaks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.soulsweaponry.entity.mobs.SoulReaperGhost;
import net.soulsweaponry.entity.mobs.Soulmass;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.soulsweaponry.entity.mobs.Soulmass$SoulmassGoal", remap = false)
public abstract class SoulmassGoalMixin {
    @Shadow
    @Final
    private Soulmass entity;

    @Redirect(method = "m_8037_",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;m_7967_(Lnet/minecraft/world/entity/Entity;)Z",
                    remap = false),
            remap = false)
    private boolean starfantasy$tagSummonedGhostAlly(Level level, Entity summoned) {
        if (summoned instanceof SoulReaperGhost ghost
                && this.entity.getTags().contains(NightProwlerTweaks.SUMMON_ALLY_TAG)) {
            ghost.setTarget(this.entity.getTarget());
            NightProwlerTweaks.copySummonAllyTags(this.entity, ghost);
            ghost.addTag(NightProwlerTweaks.NO_LOOT_SOULMASS_TAG);
        }
        return level.addFreshEntity(summoned);
    }
}
