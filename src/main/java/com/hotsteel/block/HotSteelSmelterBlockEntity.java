package com.hotsteel.block;

import com.hotsteel.logic.AutoSmeltHelper;
import com.hotsteel.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Block entity for the Hot Steel Smelter: any ore/raw item that lands on the
 * block is instantly smelted (converted to its ingot form, same map as the Hot
 * Steel pickaxe) with a lava-flash effect.
 */
public class HotSteelSmelterBlockEntity extends BlockEntity {

    public HotSteelSmelterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** Constructor matching the {@code BlockEntityType.BlockEntitySupplier} signature. */
    public HotSteelSmelterBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.HOT_STEEL_SMELTER, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  HotSteelSmelterBlockEntity be) {
        if (level.isClientSide) {
            return;
        }
        ServerLevel server = (ServerLevel) level;
        AABB box = new AABB(pos).expandTowards(0.0, 1.0, 0.0).inflate(0.3, 0.1, 0.3);
        List<ItemEntity> items = server.getEntitiesOfClass(ItemEntity.class, box);
        if (items.isEmpty()) {
            return;
        }
        for (ItemEntity itemEntity : items) {
            ItemStack stack = itemEntity.getItem();
            Item smelted = AutoSmeltHelper.SMELT_MAP.get(stack.getItem());
            if (smelted == null) {
                continue;
            }
            itemEntity.setItem(new ItemStack(smelted, stack.getCount()));
            server.sendParticles(ParticleTypes.LAVA,
                pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                12, 0.3, 0.1, 0.3, 0.0);
            level.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.8f, 1.2f);
            // Award the advancement to any nearby player.
            if (server.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10.0, false)
                    instanceof net.minecraft.server.level.ServerPlayer player) {
                com.hotsteel.logic.AdvancementHelper.award(player, "smelter_use", "smelt_ore");
            }
        }
    }
}
