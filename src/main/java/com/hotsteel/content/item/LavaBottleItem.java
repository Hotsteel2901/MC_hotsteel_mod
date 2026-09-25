package com.hotsteel.content.item;

import com.hotsteel.content.entity.LavaBottleEntity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** A bottle of liquid lava. Throw it to flood the area with lava and start fires. */
public class LavaBottleItem extends Item {

    public LavaBottleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            LavaBottleEntity bottle = new LavaBottleEntity(level, player, stack);
            // Launch from in front of the eyes: the vanilla origin (eyeY - 0.1) is inside the
            // thrower's head, so a downward throw shattered on them and flooded their own feet.
            bottle.setPos(com.hotsteel.logic.LaunchHelper.muzzle(player, 0.6));
            bottle.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 1.5f, 1.0f);
            level.addFreshEntity(bottle);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5f, 0.6f);
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.success(stack);
    }
}
