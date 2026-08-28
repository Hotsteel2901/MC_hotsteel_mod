package com.hotsteel.item;

import com.hotsteel.logic.AdvancementHelper;
import com.hotsteel.registry.ModEffects;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/**
 * Molten Core: the blazing heart of a Fire Wraith. Right-click to consume it
 * and instantly smelt every smeltable item in your inventory (using the real
 * furnace recipe map — no furnace needed), while gaining a short burst of
 * Super Fire Resistance.
 */
public class MoltenCoreItem extends Item {

    public MoltenCoreItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide) {
            int smelted = smeltInventory(level, player);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            player.getCooldowns().addCooldown(this, 20);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.PLAYERS, 0.9f, 0.8f);
            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.LAVA,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    40, 0.5, 0.5, 0.5, 0.0);
            }
            player.addEffect(new MobEffectInstance(ModEffects.SUPER_FIRE_RESISTANCE, 400, 0));
            if (player instanceof ServerPlayer sp) {
                AdvancementHelper.award(sp, "molten_core_use", "use_molten_core");
            }
        }
        return InteractionResultHolder.success(stack);
    }

    /** Smelt every furnace-smeltable stack in the player's inventory in place. */
    private int smeltInventory(Level level, Player player) {
        int smelted = 0;
        var recipes = level.getRecipeManager();
        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack slot = inventory.getItem(i);
            if (slot.isEmpty()) {
                continue;
            }
            SingleRecipeInput input = new SingleRecipeInput(slot);
            var found = recipes.getRecipeFor(RecipeType.SMELTING, input, level);
            if (found.isPresent()) {
                ItemStack result = found.get().value().assemble(input, level.registryAccess());
                if (!result.isEmpty()) {
                    inventory.setItem(i, new ItemStack(result.getItem(), slot.getCount()));
                    smelted += slot.getCount();
                }
            }
        }
        return smelted;
    }
}
