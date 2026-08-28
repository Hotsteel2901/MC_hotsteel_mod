package com.hotsteel.entity;

import com.hotsteel.registry.ModEntities;
import com.hotsteel.registry.ModItems;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Arrow entity shot from a Hot Steel arrow. Flies exactly like a vanilla arrow
 * (works with bows, crossbows and dispensers) but uses its own entity type so it
 * renders with the molten-hot texture, and ignites whatever it hits.
 * <p>
 * IMPORTANT: the constructor must receive a non-null/non-empty {@code leftover}
 * weapon stack when shot from a bow/crossbow — vanilla throws
 * {@code IllegalArgumentException("Invalid weapon firing an arrow")} when the
 * leftover is an empty stack.
 */
public class HotSteelArrowEntity extends AbstractArrow {

    /** Fire ticks applied to a hit target (5s). */
    private static final int IGNITE_TICKS = 100;

    public HotSteelArrowEntity(EntityType<? extends HotSteelArrowEntity> type, Level level) {
        super(type, level);
    }

    /** Shot from a bow / crossbow by a living shooter (leftover = the weapon stack). */
    public HotSteelArrowEntity(Level level, LivingEntity shooter, ItemStack pickup, ItemStack leftover) {
        super(ModEntities.HOT_STEEL_ARROW, shooter, level, pickup, leftover);
        this.setBaseDamage(2.5);
        this.pickup = Pickup.ALLOWED;
    }

    /** Dispensed from a dispenser/dropper (no shooter). */
    public HotSteelArrowEntity(Level level, double x, double y, double z,
                               ItemStack pickup, ItemStack leftover) {
        super(ModEntities.HOT_STEEL_ARROW, x, y, z, level, pickup, leftover);
        this.setBaseDamage(2.5);
        this.pickup = Pickup.ALLOWED;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.HOT_STEEL_ARROW);
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        if (!this.level().isClientSide()) {
            target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), IGNITE_TICKS));
        }
    }
}
