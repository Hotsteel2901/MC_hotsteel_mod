package com.hotsteel.registry;

import com.hotsteel.HotSteel;
import com.hotsteel.item.HotSteelBowItem;
import com.hotsteel.item.HotSteelCrossbowItem;
import com.hotsteel.item.HotSteelShieldItem;
import com.hotsteel.item.HotSteelTridentItem;
import com.hotsteel.item.KnifeItem;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;

public final class ModItems {

    private ModItems() {}

    // ---- Materials ----
    public static final Item CRUDE_STEEL = register("crude_steel",
        new Item(new Item.Properties()));

    public static final Item STEEL_INGOT = register("steel_ingot",
        new Item(new Item.Properties().fireResistant()));

    public static final Item HOT_STEEL_INGOT = register("hot_steel_ingot",
        new Item(new Item.Properties().fireResistant()));

    // ---- Armor ----
    public static final Item HOT_STEEL_HELMET = register("hot_steel_helmet",
        new ArmorItem(ModMaterials.HOT_STEEL_ARMOR, ArmorItem.Type.HELMET,
            new Item.Properties().fireResistant()
                .durability(ArmorItem.Type.HELMET.getDurability(ModMaterials.ARMOR_DURABILITY_MULT))));

    public static final Item HOT_STEEL_CHESTPLATE = register("hot_steel_chestplate",
        new ArmorItem(ModMaterials.HOT_STEEL_ARMOR, ArmorItem.Type.CHESTPLATE,
            new Item.Properties().fireResistant()
                .durability(ArmorItem.Type.CHESTPLATE.getDurability(ModMaterials.ARMOR_DURABILITY_MULT))));

    public static final Item HOT_STEEL_LEGGINGS = register("hot_steel_leggings",
        new ArmorItem(ModMaterials.HOT_STEEL_ARMOR, ArmorItem.Type.LEGGINGS,
            new Item.Properties().fireResistant()
                .durability(ArmorItem.Type.LEGGINGS.getDurability(ModMaterials.ARMOR_DURABILITY_MULT))));

    public static final Item HOT_STEEL_BOOTS = register("hot_steel_boots",
        new ArmorItem(ModMaterials.HOT_STEEL_ARMOR, ArmorItem.Type.BOOTS,
            new Item.Properties().fireResistant()
                .durability(ArmorItem.Type.BOOTS.getDurability(ModMaterials.ARMOR_DURABILITY_MULT))));

    // ---- Tools ----
    public static final Item HOT_STEEL_SWORD = register("hot_steel_sword",
        new SwordItem(ModMaterials.HotSteelTier.INSTANCE, new Item.Properties().fireResistant()
            .attributes(SwordItem.createAttributes(ModMaterials.HotSteelTier.INSTANCE, 4, -2.4f))));

    public static final Item HOT_STEEL_PICKAXE = register("hot_steel_pickaxe",
        new PickaxeItem(ModMaterials.HotSteelTier.INSTANCE, new Item.Properties().fireResistant()
            .attributes(PickaxeItem.createAttributes(ModMaterials.HotSteelTier.INSTANCE, 1.0f, -2.8f))));

    public static final Item HOT_STEEL_AXE = register("hot_steel_axe",
        new AxeItem(ModMaterials.HotSteelTier.INSTANCE, new Item.Properties().fireResistant()
            .attributes(AxeItem.createAttributes(ModMaterials.HotSteelTier.INSTANCE, 6.0f, -3.0f))));

    public static final Item HOT_STEEL_SHOVEL = register("hot_steel_shovel",
        new ShovelItem(ModMaterials.HotSteelTier.INSTANCE, new Item.Properties().fireResistant()
            .attributes(ShovelItem.createAttributes(ModMaterials.HotSteelTier.INSTANCE, 1.5f, -3.0f))));

    public static final Item HOT_STEEL_HOE = register("hot_steel_hoe",
        new HoeItem(ModMaterials.HotSteelTier.INSTANCE, new Item.Properties().fireResistant()
            .attributes(HoeItem.createAttributes(ModMaterials.HotSteelTier.INSTANCE, -4.0f, 0.0f))));

    // ---- New weapon: knife (light & fast) ----
    public static final Item HOT_STEEL_KNIFE = register("hot_steel_knife",
        new KnifeItem(ModMaterials.HotSteelTier.INSTANCE, new Item.Properties().fireResistant()
            .attributes(SwordItem.createAttributes(ModMaterials.HotSteelTier.INSTANCE, 1, -2.0f))));

    // ---- Ranged / special ----
    public static final Item HOT_STEEL_BOW = register("hot_steel_bow",
        new HotSteelBowItem(new Item.Properties().fireResistant().durability(1000)));

    public static final Item HOT_STEEL_CROSSBOW = register("hot_steel_crossbow",
        new HotSteelCrossbowItem(new Item.Properties().fireResistant().durability(1200)));

    public static final Item HOT_STEEL_TRIDENT = register("hot_steel_trident",
        new HotSteelTridentItem(new Item.Properties().fireResistant().durability(500)
            .attributes(HotSteelTridentItem.createAttributes())));

    public static final Item HOT_STEEL_SHIELD = register("hot_steel_shield",
        new HotSteelShieldItem(new Item.Properties().fireResistant().durability(500)));

    private static Item register(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, HotSteel.id(name), item);
    }

    public static void register() {
        HotSteel.LOGGER.info("Registering Hot Steel items");
    }
}
