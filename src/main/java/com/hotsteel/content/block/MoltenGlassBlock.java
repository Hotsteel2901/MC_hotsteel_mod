package com.hotsteel.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 「熔火玻璃」— furnace glass with the heat still sealed inside it.
 *
 * <p>It is see-through (assigned the translucent render layer client-side) and
 * lit from within, but its defining trait is the burn: any living thing that
 * walks across the pane — or is caught inside it — is set alight. Fire-immune
 * mobs are unaffected, which is what keeps it a tool rather than a trap for
 * your own Nether farms.
 */
public class MoltenGlassBlock extends TransparentBlock {

    /** Fire ticks applied to whatever touches the pane (2s). */
    private static final int IGNITE_TICKS = 40;

    public MoltenGlassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        ignite(level, entity);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        ignite(level, entity);
    }

    private static void ignite(Level level, Entity entity) {
        if (level.isClientSide || !(entity instanceof LivingEntity living) || living.fireImmune()) {
            return;
        }
        living.setRemainingFireTicks(Math.max(living.getRemainingFireTicks(), IGNITE_TICKS));
    }
}