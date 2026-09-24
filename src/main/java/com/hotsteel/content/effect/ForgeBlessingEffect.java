package com.hotsteel.content.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 「熔炉祝福」— granted by the Forge Heart. While active the bearer cannot burn:
 * fire is extinguished every tick. Fire/lava damage is cancelled in
 * {@link com.hotsteel.logic.SuperFireResistanceHandler}.
 */
public class ForgeBlessingEffect extends MobEffect {

    public ForgeBlessingEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFB020);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.isOnFire()) {
            entity.clearFire();
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
