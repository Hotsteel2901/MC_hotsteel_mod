package com.hotsteel.datagen;

import java.util.concurrent.CompletableFuture;

import com.hotsteel.registry.ModBlocks;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.core.HolderLookup;

public class ModBlockLootProvider extends FabricBlockLootTableProvider {

    public ModBlockLootProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public void generate() {
        this.dropSelf(ModBlocks.CRUDE_STEEL_BLOCK);
        this.dropSelf(ModBlocks.STEEL_BLOCK);
        this.dropSelf(ModBlocks.HOT_STEEL_BLOCK);
        this.dropSelf(ModBlocks.HOT_STEEL_STAIRS);
        this.dropSelf(ModBlocks.HOT_STEEL_SLAB);
        this.dropSelf(ModBlocks.HOT_STEEL_WALL);
        this.dropSelf(ModBlocks.HOT_STEEL_FORGE);
        this.dropSelf(ModBlocks.HOT_STEEL_SMELTER);
        this.dropSelf(ModBlocks.HOT_STEEL_DOOR);
        this.dropSelf(ModBlocks.HOT_STEEL_TRAPDOOR);
        this.dropSelf(ModBlocks.HOT_STEEL_FENCE);
        this.dropSelf(ModBlocks.HOT_STEEL_BRICKS);
        this.dropSelf(ModBlocks.HOT_STEEL_PRESSURE_PLATE);
        this.dropSelf(ModBlocks.HOT_STEEL_LANTERN);
        this.dropSelf(ModBlocks.HOT_STEEL_CHAIN);
        this.dropSelf(ModBlocks.HOT_STEEL_LADDER);
    }
}
