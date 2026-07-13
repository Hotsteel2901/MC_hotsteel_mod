package com.hotsteel.datagen;

import java.util.concurrent.CompletableFuture;

import com.hotsteel.registry.ModBlocks;
import com.hotsteel.registry.ModItems;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public class ModRecipeProvider extends FabricRecipeProvider {

    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public void buildRecipes(RecipeOutput exporter) {
        // iron ingot -> crude steel (blast furnace)
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(Items.IRON_INGOT),
                RecipeCategory.MISC, ModItems.CRUDE_STEEL, 0.7f, 100)
            .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
            .save(exporter, "crude_steel_from_blasting");

        // 4 crude steel -> crude steel block
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.CRUDE_STEEL_BLOCK)
            .pattern("SS")
            .pattern("SS")
            .define('S', ModItems.CRUDE_STEEL)
            .unlockedBy("has_crude_steel", has(ModItems.CRUDE_STEEL))
            .save(exporter);

        // crude steel block -> steel ingot (blast furnace)
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(ModBlocks.CRUDE_STEEL_BLOCK),
                RecipeCategory.MISC, ModItems.STEEL_INGOT, 1.0f, 100)
            .unlockedBy("has_crude_steel_block", has(ModBlocks.CRUDE_STEEL_BLOCK))
            .save(exporter, "steel_ingot_from_blasting");

        // ---- Equipment from hot steel ingot ----
        // Armor
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.HOT_STEEL_HELMET)
            .pattern("III").pattern("I I")
            .define('I', ModItems.HOT_STEEL_INGOT)
            .unlockedBy("has_hot_steel_ingot", has(ModItems.HOT_STEEL_INGOT)).save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.HOT_STEEL_CHESTPLATE)
            .pattern("I I").pattern("III").pattern("III")
            .define('I', ModItems.HOT_STEEL_INGOT)
            .unlockedBy("has_hot_steel_ingot", has(ModItems.HOT_STEEL_INGOT)).save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.HOT_STEEL_LEGGINGS)
            .pattern("III").pattern("I I").pattern("I I")
            .define('I', ModItems.HOT_STEEL_INGOT)
            .unlockedBy("has_hot_steel_ingot", has(ModItems.HOT_STEEL_INGOT)).save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.HOT_STEEL_BOOTS)
            .pattern("I I").pattern("I I")
            .define('I', ModItems.HOT_STEEL_INGOT)
            .unlockedBy("has_hot_steel_ingot", has(ModItems.HOT_STEEL_INGOT)).save(exporter);

        // Tools & weapons
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.HOT_STEEL_SWORD)
            .pattern("I").pattern("I").pattern("S")
            .define('I', ModItems.HOT_STEEL_INGOT).define('S', Items.STICK)
            .unlockedBy("has_hot_steel_ingot", has(ModItems.HOT_STEEL_INGOT)).save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.HOT_STEEL_KNIFE)
            .pattern("I").pattern("S")
            .define('I', ModItems.HOT_STEEL_INGOT).define('S', Items.STICK)
            .unlockedBy("has_hot_steel_ingot", has(ModItems.HOT_STEEL_INGOT)).save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.HOT_STEEL_PICKAXE)
            .pattern("III").pattern(" S ").pattern(" S ")
            .define('I', ModItems.HOT_STEEL_INGOT).define('S', Items.STICK)
            .unlockedBy("has_hot_steel_ingot", has(ModItems.HOT_STEEL_INGOT)).save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.HOT_STEEL_AXE)
            .pattern("II").pattern("IS").pattern(" S")
            .define('I', ModItems.HOT_STEEL_INGOT).define('S', Items.STICK)
            .unlockedBy("has_hot_steel_ingot", has(ModItems.HOT_STEEL_INGOT)).save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.HOT_STEEL_SHOVEL)
            .pattern("I").pattern("S").pattern("S")
            .define('I', ModItems.HOT_STEEL_INGOT).define('S', Items.STICK)
            .unlockedBy("has_hot_steel_ingot", has(ModItems.HOT_STEEL_INGOT)).save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.HOT_STEEL_HOE)
            .pattern("II").pattern(" S").pattern(" S")
            .define('I', ModItems.HOT_STEEL_INGOT).define('S', Items.STICK)
            .unlockedBy("has_hot_steel_ingot", has(ModItems.HOT_STEEL_INGOT)).save(exporter);

        // Ranged / special
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.HOT_STEEL_BOW)
            .pattern(" IR").pattern("I R").pattern(" IR")
            .define('I', ModItems.HOT_STEEL_INGOT).define('R', Items.STRING)
            .unlockedBy("has_hot_steel_ingot", has(ModItems.HOT_STEEL_INGOT)).save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.HOT_STEEL_CROSSBOW)
            .pattern("ITI").pattern("RSR").pattern(" I ")
            .define('I', ModItems.HOT_STEEL_INGOT).define('S', Items.TRIPWIRE_HOOK)
            .define('T', Items.STICK).define('R', Items.STRING)
            .unlockedBy("has_hot_steel_ingot", has(ModItems.HOT_STEEL_INGOT)).save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.HOT_STEEL_TRIDENT)
            .pattern("III").pattern(" I ").pattern(" I ")
            .define('I', ModItems.HOT_STEEL_INGOT)
            .unlockedBy("has_hot_steel_ingot", has(ModItems.HOT_STEEL_INGOT)).save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.HOT_STEEL_SHIELD)
            .pattern("I I").pattern("III").pattern(" I ")
            .define('I', ModItems.HOT_STEEL_INGOT)
            .unlockedBy("has_hot_steel_ingot", has(ModItems.HOT_STEEL_INGOT)).save(exporter);
    }
}
