package com.hotsteel.mixin;

import java.util.Map;

import com.hotsteel.registry.ModItems;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hot Steel fishing rod: any raw fish caught with it is cooked instantly.
 * The {@code retrieve} method drops the catch into the player's inventory, so we
 * scan the inventory right after and upgrade raw fish to their cooked form.
 */
@Mixin(FishingHook.class)
public abstract class FishingHookMixin {

    @Unique
    private static final Map<Item, Item> COOKED = Map.of(
        Items.COD, Items.COOKED_COD,
        Items.SALMON, Items.COOKED_SALMON);

    @Inject(method = "retrieve", at = @At("TAIL"))
    private void hotsteel$cookFish(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        FishingHook self = (FishingHook) (Object) this;
        if (self.level().isClientSide) {
            return;
        }
        Player player = self.getPlayerOwner();
        if (player == null
            || !(player.getMainHandItem().is(ModItems.HOT_STEEL_FISHING_ROD)
                || player.getOffhandItem().is(ModItems.HOT_STEEL_FISHING_ROD))) {
            return;
        }
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack slot = inv.getItem(i);
            Item cooked = COOKED.get(slot.getItem());
            if (cooked != null && !slot.isEmpty()) {
                inv.setItem(i, new ItemStack(cooked, slot.getCount()));
            }
        }
    }
}
