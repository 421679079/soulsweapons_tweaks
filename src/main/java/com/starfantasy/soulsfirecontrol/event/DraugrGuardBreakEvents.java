package com.starfantasy.soulsfirecontrol.event;

import com.starfantasy.soulsfirecontrol.StarFantasySoulsFireControl;
import com.starfantasy.soulsfirecontrol.combat.guard.AccursedLordGuardBreakTracker;
import com.starfantasy.soulsfirecontrol.combat.guard.ChaosMonarchGuardBreakTracker;
import com.starfantasy.soulsfirecontrol.combat.guard.DayStalkerGuardBreakTracker;
import com.starfantasy.soulsfirecontrol.combat.guard.GuardBreakTracker;
import com.starfantasy.soulsfirecontrol.combat.guard.MoonknightGuardBreakTracker;
import com.starfantasy.soulsfirecontrol.combat.guard.NightShadeGuardBreakTracker;
import com.starfantasy.soulsfirecontrol.combat.guard.NightProwlerGuardBreakTracker;
import com.starfantasy.soulsfirecontrol.combat.guard.SlashBladeGuardCompat;
import com.starfantasy.soulsfirecontrol.util.DayStalkerTweaks;
import com.starfantasy.soulsfirecontrol.util.AccursedLordTweaks;
import com.starfantasy.soulsfirecontrol.util.ChaosMonarchTweaks;
import com.starfantasy.soulsfirecontrol.util.MoonknightTweaks;
import com.starfantasy.soulsfirecontrol.util.NightProwlerTweaks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.soulsweaponry.entity.mobs.DayStalker;
import net.soulsweaponry.entity.mobs.DraugrBoss;
import net.soulsweaponry.entity.mobs.NightShade;
import net.soulsweaponry.entity.mobs.NightProwler;
import net.soulsweaponry.entity.mobs.ChaosMonarch;
import net.soulsweaponry.entity.mobs.AccursedLordBoss;
import net.soulsweaponry.entity.mobs.Moonknight;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StarFantasySoulsFireControl.MOD_ID)
public final class DraugrGuardBreakEvents {
    private DraugrGuardBreakEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onPlayerPerfectGuardedDraugrAttack(LivingAttackEvent event) {
        if (!event.isCanceled() || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        DraugrBoss boss = findDraugrAttacker(event.getSource());
        if (boss != null && SlashBladeGuardCompat.isUsableMainhandSlashBlade(player)) {
            GuardBreakTracker.recordPerfectGuard(boss);
        }
        NightShade shade = findNightShadeMeleeAttacker(event.getSource());
        if (shade != null && SlashBladeGuardCompat.isUsableMainhandSlashBlade(player)) {
            NightShadeGuardBreakTracker.recordPerfectGuard(shade, player);
        }
        NightProwler prowler = findNightProwlerDirectAttacker(event.getSource());
        if (prowler != null && !NightProwlerTweaks.suppressesGuardBreak(prowler)
                && SlashBladeGuardCompat.isUsableMainhandSlashBlade(player)) {
            NightProwlerGuardBreakTracker.recordPerfectGuard(prowler, player);
        }
        DayStalker stalker = findDayStalkerDirectAttacker(event.getSource());
        if (stalker != null && !DayStalkerTweaks.suppressesGuardBreak(stalker)
                && SlashBladeGuardCompat.isUsableMainhandSlashBlade(player)) {
            DayStalkerGuardBreakTracker.recordPerfectGuard(stalker, player);
        }
        ChaosMonarch monarch = findChaosMonarchDirectAttacker(event.getSource());
        if (monarch != null && ChaosMonarchTweaks.rewardsGuardBreak(monarch)
                && SlashBladeGuardCompat.isUsableMainhandSlashBlade(player)) {
            ChaosMonarchGuardBreakTracker.recordPerfectGuard(monarch, player);
        }
        AccursedLordBoss accursedLord = findAccursedLordDirectAttacker(event.getSource());
        if (accursedLord != null && AccursedLordTweaks.rewardsGuardBreak(accursedLord)
                && SlashBladeGuardCompat.isUsableMainhandSlashBlade(player)) {
            AccursedLordGuardBreakTracker.recordPerfectGuard(accursedLord, player);
        }
        Moonknight moonknight = findMoonknightDirectAttacker(event.getSource());
        if (moonknight != null && MoonknightTweaks.rewardsGuardBreak(moonknight)
                && SlashBladeGuardCompat.isUsableMainhandSlashBlade(player)) {
            MoonknightGuardBreakTracker.recordPerfectGuard(moonknight, player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerHurtByGuardBreakBoss(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer) || event.getAmount() <= 0.0F) {
            return;
        }
        NightProwler prowler = findNightProwlerDirectAttacker(event.getSource());
        if (prowler != null && !NightProwlerTweaks.suppressesGuardBreak(prowler)) {
            NightProwlerGuardBreakTracker.recordPlayerHit(prowler);
        }
        DayStalker stalker = findDayStalkerDirectAttacker(event.getSource());
        if (stalker != null && !DayStalkerTweaks.suppressesGuardBreak(stalker)) {
            DayStalkerGuardBreakTracker.recordPlayerHit(stalker);
        }
        ChaosMonarch monarch = findChaosMonarchDirectAttacker(event.getSource());
        if (monarch != null && ChaosMonarchTweaks.rewardsGuardBreak(monarch)) {
            ChaosMonarchGuardBreakTracker.recordPlayerHit(monarch);
        }
    }

    private static DraugrBoss findDraugrAttacker(DamageSource source) {
        Entity direct = source.getDirectEntity();
        if (direct instanceof DraugrBoss boss) {
            return boss;
        }
        Entity attacker = source.getEntity();
        if (attacker instanceof DraugrBoss boss) {
            return boss;
        }
        return null;
    }

    private static NightShade findNightShadeMeleeAttacker(DamageSource source) {
        Entity direct = source.getDirectEntity();
        if (direct instanceof NightShade boss) {
            return boss;
        }
        if (direct == null && source.getEntity() instanceof NightShade boss) {
            return boss;
        }
        return null;
    }

    private static NightProwler findNightProwlerDirectAttacker(DamageSource source) {
        Entity direct = source.getDirectEntity();
        if (direct instanceof NightProwler boss) {
            return boss;
        }
        if (direct == null && source.getEntity() instanceof NightProwler boss) {
            return boss;
        }
        return null;
    }

    private static DayStalker findDayStalkerDirectAttacker(DamageSource source) {
        Entity direct = source.getDirectEntity();
        if (direct instanceof DayStalker boss) {
            return boss;
        }
        if (direct == null && source.getEntity() instanceof DayStalker boss) {
            return boss;
        }
        return null;
    }

    private static ChaosMonarch findChaosMonarchDirectAttacker(DamageSource source) {
        Entity direct = source.getDirectEntity();
        if (direct instanceof ChaosMonarch boss) {
            return boss;
        }
        if (direct == null && source.getEntity() instanceof ChaosMonarch boss) {
            return boss;
        }
        return null;
    }

    private static AccursedLordBoss findAccursedLordDirectAttacker(DamageSource source) {
        Entity direct = source.getDirectEntity();
        if (direct instanceof AccursedLordBoss boss) {
            return boss;
        }
        if (direct == null && source.getEntity() instanceof AccursedLordBoss boss) {
            return boss;
        }
        return null;
    }

    private static Moonknight findMoonknightDirectAttacker(DamageSource source) {
        Entity direct = source.getDirectEntity();
        if (direct instanceof Moonknight boss) {
            return boss;
        }
        if (direct == null && source.getEntity() instanceof Moonknight boss) {
            return boss;
        }
        return null;
    }
}
