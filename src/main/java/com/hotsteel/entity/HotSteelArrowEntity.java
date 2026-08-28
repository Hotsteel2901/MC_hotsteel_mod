package com.hotsteel.entity;

import com.hotsteel.registry.ModEntities;
import com.hotsteel.registry.ModItems;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/** Arrow entity shot from a Hot Steel arrow — ignites its target on hit. */
public class HotSteelArrowEntity extends AbstractArrow {

    /** Fire ticks applied to a hit target (5s). */
    private static final int IGNITE_TICKS = 100;

    public HotSteelArrowEntity(EntityType<? extends HotSteelArrowEntity> type, Level level) {
        super(type, level);
    }

    public HotSteelArrowEntity(Level level, LivingEntity shooter, ItemStack stack) {
        super(ModEntities.HOT_STEEL_ARROW, shooter, level, stack.copy(), ItemStack.EMPTY);
        this.setBaseDamage(2.5);
        this.pickup = Pickup.ALLOWED;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.HOT_STEEL_ARROW);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide()
            && result.getEntity() instanceof LivingEntity living) {
            living.setRemainingFireTicks(Math.max(living.getRemainingFireTicks(), IGNITE_TICKS));
        }
    }
}
