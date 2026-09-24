package com.hotsteel.registry;

import com.hotsteel.HotSteel;
import com.hotsteel.content.block.HotSteelSmelterBlockEntity;
import com.hotsteel.content.block.MoltenAltarBlockEntity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {

    private ModBlockEntities() {}

    public static final BlockEntityType<HotSteelSmelterBlockEntity> HOT_STEEL_SMELTER =
        Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            HotSteel.id("hot_steel_smelter"),
            BlockEntityType.Builder.of(HotSteelSmelterBlockEntity::new, ModBlocks.HOT_STEEL_SMELTER)
                .build(null));

    public static final BlockEntityType<MoltenAltarBlockEntity> MOLTEN_ALTAR =
        Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            HotSteel.id("molten_altar"),
            BlockEntityType.Builder.of(MoltenAltarBlockEntity::new, ModBlocks.MOLTEN_ALTAR)
                .build(null));

    public static void register() {
        HotSteel.LOGGER.info("Registering Hot Steel block entities");
    }
}
