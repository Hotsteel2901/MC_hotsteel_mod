package com.hotsteel.entity;

import com.hotsteel.registry.ModEntities;
import com.hotsteel.registry.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * A thrown bottle of liquid lava. Shatters on impact, flooding the surrounding
 * blocks with lava and igniting the area.
 */
public class LavaBottleEntity extends ThrowableItemProjectile {

    public LavaBottleEntity(EntityType<? extends LavaBottleEntity> type, Level level) {
        super(type, level);
    }

    public LavaBottleEntity(Level level, LivingEntity shooter, ItemStack stack) {
        super(ModEntities.LAVA_BOTTLE, shooter, level);
        this.setItem(stack);
    }

    public LavaBottleEntity(Level level, double x, double y, double z, ItemStack stack) {
        super(ModEntities.LAVA_BOTTLE, x, y, z, level);
        this.setItem(stack);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.LAVA_BOTTLE;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level().isClientSide) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel) this.level();
        serverLevel.sendParticles(ParticleTypes.LAVA,
            this.getX(), this.getY(), this.getZ(), 40, 0.5, 0.5, 0.5, 0.1);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1.0f, 0.8f);

        BlockPos center = this.blockPosition();
        if (result instanceof BlockHitResult blockHit) {
            center = blockHit.getBlockPos();
        }
        splashLava(serverLevel, center);
        this.discard();
    }

    /** Flood a 3x3 horizontal area (and one block down) with lava where possible, then ignite the rim. */
    private static void splashLava(ServerLevel level, BlockPos center) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos pos = center.offset(dx, 0, dz);
                tryPlaceLava(level, pos);
                tryPlaceLava(level, pos.below());
            }
        }
        // Ignite a wider ring around the impact
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) < 2) {
                    continue;
                }
                BlockPos pos = center.offset(dx, 0, dz);
                BlockState state = level.getBlockState(pos);
                BlockState below = level.getBlockState(pos.below());
                if (state.isAir() && !below.isAir() && below.getFluidState().isEmpty()) {
                    level.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
                }
            }
        }
    }

    private static void tryPlaceLava(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.canBeReplaced(Fluids.LAVA)) {
            level.setBlock(pos, Blocks.LAVA.defaultBlockState(), 3);
        }
    }
}
