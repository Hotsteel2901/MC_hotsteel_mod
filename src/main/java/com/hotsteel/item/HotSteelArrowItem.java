package com.hotsteel.item;

import com.hotsteel.entity.HotSteelArrowEntity;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Hot Steel arrow: flies like a normal arrow but ignites whatever it hits. */
public class HotSteelArrowItem extends ArrowItem {

    public HotSteelArrowItem(Properties properties) {
        super(properties);
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity shooter,
                                     ItemStack leftoverItemStack) {
        return new HotSteelArrowEntity(level, shooter, stack.copy(), leftoverItemStack);
    }

    /** Dispenser support: shoot a Hot Steel arrow (no shooter, leftover must be null). */
    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack stack, Direction direction) {
        HotSteelArrowEntity arrow = new HotSteelArrowEntity(
            level, position.x(), position.y(), position.z(), stack.copy(), null);
        arrow.pickup = AbstractArrow.Pickup.ALLOWED;
        return arrow;
    }
}
