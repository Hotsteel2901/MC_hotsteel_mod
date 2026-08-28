package com.hotsteel.block;

import java.util.Set;

import com.hotsteel.logic.AdvancementHelper;
import com.hotsteel.registry.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Hot Steel forge: a glowing decorative block that repairs damaged Hot Steel
 * equipment. Right-click the forge while holding a damaged Hot Steel tool/armor
 * to restore it to full durability in exchange for Hot Steel ingots
 * (1 ingot per 200 durability, minimum 1).
 */
public class HotSteelForgeBlock extends Block {

    /** All Hot Steel equipment that can be repaired at the forge. */
    private static final Set<Item> REPAIRABLE = Set.of(
        ModItems.HOT_STEEL_HELMET, ModItems.HOT_STEEL_CHESTPLATE,
        ModItems.HOT_STEEL_LEGGINGS, ModItems.HOT_STEEL_BOOTS,
        ModItems.HOT_STEEL_SWORD, ModItems.HOT_STEEL_MACE, ModItems.HOT_STEEL_KNIFE,
        ModItems.HOT_STEEL_PICKAXE, ModItems.HOT_STEEL_AXE, ModItems.HOT_STEEL_SHOVEL,
        ModItems.HOT_STEEL_HOE, ModItems.HOT_STEEL_BOW, ModItems.HOT_STEEL_CROSSBOW,
        ModItems.HOT_STEEL_TRIDENT, ModItems.HOT_STEEL_SHIELD);

    /** Hot Steel ingots consumed per 200 durability restored. */
    private static final int DURABILITY_PER_INGOT = 200;

    public HotSteelForgeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                              BlockPos pos, Player player, InteractionHand hand,
                                              BlockHitResult hitResult) {
        if (!REPAIRABLE.contains(stack.getItem()) || stack.getMaxDamage() <= 0) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        int damage = stack.getDamageValue();
        if (damage <= 0) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                    Component.translatable("message.hotsteel.forge_no_damage"), true);
            }
            return ItemInteractionResult.SUCCESS;
        }

        int ingotsNeeded = Math.max(1, (damage + DURABILITY_PER_INGOT - 1) / DURABILITY_PER_INGOT);
        int available = countIngots(player);
        if (available < ingotsNeeded) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                    Component.translatable("message.hotsteel.forge_need_ingots", ingotsNeeded), true);
            }
            return ItemInteractionResult.SUCCESS;
        }

        if (!level.isClientSide) {
            if (!player.getAbilities().instabuild) {
                removeIngots(player, ingotsNeeded);
            }
            stack.setDamageValue(0);
            level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0f, 1.2f);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.LAVA,
                    pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5, 20, 0.3, 0.1, 0.3, 0.0);
            }
            player.displayClientMessage(
                Component.translatable("message.hotsteel.forge_repair", ingotsNeeded), true);
            if (player instanceof ServerPlayer serverPlayer) {
                AdvancementHelper.award(serverPlayer, "forge_repair", "repair_gear");
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    private static int countIngots(Player player) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.HOT_STEEL_INGOT)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void removeIngots(Player player, int amount) {
        for (ItemStack stack : player.getInventory().items) {
            if (amount <= 0) {
                break;
            }
            if (stack.is(ModItems.HOT_STEEL_INGOT)) {
                int removed = Math.min(stack.getCount(), amount);
                stack.shrink(removed);
                amount -= removed;
            }
        }
    }
}
