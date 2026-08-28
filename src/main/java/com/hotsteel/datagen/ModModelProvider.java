package com.hotsteel.datagen;

import com.hotsteel.registry.ModBlocks;
import com.hotsteel.registry.ModItems;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.ModelTemplates;

public class ModModelProvider extends FabricModelProvider {

    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators gen) {
        gen.createTrivialCube(ModBlocks.CRUDE_STEEL_BLOCK);
        gen.createTrivialCube(ModBlocks.STEEL_BLOCK);
        gen.createTrivialCube(ModBlocks.HOT_STEEL_BLOCK);
        gen.createTrivialCube(ModBlocks.HOT_STEEL_BRICKS);
        gen.createTrivialCube(ModBlocks.HOT_STEEL_SMELTER);
        // forge/lantern/door/trapdoor/fence/pressure-plate models are hand-written
    }

    @Override
    public void generateItemModels(ItemModelGenerators gen) {
        gen.generateFlatItem(ModItems.CRUDE_STEEL, ModelTemplates.FLAT_ITEM);
        gen.generateFlatItem(ModItems.STEEL_INGOT, ModelTemplates.FLAT_ITEM);
        gen.generateFlatItem(ModItems.HOT_STEEL_INGOT, ModelTemplates.FLAT_ITEM);
        gen.generateFlatItem(ModItems.HOT_STEEL_NUGGET, ModelTemplates.FLAT_ITEM);
        gen.generateFlatItem(ModItems.MOLTEN_CORE, ModelTemplates.FLAT_ITEM);
        gen.generateFlatItem(ModItems.HOT_STEEL_APPLE, ModelTemplates.FLAT_ITEM);

        gen.generateFlatItem(ModItems.HOT_STEEL_HELMET, ModelTemplates.FLAT_ITEM);
        gen.generateFlatItem(ModItems.HOT_STEEL_CHESTPLATE, ModelTemplates.FLAT_ITEM);
        gen.generateFlatItem(ModItems.HOT_STEEL_LEGGINGS, ModelTemplates.FLAT_ITEM);
        gen.generateFlatItem(ModItems.HOT_STEEL_BOOTS, ModelTemplates.FLAT_ITEM);

        gen.generateFlatItem(ModItems.HOT_STEEL_SWORD, ModelTemplates.FLAT_HANDHELD_ITEM);
        gen.generateFlatItem(ModItems.HOT_STEEL_MACE, ModelTemplates.FLAT_HANDHELD_ITEM);
        gen.generateFlatItem(ModItems.HOT_STEEL_KNIFE, ModelTemplates.FLAT_HANDHELD_ITEM);
        gen.generateFlatItem(ModItems.HOT_STEEL_SICKLE, ModelTemplates.FLAT_HANDHELD_ITEM);
        gen.generateFlatItem(ModItems.HOT_STEEL_PICKAXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        gen.generateFlatItem(ModItems.HOT_STEEL_AXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        gen.generateFlatItem(ModItems.HOT_STEEL_SHOVEL, ModelTemplates.FLAT_HANDHELD_ITEM);
        gen.generateFlatItem(ModItems.HOT_STEEL_HOE, ModelTemplates.FLAT_HANDHELD_ITEM);
        gen.generateFlatItem(ModItems.HOT_STEEL_PAXEL, ModelTemplates.FLAT_HANDHELD_ITEM);
        gen.generateFlatItem(ModItems.HOT_STEEL_ARROW, ModelTemplates.FLAT_ITEM);
        gen.generateFlatItem(ModItems.HOT_STEEL_FIREBALL, ModelTemplates.FLAT_ITEM);

        // bow / crossbow / trident / shield / fishing-rod item models are hand-written
    }
}
