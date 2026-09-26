package com.hotsteel.client.model;

import java.util.ArrayList;
import java.util.List;

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
 * The Ancient Forgeborn's three bodies in one rig — the same trick vanilla uses
 * for the Wither: a shared root with one complete skeleton per form, toggled by
 * {@link ModelPart#visible}. That keeps a single renderer and a single entity
 * model, while letting each phase be a physically different creature.
 *
 * <pre>
 *   phase 1  熔铸之怒   armoured colossus          56 px tall, walks and stamps
 *   phase 2  烈焰喷发   the Ascendant               57 px tall, floats, 4 arms,
 *                                                   crown, halo ring, wings
 *   phase 3  岩浆狂潮   the Molten Horror           46 px tall, hunched, spikes,
 *                                                   exposed heart, dragging claws
 * </pre>
 *
 * <p>Phase 1's parts keep their original names (torso / head / rightArm …) so
 * the leg-and-arm walk cycle from the first release is unchanged. Phases 2 and 3
 * use the {@code p2_} / {@code p3_} prefixes.
 *
 * <p>All UV rectangles come from {@code tools/gen_forgeborn_forms.py} and
 * {@code tools/gen_all_textures.py :: t_ancient_forgeborn}; every box fits inside
 * the rectangle its {@code texOffs} points at.
 */
public class AncientForgebornModel extends HierarchicalModel<AncientForgebornEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
        new ModelLayerLocation(HotSteel.id("ancient_forgeborn"), "main");

    /** Height of each body in texture pixels; the renderer scales to the hitbox. */
    public static final float FORM1_HEIGHT_PX = 56.0f;
    public static final float FORM2_HEIGHT_PX = 57.0f;
    public static final float FORM3_HEIGHT_PX = 46.0f;

    private final ModelPart root;

    // ---- phase 1: the colossus ------------------------------------------
    private final ModelPart torso;
    private final ModelPart chestCore;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightForearm;
    private final ModelPart leftForearm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    // ---- phase 2: the Ascendant -----------------------------------------
    private final ModelPart p2Robe;
    private final ModelPart p2Torso;
    private final ModelPart p2Head;
    private final ModelPart p2Halo;
    private final ModelPart p2Core;
    private final ModelPart p2RightUpper;
    private final ModelPart p2LeftUpper;
    private final ModelPart p2RightFore;
    private final ModelPart p2LeftFore;
    private final ModelPart p2RightLower;
    private final ModelPart p2LeftLower;
    private final ModelPart p2RightWing;
    private final ModelPart p2LeftWing;

    // ---- phase 3: the Molten Horror -------------------------------------
    private final ModelPart p3Torso;
    private final ModelPart p3Skull;
    private final ModelPart p3Heart;
    private final ModelPart p3RightArm;
    private final ModelPart p3LeftArm;
    private final ModelPart p3RightFore;
    private final ModelPart p3LeftFore;
    private final ModelPart p3RightClaw;
    private final ModelPart p3LeftClaw;
    private final ModelPart p3RightLeg;
    private final ModelPart p3LeftLeg;

    /** Part groups used to hide the two forms that are not in play. */
    private final List<ModelPart> form1 = new ArrayList<>();
    private final List<ModelPart> form2 = new ArrayList<>();
    private final List<ModelPart> form3 = new ArrayList<>();

    public AncientForgebornModel(ModelPart root) {
        this.root = root;

        // phase 1
        this.torso = root.getChild("torso");
        this.chestCore = this.torso.getChild("chest_core");
        this.head = this.torso.getChild("head");
        this.rightArm = this.torso.getChild("rightArm");
        this.leftArm = this.torso.getChild("leftArm");
        this.rightForearm = this.rightArm.getChild("right_forearm");
        this.leftForearm = this.leftArm.getChild("left_forearm");
        this.rightLeg = root.getChild("rightLeg");
        this.leftLeg = root.getChild("leftLeg");
        form1.add(this.torso);
        form1.add(root.getChild("rightLeg"));
        form1.add(root.getChild("leftLeg"));

        // phase 2
        this.p2Robe = root.getChild("p2_robe");
        this.p2Torso = root.getChild("p2_torso");
        this.p2Head = this.p2Torso.getChild("p2_head");
        this.p2Halo = this.p2Torso.getChild("p2_halo");
        this.p2Core = this.p2Torso.getChild("p2_core");
        this.p2RightUpper = this.p2Torso.getChild("p2_right_upper");
        this.p2LeftUpper = this.p2Torso.getChild("p2_left_upper");
        this.p2RightFore = this.p2RightUpper.getChild("p2_right_forearm");
        this.p2LeftFore = this.p2LeftUpper.getChild("p2_left_forearm");
        this.p2RightLower = this.p2Robe.getChild("p2_right_lower");
        this.p2LeftLower = this.p2Robe.getChild("p2_left_lower");
        this.p2RightWing = this.p2Torso.getChild("p2_right_wing");
        this.p2LeftWing = this.p2Torso.getChild("p2_left_wing");
        form2.add(this.p2Robe);
        form2.add(root.getChild("p2_waist"));
        form2.add(this.p2Torso);

        // phase 3
        this.p3Torso = root.getChild("p3_torso");
        this.p3Skull = root.getChild("p3_skull");
        this.p3Heart = this.p3Torso.getChild("p3_heart");
        this.p3RightArm = this.p3Torso.getChild("p3_right_arm");
        this.p3LeftArm = this.p3Torso.getChild("p3_left_arm");
        this.p3RightFore = this.p3RightArm.getChild("p3_right_forearm");
        this.p3LeftFore = this.p3LeftArm.getChild("p3_left_forearm");
        this.p3RightClaw = this.p3RightFore.getChild("p3_right_claw");
        this.p3LeftClaw = this.p3LeftFore.getChild("p3_left_claw");
        this.p3RightLeg = root.getChild("p3_right_leg");
        this.p3LeftLeg = root.getChild("p3_left_leg");
        form3.add(this.p3Torso);
        form3.add(this.p3Skull);
        form3.add(this.p3RightLeg);
        form3.add(this.p3LeftLeg);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        buildColossus(root);
        buildAscendant(root);
        buildHorror(root);

        return LayerDefinition.create(mesh, 128, 256);
    }

    // ======================================================================
    // Phase 1 — 熔铸之怒: the armoured colossus (unchanged from the first release)
    // ======================================================================
    private static void buildColossus(PartDefinition root) {
        CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 0)
            .addBox(-3.5f, 0.0f, -3.5f, 7, 20, 7, CubeDeformation.NONE);
        root.addOrReplaceChild("rightLeg", leg, PartPose.offset(-6.0f, 4.0f, 0.0f));
        root.addOrReplaceChild("leftLeg",
            CubeListBuilder.create().texOffs(0, 0)
                .addBox(-3.5f, 0.0f, -3.5f, 7, 20, 7, CubeDeformation.NONE),
            PartPose.offset(6.0f, 4.0f, 0.0f));

        PartDefinition torso = root.addOrReplaceChild("torso",
            CubeListBuilder.create().texOffs(0, 28)
                .addBox(-10.0f, -26.0f, -6.0f, 20, 26, 12, CubeDeformation.NONE),
            PartPose.offset(0.0f, 4.0f, 0.0f));

        torso.addOrReplaceChild("chest_core",
            CubeListBuilder.create().texOffs(64, 28)
                .addBox(-5.0f, -18.0f, -7.0f, 10, 10, 2, CubeDeformation.NONE),
            PartPose.offset(0.0f, 0.0f, 0.0f));

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

        rightArm.addOrReplaceChild("right_forearm",
            CubeListBuilder.create().texOffs(64, 63)
                .addBox(-3.0f, 0.0f, -3.0f, 6, 12, 6, CubeDeformation.NONE)
                .addBox("right_fist", -3.0f, 12.0f, -3.0f, 6, 7, 6, CubeDeformation.NONE, 92, 41),
            PartPose.offset(0.0f, 14.0f, 0.0f));
        leftArm.addOrReplaceChild("left_forearm",
            CubeListBuilder.create().texOffs(64, 63)
                .addBox(-3.0f, 0.0f, -3.0f, 6, 12, 6, CubeDeformation.NONE)
                .addBox("left_fist", -3.0f, 12.0f, -3.0f, 6, 7, 6, CubeDeformation.NONE, 92, 41),
            PartPose.offset(0.0f, 14.0f, 0.0f));
    }

    // ======================================================================
    // Phase 2 — 烈焰喷发: the Ascendant. No legs; a floating robe instead.
    // ======================================================================
    private static void buildAscendant(PartDefinition root) {
        PartDefinition robe = root.addOrReplaceChild("p2_robe",
            CubeListBuilder.create().texOffs(0, 37)
                .addBox(-9.0f, -12.0f, -7.0f, 18, 12, 14, CubeDeformation.NONE)
                .addBox("hem", -5.0f, 0.0f, -5.0f, 10, 8, 10, CubeDeformation.NONE, 0, 37),
            PartPose.offset(0.0f, 16.0f, 0.0f));
        root.addOrReplaceChild("p2_waist",
            CubeListBuilder.create().texOffs(0, 112)
                .addBox(-6.0f, -6.0f, -5.0f, 12, 6, 10, CubeDeformation.NONE),
            PartPose.offset(0.0f, 4.0f, 0.0f));

        // the second pair of arms hangs from the robe
        robe.addOrReplaceChild("p2_right_lower",
            CubeListBuilder.create().texOffs(107, 88)
                .addBox(-2.5f, 0.0f, -2.5f, 5, 12, 5, CubeDeformation.NONE),
            PartPose.offset(-8.0f, -8.0f, 0.0f));
        robe.addOrReplaceChild("p2_left_lower",
            CubeListBuilder.create().texOffs(107, 88)
                .addBox(-2.5f, 0.0f, -2.5f, 5, 12, 5, CubeDeformation.NONE),
            PartPose.offset(8.0f, -8.0f, 0.0f));

        PartDefinition torso = root.addOrReplaceChild("p2_torso",
            CubeListBuilder.create().texOffs(0, 0)
                .addBox(-13.0f, -22.0f, -7.0f, 26, 22, 14, CubeDeformation.NONE),
            PartPose.offset(0.0f, 0.0f, 0.0f));

        torso.addOrReplaceChild("p2_core",
            CubeListBuilder.create().texOffs(82, 112)
                .addBox(-6.0f, -6.0f, -3.0f, 12, 12, 3, CubeDeformation.NONE),
            PartPose.offset(0.0f, -12.0f, -8.0f));

        PartDefinition head = torso.addOrReplaceChild("p2_head",
            CubeListBuilder.create().texOffs(0, 88)
                .addBox(-7.0f, -9.0f, -7.0f, 14, 9, 14, CubeDeformation.NONE)
                .addBox("eye_right", -5.0f, -6.0f, -8.0f, 5, 3, 1, CubeDeformation.NONE, 42, 129)
                .addBox("eye_left", 0.0f, -6.0f, -8.0f, 5, 3, 1, CubeDeformation.NONE, 42, 129)
                .addBox("horn_front_right", -8.0f, -6.0f, -7.0f, 4, 4, 4, CubeDeformation.NONE, 25, 129)
                .addBox("horn_front_left", 4.0f, -6.0f, -7.0f, 4, 4, 4, CubeDeformation.NONE, 25, 129)
                .addBox("crest", -2.0f, -11.0f, -3.0f, 4, 7, 4, CubeDeformation.NONE, 25, 129),
            PartPose.offset(0.0f, -22.0f, 0.0f));

        // spinning ring of fire above the head
        PartDefinition halo = torso.addOrReplaceChild("p2_halo",
            CubeListBuilder.create().texOffs(0, 64)
                .addBox(-10.0f, -1.0f, -10.0f, 20, 2, 2, CubeDeformation.NONE)
                .addBox("bar_south", -10.0f, -1.0f, 8.0f, 20, 2, 2, CubeDeformation.NONE, 0, 64)
                .addBox("bar_west", -10.0f, -1.0f, -8.0f, 2, 2, 16, CubeDeformation.NONE, 0, 64)
                .addBox("bar_east", 8.0f, -1.0f, -8.0f, 2, 2, 16, CubeDeformation.NONE, 0, 64),
            PartPose.offset(0.0f, -27.0f, 0.0f));

        CubeListBuilder pauldron = CubeListBuilder.create().texOffs(45, 112)
            .addBox(-4.5f, -3.0f, -4.5f, 9, 7, 9, CubeDeformation.NONE);
        torso.addOrReplaceChild("p2_right_pauldron", pauldron, PartPose.offset(-13.0f, -20.0f, 0.0f));
        torso.addOrReplaceChild("p2_left_pauldron",
            CubeListBuilder.create().texOffs(45, 112)
                .addBox(-4.5f, -3.0f, -4.5f, 9, 7, 9, CubeDeformation.NONE),
            PartPose.offset(13.0f, -20.0f, 0.0f));

        torso.addOrReplaceChild("p2_right_wing",
            CubeListBuilder.create().texOffs(81, 0)
                .addBox(-1.0f, -18.0f, -7.0f, 3, 18, 14, CubeDeformation.NONE),
            PartPose.offset(-12.0f, -2.0f, 6.0f));
        torso.addOrReplaceChild("p2_left_wing",
            CubeListBuilder.create().texOffs(81, 0)
                .addBox(-2.0f, -18.0f, -7.0f, 3, 18, 14, CubeDeformation.NONE),
            PartPose.offset(12.0f, -2.0f, 6.0f));

        PartDefinition rightUpper = torso.addOrReplaceChild("p2_right_upper",
            CubeListBuilder.create().texOffs(57, 88)
                .addBox(-3.0f, 0.0f, -3.0f, 6, 14, 6, CubeDeformation.NONE),
            PartPose.offset(-12.0f, -17.0f, 0.0f));
        PartDefinition leftUpper = torso.addOrReplaceChild("p2_left_upper",
            CubeListBuilder.create().texOffs(57, 88)
                .addBox(-3.0f, 0.0f, -3.0f, 6, 14, 6, CubeDeformation.NONE),
            PartPose.offset(12.0f, -17.0f, 0.0f));

        rightUpper.addOrReplaceChild("p2_right_forearm",
            CubeListBuilder.create().texOffs(82, 88)
                .addBox(-3.0f, 0.0f, -3.0f, 6, 13, 6, CubeDeformation.NONE)
                .addBox("right_fist", -3.0f, 13.0f, -3.0f, 6, 7, 6, CubeDeformation.NONE, 0, 129),
            PartPose.offset(0.0f, 14.0f, 0.0f));
        leftUpper.addOrReplaceChild("p2_left_forearm",
            CubeListBuilder.create().texOffs(82, 88)
                .addBox(-3.0f, 0.0f, -3.0f, 6, 13, 6, CubeDeformation.NONE)
                .addBox("left_fist", -3.0f, 13.0f, -3.0f, 6, 7, 6, CubeDeformation.NONE, 0, 129),
            PartPose.offset(0.0f, 14.0f, 0.0f));
    }

    // ======================================================================
    // Phase 3 — 岩浆狂潮: the Molten Horror. Hunched, spiked, claws bared.
    // ======================================================================
    private static void buildHorror(PartDefinition root) {
        CubeListBuilder leg = CubeListBuilder.create().texOffs(29, 39)
            .addBox(-4.5f, 0.0f, -4.5f, 9, 13, 9, CubeDeformation.NONE)
            .addBox("foot", -5.0f, 13.0f, -5.0f, 10, 5, 10, CubeDeformation.NONE, 0, 84);
        root.addOrReplaceChild("p3_right_leg", leg, PartPose.offset(-9.0f, 6.0f, 0.0f));
        root.addOrReplaceChild("p3_left_leg",
            CubeListBuilder.create().texOffs(29, 39)
                .addBox(-4.5f, 0.0f, -4.5f, 9, 13, 9, CubeDeformation.NONE)
                .addBox("foot", -5.0f, 13.0f, -5.0f, 10, 5, 10, CubeDeformation.NONE, 0, 84),
            PartPose.offset(9.0f, 6.0f, 0.0f));

        PartDefinition torso = root.addOrReplaceChild("p3_torso",
            CubeListBuilder.create().texOffs(0, 0)
                .addBox(-14.0f, -18.0f, -8.0f, 28, 22, 16, CubeDeformation.NONE),
            PartPose.offsetAndRotation(0.0f, 6.0f, 0.0f, 0.30f, 0.0f, 0.0f));

        torso.addOrReplaceChild("p3_back_plate",
            CubeListBuilder.create().texOffs(72, 64)
                .addBox(-8.0f, -6.0f, 0.0f, 16, 12, 4, CubeDeformation.NONE),
            PartPose.offset(0.0f, -14.0f, 8.0f));
        for (int i = 0; i < 3; i++) {
            torso.addOrReplaceChild("p3_spike" + i,
                CubeListBuilder.create().texOffs(41, 84)
                    .addBox(-2.0f, -10.0f, -2.0f, 4, 10, 4, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-6.0f + i * 6.0f, -14.0f, 11.0f,
                    -0.5f - i * 0.12f, 0.0f, 0.0f));
        }
        torso.addOrReplaceChild("p3_heart",
            CubeListBuilder.create().texOffs(0, 64)
                .addBox(-7.5f, -7.5f, -2.0f, 15, 15, 4, CubeDeformation.NONE),
            PartPose.offset(0.0f, -9.0f, -9.0f));

        // the skull is fused into the shoulders, so it hangs off the root
        root.addOrReplaceChild("p3_skull",
            CubeListBuilder.create().texOffs(66, 39)
                .addBox(-6.5f, -8.0f, -6.5f, 13, 8, 13, CubeDeformation.NONE)
                .addBox("eye_right", -4.5f, -5.0f, -7.5f, 4, 2, 1, CubeDeformation.NONE, 58, 84)
                .addBox("eye_left", 0.5f, -5.0f, -7.5f, 4, 2, 1, CubeDeformation.NONE, 58, 84),
            PartPose.offsetAndRotation(0.0f, -4.0f, -6.0f, 0.42f, 0.0f, 0.0f));

        PartDefinition rightArm = torso.addOrReplaceChild("p3_right_arm",
            CubeListBuilder.create().texOffs(89, 0)
                .addBox(-3.5f, 0.0f, -3.5f, 7, 19, 7, CubeDeformation.NONE),
            PartPose.offset(-15.0f, -14.0f, 0.0f));
        PartDefinition leftArm = torso.addOrReplaceChild("p3_left_arm",
            CubeListBuilder.create().texOffs(89, 0)
                .addBox(-3.5f, 0.0f, -3.5f, 7, 19, 7, CubeDeformation.NONE),
            PartPose.offset(15.0f, -14.0f, 0.0f));

        rightArm.addOrReplaceChild("p3_right_forearm",
            CubeListBuilder.create().texOffs(0, 39)
                .addBox(-3.5f, 0.0f, -3.5f, 7, 17, 7, CubeDeformation.NONE),
            PartPose.offset(0.0f, 19.0f, 0.0f));
        leftArm.addOrReplaceChild("p3_left_forearm",
            CubeListBuilder.create().texOffs(0, 39)
                .addBox(-3.5f, 0.0f, -3.5f, 7, 17, 7, CubeDeformation.NONE),
            PartPose.offset(0.0f, 19.0f, 0.0f));

        rightArm.getChild("p3_right_forearm").addOrReplaceChild("p3_right_claw",
            CubeListBuilder.create().texOffs(39, 64)
                .addBox(-4.0f, 0.0f, -4.0f, 8, 10, 8, CubeDeformation.NONE),
            PartPose.offset(0.0f, 17.0f, 0.0f));
        leftArm.getChild("p3_left_forearm").addOrReplaceChild("p3_left_claw",
            CubeListBuilder.create().texOffs(39, 64)
                .addBox(-4.0f, 0.0f, -4.0f, 8, 10, 8, CubeDeformation.NONE),
            PartPose.offset(0.0f, 17.0f, 0.0f));
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(AncientForgebornEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        int phase = entity.getPhase();
        setFormVisible(1, phase == 1);
        setFormVisible(2, phase == 2);
        setFormVisible(3, phase == 3);

        switch (phase) {
            case 2 -> animAscendant(entity, limbSwing, limbSwingAmount, ageInTicks,
                netHeadYaw, headPitch);
            case 3 -> animHorror(entity, limbSwing, limbSwingAmount, ageInTicks,
                netHeadYaw, headPitch);
            default -> animColossus(entity, limbSwing, limbSwingAmount, ageInTicks,
                netHeadYaw, headPitch);
        }
    }

    private void setFormVisible(int form, boolean visible) {
        for (ModelPart part : switch (form) {
            case 2 -> form2;
            case 3 -> form3;
            default -> form1;
        }) {
            part.visible = visible;
        }
    }

    private void animColossus(AncientForgebornEntity entity, float limbSwing, float limbSwingAmount,
                              float ageInTicks, float netHeadYaw, float headPitch) {
        float speed = Mth.clamp(limbSwingAmount, 0.0f, 1.0f);
        float degToRad = (float) (Math.PI / 180.0);
        boolean raging = entity.getPhase() >= 3 && !entity.isTransforming();

        // Form 1 stands on the ground and never leans its whole body.
        this.root.y = 0.0f;
        this.root.zRot = 0.0f;

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
        this.torso.y = 4.0f + Mth.abs(Mth.cos(limbSwing * 0.6662f)) * 0.8f * speed;
        this.torso.zRot = Mth.cos(limbSwing * 0.3331f) * 0.04f * speed;

        this.head.yRot = netHeadYaw * degToRad * 0.7f;
        this.head.xRot = headPitch * degToRad * 0.6f;

        // Forge-core pulses slowly.
        float pulse = 1.0f + Mth.sin(ageInTicks * 0.12f) * 0.06f;
        this.chestCore.xScale = pulse;
        this.chestCore.yScale = pulse;
        this.chestCore.zScale = pulse;
    }

    private void animAscendant(AncientForgebornEntity entity, float limbSwing, float limbSwingAmount,
                               float ageInTicks, float netHeadYaw, float headPitch) {
        float degToRad = (float) (Math.PI / 180.0);
        float hover = Mth.sin(ageInTicks * 0.06f);
        float slow = Mth.sin(ageInTicks * 0.045f);
        float casting = entity.isTransforming() ? 1.0f : 0.0f;

        // It never touches the ground: a slow rise and fall, deeper while casting.
        this.root.y = -4.0f + hover * 1.6f - casting * 6.0f;
        this.root.zRot = slow * 0.02f;
        this.p2Robe.xRot = Mth.cos(ageInTicks * 0.06f) * 0.05f;
        this.p2Robe.zRot = -slow * 0.05f;

        // Torso weaves; while casting it throws its chest open.
        this.p2Torso.xRot = -0.06f + slow * 0.04f - casting * 0.25f;
        this.p2Torso.yRot = Mth.sin(ageInTicks * 0.03f) * 0.06f;

        // Two pairs of arms, deliberately out of phase: a slow conjuring motion.
        float weave = Mth.sin(ageInTicks * 0.08f);
        float weaveOpp = Mth.cos(ageInTicks * 0.08f);
        this.p2RightUpper.xRot = -0.5f + weave * 0.28f - casting * 0.7f;
        this.p2LeftUpper.xRot = -0.5f + weaveOpp * 0.28f - casting * 0.7f;
        this.p2RightUpper.zRot = 0.34f + weave * 0.12f;
        this.p2LeftUpper.zRot = -0.34f - weaveOpp * 0.12f;
        this.p2RightFore.xRot = -0.7f + weaveOpp * 0.35f;
        this.p2LeftFore.xRot = -0.7f + weave * 0.35f;
        this.p2RightLower.xRot = 0.15f + weave * 0.3f;
        this.p2LeftLower.xRot = 0.15f + weaveOpp * 0.3f;
        this.p2RightLower.zRot = 0.24f;
        this.p2LeftLower.zRot = -0.24f;

        // Wings flare wide when it casts, and beat gently otherwise.
        float beat = Mth.sin(ageInTicks * 0.14f) * 0.12f;
        this.p2RightWing.zRot = 0.5f + beat + casting * 0.5f;
        this.p2LeftWing.zRot = -0.5f - beat - casting * 0.5f;
        this.p2RightWing.yRot = -0.3f;
        this.p2LeftWing.yRot = 0.3f;

        // The halo turns while the body stays put.
        this.p2Halo.yRot = ageInTicks * 0.035f;
        this.p2Halo.y = -27.0f + Mth.sin(ageInTicks * 0.06f) * 1.2f;

        this.p2Head.yRot = netHeadYaw * degToRad * 0.55f;
        this.p2Head.xRot = headPitch * degToRad * 0.45f + 0.08f;

        float pulse = 1.0f + Mth.sin(ageInTicks * 0.3f) * 0.12f;
        this.p2Core.xScale = pulse;
        this.p2Core.yScale = pulse;
        this.p2Core.zScale = pulse;
    }

    private void animHorror(AncientForgebornEntity entity, float limbSwing, float limbSwingAmount,
                            float ageInTicks, float netHeadYaw, float headPitch) {
        float speed = Mth.clamp(limbSwingAmount, 0.0f, 1.0f);
        float degToRad = (float) (Math.PI / 180.0);
        boolean raging = !entity.isTransforming();

        // The Horror stays planted: only the bound lifts it.
        this.root.zRot = 0.0f;

        // It lopes: a low, fast gait rather than the colossus's stomp.
        float stride = Mth.cos(limbSwing * 0.9f) * 0.85f * speed;
        float strideOpp = Mth.cos(limbSwing * 0.9f + (float) Math.PI) * 0.85f * speed;
        this.p3RightLeg.xRot = stride;
        this.p3LeftLeg.xRot = strideOpp;

        this.root.y = Mth.abs(Mth.cos(limbSwing * 0.9f)) * 1.1f * speed
            + Mth.sin(ageInTicks * 0.08f) * 0.4f;
        this.p3Torso.xRot = 0.30f + Mth.abs(stride) * 0.18f;

        // Arms swing long and loose; when raging they rear up to strike.
        float ready = raging ? -0.9f : -0.2f;
        this.p3RightArm.xRot = strideOpp * 0.7f + ready + Mth.sin(ageInTicks * 0.09f) * 0.1f;
        this.p3LeftArm.xRot = stride * 0.7f + ready + Mth.cos(ageInTicks * 0.09f) * 0.1f;
        this.p3RightArm.zRot = 0.30f;
        this.p3LeftArm.zRot = -0.30f;
        this.p3RightFore.xRot = 0.55f + Mth.abs(stride) * 0.25f;
        this.p3LeftFore.xRot = 0.55f + Mth.abs(strideOpp) * 0.25f;

        // Claws splay open in the rage phase.
        float splay = raging ? 0.35f : 0.05f;
        this.p3RightClaw.zRot = splay + Mth.sin(ageInTicks * 0.2f) * 0.06f;
        this.p3LeftClaw.zRot = -splay - Mth.cos(ageInTicks * 0.2f) * 0.06f;
        this.p3RightClaw.xRot = raging ? 0.4f : 0.1f;
        this.p3LeftClaw.xRot = raging ? 0.4f : 0.1f;

        // The skull is barely attached; it lurches and hunts.
        this.p3Skull.yRot = netHeadYaw * degToRad * 0.6f;
        this.p3Skull.xRot = 0.42f + headPitch * degToRad * 0.5f;

        // The bare heart pounds visibly.
        float beat = 1.0f + Mth.sin(ageInTicks * 0.42f) * 0.14f;
        this.p3Heart.xScale = beat;
        this.p3Heart.yScale = beat;
        this.p3Heart.zScale = 1.0f + Mth.sin(ageInTicks * 0.42f) * 0.22f;
    }
}