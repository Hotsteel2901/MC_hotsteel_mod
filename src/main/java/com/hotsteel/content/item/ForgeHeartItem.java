package com.hotsteel.content.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

import com.hotsteel.registry.ModEffects;

/**
 * 「熔炉之心」— the trophy of the final chapter. Right-clicking it invokes the
 * blessing of the Molten Age: 60 seconds of Forge Blessing (fire/lava immunity
 * plus igniting attacks). The heart is consumed and then goes on cooldown.
 */
public class ForgeHeartItem extends Item {

    private static final int BLESSING_TICKS = 1200; // 60s
    private static final int COOLDOWN_TICKS = 600;  // 30s

    public ForgeHeartItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide) {
            player.addEffect(new MobEffectInstance(ModEffects.FORGE_BLESSING,
                BLESSING_TICKS, 0, false, true, true));
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.FLAME,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    40, 0.5, 0.8, 0.5, 0.05);
                serverLevel.sendParticles(ParticleTypes.LAVA,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    16, 0.4, 0.6, 0.4, 0.0);
            }
            stack.shrink(1);
            // Server-side only: playing it on both sides made the client hear it twice.
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.9f, 1.4f);
        }
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
