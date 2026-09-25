package com.hotsteel.content.item;

import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 「热钢战锤」— a heavy smash weapon. It keeps the vanilla mace's high-fall smash,
 * and adds <b>ground breaker</b>: the impact of every landed hit ripples outwards,
 * bruising and shoving everything standing beside the target. Melee hits also
 * ignite targets (shared hot-steel melee mixin).
 */
public class HotSteelMaceItem extends MaceItem {

    private static final double SHOCK_RADIUS = 2.5;
    private static final float SHOCK_DAMAGE = 4.0f;
    private static final double SHOCK_PUSH = 0.6;

    public HotSteelMaceItem(Properties properties) {
        super(properties);
    }

    public static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 8.0, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED,
                new AttributeModifier(BASE_ATTACK_SPEED_ID, -3.0, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND)
            .build();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean hit = super.hurtEnemy(stack, target, attacker);
        if (!hit || attacker.level().isClientSide) {
            return hit;
        }
        AABB shock = target.getBoundingBox().inflate(SHOCK_RADIUS);
        for (LivingEntity bystander : attacker.level().getEntitiesOfClass(LivingEntity.class, shock)) {
            if (bystander == attacker || bystander == target || bystander.isAlliedTo(attacker)) {
                continue;
            }
            if (attacker instanceof Player player) {
                bystander.hurt(attacker.damageSources().playerAttack(player), SHOCK_DAMAGE);
            } else {
                bystander.hurt(attacker.damageSources().mobAttack(attacker), SHOCK_DAMAGE);
            }
            Vec3 push = bystander.position().subtract(attacker.position());
            Vec3 flat = new Vec3(push.x, 0.0, push.z).normalize().scale(SHOCK_PUSH);
            bystander.push(flat.x, 0.3, flat.z);
        }
        return hit;
    }
}
