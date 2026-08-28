package com.hotsteel.client;

import com.hotsteel.HotSteel;
import com.hotsteel.entity.LavaGolemEntity;

import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Renders the Lava Golem with the iron-golem model but a molten-hot texture. */
public class LavaGolemRenderer extends MobRenderer<LavaGolemEntity, IronGolemModel<LavaGolemEntity>> {

    public static final ResourceLocation TEXTURE = HotSteel.id("textures/entity/lava_golem.png");

    public LavaGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new IronGolemModel<>(context.bakeLayer(ModelLayers.IRON_GOLEM)), 0.7f);
    }

    @Override
    public ResourceLocation getTextureLocation(LavaGolemEntity entity) {
        return TEXTURE;
    }
}
