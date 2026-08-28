package com.hotsteel.mixin;

import java.util.List;

import com.hotsteel.registry.ModItems;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

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
        } else if (item == ModItems.HOT_STEEL_SWORD || item == ModItems.HOT_STEEL_KNIFE
            || item == ModItems.HOT_STEEL_AXE || item == ModItems.HOT_STEEL_MACE
            || item == ModItems.HOT_STEEL_TRIDENT) {
            tooltip.add(Component.translatable("item.hotsteel.melee.lore")
                .withStyle(ChatFormatting.GOLD));
        } else if (item == ModItems.HOT_STEEL_ARROW) {
            tooltip.add(Component.translatable("item.hotsteel.hot_steel_arrow.lore")
                .withStyle(ChatFormatting.GOLD));
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
