package com.hotsteel.item;

import com.hotsteel.registry.ModEffects;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Hot Steel Apple: a molten-hot apple forged from steel. Eating it grants a
 * solid chunk of Fire Resistance plus a burst of Super Fire Resistance (the
 * armor/potion effect) and a short Regeneration.
 */
public class HotSteelAppleItem extends Item {

    public HotSteelAppleItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
            entity.addEffect(new MobEffectInstance(ModEffects.SUPER_FIRE_RESISTANCE, 300, 0));
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1));
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
