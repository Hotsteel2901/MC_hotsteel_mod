package com.hotsteel.registry;

import com.hotsteel.HotSteel;
import com.hotsteel.entity.HotSteelTridentEntity;

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

    private static <T extends Entity> EntityType<T> register(String name, EntityType<T> type) {
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, HotSteel.id(name), type);
    }

    public static void register() {
        HotSteel.LOGGER.info("Registering Hot Steel entities");
    }
}
