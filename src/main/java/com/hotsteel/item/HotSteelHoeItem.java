package com.hotsteel.item;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Hot Steel hoe: besides tilling like a normal hoe, right-clicking a fully-grown
 * crop harvests it (plus the surrounding 3x3) and replants it immediately.
 */
public class HotSteelHoeItem extends HoeItem {

    public HotSteelHoeItem(Tier tier, Item.Properties properties) {
        super(tier, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
            if (!level.isClientSide) {
                harvestCrops(level, pos);
            }
            Player player = context.getPlayer();
            if (player != null && !player.getAbilities().instabuild) {
                context.getItemInHand().hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    /** Harvest a 3x3 area of mature crops and replant them at age 0. */
    private void harvestCrops(Level level, BlockPos center) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos pos = center.offset(dx, 0, dz);
                BlockState state = level.getBlockState(pos);
                if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
                    if (level instanceof ServerLevel serverLevel) {
                        List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, null);
                        for (ItemStack drop : drops) {
                            Block.popResource(level, pos, drop);
                        }
                    }
                    level.setBlock(pos, crop.defaultBlockState(), 3);
                }
            }
        }
    }
}
