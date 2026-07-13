package com.hotsteel.registry;

import com.hotsteel.HotSteel;
import com.hotsteel.effect.SuperFireResistanceEffect;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

public final class ModEffects {

    private ModEffects() {}

    public static final Holder<MobEffect> SUPER_FIRE_RESISTANCE = Registry.registerForHolder(
        BuiltInRegistries.MOB_EFFECT,
        HotSteel.id("super_fire_resistance"),
        new SuperFireResistanceEffect());

    public static void register() {
        HotSteel.LOGGER.info("Registering Hot Steel effects");
    }
}
