package com.hotsteel.content.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Hot Steel hoe — a field preparation tool. Right-clicking tillable ground tills
 * the whole 3x3 around it at once.
 * <p>
 * Harvesting is deliberately NOT part of this tool: that job belongs to the
 * {@link HotSteelSickleItem}, so the hoe and the sickle never do the same thing.
 */
public class HotSteelHoeItem extends HoeItem {

    /** Blocks this hoe can turn into farmland, and what they become. */
    private static final int RADIUS = 1;

    public HotSteelHoeItem(Tier tier, Item.Properties properties) {
        super(tier, properties);
    }

    /** Called after a successful area-till; subclasses use it for extra effects. */
    protected void afterTill(Level level, BlockPos center, Player player, ItemStack tool) {
        // no extra effect on the Hot Steel hoe
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return super.useOn(context);
        }
        Level level = context.getLevel();
        BlockPos center = context.getClickedPos();
        if (!canTill(level, center)) {
            return super.useOn(context);
        }
        if (!level.isClientSide) {
            ItemStack tool = context.getItemInHand();
            int tilled = 0;
            for (int dx = -RADIUS; dx <= RADIUS; dx++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    BlockPos pos = center.offset(dx, 0, dz);
                    if (canTill(level, pos)) {
                        level.setBlock(pos, Blocks.FARMLAND.defaultBlockState(), 11);
                        if (!player.getAbilities().instabuild) {
                            tool.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                        }
                        tilled++;
                    }
                }
            }
            if (tilled > 0) {
                afterTill(level, center, player, tool);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Vanilla-like tilling rule: dirt-ish ground with air above. */
    private static boolean canTill(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        boolean tillable = block == Blocks.GRASS_BLOCK || block == Blocks.DIRT
            || block == Blocks.COARSE_DIRT || block == Blocks.ROOTED_DIRT;
        return tillable && level.getBlockState(pos.above()).isAir()
            && !level.getBlockState(pos.above()).isFaceSturdy(level, pos.above(), Direction.DOWN);
    }
}
