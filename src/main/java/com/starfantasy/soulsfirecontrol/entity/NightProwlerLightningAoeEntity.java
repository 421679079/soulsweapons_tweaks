package com.starfantasy.soulsfirecontrol.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class NightProwlerLightningAoeEntity extends Entity {
    private static final EntityDataAccessor<Float> RADIUS =
            SynchedEntityData.defineId(NightProwlerLightningAoeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DURATION =
            SynchedEntityData.defineId(NightProwlerLightningAoeEntity.class, EntityDataSerializers.INT);

    public NightProwlerLightningAoeEntity(EntityType<? extends NightProwlerLightningAoeEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public NightProwlerLightningAoeEntity(Level level, Vec3 position, int duration, float radius) {
        this(TwinMeteorEntityRegistry.NIGHT_PROWLER_LIGHTNING_AOE.get(), level);
        this.entityData.set(RADIUS, Math.max(0.1F, radius));
        this.entityData.set(DURATION, Math.max(1, duration));
        this.setPos(position);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(RADIUS, 0.1F);
        this.entityData.define(DURATION, 1);
    }

    @Override
    public void tick() {
        super.tick();
        this.noPhysics = true;
        this.setNoGravity(true);
        if (this.tickCount >= this.getDuration()) {
            this.discard();
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double renderDistance = Math.max(64.0D, this.getRadius() * 10.0D);
        return distance < renderDistance * renderDistance;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.entityData.set(RADIUS, tag.getFloat("Radius"));
        this.entityData.set(DURATION, tag.getInt("Duration"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Radius", this.getRadius());
        tag.putInt("Duration", this.getDuration());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public float getRadius() {
        return this.entityData.get(RADIUS);
    }

    public int getDuration() {
        return this.entityData.get(DURATION);
    }
}
