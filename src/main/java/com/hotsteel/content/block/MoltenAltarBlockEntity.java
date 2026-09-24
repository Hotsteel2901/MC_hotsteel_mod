package com.hotsteel.content.block;

import com.hotsteel.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for the Molten Altar. It only provides ambience: a slow pulse of
 * ember particles and the occasional lava pop, which makes an armed altar easy
 * to spot in the dark.
 */
public class MoltenAltarBlockEntity extends BlockEntity {

    public MoltenAltarBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public MoltenAltarBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.MOLTEN_ALTAR, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  MoltenAltarBlockEntity be) {
        if (level.isClientSide) {
            return;
        }
        if (level.getGameTime() % 10L != 0L) {
            return;
        }
        ServerLevel server = (ServerLevel) level;
        server.sendParticles(ParticleTypes.SMOKE,
            pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
            1, 0.2, 0.1, 0.2, 0.01);
        if (level.getGameTime() % 60L == 0L) {
            server.sendParticles(ParticleTypes.LAVA,
                pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                2, 0.2, 0.1, 0.2, 0.0);
            level.playSound(null, pos, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.3f, 1.4f);
        }
    }
}
