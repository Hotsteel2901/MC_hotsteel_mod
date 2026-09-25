package com.hotsteel.content.item;

import com.hotsteel.logic.LaunchHelper;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 「热钢火球」— a real explosive fireball, thrown like a ghast's.
 * <p>
 * It used to spawn a {@code SmallFireball} (the blaze's non-explosive bolt) right at the
 * player's eyes, so it never exploded despite the description and detonated in the
 * thrower's face whenever they aimed downwards. Now it launches from a muzzle point in
 * front of the eyes and detonates on impact.
 */
public class HotSteelFireballItem extends Item {

    private static final int EXPLOSION_POWER = 1;
    private static final int COOLDOWN_TICKS = 20;
    private static final double MUZZLE_FORWARD = 0.8;

    public HotSteelFireballItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide) {
            // The fireball's flight is driven by the projectile's own acceleration, so the
            // constructor takes a direction (it normalises internally) plus the blast power.
            Vec3 dir = player.getLookAngle();
            LargeFireball fireball = new LargeFireball(level, player, dir, EXPLOSION_POWER);
            fireball.setPos(LaunchHelper.muzzle(player, MUZZLE_FORWARD));
            level.addFreshEntity(fireball);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GHAST_SHOOT, SoundSource.PLAYERS, 0.8f, 1.3f);
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
            if (level instanceof ServerLevel) {
                player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
            }
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.success(stack);
    }
}
