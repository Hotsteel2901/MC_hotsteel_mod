package com.hotsteel.content.item;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.function.UnaryOperator;

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
 * <p>
 * Subclasses can rewrite the drops of the felled logs through
 * {@link #felledLogDropTransform} — that is how the Molten-forged axe chars them
 * into charcoal.
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

    /** Rewrites the drops of the logs felled by this axe; null means "leave them alone". */
    protected UnaryOperator<ItemStack> felledLogDropTransform(Level level) {
        return null;
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
        UnaryOperator<ItemStack> transform = felledLogDropTransform(level);
        for (BlockPos pos : toBreak) {
            if (pos.equals(start)) {
                continue; // already broken by the normal mining call
            }
            BlockBreakHelper.breakBlock(level, pos, player, stack, transform);
        }
        if (toBreak.size() > 1 && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            AdvancementHelper.award(serverPlayer, "tree_felling", "fell_tree");
        }
    }
}
