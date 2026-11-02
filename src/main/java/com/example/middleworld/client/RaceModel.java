package com.example.middleworld.client;

import com.example.middleworld.MiddleWorld;
import com.example.middleworld.entity.FactionMob;
import com.example.middleworld.entity.Race;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

/**
 * One model for all three races. Every race has the same parts (so the keyframe animations are
 * shared) but different proportions, set in {@link #spec(Race)}. 64x64 texture layout, identical
 * for every race. KEEP IN SYNC with tools/gen_textures.py:
 *   head 0,0 | body 0,16 | right_arm 20,16 | left_arm 32,16 | right_leg 44,16 | left_leg 0,32
 *   right_ear 32,0 | left_ear 32,6 | nose 44,0 | beard 44,8 | loincloth 0,48
 *   hat 24,48 | hat_tip 0,54
 */
public class RaceModel extends HierarchicalModel<FactionMob> implements ArmedModel {
    private static final Map<Race, ModelLayerLocation> LAYERS = new EnumMap<>(Race.class);

    static {
        for (Race race : Race.values()) {
            LAYERS.put(race, new ModelLayerLocation(
                    ResourceLocation.fromNamespaceAndPath(MiddleWorld.MODID, race.id), "main"));
        }
    }

    public static ModelLayerLocation layer(Race race) {
        return LAYERS.get(race);
    }

    /** Proportions and movement style of one race. */
    private record Spec(int bodyH, int armLen, int legLen, float headZ,
                        float earW, float earH, float earRot, float earY,
                        float noseW, float noseH, float noseD,
                        boolean beard,
                        float walkFreq, float walkAmp, float bob, float lean, float earFlap, float waddle) {
    }

    private static Spec spec(Race race) {
        return switch (race) {
            //                  body arm leg headZ  earW earH earRot earY  noseW H   D   beard  freq amp  bob  lean  flap waddle
            case GOBLIN -> new Spec(7, 7, 6, -1.0F, 4.0F, 3.0F, 0.40F, -5.0F, 2.0F, 2.0F, 2.0F, false, 1.10F, 1.5F, 1.4F, 0.25F, 0.30F, 0.00F);
            case ELF -> new Spec(9, 10, 10, 0.0F, 4.0F, 3.0F, 0.70F, -4.0F, 1.0F, 2.0F, 1.0F, false, 0.70F, 1.1F, 0.5F, 0.08F, 0.05F, 0.00F);
            case DWARF -> new Spec(8, 7, 5, 0.0F, 2.0F, 2.0F, 0.10F, -4.0F, 3.0F, 3.0F, 2.0F, true, 0.90F, 1.0F, 0.6F, 0.10F, 0.00F, 0.09F);
        };
    }

    private final Spec spec;
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart rightEar;
    private final ModelPart leftEar;
    private final ModelPart hat;
    private final ModelPart hatTip;

    public RaceModel(ModelPart root, Race race) {
        this.spec = spec(race);
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.rightEar = this.head.getChild("right_ear");
        this.leftEar = this.head.getChild("left_ear");
        this.hat = this.head.getChild("hat");
        this.hatTip = this.head.getChild("hat_tip");
    }

