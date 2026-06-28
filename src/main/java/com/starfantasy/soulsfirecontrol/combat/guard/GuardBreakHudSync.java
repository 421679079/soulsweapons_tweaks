package com.starfantasy.soulsfirecontrol.combat.guard;

import com.starfantasy.soulsfirecontrol.config.ChaosMonarchConfig;
import com.starfantasy.soulsfirecontrol.mixin.BossEntityAccessor;
import com.starfantasy.soulsfirecontrol.network.GuardBreakHudPacket;
import com.starfantasy.soulsfirecontrol.network.SoulsTweaksNetwork;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraftforge.network.PacketDistributor;
import net.soulsweaponry.entity.mobs.BossEntity;
import net.soulsweaponry.entity.mobs.DraugrBoss;

import java.util.UUID;

public final class GuardBreakHudSync {
    private GuardBreakHudSync() {
    }

    public static void syncIdle(DraugrBoss boss) {
        syncIdle(boss, requiredGuards());
    }

    public static void syncGuarded(DraugrBoss boss, int guardCount) {
        int required = requiredGuards();
        syncGuarded(boss, guardCount, required);
    }

    public static void syncStunTriggered(DraugrBoss boss) {
        int required = requiredGuards();
        syncTriggered(boss, required);
    }

    public static void clearOrReset(DraugrBoss boss) {
        clearOrReset(boss, requiredGuards());
    }

    public static void refresh(DraugrBoss boss, int guardCount) {
        refresh(boss, guardCount, requiredGuards());
    }

    public static void syncIdle(BossEntity boss, int requiredGuards) {
        sync(boss, 0, sanitizeRequiredGuards(requiredGuards), true);
    }

    public static void syncGuarded(BossEntity boss, int guardCount, int requiredGuards) {
        int required = sanitizeRequiredGuards(requiredGuards);
        sync(boss, Math.min(Math.max(0, guardCount), required), required, true);
    }

    public static void syncTriggered(BossEntity boss, int requiredGuards) {
        int required = sanitizeRequiredGuards(requiredGuards);
        sync(boss, required, required, true);
    }

    public static void clearOrReset(BossEntity boss, int requiredGuards) {
        if (boss.isDeadOrDying()) {
            sync(boss, 0, sanitizeRequiredGuards(requiredGuards), false);
        } else {
            syncIdle(boss, requiredGuards);
        }
    }

    public static void hide(BossEntity boss, int requiredGuards) {
        sync(boss, 0, sanitizeRequiredGuards(requiredGuards), false);
    }

    public static void refresh(BossEntity boss, int guardCount, int requiredGuards) {
        syncGuarded(boss, guardCount, requiredGuards);
    }

    private static int requiredGuards() {
        return Math.max(1, ChaosMonarchConfig.getDraugrBossGuardBreakRequiredGuards());
    }

    private static int sanitizeRequiredGuards(int requiredGuards) {
        return Math.max(1, requiredGuards);
    }

    private static void sync(BossEntity boss, int guardCount, int requiredGuards, boolean visible) {
        if (boss.level().isClientSide()) {
            return;
        }
        ServerBossEvent bossBar = ((BossEntityAccessor) boss).starfantasy$getBossBar();
        UUID bossEventId = bossBar.getId();
        SoulsTweaksNetwork.CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY.with(() -> boss),
                new GuardBreakHudPacket(boss.getId(), bossEventId, guardCount, requiredGuards, visible)
        );
    }
}
