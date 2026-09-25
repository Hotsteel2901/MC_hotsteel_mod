package com.hotsteel.logic;

import java.util.List;
import java.util.function.UnaryOperator;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Shared helper for breaking blocks as a player: spawns drops and charges tool durability. */
public final class BlockBreakHelper {

    private BlockBreakHelper() {}

    public static void breakBlock(Level level, BlockPos pos, Player player, ItemStack tool) {
        breakBlock(level, pos, player, tool, null, true);
    }

    public static void breakBlock(Level level, BlockPos pos, Player player, ItemStack tool,
                                  UnaryOperator<ItemStack> transform) {
        breakBlock(level, pos, player, tool, transform, true);
    }

    /**
     * Breaks a block with the given tool.
     * <p>
     * {@code transform} (optional) rewrites each drop before it is spawned — this is how the
     * Molten-forged axe chars felled logs and the Molten-forged shovel vitrifies sand.
     * <p>
     * {@code chargeDurability} = false leaves the tool alone; bulk operations (felling a whole
     * tree) use that and charge the wear once for the entire batch instead of per block, which
     * otherwise fires one durability sound per log.
     */
    public static void breakBlock(Level level, BlockPos pos, Player player, ItemStack tool,
                                  UnaryOperator<ItemStack> transform, boolean chargeDurability) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return;
        }
        if (level instanceof ServerLevel serverLevel) {
            List<ItemStack> drops = Block.getDrops(state, serverLevel, pos,
                level.getBlockEntity(pos), player, tool);
            for (ItemStack drop : drops) {
                ItemStack result = drop;
                if (transform != null) {
                    result = transform.apply(drop);
                    if (result == null || result.isEmpty()) {
                        continue;
                    }
                }
                Block.popResource(level, pos, result);
            }
        }
        level.destroyBlock(pos, false);
        if (chargeDurability && !player.getAbilities().instabuild) {
            tool.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        }
    }
}
