package com.hotsteel.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Throwable Hot Steel fireball — a small fireball that explodes and sets things alight. */
public class HotSteelFireballItem extends Item {

    public HotSteelFireballItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            SmallFireball fireball = new SmallFireball(level, player, player.getLookAngle().scale(2.0));
            level.addFreshEntity(fireball);
            player.getCooldowns().addCooldown(this, 8);
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.success(stack);
    }
}
