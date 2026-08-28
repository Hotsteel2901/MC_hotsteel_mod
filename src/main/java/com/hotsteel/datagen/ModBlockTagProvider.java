package com.hotsteel.datagen;

import java.util.concurrent.CompletableFuture;

import com.hotsteel.registry.ModBlocks;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;

public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {

    public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    private static ResourceKey<Block> key(Block block) {
        return BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow();
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .add(key(ModBlocks.CRUDE_STEEL_BLOCK))
            .add(key(ModBlocks.STEEL_BLOCK))
            .add(key(ModBlocks.HOT_STEEL_BLOCK))
            .add(key(ModBlocks.HOT_STEEL_STAIRS))
            .add(key(ModBlocks.HOT_STEEL_SLAB))
            .add(key(ModBlocks.HOT_STEEL_WALL))
            .add(key(ModBlocks.HOT_STEEL_FORGE))
            .add(key(ModBlocks.HOT_STEEL_SMELTER))
            .add(key(ModBlocks.HOT_STEEL_DOOR))
            .add(key(ModBlocks.HOT_STEEL_TRAPDOOR))
            .add(key(ModBlocks.HOT_STEEL_FENCE))
            .add(key(ModBlocks.HOT_STEEL_BRICKS))
            .add(key(ModBlocks.HOT_STEEL_PRESSURE_PLATE))
            .add(key(ModBlocks.HOT_STEEL_LANTERN))
            .add(key(ModBlocks.HOT_STEEL_CHAIN));
        this.tag(BlockTags.MINEABLE_WITH_AXE)
            .add(key(ModBlocks.HOT_STEEL_LADDER));
        this.tag(BlockTags.NEEDS_STONE_TOOL)
            .add(key(ModBlocks.CRUDE_STEEL_BLOCK))
            .add(key(ModBlocks.STEEL_BLOCK));
        this.tag(BlockTags.NEEDS_DIAMOND_TOOL)
            .add(key(ModBlocks.HOT_STEEL_BLOCK))
            .add(key(ModBlocks.HOT_STEEL_STAIRS))
            .add(key(ModBlocks.HOT_STEEL_SLAB))
            .add(key(ModBlocks.HOT_STEEL_WALL))
            .add(key(ModBlocks.HOT_STEEL_FORGE))
            .add(key(ModBlocks.HOT_STEEL_SMELTER))
            .add(key(ModBlocks.HOT_STEEL_DOOR))
            .add(key(ModBlocks.HOT_STEEL_TRAPDOOR))
            .add(key(ModBlocks.HOT_STEEL_FENCE))
            .add(key(ModBlocks.HOT_STEEL_BRICKS))
            .add(key(ModBlocks.HOT_STEEL_PRESSURE_PLATE))
            .add(key(ModBlocks.HOT_STEEL_LANTERN))
            .add(key(ModBlocks.HOT_STEEL_CHAIN));
        // Hot steel block can power beacons like the vanilla metal blocks.
        this.tag(BlockTags.BEACON_BASE_BLOCKS).add(key(ModBlocks.HOT_STEEL_BLOCK));
        // Decor category tags.
        this.tag(BlockTags.STAIRS).add(key(ModBlocks.HOT_STEEL_STAIRS));
        this.tag(BlockTags.SLABS).add(key(ModBlocks.HOT_STEEL_SLAB));
        this.tag(BlockTags.WALLS).add(key(ModBlocks.HOT_STEEL_WALL));
        // Doors / trapdoors / fences / pressure plates
        this.tag(BlockTags.DOORS).add(key(ModBlocks.HOT_STEEL_DOOR));
        this.tag(BlockTags.TRAPDOORS).add(key(ModBlocks.HOT_STEEL_TRAPDOOR));
        this.tag(BlockTags.FENCES).add(key(ModBlocks.HOT_STEEL_FENCE));
        this.tag(BlockTags.FENCE_GATES).add(key(ModBlocks.HOT_STEEL_FENCE));
    }
}
