package com.starfantasy.soulsfirecontrol.entity;

import com.starfantasy.soulsfirecontrol.StarFantasySoulsFireControl;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class TwinMeteorEntityRegistry {
    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, StarFantasySoulsFireControl.MOD_ID);

    public static final RegistryObject<EntityType<TwinMeteorEntity>> DAY_STALKER_METEOR =
            ENTITIES.register("day_stalker_meteor", () -> meteorType("day_stalker_meteor"));

    public static final RegistryObject<EntityType<TwinMeteorEntity>> NIGHT_PROWLER_METEOR =
            ENTITIES.register("night_prowler_meteor", () -> meteorType("night_prowler_meteor"));

    public static final RegistryObject<EntityType<TwinMeteorEntity>> CHAOS_FROST_METEOR =
            ENTITIES.register("chaos_frost_meteor", () -> meteorType("chaos_frost_meteor"));

    public static final RegistryObject<EntityType<NightProwlerLightningAoeEntity>> NIGHT_PROWLER_LIGHTNING_AOE =
            ENTITIES.register("night_prowler_lightning_aoe",
                    () -> EntityType.Builder.<NightProwlerLightningAoeEntity>of(
                                    NightProwlerLightningAoeEntity::new, MobCategory.MISC)
                            .sized(1.0F, 0.1F)
                            .clientTrackingRange(96)
                            .updateInterval(1)
                            .fireImmune()
                            .noSave()
                            .noSummon()
                            .build(StarFantasySoulsFireControl.id("night_prowler_lightning_aoe").toString()));

    public static final RegistryObject<EntityType<ChaosBarrageProjectileEntity>> CHAOS_BARRAGE_PROJECTILE =
            ENTITIES.register("chaos_barrage_projectile",
                    () -> EntityType.Builder.<ChaosBarrageProjectileEntity>of(
                                    ChaosBarrageProjectileEntity::new, MobCategory.MISC)
                            .sized(0.6F, 0.6F)
                            .clientTrackingRange(96)
                            .updateInterval(1)
                            .fireImmune()
                            .noSave()
                            .noSummon()
                            .build(StarFantasySoulsFireControl.id("chaos_barrage_projectile").toString()));

    public static final RegistryObject<EntityType<ChaosWitherSkullProjectileEntity>> CHAOS_WITHER_SKULL =
            ENTITIES.register("chaos_wither_skull",
                    () -> EntityType.Builder.<ChaosWitherSkullProjectileEntity>of(
                                    ChaosWitherSkullProjectileEntity::new, MobCategory.MISC)
                            .sized(0.3125F, 0.3125F)
                            .clientTrackingRange(96)
                            .updateInterval(1)
                            .fireImmune()
                            .noSave()
                            .noSummon()
                            .build(StarFantasySoulsFireControl.id("chaos_wither_skull").toString()));

    private TwinMeteorEntityRegistry() {
    }

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
    }

    private static EntityType<TwinMeteorEntity> meteorType(String name) {
        return EntityType.Builder.<TwinMeteorEntity>of(TwinMeteorEntity::new, MobCategory.MISC)
                .sized(0.9F, 0.9F)
                .clientTrackingRange(96)
                .updateInterval(1)
                .fireImmune()
                .build(StarFantasySoulsFireControl.id(name).toString());
    }
}
