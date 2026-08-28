package com.hotsteel.registry;

import com.hotsteel.HotSteel;
import com.hotsteel.entity.FireWraithEntity;
import com.hotsteel.entity.HotSteelArrowEntity;
import com.hotsteel.entity.HotSteelTridentEntity;
import com.hotsteel.entity.LavaBottleEntity;
import com.hotsteel.entity.LavaGolemEntity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {

    private ModEntities() {}

    public static final EntityType<HotSteelTridentEntity> HOT_STEEL_TRIDENT = register(
        "hot_steel_trident",
        EntityType.Builder.<HotSteelTridentEntity>of(HotSteelTridentEntity::new, MobCategory.MISC)
            .sized(0.5f, 0.5f)
            .clientTrackingRange(4)
            .updateInterval(20)
            .build("hot_steel_trident"));

    public static final EntityType<HotSteelArrowEntity> HOT_STEEL_ARROW = register(
        "hot_steel_arrow",
        EntityType.Builder.<HotSteelArrowEntity>of(HotSteelArrowEntity::new, MobCategory.MISC)
            .sized(0.5f, 0.5f)
            .clientTrackingRange(4)
            .updateInterval(20)
            .build("hot_steel_arrow"));

    /** Thrown bottle of lava. */
    public static final EntityType<LavaBottleEntity> LAVA_BOTTLE = register(
        "lava_bottle",
        EntityType.Builder.<LavaBottleEntity>of(LavaBottleEntity::new, MobCategory.MISC)
            .sized(0.25f, 0.25f)
            .clientTrackingRange(4)
            .updateInterval(10)
            .build("lava_bottle"));

    /** Molten guardian golem. */
    public static final EntityType<LavaGolemEntity> LAVA_GOLEM = register(
        "lava_golem",
        EntityType.Builder.<LavaGolemEntity>of(LavaGolemEntity::new, MobCategory.CREATURE)
            .sized(1.4f, 2.7f)
            .clientTrackingRange(10)
            .fireImmune()
            .build("lava_golem"));

    /** Blazing Nether wraith — stronger than a Blaze, drops Molten Cores. */
    public static final EntityType<FireWraithEntity> FIRE_WRAITH = register(
        "fire_wraith",
        EntityType.Builder.<FireWraithEntity>of(FireWraithEntity::new, MobCategory.MONSTER)
            .sized(0.6f, 1.8f)
            .clientTrackingRange(8)
            .fireImmune()
            .build("fire_wraith"));

    private static <T extends Entity> EntityType<T> register(String name, EntityType<T> type) {
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, HotSteel.id(name), type);
    }

    public static void register() {
        HotSteel.LOGGER.info("Registering Hot Steel entities");
    }
}
