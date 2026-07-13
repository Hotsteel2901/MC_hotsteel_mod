package com.hotsteel.datagen;

import java.util.concurrent.CompletableFuture;

import com.hotsteel.registry.ModBlocks;
import com.hotsteel.registry.ModCreativeTab;
import com.hotsteel.registry.ModEffects;
import com.hotsteel.registry.ModEntities;
import com.hotsteel.registry.ModItems;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

public class ModEnglishLangProvider extends FabricLanguageProvider {

    public ModEnglishLangProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, "en_us", registries);
    }

    @Override
    public void generateTranslations(HolderLookup.Provider registries, TranslationBuilder tb) {
        tb.add(ModItems.CRUDE_STEEL, "Crude Steel");
        tb.add(ModItems.STEEL_INGOT, "Steel Ingot");
        tb.add(ModItems.HOT_STEEL_INGOT, "Hot Steel Ingot");
        tb.add(ModBlocks.CRUDE_STEEL_BLOCK, "Block of Crude Steel");

        tb.add(ModItems.HOT_STEEL_HELMET, "Hot Steel Helmet");
        tb.add(ModItems.HOT_STEEL_CHESTPLATE, "Hot Steel Chestplate");
        tb.add(ModItems.HOT_STEEL_LEGGINGS, "Hot Steel Leggings");
        tb.add(ModItems.HOT_STEEL_BOOTS, "Hot Steel Boots");

        tb.add(ModItems.HOT_STEEL_SWORD, "Hot Steel Sword");
        tb.add(ModItems.HOT_STEEL_KNIFE, "Hot Steel Knife");
        tb.add(ModItems.HOT_STEEL_PICKAXE, "Hot Steel Pickaxe");
        tb.add(ModItems.HOT_STEEL_AXE, "Hot Steel Axe");
        tb.add(ModItems.HOT_STEEL_SHOVEL, "Hot Steel Shovel");
        tb.add(ModItems.HOT_STEEL_HOE, "Hot Steel Hoe");

        tb.add(ModItems.HOT_STEEL_BOW, "Hot Steel Bow");
        tb.add(ModItems.HOT_STEEL_CROSSBOW, "Hot Steel Crossbow");
        tb.add(ModItems.HOT_STEEL_TRIDENT, "Hot Steel Trident");
        tb.add(ModItems.HOT_STEEL_SHIELD, "Hot Steel Shield");

        tb.add(ModEntities.HOT_STEEL_TRIDENT, "Hot Steel Trident");
        tb.add(ModEffects.SUPER_FIRE_RESISTANCE.value(), "Super Fire Resistance");
        tb.add(ModCreativeTab.HOT_STEEL_TAB_KEY, "Hot Steel");

        // Advancements (kept punchy & a little tongue-in-cheek)
        tb.add("advancements.hotsteel.crude_steel.title", "Is This... Steel?");
        tb.add("advancements.hotsteel.crude_steel.description", "Smelt your very first Crude Steel. Looks a bit rough, honestly.");
        tb.add("advancements.hotsteel.steel_ingot.title", "Ooh, a Steel Ingot!");
        tb.add("advancements.hotsteel.steel_ingot.description", "Get your first Steel Ingot. Shiny, fireproof, and totally useless... for now.");
        tb.add("advancements.hotsteel.hot_steel_ingot.title", "?! Hot Hot ?!");
        tb.add("advancements.hotsteel.hot_steel_ingot.description", "Get your first Hot Steel Ingot. Yeow \u2014 do NOT grab that bare-handed.");
        tb.add("advancements.hotsteel.full_armor.title", "Burn, Blazing Steel!");
        tb.add("advancements.hotsteel.full_armor.description", "Suit up in a full set of Hot Steel armor. Lava? Never heard of her.");
        tb.add("advancements.hotsteel.hot_steel_hoe.title", "The Ultimate Ultimate Dedication");
        tb.add("advancements.hotsteel.hot_steel_hoe.description", "Craft a Hot Steel Hoe. The mod's best material... on a hoe. Respect.");

        // Chat messages
        tb.add("message.hotsteel.super_fire_on", "Super Fire Resistance ONLINE!");
        tb.add("message.hotsteel.super_fire_off", "Super Fire Resistance OFFLINE!");
    }
}
