package com.hotsteel.content.item;

import java.util.function.UnaryOperator;

import com.hotsteel.logic.TreeFeller;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
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

    public HotSteelAxeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        boolean broken = super.mineBlock(stack, level, state, pos, miner);
        // Sneak to chop a single log: without that you would demolish any player-built
        // log wall along with the tree.
        if (!level.isClientSide && state.is(BlockTags.LOGS) && miner instanceof Player player
            && !player.isShiftKeyDown()) {
            TreeFeller.fell(level, pos, state.getBlock(), player, stack, felledLogDropTransform(level));
        }
        return broken;
    }

    /** Rewrites the drops of the logs felled by this axe; null means "leave them alone". */
    protected UnaryOperator<ItemStack> felledLogDropTransform(Level level) {
        return null;
    }
}
