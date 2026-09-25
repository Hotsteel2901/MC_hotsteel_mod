package com.hotsteel.mixin;

import java.util.List;

import com.hotsteel.registry.ModBlocks;
import com.hotsteel.registry.ModItems;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Describes what every Hot Steel / Molten-forged item actually does.
 * <p>
 * The two sets are deliberately distinct, so each piece gets its own line — the
 * Hot Steel hoe tills while the sickle harvests, the Molten-forged sword throws a
 * flame wave while the Hot Steel sword strikes a point, and so on.
 */
@Mixin(Item.class)
public abstract class ItemTooltipMixin {

    @Inject(method = "appendHoverText", at = @At("TAIL"))
    private void hotsteel$addLore(ItemStack stack, Item.TooltipContext context,
                                  List<Component> tooltip, TooltipFlag flag, CallbackInfo ci) {
        Item item = stack.getItem();

        // ---- materials & story items -------------------------------------
        if (item == ModItems.MOLTEN_SHARD) {
            hotsteel$line(tooltip, "item.hotsteel.molten_shard.lore");
        } else if (item == ModItems.FORGE_HEART) {
            hotsteel$line(tooltip, "item.hotsteel.forge_heart.lore");
        } else if (item == ModItems.HOT_STEEL_CODEX) {
            hotsteel$line(tooltip, "item.hotsteel.hot_steel_codex.lore");
        } else if (item == ModItems.SCORCHED_PAGE) {
            hotsteel$line(tooltip, "item.hotsteel.scorched_page.lore");
        } else if (item == ModItems.MOLTEN_CORE) {
            hotsteel$line(tooltip, "item.hotsteel.molten_core.lore");
        } else if (item == ModItems.HOT_STEEL_APPLE) {
            hotsteel$line(tooltip, "item.hotsteel.hot_steel_apple.lore");
        } else if (item == ModItems.HOT_STEEL_ARROW) {
            hotsteel$line(tooltip, "item.hotsteel.hot_steel_arrow.lore");
        } else if (item == ModItems.HOT_STEEL_FIREBALL) {
            hotsteel$line(tooltip, "item.hotsteel.hot_steel_fireball.lore");
        } else if (item == ModItems.LAVA_BOTTLE) {
            hotsteel$line(tooltip, "item.hotsteel.lava_bottle.lore");

        // ---- Hot Steel tools & weapons -----------------------------------
        } else if (item == ModItems.HOT_STEEL_PICKAXE) {
            hotsteel$line(tooltip, "item.hotsteel.hot_steel_pickaxe.lore");
        } else if (item == ModItems.HOT_STEEL_PAXEL) {
            hotsteel$line(tooltip, "item.hotsteel.hot_steel_paxel.lore");
        } else if (item == ModItems.HOT_STEEL_SWORD) {
            hotsteel$line(tooltip, "item.hotsteel.hot_steel_sword.lore");
        } else if (item == ModItems.HOT_STEEL_KNIFE) {
            hotsteel$line(tooltip, "item.hotsteel.hot_steel_knife.lore");
        } else if (item == ModItems.HOT_STEEL_MACE) {
            hotsteel$line(tooltip, "item.hotsteel.hot_steel_mace.lore");
        } else if (item == ModItems.HOT_STEEL_AXE) {
            hotsteel$line(tooltip, "item.hotsteel.hot_steel_axe.lore");
        } else if (item == ModItems.HOT_STEEL_SHOVEL) {
            hotsteel$line(tooltip, "item.hotsteel.hot_steel_shovel.lore");
        } else if (item == ModItems.HOT_STEEL_HOE) {
            hotsteel$line(tooltip, "item.hotsteel.hot_steel_hoe.lore");
        } else if (item == ModItems.HOT_STEEL_SICKLE) {
            hotsteel$line(tooltip, "item.hotsteel.hot_steel_sickle.lore");
        } else if (item == ModItems.HOT_STEEL_FISHING_ROD) {
            hotsteel$line(tooltip, "item.hotsteel.hot_steel_fishing_rod.lore");
        } else if (item == ModItems.HOT_STEEL_TRIDENT) {
            hotsteel$line(tooltip, "item.hotsteel.melee.lore");

        // ---- Molten-forged tools & weapons -------------------------------
        } else if (item == ModItems.MOLTEN_STEEL_SWORD) {
            hotsteel$line(tooltip, "item.hotsteel.molten_steel_sword.lore");
        } else if (item == ModItems.MOLTEN_STEEL_SCYTHE) {
            hotsteel$line(tooltip, "item.hotsteel.molten_steel_scythe.lore");
        } else if (item == ModItems.MOLTEN_STEEL_PICKAXE) {
            hotsteel$line(tooltip, "item.hotsteel.molten_steel_pickaxe.lore");
        } else if (item == ModItems.MOLTEN_STEEL_AXE) {
            hotsteel$line(tooltip, "item.hotsteel.molten_steel_axe.lore");
        } else if (item == ModItems.MOLTEN_STEEL_SHOVEL) {
            hotsteel$line(tooltip, "item.hotsteel.molten_steel_shovel.lore");
        } else if (item == ModItems.MOLTEN_STEEL_HOE) {
            hotsteel$line(tooltip, "item.hotsteel.molten_steel_hoe.lore");

        // ---- spawn eggs --------------------------------------------------
        } else if (item == ModItems.LAVA_GOLEM_SPAWN_EGG) {
            hotsteel$line(tooltip, "item.hotsteel.lava_golem_spawn_egg.lore");
        } else if (item == ModItems.FIRE_WRAITH_SPAWN_EGG) {
            hotsteel$line(tooltip, "item.hotsteel.fire_wraith_spawn_egg.lore");
        } else if (item == ModItems.SLAG_CRAWLER_SPAWN_EGG) {
            hotsteel$line(tooltip, "item.hotsteel.slag_crawler_spawn_egg.lore");
        } else if (item == ModItems.EMBER_WISP_SPAWN_EGG) {
            hotsteel$line(tooltip, "item.hotsteel.ember_wisp_spawn_egg.lore");
        } else if (item == ModItems.ANCIENT_FORGEBORN_SPAWN_EGG) {
            hotsteel$line(tooltip, "item.hotsteel.ancient_forgeborn_spawn_egg.lore");

        // ---- armor: the two sets have different bonuses ------------------
        } else if (item instanceof ArmorItem armor) {
            if (hotsteel$isMoltenArmor(armor)) {
                hotsteel$line(tooltip, "item.hotsteel.molten_armor.lore");
            } else {
                hotsteel$line(tooltip, "item.hotsteel.armor.lore");
            }

        // ---- blocks ------------------------------------------------------
        } else if (item instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (block == ModBlocks.HOT_STEEL_FORGE) {
                hotsteel$line(tooltip, "block.hotsteel.hot_steel_forge.lore");
            } else if (block == ModBlocks.HOT_STEEL_SMELTER) {
                hotsteel$line(tooltip, "block.hotsteel.hot_steel_smelter.lore");
            } else if (block == ModBlocks.HOT_STEEL_PRESSURE_PLATE) {
                hotsteel$line(tooltip, "block.hotsteel.hot_steel_pressure_plate.lore");
            } else if (block == ModBlocks.HOT_STEEL_CHAIN) {
                hotsteel$line(tooltip, "block.hotsteel.hot_steel_chain.lore");
            } else if (block == ModBlocks.HOT_STEEL_LADDER) {
                hotsteel$line(tooltip, "block.hotsteel.hot_steel_ladder.lore");
            } else if (block == ModBlocks.HOT_STEEL_BLOCK) {
                hotsteel$line(tooltip, "block.hotsteel.hot_steel_block.lore");
            } else if (block == ModBlocks.MOLTEN_ALTAR) {
                hotsteel$line(tooltip, "block.hotsteel.molten_altar.lore");
            } else if (block == ModBlocks.MOLTEN_STEEL_BLOCK) {
                hotsteel$line(tooltip, "block.hotsteel.molten_steel_block.lore");
            }
        }
    }

    @Unique
    private static boolean hotsteel$isMoltenArmor(ArmorItem armor) {
        return armor.getMaterial().value()
            == com.hotsteel.registry.ModMaterials.MOLTEN_STEEL_ARMOR.value();
    }

    @Unique
    private static void hotsteel$line(List<Component> tooltip, String key) {
        tooltip.add(Component.translatable(key).withStyle(ChatFormatting.GOLD));
    }
}
