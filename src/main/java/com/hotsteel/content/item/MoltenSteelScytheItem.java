package com.hotsteel.content.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.phys.AABB;

/**
 * 「熔铸战镰」— a heavy sweeping weapon. Every hit also scorches the enemies
 * standing beside the target (3×3 area around it) and sets them alight, and every
 * kill feeds the wielder: the scythe drinks the dying heat and heals its owner.
 */
public class MoltenSteelScytheItem extends SwordItem {

    private static final float SWEEP_DAMAGE = 6.0f;
    private static final double SWEEP_RADIUS = 2.0;
    private static final int SWEEP_FIRE_TICKS = 80;
    private static final float KILL_HEAL = 2.0f;

    public MoltenSteelScytheItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        if (attacker.level().isClientSide) {
            return result;
        }
        AABB box = target.getBoundingBox().inflate(SWEEP_RADIUS);
        for (LivingEntity bystander : attacker.level().getEntitiesOfClass(LivingEntity.class, box)) {
            if (bystander == attacker || bystander == target || bystander.isAlliedTo(attacker)) {
                continue;
            }
            if (attacker instanceof Player player) {
                bystander.hurt(attacker.damageSources().playerAttack(player), SWEEP_DAMAGE);
            } else {
                bystander.hurt(attacker.damageSources().mobAttack(attacker), SWEEP_DAMAGE);
            }
            bystander.setRemainingFireTicks(
                Math.max(bystander.getRemainingFireTicks(), SWEEP_FIRE_TICKS));
            attacker.level().broadcastEntityEvent(bystander, (byte) 37); // burning animation
        }
        // 熔铸战镰 · 汲取: a kill returns health to the wielder.
        if (target.isDeadOrDying() && attacker.getHealth() < attacker.getMaxHealth()) {
            attacker.heal(KILL_HEAL);
        }
        return result;
    }
}
