package com.hotsteel.registry;

import com.hotsteel.HotSteel;
import com.hotsteel.item.HotSteelArrowItem;
import com.hotsteel.item.HotSteelAxeItem;
import com.hotsteel.item.HotSteelBowItem;
import com.hotsteel.item.HotSteelCrossbowItem;
import com.hotsteel.item.HotSteelFishingRodItem;
import com.hotsteel.item.HotSteelFireballItem;
import com.hotsteel.item.HotSteelHoeItem;
import com.hotsteel.item.HotSteelAppleItem;
import com.hotsteel.item.HotSteelMaceItem;
import com.hotsteel.item.HotSteelPaxelItem;
import com.hotsteel.item.HotSteelShieldItem;
import com.hotsteel.item.HotSteelShovelItem;
import com.hotsteel.item.HotSteelSickleItem;
import com.hotsteel.item.HotSteelSwordItem;
import com.hotsteel.item.HotSteelTridentItem;
import com.hotsteel.item.KnifeItem;
import com.hotsteel.item.LavaBottleItem;
import com.hotsteel.item.MoltenCoreItem;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.DispenserBlock;

public final class ModItems {

    private ModItems() {}

    // ---- Materials ----
    public static final Item CRUDE_STEEL = register("crude_steel",
        new Item(new Item.Properties()));

    public static final Item STEEL_INGOT = register("steel_ingot",
        new Item(new Item.Properties().fireResistant()));

    public static final Item HOT_STEEL_INGOT = register("hot_steel_ingot",
        new Item(new Item.Properties().fireResistant()));

    /** Compact form of a hot steel ingot (9 = 1 ingot). */
    public static final Item HOT_STEEL_NUGGET = register("hot_steel_nugget",
        new Item(new Item.Properties().fireResistant()));

    /** Rare molten heart dropped by Fire Wraiths — right-click to instantly
     *  smelt everything in your inventory. */
    public static final Item MOLTEN_CORE = register("molten_core",
        new MoltenCoreItem(new Item.Properties().fireResistant()));

    /** Paxel: pickaxe + axe + shovel merged into one auto-smelting tool. */
    public static final Item HOT_STEEL_PAXEL = register("hot_steel_paxel",
        new HotSteelPaxelItem(ModMaterials.HotSteelTier.INSTANCE, new Item.Properties().fireResistant()
            .attributes(PickaxeItem.createAttributes(ModMaterials.HotSteelTier.INSTANCE, 3.0f, -2.8f))));

    /** Hot Steel Apple: molten food that grants fire immunity + regeneration. */
    public static final Item HOT_STEEL_APPLE = register("hot_steel_apple",
        new HotSteelAppleItem(new Item.Properties().fireResistant()
            .food(new net.minecraft.world.food.FoodProperties.Builder()
                .nutrition(8)
                .saturationModifier(1.2f)
                .alwaysEdible()
                .build())));

    /** Fishing rod that auto-cooks any fish it catches. */
    public static final Item HOT_STEEL_FISHING_ROD = register("hot_steel_fishing_rod",
        new HotSteelFishingRodItem(new Item.Properties().durability(600)));

    /** Sickle: harvests a 5x5 area of mature crops and replants them. */
    public static final Item HOT_STEEL_SICKLE = register("hot_steel_sickle",
        new HotSteelSickleItem(ModMaterials.HotSteelTier.INSTANCE, new Item.Properties().fireResistant()
            .attributes(SwordItem.createAttributes(ModMaterials.HotSteelTier.INSTANCE, 2, -2.6f))));

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
        new HotSteelSwordItem(ModMaterials.HotSteelTier.INSTANCE, new Item.Properties().fireResistant()
            .attributes(SwordItem.createAttributes(ModMaterials.HotSteelTier.INSTANCE, 4, -2.4f))));

    public static final Item HOT_STEEL_PICKAXE = register("hot_steel_pickaxe",
        new PickaxeItem(ModMaterials.HotSteelTier.INSTANCE, new Item.Properties().fireResistant()
            .attributes(PickaxeItem.createAttributes(ModMaterials.HotSteelTier.INSTANCE, 1.0f, -2.8f))));

    public static final Item HOT_STEEL_AXE = register("hot_steel_axe",
        new HotSteelAxeItem(ModMaterials.HotSteelTier.INSTANCE, new Item.Properties().fireResistant()
            .attributes(AxeItem.createAttributes(ModMaterials.HotSteelTier.INSTANCE, 6.0f, -3.0f))));

    public static final Item HOT_STEEL_SHOVEL = register("hot_steel_shovel",
        new HotSteelShovelItem(ModMaterials.HotSteelTier.INSTANCE, new Item.Properties().fireResistant()
            .attributes(ShovelItem.createAttributes(ModMaterials.HotSteelTier.INSTANCE, 1.5f, -3.0f))));

    public static final Item HOT_STEEL_HOE = register("hot_steel_hoe",
        new HotSteelHoeItem(ModMaterials.HotSteelTier.INSTANCE, new Item.Properties().fireResistant()
            .attributes(HoeItem.createAttributes(ModMaterials.HotSteelTier.INSTANCE, -4.0f, 0.0f))));

    // ---- New weapon: knife (light & fast) ----
    public static final Item HOT_STEEL_KNIFE = register("hot_steel_knife",
        new KnifeItem(ModMaterials.HotSteelTier.INSTANCE, new Item.Properties().fireResistant()
            .attributes(SwordItem.createAttributes(ModMaterials.HotSteelTier.INSTANCE, 1, -2.0f))));

    // ---- New weapon: mace (heavy smash) ----
    public static final Item HOT_STEEL_MACE = register("hot_steel_mace",
        new HotSteelMaceItem(new Item.Properties().fireResistant()
            .attributes(HotSteelMaceItem.createAttributes())));

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

    // ---- New ammunition: igniting arrows ----
    public static final Item HOT_STEEL_ARROW = register("hot_steel_arrow",
        new HotSteelArrowItem(new Item.Properties().fireResistant()));

    // ---- New throwable: fireball ----
    public static final Item HOT_STEEL_FIREBALL = register("hot_steel_fireball",
        new HotSteelFireballItem(new Item.Properties().fireResistant()));

    // ---- New throwable: lava bottle ----
    public static final Item LAVA_BOTTLE = register("lava_bottle",
        new LavaBottleItem(new Item.Properties()));

    // ---- Spawn egg: lava golem ----
    public static final Item LAVA_GOLEM_SPAWN_EGG = register("lava_golem_spawn_egg",
        new SpawnEggItem(ModEntities.LAVA_GOLEM, 0x3d1707, 0xe0611f, new Item.Properties()));

    // ---- Spawn egg: fire wraith ----
    public static final Item FIRE_WRAITH_SPAWN_EGG = register("fire_wraith_spawn_egg",
        new SpawnEggItem(ModEntities.FIRE_WRAITH, 0xff5500, 0x2a0505, new Item.Properties()));

    private static Item register(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, HotSteel.id(name), item);
    }

    public static void register() {
        // Let dispensers actually shoot the Hot Steel arrow instead of dropping it.
        DispenserBlock.registerProjectileBehavior(HOT_STEEL_ARROW);
        HotSteel.LOGGER.info("Registering Hot Steel items");
    }
}
