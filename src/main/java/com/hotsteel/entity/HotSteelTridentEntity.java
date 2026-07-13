package com.hotsteel.entity;

import com.hotsteel.registry.ModEntities;
import com.hotsteel.registry.ModItems;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Thrown Hot Steel trident. Behaves like an arrow that drops the trident on landing. */
public class HotSteelTridentEntity extends AbstractArrow {

    public HotSteelTridentEntity(EntityType<? extends HotSteelTridentEntity> type, Level level) {
        super(type, level);
    }

    public HotSteelTridentEntity(Level level, LivingEntity shooter, ItemStack stack) {
        super(ModEntities.HOT_STEEL_TRIDENT, shooter, level, stack.copy(), null);
        this.setBaseDamage(4.0);
        this.pickup = Pickup.ALLOWED;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.HOT_STEEL_TRIDENT);
    }

    public boolean isFoil() {
        return false;
    }
}