    public static LayerDefinition createBodyLayer(Race race) {
        Spec s = spec(race);
        float legY = 24.0F - s.legLen();
        float bodyY = legY - s.bodyH();

        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, bodyY, s.headZ()));
        head.addOrReplaceChild("nose",
                CubeListBuilder.create().texOffs(44, 0).addBox(-s.noseW() / 2.0F, -s.noseH() / 2.0F, -s.noseD(),
                        s.noseW(), s.noseH(), s.noseD()),
                PartPose.offset(0.0F, -3.0F, -4.0F));
        head.addOrReplaceChild("right_ear",
                CubeListBuilder.create().texOffs(32, 0).addBox(-s.earW(), -s.earH() / 2.0F, -0.5F, s.earW(), s.earH(), 1.0F),
                PartPose.offsetAndRotation(-4.0F, s.earY(), 0.0F, 0.0F, 0.0F, s.earRot()));
        head.addOrReplaceChild("left_ear",
                CubeListBuilder.create().texOffs(32, 6).mirror().addBox(0.0F, -s.earH() / 2.0F, -0.5F, s.earW(), s.earH(), 1.0F),
                PartPose.offsetAndRotation(4.0F, s.earY(), 0.0F, 0.0F, 0.0F, -s.earRot()));
        // Helmet / hood and its tip. Shown or hidden per role in setupAnim.
        head.addOrReplaceChild("hat",
                CubeListBuilder.create().texOffs(24, 48).addBox(-4.5F, -9.5F, -4.5F, 9.0F, 3.0F, 9.0F),
                PartPose.ZERO);
        head.addOrReplaceChild("hat_tip",
                CubeListBuilder.create().texOffs(0, 54).addBox(-2.0F, -13.0F, -2.0F, 4.0F, 4.0F, 4.0F),
                PartPose.ZERO);
        if (s.beard()) {
            head.addOrReplaceChild("beard",
                    CubeListBuilder.create().texOffs(44, 8).addBox(-3.0F, -3.0F, -5.0F, 6.0F, 5.0F, 2.0F),
                    PartPose.ZERO);
        }

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 16).addBox(-3.0F, 0.0F, -2.0F, 6.0F, s.bodyH(), 4.0F),
                PartPose.offset(0.0F, bodyY, 0.0F));
        body.addOrReplaceChild("loincloth",
                CubeListBuilder.create().texOffs(0, 48).addBox(-3.0F, s.bodyH() - 1.0F, -3.0F, 6.0F, 4.0F, 1.0F),
                PartPose.ZERO);

        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(20, 16).addBox(-1.5F, -1.0F, -1.5F, 3.0F, s.armLen(), 3.0F),
                PartPose.offset(-4.5F, bodyY + 1.0F, 0.0F));
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(32, 16).addBox(-1.5F, -1.0F, -1.5F, 3.0F, s.armLen(), 3.0F),
                PartPose.offset(4.5F, bodyY + 1.0F, 0.0F));

        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(44, 16).addBox(-1.5F, 0.0F, -1.5F, 3.0F, s.legLen(), 3.0F),
                PartPose.offset(-1.5F, legY, 0.0F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(0, 32).addBox(-1.5F, 0.0F, -1.5F, 3.0F, s.legLen(), 3.0F),
                PartPose.offset(1.5F, legY, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    /** Puts held items at the end of the arm, whatever the arm length of this race is. */
    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
        ModelPart part = arm == HumanoidArm.RIGHT ? this.rightArm : this.leftArm;
        part.translateAndRotate(poseStack);
        // Item layer assumes a 12 pixel humanoid arm (hand 10 px below the pivot). Ours ends at armLen - 1.
        poseStack.translate(0.0F, -(11.0F - this.spec.armLen()) / 16.0F, 0.0F);
    }

    @Override
    public void setupAnim(FactionMob entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.hat.visible = entity.role().hat;
        this.hatTip.visible = entity.role().hatTip;

        this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        this.head.xRot = headPitch * Mth.DEG_TO_RAD;

        Spec s = this.spec;
        float cycle = limbSwing * s.walkFreq();
        float amount = Math.min(limbSwingAmount * 1.4F, 1.0F);
        this.rightLeg.xRot = Mth.cos(cycle) * s.walkAmp() * amount;
        this.leftLeg.xRot = Mth.cos(cycle + Mth.PI) * s.walkAmp() * amount;
        this.rightArm.xRot = Mth.cos(cycle + Mth.PI) * s.walkAmp() * 0.75F * amount;
        this.leftArm.xRot = Mth.cos(cycle) * s.walkAmp() * 0.75F * amount;

        float bob = Math.abs(Mth.cos(cycle)) * s.bob() * amount;
        this.body.y -= bob;
        this.head.y -= bob;
        this.rightArm.y -= bob;
        this.leftArm.y -= bob;

        this.body.xRot += s.lean() * amount;
        this.body.yRot += Mth.cos(cycle) * 0.15F * amount;
        this.body.zRot += Mth.sin(cycle) * s.waddle() * amount;

        float flap = Mth.sin(ageInTicks * 0.9F) * s.earFlap() * 0.5F * amount + s.earFlap() * amount;
        this.rightEar.zRot -= flap;
        this.leftEar.zRot += flap;

        if (entity.isAggressive()) {
            if (entity.role().ranged) {
                // Bow drawn or spell hand raised, both arms forward.
                this.rightArm.xRot = -1.45F + this.head.xRot * 0.4F;
                this.rightArm.yRot = -0.15F;
                this.leftArm.xRot = -1.45F + this.head.xRot * 0.4F;
                this.leftArm.yRot = 0.45F;
            } else {
                this.rightArm.xRot -= 1.2F;
                this.rightArm.zRot -= 0.1F;
                this.leftArm.xRot -= 0.5F;
                this.leftArm.zRot += 0.1F;
            }
        }

        this.animate(entity.idleAnimationState, MobAnimations.IDLE, ageInTicks);
        boolean gesture = entity.role().ranged
                || entity.role() == com.example.middleworld.entity.Role.HEALER
                || entity.role() == com.example.middleworld.entity.Role.CAPTAIN;
        this.animate(entity.attackAnimationState, gesture ? MobAnimations.CAST : MobAnimations.ATTACK, ageInTicks);
    }
}
