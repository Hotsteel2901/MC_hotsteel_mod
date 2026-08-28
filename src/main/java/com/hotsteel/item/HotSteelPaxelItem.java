package com.hotsteel.item;

import com.hotsteel.registry.ModMaterials;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Hot Steel Paxel: a pickaxe + axe + shovel merged into one tool. It mines
 * anything a pickaxe, axe or shovel can, at full Hot Steel speed, and — like
 * the pickaxe — auto-smelts ores (see {@code BlockMixin}).
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
}
