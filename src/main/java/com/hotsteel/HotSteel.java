package com.hotsteel;

import com.hotsteel.logic.SuperFireResistanceHandler;
import com.hotsteel.registry.ModBlocks;
import com.hotsteel.registry.ModCreativeTab;
import com.hotsteel.registry.ModEffects;
import com.hotsteel.registry.ModEntities;
import com.hotsteel.registry.ModItems;
import com.hotsteel.registry.ModMaterials;
import net.fabricmc.api.ModInitializer;
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
        ModEntities.register();
        ModEffects.register();
        ModItems.register();
        ModCreativeTab.register();
        SuperFireResistanceHandler.register();
        LOGGER.info("Hot Steel initialized");
    }
}
