package com.hotsteel.content.item;

import com.hotsteel.logic.TreeFeller;
import com.hotsteel.registry.ModMaterials;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Hot Steel Paxel: a pickaxe + axe + shovel merged into one tool. It mines
 * anything a pickaxe, axe or shovel can, at full Hot Steel speed, and — like
 * the pickaxe — auto-smelts ores (see {@code BlockMixin}). Being axe-capable it
 * also fells whole trees (sneak to chop a single log), like the Hot Steel axe.
 */
public class HotSteelPaxelItem extends PickaxeItem {

    public HotSteelPaxelItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    private static boolean isPaxelBlock(BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_PICKAXE)
            || state.is(BlockTags.MINEABLE_WITH_AXE)
            || state.is(BlockTags.MINEABLE_WITH_SHOVEL);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (isPaxelBlock(state)) {
            return ModMaterials.HotSteelTier.INSTANCE.getSpeed();
        }
        return 1.0f;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        if (!isPaxelBlock(state)) {
            return false;
        }
        // Hot Steel tier is correct for anything up to netherite level.
        return !state.is(ModMaterials.HotSteelTier.INSTANCE.getIncorrectBlocksForDrops());
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        boolean broken = super.mineBlock(stack, level, state, pos, miner);
        // Same convention as the axe: breaking a log fells the trunk, sneak for a single log.
        if (!level.isClientSide && state.is(BlockTags.LOGS) && miner instanceof Player player
            && !player.isShiftKeyDown()) {
            TreeFeller.fell(level, pos, state.getBlock(), player, stack, null);
        }
        return broken;
    }
}
