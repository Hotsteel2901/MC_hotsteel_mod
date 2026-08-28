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
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
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

        // ---- Storage blocks ----
        // 9 steel ingot <-> steel block
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.STEEL_BLOCK)
            .pattern("SSS").pattern("SSS").pattern("SSS")
            .define('S', ModItems.STEEL_INGOT)
            .unlockedBy("has_steel_ingot", has(ModItems.STEEL_INGOT)).save(exporter);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.STEEL_INGOT, 9)
            .requires(ModBlocks.STEEL_BLOCK)
            .unlockedBy("has_steel_block", has(ModBlocks.STEEL_BLOCK)).save(exporter);

        // 9 hot steel ingot <-> hot steel block
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.HOT_STEEL_BLOCK)
            .pattern("III").pattern("III").pattern("III")
            .define('I', ModItems.HOT_STEEL_INGOT)
            .unlockedBy("has_hot_steel_ingot", has(ModItems.HOT_STEEL_INGOT)).save(exporter);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.HOT_STEEL_INGOT, 9)
            .requires(ModBlocks.HOT_STEEL_BLOCK)
            .unlockedBy("has_hot_steel_block", has(ModBlocks.HOT_STEEL_BLOCK)).save(exporter);

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
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.HOT_STEEL_MACE)
            .pattern(" I ").pattern("III").pattern(" I ")
            .define('I', ModItems.HOT_STEEL_INGOT)
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

        // 4 arrows + 1 hot steel ingot -> 4 hot steel arrows
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, ModItems.HOT_STEEL_ARROW, 4)
            .requires(Items.ARROW, 4)
            .requires(ModItems.HOT_STEEL_INGOT)
            .unlockedBy("has_hot_steel_ingot", has(ModItems.HOT_STEEL_INGOT)).save(exporter);

        // ---- Hot steel decorative blocks ----
        // 6 hot steel blocks -> 4 stairs
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.HOT_STEEL_STAIRS, 4)
            .pattern("B  ").pattern("BB ").pattern("BBB")
            .define('B', ModBlocks.HOT_STEEL_BLOCK)
            .unlockedBy("has_hot_steel_block", has(ModBlocks.HOT_STEEL_BLOCK)).save(exporter);
        // 3 hot steel blocks -> 6 slabs
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.HOT_STEEL_SLAB, 6)
            .pattern("BBB")
            .define('B', ModBlocks.HOT_STEEL_BLOCK)
            .unlockedBy("has_hot_steel_block", has(ModBlocks.HOT_STEEL_BLOCK)).save(exporter);
        // 6 hot steel blocks -> 6 walls
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.HOT_STEEL_WALL, 6)
            .pattern("BBB").pattern("BBB")
            .define('B', ModBlocks.HOT_STEEL_BLOCK)
            .unlockedBy("has_hot_steel_block", has(ModBlocks.HOT_STEEL_BLOCK)).save(exporter);

        // ---- Hot steel forge ----
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.HOT_STEEL_FORGE)
            .pattern("III").pattern("ILI").pattern("III")
            .define('I', ModItems.HOT_STEEL_INGOT).define('L', Items.LAVA_BUCKET)
            .unlockedBy("has_hot_steel_ingot", has(ModItems.HOT_STEEL_INGOT)).save(exporter);

        // ---- Lava bottle ----
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.LAVA_BOTTLE, 3)
            .requires(Items.GLASS_BOTTLE)
            .requires(Items.LAVA_BUCKET)
            .unlockedBy("has_lava_bucket", has(Items.LAVA_BUCKET)).save(exporter);

        // ---- Hot steel nuggets <-> ingot ----
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.HOT_STEEL_INGOT)
            .pattern("NNN").pattern("NNN").pattern("NNN")
            .define('N', ModItems.HOT_STEEL_NUGGET)
            .unlockedBy("has_hot_steel_nugget", has(ModItems.HOT_STEEL_NUGGET))
            .save(exporter, "hot_steel_ingot_from_nugget");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.HOT_STEEL_NUGGET, 9)
            .requires(ModItems.HOT_STEEL_INGOT)
            .unlockedBy("has_hot_steel_ingot", has(ModItems.HOT_STEEL_INGOT)).save(exporter);

        // ---- Hot steel fishing rod ----
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.HOT_STEEL_FISHING_ROD)
            .pattern("  I").pattern(" IS").pattern("I S")
            .define('I', ModItems.HOT_STEEL_INGOT).define('S', Items.STRING)
            .unlockedBy("has_hot_steel_ingot", has(ModItems.HOT_STEEL_INGOT)).save(exporter);

        // ---- Hot steel sickle ----
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.HOT_STEEL_SICKLE)
            .pattern(" II").pattern("I  ").pattern(" S ")
            .define('I', ModItems.HOT_STEEL_INGOT).define('S', Items.STICK)
            .unlockedBy("has_hot_steel_ingot", has(ModItems.HOT_STEEL_INGOT)).save(exporter);

        // ---- Hot steel bricks ----
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.HOT_STEEL_BRICKS, 4)
            .pattern("BB").pattern("BB")
            .define('B', ModBlocks.HOT_STEEL_BLOCK)
            .unlockedBy("has_hot_steel_block", has(ModBlocks.HOT_STEEL_BLOCK)).save(exporter);

        // ---- Hot steel door / trapdoor / fence ----
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.HOT_STEEL_DOOR, 3)
            .pattern("BB").pattern("BB").pattern("BB")
            .define('B', ModBlocks.HOT_STEEL_BLOCK)
            .unlockedBy("has_hot_steel_block", has(ModBlocks.HOT_STEEL_BLOCK)).save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.HOT_STEEL_TRAPDOOR, 2)
            .pattern("BB").pattern("BB")
            .define('B', ModBlocks.HOT_STEEL_BLOCK)
            .unlockedBy("has_hot_steel_block", has(ModBlocks.HOT_STEEL_BLOCK)).save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.HOT_STEEL_FENCE, 3)
            .pattern("B B").pattern("B B")
            .define('B', ModBlocks.HOT_STEEL_BLOCK)
            .unlockedBy("has_hot_steel_block", has(ModBlocks.HOT_STEEL_BLOCK)).save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.HOT_STEEL_PRESSURE_PLATE)
            .pattern("BB")
            .define('B', ModBlocks.HOT_STEEL_BLOCK)
            .unlockedBy("has_hot_steel_block", has(ModBlocks.HOT_STEEL_BLOCK)).save(exporter);

        // ---- Hot steel smelter ----
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.HOT_STEEL_SMELTER)
            .pattern("HHH").pattern("HIH").pattern("HHH")
            .define('H', ModBlocks.HOT_STEEL_BLOCK).define('I', Items.IRON_INGOT)
            .unlockedBy("has_hot_steel_block", has(ModBlocks.HOT_STEEL_BLOCK)).save(exporter);

        // ---- Molten core can be smelted back into 2 hot steel ingots ----
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.HOT_STEEL_INGOT, 2)
            .requires(ModItems.MOLTEN_CORE)
            .unlockedBy("has_molten_core", has(ModItems.MOLTEN_CORE)).save(exporter, "molten_core_to_ingots");

        // ---- Hot steel paxel (pickaxe + axe + shovel) ----
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.HOT_STEEL_PAXEL)
            .pattern("IPI").pattern("SAS").pattern(" S ")
            .define('I', ModItems.HOT_STEEL_INGOT)
            .define('P', ModItems.HOT_STEEL_PICKAXE)
            .define('A', ModItems.HOT_STEEL_AXE)
            .define('S', Items.STICK)
            .unlockedBy("has_hot_steel_pickaxe", has(ModItems.HOT_STEEL_PICKAXE)).save(exporter);

        // ---- Hot steel apple ----
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, ModItems.HOT_STEEL_APPLE)
            .pattern("III").pattern("IAI").pattern("III")
            .define('I', ModItems.HOT_STEEL_INGOT).define('A', Items.APPLE)
            .unlockedBy("has_hot_steel_ingot", has(ModItems.HOT_STEEL_INGOT)).save(exporter);

        // ---- Hot steel chain ----
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.HOT_STEEL_CHAIN, 6)
            .pattern("N N").pattern("N N").pattern("N N")
            .define('N', ModItems.HOT_STEEL_NUGGET)
            .unlockedBy("has_hot_steel_nugget", has(ModItems.HOT_STEEL_NUGGET)).save(exporter);

        // ---- Hot steel ladder ----
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.HOT_STEEL_LADDER, 4)
            .pattern("I I").pattern("III").pattern("I I")
            .define('I', ModItems.HOT_STEEL_INGOT)
            .unlockedBy("has_hot_steel_ingot", has(ModItems.HOT_STEEL_INGOT)).save(exporter);
    }
}
