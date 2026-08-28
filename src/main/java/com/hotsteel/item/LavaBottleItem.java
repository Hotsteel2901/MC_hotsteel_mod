package com.hotsteel.item;

import com.hotsteel.entity.LavaBottleEntity;

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
