package com.hotsteel.content.item;

import java.util.function.UnaryOperator;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 「熔铸钢铲」— keeps the Hot Steel shovel's 3x3 dig, and adds <b>vitrification</b>:
 * sand it digs comes out as glass instead of sand, so a desert becomes windows.
 */
public class MoltenSteelShovelItem extends HotSteelShovelItem {

    public MoltenSteelShovelItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    protected UnaryOperator<ItemStack> digDropTransform(BlockState state) {
        if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
            return stack -> new ItemStack(Items.GLASS, stack.getCount());
        }
        return null;
    }
}
