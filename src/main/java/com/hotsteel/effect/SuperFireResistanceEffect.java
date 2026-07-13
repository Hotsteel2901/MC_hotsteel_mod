package com.hotsteel.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Marker effect used only as an on-screen icon + countdown for "Super Fire Resistance".
 * The actual behaviour (fire/lava immunity, lava swimming) lives in
 * {@link com.hotsteel.logic.SuperFireResistanceHandler}.
 */
public class SuperFireResistanceEffect extends MobEffect {

    public SuperFireResistanceEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF7A1E);
    }
}
