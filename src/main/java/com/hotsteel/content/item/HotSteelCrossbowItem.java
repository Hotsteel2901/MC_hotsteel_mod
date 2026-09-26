package com.hotsteel.content.item;

import com.hotsteel.logic.HotSteelRangedHandler;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 「热钢弩」— a heavier crossbow than the vanilla one.
 *
 * <p>Every bolt it launches carries extra punch ({@link HotSteelRangedHandler#heavyBolt})
 * and detonates on impact: the loadout is the burst, so the crossbow plays as a
 * close-quarters siege weapon while the bow stays the precise, igniting one.
 */
public class HotSteelCrossbowItem extends CrossbowItem {

    public HotSteelCrossbowItem(Properties properties) {
        super(properties);
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon,
                                          ItemStack ammo, boolean crit) {
        Projectile projectile = super.createProjectile(level, shooter, weapon, ammo, crit);
        if (projectile instanceof AbstractArrow arrow) {
            HotSteelRangedHandler.heavyBolt(arrow);
        }
        return projectile;
    }
}