package com.hotsteel.registry;

import com.hotsteel.HotSteel;
import com.hotsteel.content.block.HotSteelForgeBlock;
import com.hotsteel.content.block.HotSteelLanternBlock;
import com.hotsteel.content.block.HotSteelPressurePlateBlock;
import com.hotsteel.content.block.HotSteelSmelterBlock;
import com.hotsteel.content.block.MoltenAltarBlock;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;

public final class ModBlocks {

    private ModBlocks() {}

    public static final Block CRUDE_STEEL_BLOCK = registerBlock("crude_steel_block",
        new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
            .mapColor(MapColor.COLOR_GRAY)
            .strength(5.0f, 6.0f)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()));

    /** Storage block for steel ingots. */
    public static final Block STEEL_BLOCK = registerBlock("steel_block",
        new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
            .mapColor(MapColor.METAL)
            .strength(6.0f, 8.0f)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()));

    /** Storage block for hot steel ingots — near-netherite toughness, beacon-compatible,
     *  glows like an active forge. */
    public static final Block HOT_STEEL_BLOCK = registerBlock("hot_steel_block",
        new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERITE_BLOCK)
            .mapColor(MapColor.FIRE)
            .strength(50.0f, 1200.0f)
            .sound(SoundType.NETHERITE_BLOCK)
            .lightLevel(state -> 15)
            .requiresCorrectToolForDrops()));

    /** Hot steel lantern — hangs like a vanilla lantern and glows (light 15). */
    public static final Block HOT_STEEL_LANTERN = registerBlock("hot_steel_lantern",
        new HotSteelLanternBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LANTERN)
            .mapColor(MapColor.FIRE)
            .strength(5.0f, 8.0f)
            .lightLevel(state -> 15)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()));

    /** Hot steel stairs — decorative glowing stairs. */
    public static final Block HOT_STEEL_STAIRS = registerBlock("hot_steel_stairs",
        new StairBlock(HOT_STEEL_BLOCK.defaultBlockState(),
            BlockBehaviour.Properties.ofFullCopy(HOT_STEEL_BLOCK)
                .lightLevel(state -> 10)));

    /** Hot steel slab — decorative glowing slab. */
    public static final Block HOT_STEEL_SLAB = registerBlock("hot_steel_slab",
        new SlabBlock(BlockBehaviour.Properties.ofFullCopy(HOT_STEEL_BLOCK)
            .lightLevel(state -> 10)));

    /** Hot steel wall — decorative glowing wall. */
    public static final Block HOT_STEEL_WALL = registerBlock("hot_steel_wall",
        new WallBlock(BlockBehaviour.Properties.ofFullCopy(HOT_STEEL_BLOCK)
            .lightLevel(state -> 10)));

    /** Hot steel forge — repairs Hot Steel gear using ingots. */
    public static final Block HOT_STEEL_FORGE = registerBlock("hot_steel_forge",
        new HotSteelForgeBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
            .mapColor(MapColor.FIRE)
            .strength(8.0f, 12.0f)
            .lightLevel(state -> 15)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()));

    /** Hot steel smelter — auto-smelts any ore dropped on top of it. */
    public static final Block HOT_STEEL_SMELTER = registerBlock("hot_steel_smelter",
        new HotSteelSmelterBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
            .mapColor(MapColor.FIRE)
            .strength(8.0f, 12.0f)
            .lightLevel(state -> 15)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()));

    /** Hot steel door — fireproof metal door. */
    public static final Block HOT_STEEL_DOOR = registerBlock("hot_steel_door",
        new DoorBlock(BlockSetType.IRON,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_DOOR)
                .mapColor(MapColor.FIRE)
                .strength(6.0f, 10.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));

    /** Hot steel trapdoor — fireproof metal trapdoor. */
    public static final Block HOT_STEEL_TRAPDOOR = registerBlock("hot_steel_trapdoor",
        new TrapDoorBlock(BlockSetType.IRON,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_TRAPDOOR)
                .mapColor(MapColor.FIRE)
                .strength(6.0f, 10.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));

    /** Hot steel fence — decorative metal fence. */
    public static final Block HOT_STEEL_FENCE = registerBlock("hot_steel_fence",
        new FenceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS)
            .mapColor(MapColor.FIRE)
            .strength(6.0f, 10.0f)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()));

    /** Hot steel bricks — chunky decorative building block. */
    public static final Block HOT_STEEL_BRICKS = registerBlock("hot_steel_bricks",
        new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERITE_BLOCK)
            .mapColor(MapColor.FIRE)
            .strength(12.0f, 200.0f)
            .sound(SoundType.NETHERITE_BLOCK)
            .lightLevel(state -> 10)
            .requiresCorrectToolForDrops()));

    /** Hot steel pressure plate — ignites whatever stands on it. */
    public static final Block HOT_STEEL_PRESSURE_PLATE = registerBlock("hot_steel_pressure_plate",
        new HotSteelPressurePlateBlock(BlockSetType.IRON,
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_PRESSURE_PLATE)
                .mapColor(MapColor.FIRE)
                .strength(3.0f, 10.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));

    /** Hot steel chain — glowing, fireproof chain for decoration. */
    public static final Block HOT_STEEL_CHAIN = registerBlock("hot_steel_chain",
        new ChainBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CHAIN)
            .mapColor(MapColor.FIRE)
            .strength(5.0f, 8.0f)
            .lightLevel(state -> 7)
            .sound(SoundType.CHAIN)
            .requiresCorrectToolForDrops()));

    /** Hot steel ladder — glowing metal ladder that never burns. */
    public static final Block HOT_STEEL_LADDER = registerBlock("hot_steel_ladder",
        new LadderBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LADDER)
            .mapColor(MapColor.FIRE)
            .strength(0.8f, 8.0f)
            .lightLevel(state -> 5)
            .sound(SoundType.METAL)));

    // =====================================================================
    //  Molten Age blocks
    // =====================================================================

    /** Storage block for Molten-forged ingots — blazing, blast-proof. */
    public static final Block MOLTEN_STEEL_BLOCK = registerBlock("molten_steel_block",
        new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERITE_BLOCK)
            .mapColor(MapColor.FIRE)
            .strength(55.0f, 1200.0f)
            .sound(SoundType.NETHERITE_BLOCK)
            .lightLevel(state -> 15)
            .requiresCorrectToolForDrops()));

    /** The ritual focus used to summon the Ancient Forgeborn. */
    public static final Block MOLTEN_ALTAR = registerBlock("molten_altar",
        new MoltenAltarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
            .mapColor(MapColor.FIRE)
            .strength(12.0f, 1200.0f)
            .lightLevel(state -> 15)
            .sound(SoundType.NETHERITE_BLOCK)
            .noOcclusion()
            .requiresCorrectToolForDrops()));

    /** Molten glass — translucent and faintly lit. */
    public static final Block MOLTEN_GLASS = registerBlock("molten_glass",
        new TransparentBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
            .mapColor(MapColor.FIRE)
            .strength(0.5f, 6.0f)
            .lightLevel(state -> 8)
            .sound(SoundType.GLASS)
            .noOcclusion()));

    /** Charred bricks — dark, soot-stained masonry of the old forges. */
    public static final Block CHARRED_BRICKS = registerBlock("charred_bricks",
        new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_BRICKS)
            .mapColor(MapColor.COLOR_BLACK)
            .strength(8.0f, 30.0f)
            .requiresCorrectToolForDrops()));

    /** Molten chain — a glowing chain for decoration. */
    public static final Block MOLTEN_STEEL_CHAIN = registerBlock("molten_steel_chain",
        new ChainBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CHAIN)
            .mapColor(MapColor.FIRE)
            .strength(6.0f, 10.0f)
            .lightLevel(state -> 10)
            .sound(SoundType.CHAIN)
            .requiresCorrectToolForDrops()));

    /** Molten lantern — the brightest crafted light of the Molten Age. */
    public static final Block MOLTEN_LANTERN = registerBlock("molten_lantern",
        new HotSteelLanternBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LANTERN)
            .mapColor(MapColor.FIRE)
            .strength(5.0f, 10.0f)
            .lightLevel(state -> 15)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()));

    private static Block registerBlock(String name, Block block) {
        ResourceLocation id = HotSteel.id(name);
        Registry.register(BuiltInRegistries.ITEM, id, new BlockItem(block, new Item.Properties()));
        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    public static void register() {
        HotSteel.LOGGER.info("Registering Hot Steel blocks");
    }
}
