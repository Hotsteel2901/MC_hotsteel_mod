package com.hotsteel.client;

import com.hotsteel.HotSteel;
import com.hotsteel.entity.FireWraithEntity;

import net.minecraft.client.model.BlazeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Renders the Fire Wraith with the Blaze model but a molten wraith texture. */
public class FireWraithRenderer extends MobRenderer<FireWraithEntity, BlazeModel<FireWraithEntity>> {

    public static final ResourceLocation TEXTURE = HotSteel.id("textures/entity/fire_wraith.png");

    public FireWraithRenderer(EntityRendererProvider.Context context) {
        super(context, new BlazeModel<>(context.bakeLayer(ModelLayers.BLAZE)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(FireWraithEntity entity) {
        return TEXTURE;
    }

    @Override
    protected int getBlockLightLevel(FireWraithEntity entity, net.minecraft.core.BlockPos pos) {
        return 15;
    }
}
