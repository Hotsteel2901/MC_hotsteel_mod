package com.hotsteel.registry;

import com.hotsteel.HotSteel;
import com.hotsteel.content.effect.ForgeBlessingEffect;
import com.hotsteel.content.effect.SuperFireResistanceEffect;

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

    /** 「熔炉祝福」 — fire immunity granted by the Forge Heart. */
    public static final Holder<MobEffect> FORGE_BLESSING = Registry.registerForHolder(
        BuiltInRegistries.MOB_EFFECT,
        HotSteel.id("forge_blessing"),
        new ForgeBlessingEffect());

    public static void register() {
        HotSteel.LOGGER.info("Registering Hot Steel effects");
    }
}
