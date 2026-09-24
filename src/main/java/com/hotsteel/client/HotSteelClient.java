package com.hotsteel.client;

import com.hotsteel.client.model.AncientForgebornModel;
import com.hotsteel.client.model.EmberWispModel;
import com.hotsteel.client.model.SlagCrawlerModel;
import com.hotsteel.client.render.AncientForgebornRenderer;
import com.hotsteel.client.render.EmberWispRenderer;
import com.hotsteel.client.render.FireWraithRenderer;
import com.hotsteel.client.render.HotSteelArrowRenderer;
import com.hotsteel.client.render.HotSteelTridentRenderer;
import com.hotsteel.client.render.LavaGolemRenderer;
import com.hotsteel.client.render.SlagCrawlerRenderer;
import com.hotsteel.registry.ModBlocks;
import com.hotsteel.registry.ModEntities;
import com.hotsteel.registry.ModItems;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;

public class HotSteelClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.HOT_STEEL_TRIDENT, HotSteelTridentRenderer::new);
        EntityRendererRegistry.register(ModEntities.HOT_STEEL_ARROW, HotSteelArrowRenderer::new);
        EntityRendererRegistry.register(ModEntities.LAVA_BOTTLE, ThrownItemRenderer::new);
        EntityRendererRegistry.register(ModEntities.LAVA_GOLEM, LavaGolemRenderer::new);
        EntityRendererRegistry.register(ModEntities.FIRE_WRAITH, FireWraithRenderer::new);
        EntityRendererRegistry.register(ModEntities.SLAG_CRAWLER, SlagCrawlerRenderer::new);
        EntityRendererRegistry.register(ModEntities.EMBER_WISP, EmberWispRenderer::new);
        EntityRendererRegistry.register(ModEntities.ANCIENT_FORGEBORN, AncientForgebornRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(SlagCrawlerModel.LAYER_LOCATION,
            SlagCrawlerModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(EmberWispModel.LAYER_LOCATION,
            EmberWispModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(AncientForgebornModel.LAYER_LOCATION,
            AncientForgebornModel::createBodyLayer);

        // Molten glass is translucent; 1.21.1 has no "render_type" in block models yet,
        // so the layer has to be assigned here.
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.MOLTEN_GLASS, RenderType.translucent());

        ResourceLocation pull = ResourceLocation.withDefaultNamespace("pull");
        ResourceLocation pulling = ResourceLocation.withDefaultNamespace("pulling");
        ResourceLocation charged = ResourceLocation.withDefaultNamespace("charged");
        ResourceLocation firework = ResourceLocation.withDefaultNamespace("firework");

        // Bow
        ItemProperties.register(ModItems.HOT_STEEL_BOW, pull, (stack, level, entity, seed) -> {
            if (entity == null || entity.getUseItem() != stack) {
                return 0.0f;
            }
            return (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / 20.0f;
        });
        ItemProperties.register(ModItems.HOT_STEEL_BOW, pulling, (stack, level, entity, seed) ->
            entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0f : 0.0f);

        // Crossbow
        ItemProperties.register(ModItems.HOT_STEEL_CROSSBOW, pull, (stack, level, entity, seed) -> {
            if (entity == null) {
                return 0.0f;
            }
            if (CrossbowItem.isCharged(stack)) {
                return 0.0f;
            }
            return (float) (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks())
                / CrossbowItem.getChargeDuration(stack, entity);
        });
        ItemProperties.register(ModItems.HOT_STEEL_CROSSBOW, pulling, (stack, level, entity, seed) ->
            entity != null && entity.isUsingItem() && entity.getUseItem() == stack
                && !CrossbowItem.isCharged(stack) ? 1.0f : 0.0f);
        ItemProperties.register(ModItems.HOT_STEEL_CROSSBOW, charged, (stack, level, entity, seed) ->
            CrossbowItem.isCharged(stack) ? 1.0f : 0.0f);
        ItemProperties.register(ModItems.HOT_STEEL_CROSSBOW, firework, (stack, level, entity, seed) -> {
            ChargedProjectiles cp = stack.get(DataComponents.CHARGED_PROJECTILES);
            return cp != null && cp.contains(Items.FIREWORK_ROCKET) ? 1.0f : 0.0f;
        });

        // Shield: raise-to-front pose when blocking
        ItemProperties.register(ModItems.HOT_STEEL_SHIELD,
            ResourceLocation.withDefaultNamespace("blocking"),
            (stack, level, entity, seed) ->
                entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0f : 0.0f);
    }
}
