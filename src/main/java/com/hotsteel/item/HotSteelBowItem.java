package com.hotsteel.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/** Hot Steel bow: sturdier than vanilla and its arrows hit harder. */
public class HotSteelBowItem extends BowItem {

    public HotSteelBowItem(Properties properties) {
        super(properties);
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammo, boolean crit) {
        ArrowItem arrowItem = ammo.getItem() instanceof ArrowItem ai ? ai : (ArrowItem) Items.ARROW;
        AbstractArrow arrow = arrowItem.createArrow(level, ammo, shooter, weapon);
        if (crit) {
            arrow.setCritArrow(true);
        }
        arrow.setBaseDamage(arrow.getBaseDamage() + 1.5);
        return arrow;
    }
}
