package com.hotsteel.client.model;

import com.hotsteel.HotSteel;
import com.hotsteel.content.entity.AncientForgebornEntity;

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
 * Model for the Ancient Forgeborn — a towering, heavily-plated humanoid forged
 * from molten steel, with a glowing forge-core in its chest.
 *
 * <p>Built to the boss's 1.6 x 3.4 block hitbox: the feet rest on the model
 * ground plane (y = 24) and the horn tips reach y = -32, i.e. 56 px = 3.5 blocks
 * tall and 27 px = 1.69 blocks across the shoulders. {@code AncientForgebornRenderer}
 * scales the rendered model by 3.4/3.5 so it matches the hitbox.
 *
 * <p>The texture atlas is 128x128. The UV rectangles below must stay in sync with
 * {@code tools/gen_all_textures.py :: t_ancient_forgeborn}:
 * <pre>
 *   leg (x2)           texOffs (0, 0)    7x20x7  -&gt; 28x27 px
 *   torso              texOffs (0, 28)  20x26x12 -&gt; 64x38 px
 *   chest core         texOffs (64, 28) 10x10x2  -&gt; 24x12 px
 *   upper arm (x2)     texOffs (64, 41)  6x14x6  -&gt; 24x20 px
 *   forearm (x2)       texOffs (64, 63)  6x12x6  -&gt; 24x18 px
 *   fist (x2)          texOffs (92, 41)  6x7x6   -&gt; 24x13 px
 *   head               texOffs (0, 67)  14x8x14  -&gt; 56x22 px
 *   horn (x2)          texOffs (56, 67)  3x4x3   -&gt; 12x7  px
 *   eye (x2)           texOffs (56, 75)  3x3x1   -&gt;  8x4  px
 *   pauldron (x2)      texOffs (88, 67)  6x6x6   -&gt; 24x12 px
 * </pre>
 */
