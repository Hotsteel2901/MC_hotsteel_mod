package com.hotsteel.item;

import java.util.List;

import com.hotsteel.logic.AdvancementHelper;
import com.hotsteel.registry.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Hot Steel sickle: like a hoe, but right-clicking a mature crop harvests a
 * 5x5 area at once and replants every harvested crop. Vanilla HoeItem's tilling
 * behaviour is inherited for everything else.
 */
public class HotSteelSickleItem extends HoeItem {

    /** Half-width of the harvest box: 5x5 with the clicked block in the middle. */
    private static final int RADIUS = 2;

    public HotSteelSickleItem(Tier tier, Properties properties) {
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
                context.getItemInHand().hurtAndBreak(1, player,
                    net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    /** Harvest a 5x5 area of mature crops and replant them at age 0. */
    private void harvestCrops(Level level, BlockPos center) {
        int harvested = 0;
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
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
                    harvested++;
                }
            }
        }
        if (harvested > 0
            && level instanceof ServerLevel sl
            && sl.getNearestPlayer(center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5, 8.0,
                    e -> e instanceof ServerPlayer)
                instanceof ServerPlayer player) {
            AdvancementHelper.award(player, "sickle_harvest", "harvest_5x5");
        }
    }
}
