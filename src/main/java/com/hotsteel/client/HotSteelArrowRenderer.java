package com.hotsteel.client;

import com.hotsteel.HotSteel;
import com.hotsteel.entity.HotSteelArrowEntity;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** Renders the Hot Steel arrow with its own molten-hot texture. */
public class HotSteelArrowRenderer extends ArrowRenderer<HotSteelArrowEntity> {

    public static final ResourceLocation TEXTURE = HotSteel.id("textures/entity/hot_steel_arrow.png");

    public HotSteelArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(HotSteelArrowEntity entity) {
        return TEXTURE;
    }
}
