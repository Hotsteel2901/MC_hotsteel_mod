package com.hotsteel.registry;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.hotsteel.HotSteel;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

public final class ModMaterials {

    private ModMaterials() {}

    /** Tool tier: stronger than netherite. */
    public enum HotSteelTier implements Tier {
        INSTANCE;

        @Override public int getUses() { return 2800; }
        @Override public float getSpeed() { return 15.0f; }
        @Override public float getAttackDamageBonus() { return 5.0f; }
        @Override public TagKey<Block> getIncorrectBlocksForDrops() { return BlockTags.INCORRECT_FOR_NETHERITE_TOOL; }
        @Override public int getEnchantmentValue() { return 18; }
        @Override public Ingredient getRepairIngredient() { return Ingredient.of(ModItems.HOT_STEEL_INGOT); }
    }

    /** Armor durability multiplier (netherite is 37). */
    public static final int ARMOR_DURABILITY_MULT = 45;

    public static final Holder<ArmorMaterial> HOT_STEEL_ARMOR = register("hot_steel",
        Util.make(new EnumMap<>(ArmorItem.Type.class), m -> {
            m.put(ArmorItem.Type.BOOTS, 4);
            m.put(ArmorItem.Type.LEGGINGS, 7);
            m.put(ArmorItem.Type.CHESTPLATE, 9);
            m.put(ArmorItem.Type.HELMET, 4);
        }),
        18,
        SoundEvents.ARMOR_EQUIP_NETHERITE,
        4.0f,
        0.15f,
        () -> Ingredient.of(ModItems.HOT_STEEL_INGOT));

    private static Holder<ArmorMaterial> register(String name,
                                                  Map<ArmorItem.Type, Integer> defense,
                                                  int enchantmentValue,
                                                  Holder<SoundEvent> equipSound,
                                                  float toughness,
                                                  float knockbackResistance,
                                                  Supplier<Ingredient> repairIngredient) {
        ResourceLocation loc = HotSteel.id(name);
        List<ArmorMaterial.Layer> layers = List.of(new ArmorMaterial.Layer(loc));
        ArmorMaterial material = new ArmorMaterial(defense, enchantmentValue, equipSound,
            repairIngredient, layers, toughness, knockbackResistance);
        return Registry.registerForHolder(BuiltInRegistries.ARMOR_MATERIAL, loc, material);
    }

    public static void init() {
        HotSteel.LOGGER.info("Registering Hot Steel materials");
    }
}
