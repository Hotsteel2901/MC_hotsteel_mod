package com.hotsteel.registry;

import com.hotsteel.HotSteel;

import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;

/** Potions added by the mod (brewing recipes are registered in {@link #register()}). */
public final class ModPotions {

    private ModPotions() {}

    /** 60s of Super Fire Resistance — fire/lava immunity without any armor. */
    public static final Holder<Potion> SUPER_FIRE_RESISTANCE = Registry.registerForHolder(
        BuiltInRegistries.POTION,
        HotSteel.id("super_fire_resistance"),
        new Potion(new MobEffectInstance(ModEffects.SUPER_FIRE_RESISTANCE, 1200, 0)));

    /** Extended: 4 minutes of Super Fire Resistance. */
    public static final Holder<Potion> SUPER_FIRE_RESISTANCE_LONG = Registry.registerForHolder(
        BuiltInRegistries.POTION,
        HotSteel.id("super_fire_resistance_long"),
        new Potion(new MobEffectInstance(ModEffects.SUPER_FIRE_RESISTANCE, 4800, 0)));

    /** Strong: level II — even faster lava movement (30s). */
    public static final Holder<Potion> SUPER_FIRE_RESISTANCE_STRONG = Registry.registerForHolder(
        BuiltInRegistries.POTION,
        HotSteel.id("super_fire_resistance_strong"),
        new Potion(new MobEffectInstance(ModEffects.SUPER_FIRE_RESISTANCE, 600, 1)));

    public static void register() {
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
            // Awkward potion + hot steel ingot -> Potion of Super Fire Resistance
            builder.addMix(Potions.AWKWARD, ModItems.HOT_STEEL_INGOT, SUPER_FIRE_RESISTANCE);
            // Redstone -> extended, glowstone -> strong
            builder.addMix(SUPER_FIRE_RESISTANCE, Items.REDSTONE, SUPER_FIRE_RESISTANCE_LONG);
            builder.addMix(SUPER_FIRE_RESISTANCE, Items.GLOWSTONE_DUST, SUPER_FIRE_RESISTANCE_STRONG);
        });
        HotSteel.LOGGER.info("Registering Hot Steel potions");
    }
}