public class AncientForgebornModel extends HierarchicalModel<AncientForgebornEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
        new ModelLayerLocation(HotSteel.id("ancient_forgeborn"), "main");

    /** Root of the torso; everything but the legs hangs off it. */
    private static final float TORSO_Y = 4.0f;

    private final ModelPart root;
    private final ModelPart torso;
    private final ModelPart chestCore;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightForearm;
    private final ModelPart leftForearm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public AncientForgebornModel(ModelPart root) {
        this.root = root;
        this.torso = root.getChild("torso");
        this.chestCore = this.torso.getChild("chest_core");
        this.head = this.torso.getChild("head");
        this.rightArm = this.torso.getChild("rightArm");
        this.leftArm = this.torso.getChild("leftArm");
        this.rightForearm = this.rightArm.getChild("right_forearm");
        this.leftForearm = this.leftArm.getChild("left_forearm");
        this.rightLeg = root.getChild("rightLeg");
        this.leftLeg = root.getChild("leftLeg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 0)
            .addBox(-3.5f, 0.0f, -3.5f, 7, 20, 7, CubeDeformation.NONE);
        root.addOrReplaceChild("rightLeg", leg, PartPose.offset(-6.0f, TORSO_Y, 0.0f));
        root.addOrReplaceChild("leftLeg",
            CubeListBuilder.create().texOffs(0, 0)
                .addBox(-3.5f, 0.0f, -3.5f, 7, 20, 7, CubeDeformation.NONE),
            PartPose.offset(6.0f, TORSO_Y, 0.0f));

        PartDefinition torso = root.addOrReplaceChild("torso",
            CubeListBuilder.create().texOffs(0, 28)
                .addBox(-10.0f, -26.0f, -6.0f, 20, 26, 12, CubeDeformation.NONE),
            PartPose.offset(0.0f, TORSO_Y, 0.0f));

        torso.addOrReplaceChild("chest_core",
            CubeListBuilder.create().texOffs(64, 28)
                .addBox(-5.0f, -18.0f, -7.0f, 10, 10, 2, CubeDeformation.NONE),
            PartPose.offset(0.0f, 0.0f, 0.0f));

        // Head: helmet block with two crown horns and two glowing eye slits.
        torso.addOrReplaceChild("head",
            CubeListBuilder.create().texOffs(0, 67)
                .addBox(-7.0f, -8.0f, -7.0f, 14, 8, 14, CubeDeformation.NONE)
                .addBox("horn_left", -8.0f, -10.0f, -3.0f, 3, 4, 3, CubeDeformation.NONE, 56, 67)
                .addBox("horn_right", 5.0f, -10.0f, -3.0f, 3, 4, 3, CubeDeformation.NONE, 56, 67)
                .addBox("eye_left", 1.0f, -6.0f, -8.0f, 3, 3, 1, CubeDeformation.NONE, 56, 75)
                .addBox("eye_right", -4.0f, -6.0f, -8.0f, 3, 3, 1, CubeDeformation.NONE, 56, 75),
            PartPose.offset(0.0f, -26.0f, 0.0f));

        CubeListBuilder pauldron = CubeListBuilder.create().texOffs(88, 67)
            .addBox(-3.0f, -2.0f, -3.0f, 6, 6, 6, CubeDeformation.NONE);
        torso.addOrReplaceChild("right_pauldron", pauldron, PartPose.offset(-10.5f, -20.0f, 0.0f));
        torso.addOrReplaceChild("left_pauldron",
            CubeListBuilder.create().texOffs(88, 67)
                .addBox(-3.0f, -2.0f, -3.0f, 6, 6, 6, CubeDeformation.NONE),
            PartPose.offset(10.5f, -20.0f, 0.0f));

        PartDefinition rightArm = torso.addOrReplaceChild("rightArm",
            CubeListBuilder.create().texOffs(64, 41)
                .addBox(-3.0f, 0.0f, -3.0f, 6, 14, 6, CubeDeformation.NONE),
            PartPose.offset(-10.5f, -18.0f, 0.0f));
        PartDefinition leftArm = torso.addOrReplaceChild("leftArm",
            CubeListBuilder.create().texOffs(64, 41)
                .addBox(-3.0f, 0.0f, -3.0f, 6, 14, 6, CubeDeformation.NONE),
            PartPose.offset(10.5f, -18.0f, 0.0f));

        PartDefinition rightForearm = rightArm.addOrReplaceChild("right_forearm",
            CubeListBuilder.create().texOffs(64, 63)
                .addBox(-3.0f, 0.0f, -3.0f, 6, 12, 6, CubeDeformation.NONE),
            PartPose.offset(0.0f, 14.0f, 0.0f));
        rightForearm.addOrReplaceChild("right_fist",
            CubeListBuilder.create().texOffs(92, 41)
                .addBox(-3.0f, 0.0f, -3.0f, 6, 7, 6, CubeDeformation.NONE),
            PartPose.offset(0.0f, 12.0f, 0.0f));

        PartDefinition leftForearm = leftArm.addOrReplaceChild("left_forearm",
            CubeListBuilder.create().texOffs(64, 63)
                .addBox(-3.0f, 0.0f, -3.0f, 6, 12, 6, CubeDeformation.NONE),
            PartPose.offset(0.0f, 14.0f, 0.0f));
        leftForearm.addOrReplaceChild("left_fist",
            CubeListBuilder.create().texOffs(92, 41)
                .addBox(-3.0f, 0.0f, -3.0f, 6, 7, 6, CubeDeformation.NONE),
            PartPose.offset(0.0f, 12.0f, 0.0f));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(AncientForgebornEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float speed = Mth.clamp(limbSwingAmount, 0.0f, 1.0f);
        float degToRad = (float) (Math.PI / 180.0);

        // Heavy walk: legs and arms swing in opposition.
        float stride = Mth.cos(limbSwing * 0.6662f) * 1.05f * speed;
        float strideOpposite = Mth.cos(limbSwing * 0.6662f + (float) Math.PI) * 1.05f * speed;
        this.rightLeg.xRot = stride;
        this.leftLeg.xRot = strideOpposite;
        this.rightArm.xRot = strideOpposite * 0.8f;
        this.leftArm.xRot = stride * 0.8f;

        // Arms hang wide around the broad torso; fists stay clenched.
        this.rightArm.zRot = 0.08f + Mth.cos(ageInTicks * 0.1f) * 0.03f;
        this.leftArm.zRot = -0.08f - Mth.cos(ageInTicks * 0.1f) * 0.03f;
        this.rightForearm.xRot = -0.25f + Mth.abs(strideOpposite) * 0.3f;
        this.leftForearm.xRot = -0.25f + Mth.abs(stride) * 0.3f;

        // The whole upper body heaves with each stomp.
        this.torso.y = TORSO_Y + Mth.abs(Mth.cos(limbSwing * 0.6662f)) * 0.8f * speed;
        this.torso.zRot = Mth.cos(limbSwing * 0.3331f) * 0.04f * speed;

        // The head tracks the player it is hunting.
        this.head.yRot = netHeadYaw * degToRad * 0.7f;
        this.head.xRot = headPitch * degToRad * 0.6f;

        // Forge-core pulses slowly.
        float pulse = 1.0f + Mth.sin(ageInTicks * 0.12f) * 0.06f;
        this.chestCore.xScale = pulse;
        this.chestCore.yScale = pulse;
        this.chestCore.zScale = pulse;
    }
}
