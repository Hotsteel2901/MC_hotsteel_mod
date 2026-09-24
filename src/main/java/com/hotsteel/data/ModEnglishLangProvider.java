package com.hotsteel.data;

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
        tb.add(ModItems.HOT_STEEL_NUGGET, "Hot Steel Nugget");
        tb.add(ModItems.MOLTEN_CORE, "Molten Core");
        tb.add(ModItems.HOT_STEEL_APPLE, "Hot Steel Apple");
        tb.add(ModBlocks.CRUDE_STEEL_BLOCK, "Block of Crude Steel");
        tb.add(ModBlocks.STEEL_BLOCK, "Block of Steel");
        tb.add(ModBlocks.HOT_STEEL_BLOCK, "Block of Hot Steel");
        tb.add(ModBlocks.HOT_STEEL_STAIRS, "Hot Steel Stairs");
        tb.add(ModBlocks.HOT_STEEL_SLAB, "Hot Steel Slab");
        tb.add(ModBlocks.HOT_STEEL_WALL, "Hot Steel Wall");
        tb.add(ModBlocks.HOT_STEEL_FORGE, "Hot Steel Forge");
        tb.add(ModBlocks.HOT_STEEL_SMELTER, "Hot Steel Smelter");
        tb.add(ModBlocks.HOT_STEEL_DOOR, "Hot Steel Door");
        tb.add(ModBlocks.HOT_STEEL_TRAPDOOR, "Hot Steel Trapdoor");
        tb.add(ModBlocks.HOT_STEEL_FENCE, "Hot Steel Fence");
        tb.add(ModBlocks.HOT_STEEL_BRICKS, "Hot Steel Bricks");
        tb.add(ModBlocks.HOT_STEEL_PRESSURE_PLATE, "Hot Steel Pressure Plate");
        tb.add(ModBlocks.HOT_STEEL_LANTERN, "Hot Steel Lantern");
        tb.add(ModBlocks.HOT_STEEL_CHAIN, "Hot Steel Chain");
        tb.add(ModBlocks.HOT_STEEL_LADDER, "Hot Steel Ladder");

        tb.add(ModItems.HOT_STEEL_HELMET, "Hot Steel Helmet");
        tb.add(ModItems.HOT_STEEL_CHESTPLATE, "Hot Steel Chestplate");
        tb.add(ModItems.HOT_STEEL_LEGGINGS, "Hot Steel Leggings");
        tb.add(ModItems.HOT_STEEL_BOOTS, "Hot Steel Boots");

        tb.add(ModItems.HOT_STEEL_SWORD, "Hot Steel Sword");
        tb.add(ModItems.HOT_STEEL_MACE, "Hot Steel Mace");
        tb.add(ModItems.HOT_STEEL_KNIFE, "Hot Steel Knife");
        tb.add(ModItems.HOT_STEEL_PICKAXE, "Hot Steel Pickaxe");
        tb.add(ModItems.HOT_STEEL_AXE, "Hot Steel Axe");
        tb.add(ModItems.HOT_STEEL_SHOVEL, "Hot Steel Shovel");
        tb.add(ModItems.HOT_STEEL_HOE, "Hot Steel Hoe");
        tb.add(ModItems.HOT_STEEL_PAXEL, "Hot Steel Paxel");

        tb.add(ModItems.HOT_STEEL_BOW, "Hot Steel Bow");
        tb.add(ModItems.HOT_STEEL_CROSSBOW, "Hot Steel Crossbow");
        tb.add(ModItems.HOT_STEEL_TRIDENT, "Hot Steel Trident");
        tb.add(ModItems.HOT_STEEL_SHIELD, "Hot Steel Shield");
        tb.add(ModItems.HOT_STEEL_ARROW, "Hot Steel Arrow");
        tb.add(ModItems.HOT_STEEL_FIREBALL, "Hot Steel Fireball");
        tb.add(ModItems.HOT_STEEL_FISHING_ROD, "Hot Steel Fishing Rod");
        tb.add(ModItems.HOT_STEEL_SICKLE, "Hot Steel Sickle");
        tb.add(ModItems.LAVA_BOTTLE, "Lava Bottle");
        tb.add(ModItems.LAVA_GOLEM_SPAWN_EGG, "Lava Golem Spawn Egg");
        tb.add(ModItems.FIRE_WRAITH_SPAWN_EGG, "Fire Wraith Spawn Egg");

        tb.add(ModEntities.HOT_STEEL_TRIDENT, "Hot Steel Trident");
        tb.add(ModEntities.LAVA_GOLEM, "Lava Golem");
        tb.add(ModEntities.FIRE_WRAITH, "Fire Wraith");
        tb.add(ModEffects.SUPER_FIRE_RESISTANCE.value(), "Super Fire Resistance");
        tb.add(ModCreativeTab.HOT_STEEL_TAB_KEY, "Hot Steel");

        // Lore
        tb.add("item.hotsteel.hot_steel_pickaxe.lore", "Auto-smelts mined ores into ingots");
        tb.add("item.hotsteel.melee.lore", "Scorching blade: ignites targets on hit");
        tb.add("item.hotsteel.hot_steel_arrow.lore", "Scorching bolt: ignites targets on impact");
        tb.add("item.hotsteel.armor.lore", "Set bonus: 2 pieces = fire immunity, 4 pieces = Super Fire Resistance");
        tb.add("item.hotsteel.hot_steel_sword.lore", "Right-click: strike a burst of fire ahead (costs durability)");
        tb.add("item.hotsteel.hot_steel_axe.lore", "Fells entire trees — breaking one log drops the whole trunk");
        tb.add("item.hotsteel.hot_steel_shovel.lore", "Sneak + use: dig a 3x3 area of soft blocks at once");
        tb.add("item.hotsteel.hot_steel_hoe.lore", "Right-click a ripe crop to harvest a 3x3 area and replant it");
        tb.add("item.hotsteel.hot_steel_fireball.lore", "Throwable: explodes and sets the area alight");
        tb.add("item.hotsteel.lava_bottle.lore", "Throwable: floods the impact area with lava");
        tb.add("item.hotsteel.lava_golem_spawn_egg.lore", "Spawns a molten guardian — fireproof, floats on lava");
        tb.add("item.hotsteel.fire_wraith_spawn_egg.lore", "Spawns a blazing Nether wraith — drops Molten Cores");
        tb.add("item.hotsteel.hot_steel_fishing_rod.lore", "Auto-cooks every fish it catches");
        tb.add("item.hotsteel.hot_steel_sickle.lore", "Harvests a 5x5 area of ripe crops and replants them");
        tb.add("block.hotsteel.hot_steel_forge.lore", "Right-click with damaged Hot Steel gear to repair it with ingots");
        tb.add("block.hotsteel.hot_steel_smelter.lore", "Drop ores on top — they smelt into ingots instantly");
        tb.add("block.hotsteel.hot_steel_pressure_plate.lore", "Sears whatever stands on it");
        tb.add("item.hotsteel.molten_core.lore", "Right-click: instantly smelt every ore in your inventory");
        tb.add("item.hotsteel.hot_steel_paxel.lore", "Pickaxe + axe + shovel in one — auto-smelts ores");
        tb.add("item.hotsteel.hot_steel_apple.lore", "Molten snack: fire immunity + regeneration on the go");
        tb.add("block.hotsteel.hot_steel_chain.lore", "Glowing fireproof chain — hangs anywhere");
        tb.add("block.hotsteel.hot_steel_ladder.lore", "A ladder that never burns, glowing softly");
        tb.add("block.hotsteel.hot_steel_block.lore", "Nearby players gain passive Fire Resistance");

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
        tb.add("advancements.hotsteel.steel_block.title", "Shelf Life");
        tb.add("advancements.hotsteel.steel_block.description", "Store your steel in a Block of Steel.");
        tb.add("advancements.hotsteel.hot_steel_block.title", "Fiery Foundation");
        tb.add("advancements.hotsteel.hot_steel_block.description", "Forge a Block of Hot Steel. Good beacon material, honestly.");
        tb.add("advancements.hotsteel.hot_steel_mace.title", "Hot Steel Smash");
        tb.add("advancements.hotsteel.hot_steel_mace.description", "Craft a Hot Steel Mace. Fall from great heights for extra damage.");
        tb.add("advancements.hotsteel.auto_smelt.title", "Lava-Forged Mining");
        tb.add("advancements.hotsteel.auto_smelt.description", "Mine an ore with a Hot Steel Pickaxe and get the ingot right away.");
        tb.add("advancements.hotsteel.set_bonus_2.title", "Flame Ward");
        tb.add("advancements.hotsteel.set_bonus_2.description", "Wear 2 pieces of Hot Steel armor to shrug off fire.");
        tb.add("advancements.hotsteel.tree_felling.title", "One Swing, Whole Forest");
        tb.add("advancements.hotsteel.tree_felling.description", "Fell an entire tree with a single Hot Steel Axe chop.");
        tb.add("advancements.hotsteel.area_dig.title", "Shovel That Hole");
        tb.add("advancements.hotsteel.area_dig.description", "Sneak-dig a 3x3 area with a Hot Steel Shovel.");
        tb.add("advancements.hotsteel.forge_repair.title", "Back to the Forge");
        tb.add("advancements.hotsteel.forge_repair.description", "Repair a damaged piece of Hot Steel gear at the Hot Steel Forge.");
        tb.add("advancements.hotsteel.lava_golem.title", "Molten Bodyguard");
        tb.add("advancements.hotsteel.lava_golem.description", "Summon a Lava Golem — it floats on lava and burns its enemies.");
        tb.add("advancements.hotsteel.fire_wraith.title", "Slay the Wraith");
        tb.add("advancements.hotsteel.fire_wraith.description", "Defeat a Fire Wraith and claim its Molten Core.");
        tb.add("advancements.hotsteel.sickle_harvest.title", "Great Harvest");
        tb.add("advancements.hotsteel.sickle_harvest.description", "Harvest a wide swath of crops with a Hot Steel Sickle.");
        tb.add("advancements.hotsteel.smelter_use.title", "Instant Smelting");
        tb.add("advancements.hotsteel.smelter_use.description", "Toss ore onto a Hot Steel Smelter and watch it turn to ingots.");
        tb.add("advancements.hotsteel.molten_core.title", "A Heart of Fire");
        tb.add("advancements.hotsteel.molten_core.description", "Hold a Molten Core — the still-burning heart of a Fire Wraith.");
        tb.add("advancements.hotsteel.molten_core_use.title", "One-Touch Smelting");
        tb.add("advancements.hotsteel.molten_core_use.description", "Consume a Molten Core to smelt your entire inventory at once.");
        tb.add("advancements.hotsteel.hot_steel_paxel.title", "Everything-Proof Tool");
        tb.add("advancements.hotsteel.hot_steel_paxel.description", "Forge a Hot Steel Paxel — pick, axe and shovel all in one.");
        tb.add("advancements.hotsteel.hot_steel_apple.title", "Bite the Heat");
        tb.add("advancements.hotsteel.hot_steel_apple.description", "Eat a Hot Steel Apple. Yes, it's hot. No, that's the point.");
        tb.add("advancements.hotsteel.hot_steel_chain.title", "Link the Forge");
        tb.add("advancements.hotsteel.hot_steel_chain.description", "Craft a Hot Steel Chain — glowing and fireproof.");

        // Chat messages
        tb.add("message.hotsteel.flame_ward_on", "Flame Ward ONLINE — fire damage ignored!");
        tb.add("message.hotsteel.flame_ward_off", "Flame Ward OFFLINE.");
        tb.add("message.hotsteel.super_fire_on", "Super Fire Resistance ONLINE!");
        tb.add("message.hotsteel.super_fire_off", "Super Fire Resistance OFFLINE!");
        tb.add("message.hotsteel.forge_no_damage", "This gear is already fully repaired.");
        tb.add("message.hotsteel.forge_need_ingots", "Not enough Hot Steel ingots — need %s.");
        tb.add("message.hotsteel.forge_repair", "Repaired at the forge (%s Hot Steel ingots).");

        // =====================================================================
        //  Molten Age
        // =====================================================================
        tb.add(ModItems.MOLTEN_SHARD, "Molten Shard");
        tb.add(ModItems.MOLTEN_STEEL_INGOT, "Molten Steel Ingot");
        tb.add(ModItems.MOLTEN_STEEL_NUGGET, "Molten Steel Nugget");
        tb.add(ModItems.FORGE_HEART, "Forge Heart");
        tb.add(ModItems.HOT_STEEL_CODEX, "Hot Steel Codex");
        tb.add(ModItems.SCORCHED_PAGE, "Scorched Page");
        tb.add(ModBlocks.MOLTEN_STEEL_BLOCK, "Block of Molten Steel");
        tb.add(ModBlocks.MOLTEN_ALTAR, "Molten Altar");
        tb.add(ModBlocks.MOLTEN_GLASS, "Molten Glass");
        tb.add(ModBlocks.CHARRED_BRICKS, "Charred Bricks");
        tb.add(ModBlocks.MOLTEN_STEEL_CHAIN, "Molten Steel Chain");
        tb.add(ModBlocks.MOLTEN_LANTERN, "Molten Lantern");

        tb.add(ModItems.MOLTEN_STEEL_HELMET, "Molten Steel Helmet");
        tb.add(ModItems.MOLTEN_STEEL_CHESTPLATE, "Molten Steel Chestplate");
        tb.add(ModItems.MOLTEN_STEEL_LEGGINGS, "Molten Steel Leggings");
        tb.add(ModItems.MOLTEN_STEEL_BOOTS, "Molten Steel Boots");

        tb.add(ModItems.MOLTEN_STEEL_SWORD, "Molten Steel Sword");
        tb.add(ModItems.MOLTEN_STEEL_SCYTHE, "Molten Steel Scythe");
        tb.add(ModItems.MOLTEN_STEEL_PICKAXE, "Molten Steel Pickaxe");
        tb.add(ModItems.MOLTEN_STEEL_AXE, "Molten Steel Axe");
        tb.add(ModItems.MOLTEN_STEEL_SHOVEL, "Molten Steel Shovel");
        tb.add(ModItems.MOLTEN_STEEL_HOE, "Molten Steel Hoe");

        tb.add(ModItems.SLAG_CRAWLER_SPAWN_EGG, "Slag Crawler Spawn Egg");
        tb.add(ModItems.EMBER_WISP_SPAWN_EGG, "Ember Wisp Spawn Egg");
        tb.add(ModItems.ANCIENT_FORGEBORN_SPAWN_EGG, "Ancient Forgeborn Spawn Egg");

        tb.add(ModEntities.SLAG_CRAWLER, "Slag Crawler");
        tb.add(ModEntities.EMBER_WISP, "Ember Wisp");
        tb.add(ModEntities.ANCIENT_FORGEBORN, "Ancient Forgeborn");
        tb.add(ModEffects.FORGE_BLESSING.value(), "Forge Blessing");

        // Codex GUI
        tb.add("gui.hotsteel.codex.title", "Chronicle of the Molten Age");
        tb.add("gui.hotsteel.codex.scroll_hint", "Scroll to read on");

        // Chronicle text — the recovered chapters of the old forge-logs
        tb.add("hotsteel.chronicle.chapter_1.title", "The Birth of Steel");
        tb.add("hotsteel.chronicle.chapter_1.body",
            "Long before the Molten Age, the old smiths knew only iron. Then, one night, a furnace was stoked hotter than any before it, and when the slag cooled, a bar of grey steel lay waiting in the ash.\n\n"
            + "That first crude steel was dark and brittle, yet it held an edge iron never could. They quenched it, folded it, and quenched it again, and from their stubborn hands came the first Steel Ingots.\n\n"
            + "The chronicle keeps but a single line of that night: \u201cThe forge burned until dawn \u2014 and dawn was steel.\u201d");
        tb.add("hotsteel.chronicle.chapter_2.title", "The Molten Path");
        tb.add("hotsteel.chronicle.chapter_2.body",
            "Steel alone was not enough. The smiths carried their ingots down to the rivers of fire that run beneath the world, and there, where lava flows like water, they learned to quench steel in the blood of the mountain.\n\n"
            + "An ingot plunged into the flow did not melt \u2014 it drank the heat and rose again as Hot Steel, glowing faintly even in the dark. Those who wore it soon found that flame turned aside, as though the metal still remembered where it was born.\n\n"
            + "From that day the road to the forge was walked barefoot, and fire ceased to be a danger.");
        tb.add("hotsteel.chronicle.chapter_3.title", "Souls of the Flame");
        tb.add("hotsteel.chronicle.chapter_3.body",
            "Deeper still, in the burning places, the smiths met things that were not quite alive: wraiths of flame that whispered in the crackle of the coals and drifted through walls of fire. They named them Fire Wraiths.\n\n"
            + "Within each wraith burned a Molten Core, a knot of heat so pure it never cooled, even in water. The smiths traded, hunted, and sometimes died for them, for they understood at last that these cores were the key to the Molten Age.\n\n"
            + "\u201cWreathed in fire,\u201d the chronicle warns, \u201cthey speak only to those who listen to the coals.\u201d");
        tb.add("hotsteel.chronicle.chapter_4.title", "A Body of Molten Steel");
        tb.add("hotsteel.chronicle.chapter_4.body",
            "The breakthrough came when a smith pressed a Molten Core into a bar of Hot Steel and struck it, again and again, while it glowed white. Metal and core fused, and what remained on the anvil was neither: Molten Steel.\n\n"
            + "Heavier than any armour they had known, and hotter, yet it never burned its wearer. To don the full harness was to become part of the forge itself \u2014 a Molten Body, untouched by flame or lava, with a heart that beat like a bellows.\n\n"
            + "\u201cHeavier the iron, hotter the heart,\u201d wrote the smith who first wore it.");
        tb.add("hotsteel.chronicle.chapter_5.title", "Master of the Forge");
        tb.add("hotsteel.chronicle.chapter_5.body",
            "All that remained was the old rite. Upon the Molten Altar, ringed by a platform of Hot Steel, set down a Molten Core and call the sleeper who has waited since the first furnace was lit: the Ancient Forgeborn.\n\n"
            + "It has guarded the fire through ages, and it does not yield its treasure lightly. Strike it down, and its heart \u2014 the Forge Heart \u2014 is yours: a still-burning ember that grants the blessing of the forge.\n\n"
            + "With the Lord of the Forge fallen, the Molten Age begins.");

        // Molten Age advancements
        tb.add("advancements.hotsteel.chapter_1.title", "The Birth of Steel");
        tb.add("advancements.hotsteel.chapter_1.description", "Smelt your first Steel Ingot and open the chronicle's first chapter.");
        tb.add("advancements.hotsteel.chapter_2.title", "The Molten Path");
        tb.add("advancements.hotsteel.chapter_2.description", "Quench steel in lava to forge Hot Steel \u2014 and fire can no longer harm you.");
        tb.add("advancements.hotsteel.chapter_3.title", "Souls of the Flame");
        tb.add("advancements.hotsteel.chapter_3.description", "Hold a Molten Shard and recover the whispered lore of the Fire Wraiths.");
        tb.add("advancements.hotsteel.chapter_4.title", "A Body of Molten Steel");
        tb.add("advancements.hotsteel.chapter_4.description", "Forge Molten Steel \u2014 heavier armour, and a hotter heart.");
        tb.add("advancements.hotsteel.chapter_5.title", "Master of the Forge");
        tb.add("advancements.hotsteel.chapter_5.description", "Defeat the Ancient Forgeborn at the Molten Altar and claim the Forge Heart.");

        // Molten Age chat messages
        tb.add("message.hotsteel.altar_hint", "Right-click the altar with a Molten Core to awaken the Ancient Forgeborn.");
        tb.add("message.hotsteel.altar_needs_platform", "The altar needs a full 3x3 platform of Blocks of Hot Steel beneath it.");
        tb.add("message.hotsteel.altar_awakened", "The forge trembles \u2014 the Ancient Forgeborn awakens!");
        tb.add("message.hotsteel.forgeborn_defeated", "The Ancient Forgeborn falls. The Forge Heart is yours.");
        tb.add("message.hotsteel.codex_progress", "Codex chapters recovered: %s / %s");
        tb.add("message.hotsteel.page_recovered", "The scorched page restores a chapter: %s");
        tb.add("message.hotsteel.page_already_complete", "Every chapter is already recovered \u2014 the page holds nothing more.");
        tb.add("message.hotsteel.molten_body_on", "Molten Body ONLINE \u2014 fire and lava cannot harm you!");
        tb.add("message.hotsteel.molten_body_off", "Molten Body OFFLINE.");
    }
}
