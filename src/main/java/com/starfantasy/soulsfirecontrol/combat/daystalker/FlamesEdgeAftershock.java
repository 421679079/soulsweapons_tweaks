package com.starfantasy.soulsfirecontrol.combat.daystalker;

import net.minecraft.world.phys.Vec3;

public final class FlamesEdgeAftershock {
    public final Vec3 center;
    public int warningDelayTicks;
    public int explosionDelayTicks;
    public boolean warningShown;

    public FlamesEdgeAftershock(Vec3 center, int warningDelayTicks, int explosionDelayTicks) {
        this.center = center;
        this.warningDelayTicks = warningDelayTicks;
        this.explosionDelayTicks = explosionDelayTicks;
    }
}
