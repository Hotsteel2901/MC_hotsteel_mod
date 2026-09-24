package com.hotsteel.registry;

import com.hotsteel.HotSteel;
import com.hotsteel.content.entity.AncientForgebornEntity;
import com.hotsteel.content.entity.EmberWispEntity;
import com.hotsteel.content.entity.FireWraithEntity;
import com.hotsteel.content.entity.HotSteelArrowEntity;
import com.hotsteel.content.entity.HotSteelTridentEntity;
import com.hotsteel.content.entity.LavaBottleEntity;
import com.hotsteel.content.entity.LavaGolemEntity;
import com.hotsteel.content.entity.SlagCrawlerEntity;

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

    /** Small, fast Nether crawler that drops Molten Shards. */
    public static final EntityType<SlagCrawlerEntity> SLAG_CRAWLER = register(
        "slag_crawler",
        EntityType.Builder.<SlagCrawlerEntity>of(SlagCrawlerEntity::new, MobCategory.MONSTER)
            .sized(1.0f, 0.7f)
            .clientTrackingRange(8)
            .fireImmune()
            .build("slag_crawler"));

    /** Floating ember spirit that snipes with small fireballs. */
    public static final EntityType<EmberWispEntity> EMBER_WISP = register(
        "ember_wisp",
        EntityType.Builder.<EmberWispEntity>of(EmberWispEntity::new, MobCategory.MONSTER)
            .sized(0.7f, 0.9f)
            .clientTrackingRange(8)
            .fireImmune()
            .build("ember_wisp"));

    /** The final boss — summoned only at the Molten Altar. */
    public static final EntityType<AncientForgebornEntity> ANCIENT_FORGEBORN = register(
        "ancient_forgeborn",
        EntityType.Builder.<AncientForgebornEntity>of(AncientForgebornEntity::new, MobCategory.MONSTER)
            .sized(1.6f, 3.4f)
            .clientTrackingRange(12)
            .fireImmune()
            .build("ancient_forgeborn"));

    private static <T extends Entity> EntityType<T> register(String name, EntityType<T> type) {
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, HotSteel.id(name), type);
    }

    public static void register() {
        HotSteel.LOGGER.info("Registering Hot Steel entities");
    }
}
