package com.hotsteel.content.item;

import java.util.function.UnaryOperator;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;

/**
 * 「熔铸钢斧」— keeps the Hot Steel axe's whole-trunk felling, and adds
 * <b>charring</b>: the logs it brings down have a chance to drop as charcoal,
 * so one swing gives fuel as well as timber.
 */
public class MoltenSteelAxeItem extends HotSteelAxeItem {

    private static final float CHAR_CHANCE = 0.35f;

    public MoltenSteelAxeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    protected UnaryOperator<ItemStack> felledLogDropTransform(Level level) {
        return stack -> {
            if (stack.is(ItemTags.LOGS) && level.random.nextFloat() < CHAR_CHANCE) {
                return new ItemStack(Items.CHARCOAL, stack.getCount());
            }
            return stack;
        };
    }
}
