package com.hotsteel.mixin;

import java.util.Map;

import com.hotsteel.registry.ModItems;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hot Steel fishing rod: any raw fish caught with it is cooked instantly, and —
 * critically — the bobber stays out. Vanilla {@code shouldStopFishing} only
 * recognises {@code Items.FISHING_ROD}, so a bobber cast from any other rod is
 * discarded the very next tick (it "shoots out and instantly disappears"). We
 * teach it to also accept the Hot Steel rod.
 */
@Mixin(FishingHook.class)
public abstract class FishingHookMixin {

    @Unique
    private static final Map<Item, Item> COOKED = Map.of(
        Items.COD, Items.COOKED_COD,
        Items.SALMON, Items.COOKED_SALMON);

    /** Keep the bobber alive when cast from a Hot Steel rod (main or offhand). */
    @Inject(method = "shouldStopFishing", at = @At("HEAD"), cancellable = true)
    private void hotsteel$keepHookForHotSteelRod(Player player, CallbackInfoReturnable<Boolean> cir) {
        boolean hasHotRod = player.getMainHandItem().is(ModItems.HOT_STEEL_FISHING_ROD)
            || player.getOffhandItem().is(ModItems.HOT_STEEL_FISHING_ROD);
        if (hasHotRod && !player.isRemoved() && player.isAlive()
            && ((FishingHook) (Object) this).distanceToSqr(player) <= 1024.0) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Actually cook the fish. Vanilla {@code retrieve} turns each looted stack into an
     * {@code ItemEntity} at the bobber and lets it fly toward the player, so the stack
     * never sits in the inventory long enough for a post-retrieve scan to catch it.
     * Instead we swap the stack to its cooked form the moment the ItemEntity is built.
     */
    @ModifyArg(
        method = "retrieve",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/item/ItemEntity;<init>(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V"),
        index = 4)
    private ItemStack hotsteel$cookCaughtFish(ItemStack stack) {
        Item cooked = COOKED.get(stack.getItem());
        if (cooked != null && !stack.isEmpty()) {
            return new ItemStack(cooked, stack.getCount());
        }
        return stack;
    }
}
