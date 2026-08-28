package com.hotsteel.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;

/**
 * Hot Steel pressure plate: a redstone pressure plate that also sears whatever
 * stands on it, setting living entities on fire for 3 seconds.
 */
public class HotSteelPressurePlateBlock extends PressurePlateBlock {

    private static final int IGNITE_TICKS = 60;

    public HotSteelPressurePlateBlock(BlockSetType type, Properties properties) {
        super(type, properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (!level.isClientSide && entity instanceof LivingEntity living
            && state.getValue(POWERED)) {
            living.setRemainingFireTicks(Math.max(living.getRemainingFireTicks(), IGNITE_TICKS));
        }
    }
}
