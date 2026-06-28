package com.starfantasy.soulsfirecontrol.mixin;

import net.minecraft.server.level.ServerBossEvent;
import net.soulsweaponry.entity.mobs.BossEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = BossEntity.class, remap = false)
public interface BossEntityAccessor {
    @Accessor("bossBar")
    ServerBossEvent starfantasy$getBossBar();
}
