package com.starfantasy.soulsfirecontrol.mixin;

import com.starfantasy.soulsfirecontrol.util.ReturningKnightTweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.soulsweaponry.entity.mobs.ReturningKnight;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.soulsweaponry.entity.ai.goal.ReturningKnightGoal", remap = false)
public abstract class ReturningKnightGoalMixin {
    @Shadow
    @Final
    private ReturningKnight boss;

    @Shadow
    private BlockPos targetPos;

    @Shadow
    private int attackStatus;

    @Inject(method = "m_8037_", at = @At(value = "FIELD",
            target = "Lnet/soulsweaponry/entity/ai/goal/ReturningKnightGoal;targetPos:Lnet/minecraft/core/BlockPos;",
            opcode = Opcodes.PUTFIELD,
            ordinal = 0,
            shift = At.Shift.AFTER))
    private void starfantasy$warnMaceOpening(CallbackInfo ci) {
        ReturningKnightTweaks.warnMaceOpening(this.boss, this.targetPos);
    }

    @Inject(method = "m_8037_", at = @At(value = "FIELD",
            target = "Lnet/soulsweaponry/entity/ai/goal/ReturningKnightGoal;targetPos:Lnet/minecraft/core/BlockPos;",
            opcode = Opcodes.PUTFIELD,
            ordinal = 1,
            shift = At.Shift.AFTER))
    private void starfantasy$warnMaceSecond(CallbackInfo ci) {
        ReturningKnightTweaks.warnMaceSecond(this.boss, this.targetPos);
    }

    @Inject(method = "m_8037_", at = @At(value = "FIELD",
            target = "Lnet/soulsweaponry/entity/ai/goal/ReturningKnightGoal;targetPos:Lnet/minecraft/core/BlockPos;",
            opcode = Opcodes.PUTFIELD,
            ordinal = 2,
            shift = At.Shift.AFTER))
    private void starfantasy$warnObliterate(CallbackInfo ci) {
        ReturningKnightTweaks.warnObliterate(this.boss, this.targetPos);
    }

    @Inject(method = "m_8037_", at = @At(value = "INVOKE",
            target = "Lnet/soulsweaponry/entity/mobs/ReturningKnight;setBlind(Z)V",
            ordinal = 0,
            shift = At.Shift.AFTER))
    private void starfantasy$warnBlind(CallbackInfo ci) {
        ReturningKnightTweaks.warnBlind(this.boss, this.attackStatus);
    }

    @Inject(method = "m_8037_", at = @At("HEAD"))
    private void starfantasy$warnRupture(CallbackInfo ci) {
        ReturningKnightTweaks.warnRupture(this.boss, this.attackStatus);
    }

    @Redirect(method = "summonAllies",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;m_7967_(Lnet/minecraft/world/entity/Entity;)Z",
                    remap = false))
    private boolean starfantasy$markReturningKnightSummonsNoLoot(Level level, Entity entity) {
        ReturningKnightTweaks.prepareNoLootSummon(entity);
        return level.addFreshEntity(entity);
    }
}
