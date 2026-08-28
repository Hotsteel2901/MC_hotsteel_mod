package com.hotsteel.item;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import com.hotsteel.logic.AdvancementHelper;
import com.hotsteel.logic.BlockBreakHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Hot Steel axe: besides chopping like a normal axe, breaking a log instantly
 * fells the whole connected trunk above/below it (up to 128 logs) so whole
 * trees drop at once. Tool durability is charged per extra log.
 */
public class HotSteelAxeItem extends AxeItem {

    private static final int MAX_LOGS = 128;

    public HotSteelAxeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        boolean broken = super.mineBlock(stack, level, state, pos, miner);
        if (!level.isClientSide && state.is(BlockTags.LOGS) && miner instanceof Player player) {
            fellTree(level, pos, state.getBlock(), player, stack);
        }
        return broken;
    }

    private void fellTree(Level level, BlockPos start, Block logBlock, Player player, ItemStack stack) {
        Set<BlockPos> toBreak = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
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
        for (BlockPos pos : toBreak) {
            if (pos.equals(start)) {
                continue; // already broken by the normal mining call
            }
            BlockBreakHelper.breakBlock(level, pos, player, stack);
        }
        if (toBreak.size() > 1 && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            AdvancementHelper.award(serverPlayer, "tree_felling", "fell_tree");
        }
    }
}
