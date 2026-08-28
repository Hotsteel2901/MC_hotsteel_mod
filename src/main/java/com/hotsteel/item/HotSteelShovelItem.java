package com.hotsteel.item;

import java.util.Set;

import com.hotsteel.logic.AdvancementHelper;
import com.hotsteel.logic.BlockBreakHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Hot Steel shovel: sneak + right-click a soft block (dirt, sand, gravel…) to
 * dig a 3x3 area at once. Drops are collected and durability is charged per block.
 */
public class HotSteelShovelItem extends ShovelItem {

    private static final Set<Block> DIGGABLE = Set.of(
        Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT,
        Blocks.PODZOL, Blocks.MYCELIUM, Blocks.SAND, Blocks.RED_SAND,
        Blocks.GRAVEL, Blocks.CLAY, Blocks.SOUL_SAND, Blocks.SOUL_SOIL,
        Blocks.MUD, Blocks.SNOW_BLOCK, Blocks.SNOW, Blocks.PACKED_MUD,
        Blocks.FARMLAND, Blocks.DIRT_PATH);

    public HotSteelShovelItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player != null && player.isShiftKeyDown()
            && isDiggable(level.getBlockState(context.getClickedPos()))) {
            if (!level.isClientSide) {
                digArea(level, context.getClickedPos(), player, context.getItemInHand());
            }
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    private static boolean isDiggable(BlockState state) {
        return DIGGABLE.contains(state.getBlock());
    }

    private static void digArea(Level level, BlockPos center, Player player, ItemStack tool) {
        int dug = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos pos = center.offset(dx, 0, dz);
                if (isDiggable(level.getBlockState(pos))) {
                    BlockBreakHelper.breakBlock(level, pos, player, tool);
                    dug++;
                }
            }
        }
        if (dug > 1 && player instanceof ServerPlayer serverPlayer) {
            AdvancementHelper.award(serverPlayer, "area_dig", "dig_3x3");
        }
    }
}
