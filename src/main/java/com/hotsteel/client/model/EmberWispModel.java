package com.hotsteel.client.model;

import com.hotsteel.HotSteel;
import com.hotsteel.content.entity.EmberWispEntity;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * Model for the Ember Wisp — a hovering molten orb with a pair of flaming wings
 * and a trailing ribbon of embers.
 *
 * <p>The texture atlas is 32x32. The UV rectangles below must stay in sync with
 * {@code tools/gen_all_textures.py :: t_ember_wisp}:
 * <pre>
 *   orb core           texOffs (0, 0)   8x8x8  -&gt;  32x16 px
 *   wing (x2)          texOffs (0, 16)  6x2x4  -&gt;  20x6  px
 *   tail ribbon        texOffs (20, 16) 2x6x2  -&gt;   8x8  px
 *   eye (x2)           texOffs (14, 24) 2x2x1  -&gt;   6x3  px
 * </pre>
 */
public class EmberWispModel extends HierarchicalModel<EmberWispEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
        new ModelLayerLocation(HotSteel.id("ember_wisp"), "main");

    /** Model-space Y of the orb pivot (well above the entity's feet). */
    private static final float CORE_Y = 17.0f;

    private final ModelPart root;
    private final ModelPart core;
    private final ModelPart wingLeft;
    private final ModelPart wingRight;
    private final ModelPart tail;
    private final ModelPart eyeLeft;
    private final ModelPart eyeRight;

    public EmberWispModel(ModelPart root) {
        this.root = root;
        this.core = root.getChild("core");
        this.wingLeft = this.core.getChild("wing_left");
        this.wingRight = this.core.getChild("wing_right");
        this.tail = this.core.getChild("tail");
        this.eyeLeft = this.core.getChild("eye_left");
        this.eyeRight = this.core.getChild("eye_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition core = root.addOrReplaceChild("core",
            CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4.0f, -4.0f, -4.0f, 8, 8, 8, CubeDeformation.NONE),
            PartPose.offset(0.0f, CORE_Y, 0.0f));

        core.addOrReplaceChild("wing_left",
            CubeListBuilder.create().texOffs(0, 16)
                .addBox(0.0f, -1.0f, -2.0f, 6, 2, 4, CubeDeformation.NONE),
            PartPose.offsetAndRotation(4.0f, 0.0f, 0.0f, 0.0f, 0.3f, -0.2f));
        core.addOrReplaceChild("wing_right",
            CubeListBuilder.create().texOffs(0, 16)
                .addBox(-6.0f, -1.0f, -2.0f, 6, 2, 4, CubeDeformation.NONE),
            PartPose.offsetAndRotation(-4.0f, 0.0f, 0.0f, 0.0f, -0.3f, 0.2f));

        core.addOrReplaceChild("tail",
            CubeListBuilder.create().texOffs(20, 16)
                .addBox(-1.0f, 0.0f, -1.0f, 2, 6, 2, CubeDeformation.NONE),
            PartPose.offset(0.0f, 4.0f, 0.0f));

        core.addOrReplaceChild("eye_left",
            CubeListBuilder.create().texOffs(14, 24)
                .addBox(-1.0f, -1.0f, -1.0f, 2, 2, 1, CubeDeformation.NONE),
            PartPose.offset(2.0f, -1.0f, -4.0f));
        core.addOrReplaceChild("eye_right",
            CubeListBuilder.create().texOffs(14, 24)
                .addBox(-1.0f, -1.0f, -1.0f, 2, 2, 1, CubeDeformation.NONE),
            PartPose.offset(-2.0f, -1.0f, -4.0f));

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(EmberWispEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float degToRad = (float) (Math.PI / 180.0);

        // Hover: the orb drifts up and down and slowly turns to face its target.
        this.core.y = CORE_Y + Mth.sin(ageInTicks * 0.18f) * 1.1f;
        this.core.xRot = Mth.sin(ageInTicks * 0.11f) * 0.08f;
        this.core.yRot = netHeadYaw * degToRad * 0.5f;

        // Wing beat: a fast flap plus a slow outward sweep.
        float flap = Mth.sin(ageInTicks * 1.1f);
        float sweep = Mth.cos(ageInTicks * 0.7f) * 0.12f;
        this.wingLeft.xRot = flap * 0.2f;
        this.wingRight.xRot = flap * 0.2f;
        this.wingLeft.yRot = 0.3f + sweep;
        this.wingRight.yRot = -0.3f - sweep;
        this.wingLeft.zRot = -0.2f + flap * 0.55f;
        this.wingRight.zRot = 0.2f - flap * 0.55f;

        // Ember ribbon trails behind the hover bob.
        this.tail.xRot = Mth.cos(ageInTicks * 0.24f) * 0.15f + 0.1f;
        this.tail.zRot = Mth.sin(ageInTicks * 0.3f) * 0.2f;
    }
}
