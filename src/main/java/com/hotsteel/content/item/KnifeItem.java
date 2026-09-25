package com.hotsteel.content.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

/**
 * 「热钢刀」— a light, fast blade. Lower damage than the sword but the quickest
 * swing of the set, and every landed hit grants the wielder a short burst of
 * speed: cut, disengage, cut again.
 */
public class KnifeItem extends SwordItem {

    private static final int SWIFT_TICKS = 60; // 3s

    public KnifeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean hit = super.hurtEnemy(stack, target, attacker);
        if (hit && !attacker.level().isClientSide
            && attacker instanceof Player player) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,
                SWIFT_TICKS, 1, false, false, true));
        }
        return hit;
    }
}
