package com.hotsteel.client.render;

import com.hotsteel.HotSteel;
import com.hotsteel.client.model.AncientForgebornModel;
import com.hotsteel.content.entity.AncientForgebornEntity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renders the Ancient Forgeborn. The boss is three creatures in one rig, so the
 * renderer picks the texture, and the scale, from the form it is currently in.
 *
 * <p>Scale maths: the model is built on the vanilla convention (the ground plane
 * is model y = 24 px, i.e. 1.5 blocks above the render origin), so the world
 * height of the drawn model is {@code scale * heightPx / 16}. Setting
 * {@code scale = hitboxHeight * 16 / heightPx} makes the art match the collision
 * box exactly, and the follow-up translate re-seats the feet on the ground —
 * without it a shorter model floats, because the vanilla {@code -1.501} offset
 * assumes a 1.5-block-tall pivot.
 */
public class AncientForgebornRenderer
    extends MobRenderer<AncientForgebornEntity, AncientForgebornModel> {

    public static final ResourceLocation PHASE_1_TEXTURE =
        HotSteel.id("textures/entity/ancient_forgeborn.png");
    public static final ResourceLocation PHASE_2_TEXTURE =
        HotSteel.id("textures/entity/ancient_forgeborn_ascendant.png");
    public static final ResourceLocation PHASE_3_TEXTURE =
        HotSteel.id("textures/entity/ancient_forgeborn_horror.png");

    /** Vanilla render origin: model y = 24 px sits 1.5 blocks above the entity's feet. */
    private static final float GROUND_PX = 24.0f;

    public AncientForgebornRenderer(EntityRendererProvider.Context context) {
        super(context, new AncientForgebornModel(context.bakeLayer(AncientForgebornModel.LAYER_LOCATION)), 2.0f);
    }

    @Override
    public ResourceLocation getTextureLocation(AncientForgebornEntity entity) {
        return switch (entity.getPhase()) {
            case 2 -> PHASE_2_TEXTURE;
            case 3 -> PHASE_3_TEXTURE;
            default -> PHASE_1_TEXTURE;
        };
    }

    @Override
    protected void scale(AncientForgebornEntity entity, PoseStack poseStack, float partialTicks) {
        float heightPx = switch (entity.getPhase()) {
            case 2 -> AncientForgebornModel.FORM2_HEIGHT_PX;
            case 3 -> AncientForgebornModel.FORM3_HEIGHT_PX;
            default -> AncientForgebornModel.FORM1_HEIGHT_PX;
        };
        float scale = entity.getBbHeight() * 16.0f / heightPx;
        poseStack.scale(scale, scale, scale);
        // Re-seat the feet: world y of the ground plane is 1.501 - scale * (GROUND_PX/16),
        // which is only 0 for scale == 1. Shift by the difference.
        poseStack.translate(0.0f, 1.501f / scale - GROUND_PX / 16.0f, 0.0f);
    }
}