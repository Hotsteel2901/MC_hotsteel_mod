package com.hotsteel.registry;

import com.hotsteel.HotSteel;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeTab {

    private ModCreativeTab() {}

    public static final ResourceKey<CreativeModeTab> HOT_STEEL_TAB_KEY =
        ResourceKey.create(Registries.CREATIVE_MODE_TAB, HotSteel.id("hot_steel"));

    public static final CreativeModeTab HOT_STEEL_TAB = Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB,
        HOT_STEEL_TAB_KEY,
        FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModItems.HOT_STEEL_INGOT))
            .title(Component.translatable("itemGroup.hotsteel.hot_steel"))
            .displayItems((params, output) -> {
                output.accept(ModItems.CRUDE_STEEL);
                output.accept(ModBlocks.CRUDE_STEEL_BLOCK);
                output.accept(ModItems.STEEL_INGOT);
                output.accept(ModItems.HOT_STEEL_INGOT);

                output.accept(ModItems.HOT_STEEL_HELMET);
                output.accept(ModItems.HOT_STEEL_CHESTPLATE);
                output.accept(ModItems.HOT_STEEL_LEGGINGS);
                output.accept(ModItems.HOT_STEEL_BOOTS);

                output.accept(ModItems.HOT_STEEL_SWORD);
                output.accept(ModItems.HOT_STEEL_KNIFE);
                output.accept(ModItems.HOT_STEEL_PICKAXE);
                output.accept(ModItems.HOT_STEEL_AXE);
                output.accept(ModItems.HOT_STEEL_SHOVEL);
                output.accept(ModItems.HOT_STEEL_HOE);

                output.accept(ModItems.HOT_STEEL_BOW);
                output.accept(ModItems.HOT_STEEL_CROSSBOW);
                output.accept(ModItems.HOT_STEEL_TRIDENT);
                output.accept(ModItems.HOT_STEEL_SHIELD);
            })
            .build());

    public static void register() {
        HotSteel.LOGGER.info("Registering Hot Steel creative tab");
    }
}
