package com.hotsteel;

import com.hotsteel.entity.LavaGolemEntity;
import com.hotsteel.logic.SuperFireResistanceHandler;
import com.hotsteel.registry.ModBlocks;
import com.hotsteel.registry.ModBlockEntities;
import com.hotsteel.registry.ModCreativeTab;
import com.hotsteel.registry.ModEffects;
import com.hotsteel.registry.ModEntities;
import com.hotsteel.registry.ModItems;
import com.hotsteel.registry.ModMaterials;
import com.hotsteel.registry.ModPotions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HotSteel implements ModInitializer {
    public static final String MOD_ID = "hotsteel";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModMaterials.init();
        ModBlocks.register();
        ModBlockEntities.register();
        ModEntities.register();
        ModEffects.register();
        ModItems.register();
        ModPotions.register();
        ModCreativeTab.register();
        FabricDefaultAttributeRegistry.register(ModEntities.LAVA_GOLEM, LavaGolemEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.FIRE_WRAITH,
            net.minecraft.world.entity.monster.Blaze.createAttributes());
        // Fire Wraiths haunt the Nether like Blazes.
        net.fabricmc.fabric.api.biome.v1.BiomeModifications.addSpawn(
            net.fabricmc.fabric.api.biome.v1.BiomeSelectors.tag(
                net.minecraft.tags.BiomeTags.IS_NETHER),
            net.minecraft.world.entity.MobCategory.MONSTER,
            ModEntities.FIRE_WRAITH, 12, 1, 3);
        SuperFireResistanceHandler.register();
        LOGGER.info("Hot Steel initialized");
    }
}
