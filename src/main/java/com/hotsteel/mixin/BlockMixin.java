package com.hotsteel.mixin;

import java.util.ArrayList;
import java.util.List;

import com.hotsteel.logic.AdvancementHelper;
import com.hotsteel.logic.AutoSmeltHelper;
import com.hotsteel.registry.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Auto-smelt: a Hot Steel pickaxe "melts" mined ore on the spot, converting the
 * drops into their smelted (ingot) form. Applied when the block's drops are
 * computed, so it works with Fortune/Silk Touch the same way vanilla would.
 * <p>
 * Only a curated set of ores/raw materials is smelted — ordinary stone etc. is
 * left untouched so the pickaxe doesn't remove cobblestone etc. from gameplay.
 * <p>
 * NOTE: the smelt map lives in {@link AutoSmeltHelper}, not as a mixin
 * {@code @Unique} static field — a static initializer there would force
 * {@code Items}/{@code Blocks} to load inside {@code Block}'s own static
 * initializer and crash vanilla {@code FireBlock} with a circular-init NPE.
 */
@Mixin(Block.class)
public abstract class BlockMixin {

    @Inject(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;",
            at = @At("RETURN"), cancellable = true)
    private static void hotsteel$autoSmelt(BlockState state, ServerLevel level, BlockPos pos,
                                           BlockEntity blockEntity, Entity entity, ItemStack tool,
                                           CallbackInfoReturnable<List<ItemStack>> cir) {
        if (!tool.is(ModItems.HOT_STEEL_PICKAXE) && !tool.is(ModItems.HOT_STEEL_PAXEL)) {
            return;
        }
        List<ItemStack> original = cir.getReturnValue();
        boolean changed = false;
        List<ItemStack> result = new ArrayList<>(original.size());
        for (ItemStack stack : original) {
            Item smelted = AutoSmeltHelper.SMELT_MAP.get(stack.getItem());
            if (smelted != null) {
                result.add(new ItemStack(smelted, stack.getCount()));
                changed = true;
            } else {
                result.add(stack.copy());
            }
        }
        if (changed) {
            cir.setReturnValue(result);
            if (entity instanceof ServerPlayer player) {
                AdvancementHelper.award(player, "auto_smelt", "smelt_ore");
            }
        }
    }
}
