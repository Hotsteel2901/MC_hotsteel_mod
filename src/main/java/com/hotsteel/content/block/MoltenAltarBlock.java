package com.hotsteel.content.block;

import com.hotsteel.content.entity.AncientForgebornEntity;
import com.hotsteel.registry.ModBlocks;
import com.hotsteel.registry.ModEntities;
import com.hotsteel.registry.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 「熔火祭坛」— the ritual focus of the Molten Age.
 * <p>
 * Standing on a 3×3 platform of Blocks of Hot Steel, the altar accepts a Molten
 * Core: right-clicking with one in hand consumes the core, erupts in lava
 * particles and summons the Ancient Forgeborn — the final boss of the story.
 */
public class MoltenAltarBlock extends Block implements EntityBlock {

    public MoltenAltarBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MoltenAltarBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                 BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return (tickLevel, pos, tickState, be) -> {
            if (be instanceof MoltenAltarBlockEntity altar) {
                MoltenAltarBlockEntity.serverTick(tickLevel, pos, tickState, altar);
            }
        };
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                             Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(ModItems.MOLTEN_CORE)) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                    Component.translatable("message.hotsteel.altar_hint"), true);
            }
            return ItemInteractionResult.SUCCESS;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        if (!hasPlatform(level, pos)) {
            player.displayClientMessage(
                Component.translatable("message.hotsteel.altar_needs_platform"), true);
            return ItemInteractionResult.SUCCESS;
        }
        BlockPos altarPos = pos;
        AncientForgebornEntity boss = ModEntities.ANCIENT_FORGEBORN.create(level);
        if (boss == null) {
            return ItemInteractionResult.SUCCESS;
        }
        boss.moveTo(altarPos.getX() + 0.5, altarPos.getY() + 1.0, altarPos.getZ() + 0.5,
            level.random.nextFloat() * 360.0f, 0.0f);
        level.addFreshEntity(boss);

        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.LAVA,
                altarPos.getX() + 0.5, altarPos.getY() + 1.2, altarPos.getZ() + 0.5,
                80, 1.2, 0.6, 1.2, 0.1);
            serverLevel.sendParticles(ParticleTypes.FLAME,
                altarPos.getX() + 0.5, altarPos.getY() + 1.2, altarPos.getZ() + 0.5,
                60, 1.0, 0.8, 1.0, 0.08);
        }
        level.playSound(null, altarPos, SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 1.6f, 0.7f);
        player.displayClientMessage(
            Component.translatable("message.hotsteel.altar_awakened"), false);
        return ItemInteractionResult.SUCCESS;
    }

    /** True if the block directly under the altar is part of a full 3×3 hot steel platform. */
    public static boolean hasPlatform(Level level, BlockPos altarPos) {
        BlockPos center = altarPos.below();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (!level.getBlockState(center.offset(dx, 0, dz)).is(ModBlocks.HOT_STEEL_BLOCK)) {
                    return false;
                }
            }
        }
        return true;
    }
}
