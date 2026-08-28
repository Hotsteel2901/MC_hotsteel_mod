package com.hotsteel.logic;

import java.util.Map;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Auto-smelt mapping for the Hot Steel pickaxe. Kept OUT of the Block mixin so
 * that no {@code Items}/{@code Blocks} reference sits inside {@code Block}'s
 * static initializer (a mixin {@code @Unique} static field there caused a
 * circular class-initialization crash in vanilla {@code FireBlock}). This class
 * is only touched lazily at runtime, when a block is actually mined.
 */
public final class AutoSmeltHelper {

    private AutoSmeltHelper() {}

    /** Maps a mined drop item to its smelted (ingot) form. */
    public static final Map<Item, Item> SMELT_MAP = Map.ofEntries(
        Map.entry(Items.RAW_IRON, Items.IRON_INGOT),
        Map.entry(Items.RAW_GOLD, Items.GOLD_INGOT),
        Map.entry(Items.RAW_COPPER, Items.COPPER_INGOT),
        Map.entry(Items.IRON_ORE, Items.IRON_INGOT),
        Map.entry(Items.DEEPSLATE_IRON_ORE, Items.IRON_INGOT),
        Map.entry(Items.GOLD_ORE, Items.GOLD_INGOT),
        Map.entry(Items.DEEPSLATE_GOLD_ORE, Items.GOLD_INGOT),
        Map.entry(Items.COPPER_ORE, Items.COPPER_INGOT),
        Map.entry(Items.DEEPSLATE_COPPER_ORE, Items.COPPER_INGOT),
        Map.entry(Items.NETHER_GOLD_ORE, Items.GOLD_INGOT),
        Map.entry(Items.NETHER_QUARTZ_ORE, Items.QUARTZ),
        Map.entry(Items.ANCIENT_DEBRIS, Items.NETHERITE_SCRAP),
        Map.entry(Items.RAW_IRON_BLOCK, Items.IRON_BLOCK),
        Map.entry(Items.RAW_GOLD_BLOCK, Items.GOLD_BLOCK),
        Map.entry(Items.RAW_COPPER_BLOCK, Items.COPPER_BLOCK)
    );
}
