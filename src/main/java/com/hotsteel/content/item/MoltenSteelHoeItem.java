package com.hotsteel.content.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 「熔铸钢锄」— keeps the Hot Steel hoe's 3x3 tilling, and adds <b>ripening</b>:
 * the residual heat of the blade pushes every crop within 5x5 one growth stage
 * forwards. Plant, till, harvest — no waiting.
 */
public class MoltenSteelHoeItem extends HotSteelHoeItem {

    private static final int RIPEN_RADIUS = 2;
    private static final float RIPEN_CHANCE = 0.5f;

    public MoltenSteelHoeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    protected void afterTill(Level level, BlockPos center, Player player, ItemStack tool) {
        for (int dx = -RIPEN_RADIUS; dx <= RIPEN_RADIUS; dx++) {
            for (int dz = -RIPEN_RADIUS; dz <= RIPEN_RADIUS; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (state.getBlock() instanceof CropBlock crop
                        && !crop.isMaxAge(state)
                        && level.random.nextFloat() < RIPEN_CHANCE) {
                        int next = Math.min(crop.getMaxAge(), crop.getAge(state) + 1);
                        level.setBlock(pos, crop.getStateForAge(next), 2);
                    }
                }
            }
        }
    }
}
