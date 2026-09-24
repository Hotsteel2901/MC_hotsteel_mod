package com.hotsteel.client.render;

import com.hotsteel.HotSteel;
import com.hotsteel.client.model.EmberWispModel;
import com.hotsteel.content.entity.EmberWispEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Renders the Ember Wisp with its custom hovering orb-and-wings model. */
public class EmberWispRenderer extends MobRenderer<EmberWispEntity, EmberWispModel> {

    public static final ResourceLocation TEXTURE = HotSteel.id("textures/entity/ember_wisp.png");

    public EmberWispRenderer(EntityRendererProvider.Context context) {
        super(context, new EmberWispModel(context.bakeLayer(EmberWispModel.LAYER_LOCATION)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(EmberWispEntity entity) {
        return TEXTURE;
    }
}
