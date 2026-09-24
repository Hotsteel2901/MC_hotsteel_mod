package com.hotsteel.client.model;

import com.hotsteel.HotSteel;
import com.hotsteel.content.entity.SlagCrawlerEntity;

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
 * Model for the Slag Crawler — a low, segmented carapace crawling on six splayed
 * legs, with a glowing maw and a stubby tail.
 *
 * <p>The texture atlas is 64x64. The UV rectangles below must stay in sync with
 * {@code tools/gen_all_textures.py :: t_slag_crawler}:
 * <pre>
 *   body segment (x3)  texOffs (0, 0)   10x6x6  -&gt;  32x12 px
 *   head               texOffs (32, 0)   8x6x6  -&gt;  28x12 px
 *   mandible (x2)      texOffs (0, 12)   2x2x5  -&gt;  14x7  px
 *   leg (x6)           texOffs (14, 12)  2x6x2  -&gt;   8x8  px
 *   tail               texOffs (0, 22)   4x4x10 -&gt;  28x14 px
 *   tail tip           texOffs (30, 22)  2x2x3  -&gt;  10x5  px
 * </pre>
 */
public class SlagCrawlerModel extends HierarchicalModel<SlagCrawlerEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
        new ModelLayerLocation(HotSteel.id("slag_crawler"), "main");

    /** Model-space Y of the three carapace segment pivots. */
    private static final float SEGMENT_Y = 20.0f;
    /** Base outward splay of the legs (left legs are negative). */
    private static final float LEG_SPLAY = 0.55f;

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart mandibleLeft;
    private final ModelPart mandibleRight;
    private final ModelPart tail;
    private final ModelPart tailTip;
    private final ModelPart segment1;
    private final ModelPart segment2;
    private final ModelPart segment3;
    /** 0-5: left/right of leg pair 0, then pairs 1 and 2. */
    private final ModelPart[] legs;

    public SlagCrawlerModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.mandibleLeft = root.getChild("mandible_left");
        this.mandibleRight = root.getChild("mandible_right");
        this.tail = root.getChild("tail");
        this.tailTip = this.tail.getChild("tail_tip");
        this.segment1 = root.getChild("segment1");
        this.segment2 = root.getChild("segment2");
        this.segment3 = root.getChild("segment3");
        this.legs = new ModelPart[] {
            root.getChild("leg_left_0"), root.getChild("leg_right_0"),
            root.getChild("leg_left_1"), root.getChild("leg_right_1"),
            root.getChild("leg_left_2"), root.getChild("leg_right_2"),
        };
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("segment1", segmentBuilder(), PartPose.offset(0.0f, SEGMENT_Y, -6.0f));
        root.addOrReplaceChild("segment2", segmentBuilder(), PartPose.offset(0.0f, SEGMENT_Y, 0.0f));
        root.addOrReplaceChild("segment3", segmentBuilder(), PartPose.offset(0.0f, SEGMENT_Y, 6.0f));

        root.addOrReplaceChild("head",
            CubeListBuilder.create().texOffs(32, 0)
                .addBox(-4.0f, -5.0f, -4.0f, 8, 6, 6, CubeDeformation.NONE),
            PartPose.offset(0.0f, 19.0f, -10.0f));

        CubeListBuilder mandible = CubeListBuilder.create().texOffs(0, 12)
            .addBox(-1.0f, 0.0f, -3.0f, 2, 2, 5, CubeDeformation.NONE);
        root.addOrReplaceChild("mandible_left", mandible, PartPose.offset(3.0f, 20.0f, -12.0f));
        root.addOrReplaceChild("mandible_right",
            CubeListBuilder.create().texOffs(0, 12)
                .addBox(-1.0f, 0.0f, -3.0f, 2, 2, 5, CubeDeformation.NONE),
            PartPose.offset(-3.0f, 20.0f, -12.0f));
        float[] legZ = { -5.0f, -5.0f, 0.0f, 0.0f, 5.0f, 5.0f };
        for (int i = 0; i < 6; i++) {
            boolean left = (i % 2 == 0);
            root.addOrReplaceChild(
                left ? "leg_left_" + (i / 2) : "leg_right_" + (i / 2),
                CubeListBuilder.create().texOffs(14, 12)
                    .addBox(-1.0f, 0.0f, -1.0f, 2, 6, 2, CubeDeformation.NONE),
                PartPose.offsetAndRotation(left ? 5.0f : -5.0f, 19.0f, legZ[i],
                    0.0f, 0.0f, left ? -LEG_SPLAY : LEG_SPLAY));
        }

        PartDefinition tail = root.addOrReplaceChild("tail",
            CubeListBuilder.create().texOffs(0, 22)
                .addBox(-2.0f, -2.0f, 0.0f, 4, 4, 10, CubeDeformation.NONE),
            PartPose.offsetAndRotation(0.0f, 19.0f, 9.0f, -0.4f, 0.0f, 0.0f));
        tail.addOrReplaceChild("tail_tip",
            CubeListBuilder.create().texOffs(30, 22)
                .addBox(-1.0f, -1.0f, 0.0f, 2, 2, 3, CubeDeformation.NONE),
            PartPose.offset(0.0f, 0.0f, 10.0f));

        return LayerDefinition.create(mesh, 64, 64);
    }

    /** One carapace plate; the three segments share a single UV rectangle. */
    private static CubeListBuilder segmentBuilder() {
        return CubeListBuilder.create().texOffs(0, 0)
            .addBox(-5.0f, -6.0f, -3.0f, 10, 6, 6, CubeDeformation.NONE);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(SlagCrawlerEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float speed = Mth.clamp(limbSwingAmount, 0.0f, 1.0f);
        float degToRad = (float) (Math.PI / 180.0);

        // The head tracks the look direction while the body ripples underneath it.
        this.head.yRot = netHeadYaw * degToRad * 0.9f;
        this.head.xRot = headPitch * degToRad * 0.6f;

        // Morphing carapace: each segment rolls with a phase offset, producing the
        // travelling wave of a crawling insect.
        ModelPart[] segments = { this.segment1, this.segment2, this.segment3 };
        for (int i = 0; i < segments.length; i++) {
            float phase = ageInTicks * 0.45f - i * 0.9f;
            float bob = Mth.cos(phase) * (0.35f + 0.45f * speed);
            ModelPart segment = segments[i];
            segment.y = SEGMENT_Y + bob;
            segment.xRot = Mth.sin(phase) * (0.05f + 0.09f * speed);
            segment.zRot = Mth.cos(phase * 0.5f) * 0.05f * speed;
        }

        // Six legs in a tripod-ish alternating gait.
        for (int i = 0; i < this.legs.length; i++) {
            int pair = i / 2;
            boolean left = (i % 2 == 0);
            float phase = limbSwing * 1.4f + pair * 2.1f + (left ? 0.0f : (float) Math.PI);
            ModelPart leg = this.legs[i];
            leg.yRot = Mth.cos(phase) * 0.55f * speed;
            leg.zRot = (left ? -LEG_SPLAY : LEG_SPLAY) + Mth.sin(phase) * 0.18f * speed;
        }

        this.tail.yRot = Mth.sin(ageInTicks * 0.25f) * 0.14f;
        this.tail.xRot = -0.4f + Mth.cos(ageInTicks * 0.3f) * 0.08f;
        this.tailTip.yRot = Mth.sin(ageInTicks * 0.35f - 0.6f) * 0.2f;

        // Mandibles chatter as the crawler bites.
        float bite = Mth.sin(ageInTicks * 0.5f) * 0.12f * (0.4f + speed);
        this.mandibleLeft.yRot = bite;
        this.mandibleRight.yRot = -bite;
    }
}
