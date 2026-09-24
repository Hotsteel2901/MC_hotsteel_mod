package com.hotsteel.client.render;

import com.hotsteel.HotSteel;
import com.hotsteel.client.model.AncientForgebornModel;
import com.hotsteel.content.entity.AncientForgebornEntity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Renders the Ancient Forgeborn boss with its custom molten colossus model. */
public class AncientForgebornRenderer
    extends MobRenderer<AncientForgebornEntity, AncientForgebornModel> {

    public static final ResourceLocation TEXTURE =
        HotSteel.id("textures/entity/ancient_forgeborn.png");

    /**
     * The model is built 56 px (3.5 blocks) tall on a 3.4 block hitbox, so the
     * rendered scale is trimmed to match the collision box.
     */
    private static final float SCALE = 3.4f * 16.0f / 56.0f;

    public AncientForgebornRenderer(EntityRendererProvider.Context context) {
        super(context, new AncientForgebornModel(context.bakeLayer(AncientForgebornModel.LAYER_LOCATION)), 1.0f);
    }

    @Override
    public ResourceLocation getTextureLocation(AncientForgebornEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(AncientForgebornEntity entity, PoseStack poseStack, float partialTicks) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }
}
