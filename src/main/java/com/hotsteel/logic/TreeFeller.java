package com.hotsteel.logic;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.function.UnaryOperator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Whole-trunk felling, shared by every axe-capable tool (Hot Steel axe,
 * Molten-forged axe and the Paxel). Breaking one log brings down the whole
 * connected trunk, up to {@link #MAX_LOGS} blocks.
 * <p>
 * Durability is charged once for the entire batch (1 per log) rather than once
 * per block, so felling a tree does not fire one durability sound per log.
 */
public final class TreeFeller {

    /** Hard cap so a (bugged or modded) huge blob of logs can't lock the server. */
    private static final int MAX_LOGS = 128;

    private TreeFeller() {}

    /**
     * Fells the trunk of {@code logBlock} connected to {@code start}.
     * <p>
     * IMPORTANT: callers invoke this from {@code Item.mineBlock()}, which runs
     * <em>after</em> the starting block has been removed — so {@code start} is already
     * air. Seeding the search at {@code start} would therefore find nothing and the
     * whole feature would silently do nothing, which is exactly the bug this used to
     * have. Seed from its neighbours instead.
     *
     * @param transform optional rewrite applied to each felled log's drops (the
     *                  Molten-forged axe uses this to char them into charcoal)
     */
    public static void fell(Level level, BlockPos start, Block logBlock, Player player,
                            ItemStack stack, UnaryOperator<ItemStack> transform) {
        Set<BlockPos> toBreak = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        for (Direction dir : Direction.values()) {
            BlockPos next = start.relative(dir);
            if (level.getBlockState(next).is(logBlock)) {
                queue.add(next);
            }
        }
        while (!queue.isEmpty() && toBreak.size() < MAX_LOGS) {
            BlockPos pos = queue.poll();
            if (!toBreak.add(pos)) {
                continue;
            }
            if (!level.getBlockState(pos).is(logBlock)) {
                continue;
            }
            for (Direction dir : Direction.values()) {
                BlockPos next = pos.relative(dir);
                if (level.getBlockState(next).is(logBlock)) {
                    queue.add(next);
                }
            }
        }
        if (toBreak.isEmpty()) {
            return; // a lone log, nothing to fell
        }
        for (BlockPos pos : toBreak) {
            BlockBreakHelper.breakBlock(level, pos, player, stack, transform, false);
        }
        // Charge the whole felling in one go (1 per log, never more than the tool has left).
        if (!player.getAbilities().instabuild) {
            int remaining = stack.getMaxDamage() - stack.getDamageValue();
            stack.hurtAndBreak(Math.min(toBreak.size(), Math.max(remaining, 0)), player,
                EquipmentSlot.MAINHAND);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            AdvancementHelper.award(serverPlayer, "tree_felling", "fell_tree");
        }
    }
}
