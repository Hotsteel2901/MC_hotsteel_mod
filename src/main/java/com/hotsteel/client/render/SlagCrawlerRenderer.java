package com.hotsteel.client.render;

import com.hotsteel.HotSteel;
import com.hotsteel.client.model.SlagCrawlerModel;
import com.hotsteel.content.entity.SlagCrawlerEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Renders the Slag Crawler with its custom segmented crawler model. */
public class SlagCrawlerRenderer extends MobRenderer<SlagCrawlerEntity, SlagCrawlerModel> {

    public static final ResourceLocation TEXTURE = HotSteel.id("textures/entity/slag_crawler.png");

    public SlagCrawlerRenderer(EntityRendererProvider.Context context) {
        super(context, new SlagCrawlerModel(context.bakeLayer(SlagCrawlerModel.LAYER_LOCATION)), 0.4f);
    }

    @Override
    public ResourceLocation getTextureLocation(SlagCrawlerEntity entity) {
        return TEXTURE;
    }
}
