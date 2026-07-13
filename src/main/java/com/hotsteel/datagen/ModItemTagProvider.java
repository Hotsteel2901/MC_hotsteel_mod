package com.hotsteel.datagen;

import java.util.concurrent.CompletableFuture;

import com.hotsteel.registry.ModItems;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;

public class ModItemTagProvider extends FabricTagProvider.ItemTagProvider {

    public ModItemTagProvider(FabricDataOutput output,
                              CompletableFuture<HolderLookup.Provider> registries,
                              FabricTagProvider.BlockTagProvider blockTags) {
        super(output, registries, blockTags);
    }

    private static ResourceKey<Item> k(Item item) {
        return BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow();
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        // Tool type tags
        this.tag(ItemTags.SWORDS).add(k(ModItems.HOT_STEEL_SWORD)).add(k(ModItems.HOT_STEEL_KNIFE));
        this.tag(ItemTags.PICKAXES).add(k(ModItems.HOT_STEEL_PICKAXE));
        this.tag(ItemTags.AXES).add(k(ModItems.HOT_STEEL_AXE));
        this.tag(ItemTags.SHOVELS).add(k(ModItems.HOT_STEEL_SHOVEL));
        this.tag(ItemTags.HOES).add(k(ModItems.HOT_STEEL_HOE));

        // Melee enchantability
        this.tag(ItemTags.SWORD_ENCHANTABLE).add(k(ModItems.HOT_STEEL_SWORD)).add(k(ModItems.HOT_STEEL_KNIFE));
        this.tag(ItemTags.FIRE_ASPECT_ENCHANTABLE).add(k(ModItems.HOT_STEEL_SWORD)).add(k(ModItems.HOT_STEEL_KNIFE));
        this.tag(ItemTags.SHARP_WEAPON_ENCHANTABLE)
            .add(k(ModItems.HOT_STEEL_SWORD)).add(k(ModItems.HOT_STEEL_KNIFE)).add(k(ModItems.HOT_STEEL_AXE));
        this.tag(ItemTags.WEAPON_ENCHANTABLE)
            .add(k(ModItems.HOT_STEEL_SWORD)).add(k(ModItems.HOT_STEEL_KNIFE)).add(k(ModItems.HOT_STEEL_AXE));

        // Mining enchantability
        this.tag(ItemTags.MINING_ENCHANTABLE)
            .add(k(ModItems.HOT_STEEL_PICKAXE)).add(k(ModItems.HOT_STEEL_AXE))
            .add(k(ModItems.HOT_STEEL_SHOVEL)).add(k(ModItems.HOT_STEEL_HOE));
        this.tag(ItemTags.MINING_LOOT_ENCHANTABLE)
            .add(k(ModItems.HOT_STEEL_PICKAXE)).add(k(ModItems.HOT_STEEL_AXE))
            .add(k(ModItems.HOT_STEEL_SHOVEL)).add(k(ModItems.HOT_STEEL_HOE));

        // Ranged / trident
        this.tag(ItemTags.BOW_ENCHANTABLE).add(k(ModItems.HOT_STEEL_BOW));
        this.tag(ItemTags.CROSSBOW_ENCHANTABLE).add(k(ModItems.HOT_STEEL_CROSSBOW));
        this.tag(ItemTags.TRIDENT_ENCHANTABLE).add(k(ModItems.HOT_STEEL_TRIDENT));

        // Armor
        this.tag(ItemTags.ARMOR_ENCHANTABLE)
            .add(k(ModItems.HOT_STEEL_HELMET)).add(k(ModItems.HOT_STEEL_CHESTPLATE))
            .add(k(ModItems.HOT_STEEL_LEGGINGS)).add(k(ModItems.HOT_STEEL_BOOTS));
        this.tag(ItemTags.EQUIPPABLE_ENCHANTABLE)
            .add(k(ModItems.HOT_STEEL_HELMET)).add(k(ModItems.HOT_STEEL_CHESTPLATE))
            .add(k(ModItems.HOT_STEEL_LEGGINGS)).add(k(ModItems.HOT_STEEL_BOOTS));
        this.tag(ItemTags.HEAD_ARMOR_ENCHANTABLE).add(k(ModItems.HOT_STEEL_HELMET));
        this.tag(ItemTags.CHEST_ARMOR_ENCHANTABLE).add(k(ModItems.HOT_STEEL_CHESTPLATE));
        this.tag(ItemTags.LEG_ARMOR_ENCHANTABLE).add(k(ModItems.HOT_STEEL_LEGGINGS));
        this.tag(ItemTags.FOOT_ARMOR_ENCHANTABLE).add(k(ModItems.HOT_STEEL_BOOTS));

        // Durability + vanishing for everything
        this.tag(ItemTags.DURABILITY_ENCHANTABLE)
            .add(k(ModItems.HOT_STEEL_HELMET)).add(k(ModItems.HOT_STEEL_CHESTPLATE))
            .add(k(ModItems.HOT_STEEL_LEGGINGS)).add(k(ModItems.HOT_STEEL_BOOTS))
            .add(k(ModItems.HOT_STEEL_SWORD)).add(k(ModItems.HOT_STEEL_KNIFE)).add(k(ModItems.HOT_STEEL_PICKAXE))
            .add(k(ModItems.HOT_STEEL_AXE)).add(k(ModItems.HOT_STEEL_SHOVEL)).add(k(ModItems.HOT_STEEL_HOE))
            .add(k(ModItems.HOT_STEEL_BOW)).add(k(ModItems.HOT_STEEL_CROSSBOW))
            .add(k(ModItems.HOT_STEEL_TRIDENT)).add(k(ModItems.HOT_STEEL_SHIELD));
        this.tag(ItemTags.VANISHING_ENCHANTABLE)
            .add(k(ModItems.HOT_STEEL_HELMET)).add(k(ModItems.HOT_STEEL_CHESTPLATE))
            .add(k(ModItems.HOT_STEEL_LEGGINGS)).add(k(ModItems.HOT_STEEL_BOOTS))
            .add(k(ModItems.HOT_STEEL_SWORD)).add(k(ModItems.HOT_STEEL_KNIFE)).add(k(ModItems.HOT_STEEL_PICKAXE))
            .add(k(ModItems.HOT_STEEL_AXE)).add(k(ModItems.HOT_STEEL_SHOVEL)).add(k(ModItems.HOT_STEEL_HOE))
            .add(k(ModItems.HOT_STEEL_BOW)).add(k(ModItems.HOT_STEEL_CROSSBOW))
            .add(k(ModItems.HOT_STEEL_TRIDENT)).add(k(ModItems.HOT_STEEL_SHIELD));
    }
}
