package com.hotsteel.logic;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Shared helper for breaking blocks as a player: spawns drops and charges tool durability. */
public final class BlockBreakHelper {

    private BlockBreakHelper() {}

    public static void breakBlock(Level level, BlockPos pos, Player player, ItemStack tool) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return;
        }
        if (level instanceof ServerLevel serverLevel) {
            List<ItemStack> drops = Block.getDrops(state, serverLevel, pos,
                level.getBlockEntity(pos), player, tool);
            for (ItemStack drop : drops) {
                Block.popResource(level, pos, drop);
            }
        }
        level.destroyBlock(pos, false);
        if (!player.getAbilities().instabuild) {
            tool.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        }
    }
}
