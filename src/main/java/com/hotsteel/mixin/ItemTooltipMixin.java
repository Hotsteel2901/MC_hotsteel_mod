package com.hotsteel.mixin;

import java.util.List;

import com.hotsteel.registry.ModBlocks;
import com.hotsteel.registry.ModItems;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds lore lines to Hot Steel items describing their passive traits:
 * melee weapons ignite targets, the pickaxe auto-smelts ores, and armor pieces
 * describe the 2-piece / 4-piece set bonuses.
 */
@Mixin(Item.class)
public abstract class ItemTooltipMixin {

    @Inject(method = "appendHoverText", at = @At("TAIL"))
    private void hotsteel$addLore(ItemStack stack, Item.TooltipContext context,
                                  List<Component> tooltip, TooltipFlag flag, CallbackInfo ci) {
        Item item = stack.getItem();
        if (item == ModItems.HOT_STEEL_PICKAXE) {
            tooltip.add(Component.translatable("item.hotsteel.hot_steel_pickaxe.lore")
                .withStyle(ChatFormatting.GOLD));
        } else if (item == ModItems.HOT_STEEL_SWORD) {
            tooltip.add(Component.translatable("item.hotsteel.hot_steel_sword.lore")
                .withStyle(ChatFormatting.GOLD));
        } else if (item == ModItems.HOT_STEEL_AXE) {
            tooltip.add(Component.translatable("item.hotsteel.hot_steel_axe.lore")
                .withStyle(ChatFormatting.GOLD));
        } else if (item == ModItems.HOT_STEEL_SHOVEL) {
            tooltip.add(Component.translatable("item.hotsteel.hot_steel_shovel.lore")
                .withStyle(ChatFormatting.GOLD));
        } else if (item == ModItems.HOT_STEEL_HOE) {
            tooltip.add(Component.translatable("item.hotsteel.hot_steel_hoe.lore")
                .withStyle(ChatFormatting.GOLD));
        } else if (item == ModItems.HOT_STEEL_KNIFE || item == ModItems.HOT_STEEL_MACE
            || item == ModItems.HOT_STEEL_TRIDENT) {
            tooltip.add(Component.translatable("item.hotsteel.melee.lore")
                .withStyle(ChatFormatting.GOLD));
        } else if (item == ModItems.HOT_STEEL_ARROW) {
            tooltip.add(Component.translatable("item.hotsteel.hot_steel_arrow.lore")
                .withStyle(ChatFormatting.GOLD));
        } else if (item == ModItems.HOT_STEEL_FIREBALL) {
            tooltip.add(Component.translatable("item.hotsteel.hot_steel_fireball.lore")
                .withStyle(ChatFormatting.GOLD));
        } else if (item == ModItems.LAVA_BOTTLE) {
            tooltip.add(Component.translatable("item.hotsteel.lava_bottle.lore")
                .withStyle(ChatFormatting.GOLD));
        } else if (item == ModItems.LAVA_GOLEM_SPAWN_EGG) {
            tooltip.add(Component.translatable("item.hotsteel.lava_golem_spawn_egg.lore")
                .withStyle(ChatFormatting.GOLD));
        } else if (item == ModItems.FIRE_WRAITH_SPAWN_EGG) {
            tooltip.add(Component.translatable("item.hotsteel.fire_wraith_spawn_egg.lore")
                .withStyle(ChatFormatting.GOLD));
        } else if (item == ModItems.HOT_STEEL_FISHING_ROD) {
            tooltip.add(Component.translatable("item.hotsteel.hot_steel_fishing_rod.lore")
                .withStyle(ChatFormatting.GOLD));
        } else if (item == ModItems.HOT_STEEL_SICKLE) {
            tooltip.add(Component.translatable("item.hotsteel.hot_steel_sickle.lore")
                .withStyle(ChatFormatting.GOLD));
        } else if (item == ModItems.MOLTEN_CORE) {
            tooltip.add(Component.translatable("item.hotsteel.molten_core.lore")
                .withStyle(ChatFormatting.GOLD));
        } else if (item instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (block == ModBlocks.HOT_STEEL_FORGE) {
                tooltip.add(Component.translatable("block.hotsteel.hot_steel_forge.lore")
                    .withStyle(ChatFormatting.GOLD));
            } else if (block == ModBlocks.HOT_STEEL_SMELTER) {
                tooltip.add(Component.translatable("block.hotsteel.hot_steel_smelter.lore")
                    .withStyle(ChatFormatting.GOLD));
            } else if (block == ModBlocks.HOT_STEEL_PRESSURE_PLATE) {
                tooltip.add(Component.translatable("block.hotsteel.hot_steel_pressure_plate.lore")
                    .withStyle(ChatFormatting.GOLD));
            }
        } else if (item instanceof ArmorItem armor) {
            if (armor.getEquipmentSlot() == EquipmentSlot.HEAD
                || armor.getEquipmentSlot() == EquipmentSlot.CHEST
                || armor.getEquipmentSlot() == EquipmentSlot.LEGS
                || armor.getEquipmentSlot() == EquipmentSlot.FEET) {
                tooltip.add(Component.translatable("item.hotsteel.armor.lore")
                    .withStyle(ChatFormatting.GOLD));
            }
        }
    }
}
