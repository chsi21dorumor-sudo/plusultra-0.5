package com.plusultra.client;

import com.plusultra.PlusUltra;
import com.plusultra.entity.VillainEntity;
import com.plusultra.entity.VillainEntity.Kind;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Humanoid base plus extra cubes per villain kind. Extra cubes use the lower half of the 64x64 skin,
 * which a plain humanoid model never touches (documented per kind below).
 */
public class VillainModel extends HumanoidModel<VillainEntity> {

    public VillainModel(ModelPart root) {
        super(root);
    }

    public static ModelLayerLocation layer(Kind kind) {
        return new ModelLayerLocation(PlusUltra.id(kind.texture), "main");
    }

    public static LayerDefinition createLayer(Kind kind) {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.0F), 0.0F);
        PartDefinition root = mesh.getRoot();
        switch (kind) {
            case DECAY_LORD -> decayLord(root);
            case NOMU -> nomu(root);
            case HERO_KILLER -> heroKiller(root);
            case BLUE_FLAME -> blueFlame(root);
            case THUG -> thug(root);
        }
        return LayerDefinition.create(mesh, 64, 64);
    }

    private static void add(PartDefinition parent, String name, int u, int v, float x, float y, float z,
                            float w, float h, float d, PartPose pose) {
        parent.addOrReplaceChild(name, CubeListBuilder.create().texOffs(u, v).addBox(x, y, z, w, h, d), pose);
    }

    /** Horns, spiked bone shoulder pads and a tattered cloak. UV: pads (0,32), cloak (20,32), horns (0,48). */
    private static void decayLord(PartDefinition root) {
        PartDefinition head = root.getChild("head");
        add(head, "horn_r", 0, 48, -0.5F, -4.0F, -0.5F, 1, 4, 1,
                PartPose.offsetAndRotation(-3.0F, -8.0F, 0.0F, 0.0F, 0.0F, -0.35F));
        add(head, "horn_l", 0, 48, -0.5F, -4.0F, -0.5F, 1, 4, 1,
                PartPose.offsetAndRotation(3.0F, -8.0F, 0.0F, 0.0F, 0.0F, 0.35F));
        add(root.getChild("right_arm"), "pad_r", 0, 32, -3.5F, -3.5F, -2.5F, 5, 2, 5, PartPose.ZERO);
        add(root.getChild("left_arm"), "pad_l", 0, 32, -1.5F, -3.5F, -2.5F, 5, 2, 5, PartPose.ZERO);
        add(root.getChild("body"), "cloak", 20, 32, -5.0F, 0.0F, 2.0F, 10, 16, 1, PartPose.ZERO);
    }

    /** Exposed brain, spine ridge and bulging arms. UV: brain (0,32), spine (0,48), arm bulk (16,48). */
    private static void nomu(PartDefinition root) {
        add(root.getChild("head"), "brain", 0, 32, -4.0F, -11.0F, -4.0F, 8, 3, 8, PartPose.ZERO);
        add(root.getChild("body"), "spine", 0, 48, -1.0F, 0.0F, 2.0F, 2, 10, 2, PartPose.ZERO);
        add(root.getChild("right_arm"), "bulk_r", 16, 48, -4.0F, -2.5F, -2.5F, 6, 6, 5, PartPose.ZERO);
        add(root.getChild("left_arm"), "bulk_l", 16, 48, -2.0F, -2.5F, -2.5F, 6, 6, 5, PartPose.ZERO);
    }

    /** Scarf, scarf tail and a sword on the back. UV: scarf (0,32), tail (28,32), sword (36,32). */
    private static void heroKiller(PartDefinition root) {
        PartDefinition body = root.getChild("body");
        add(body, "scarf", 0, 32, -4.5F, -1.0F, -2.5F, 9, 3, 5, PartPose.ZERO);
        add(body, "scarf_tail", 28, 32, -1.0F, 2.0F, 2.5F, 3, 10, 1, PartPose.ZERO);
        add(body, "sword", 36, 32, -0.5F, -3.0F, 3.0F, 1, 18, 1, PartPose.rotation(0.0F, 0.0F, 0.7F));
    }

    /** Three hair spikes, a short torn cape and a single armoured shoulder. UV: cape (0,32), plate (24,32), spikes (0,48). */
    private static void blueFlame(PartDefinition root) {
        PartDefinition head = root.getChild("head");
        add(head, "spike_l", 0, 48, -0.5F, -3.0F, -0.5F, 1, 3, 1,
                PartPose.offsetAndRotation(-2.0F, -8.0F, 0.0F, 0.0F, 0.0F, -0.3F));
        add(head, "spike_m", 0, 48, -0.5F, -4.0F, -0.5F, 1, 3, 1,
                PartPose.offsetAndRotation(0.0F, -8.0F, 0.5F, -0.15F, 0.0F, 0.0F));
        add(head, "spike_r", 0, 48, -0.5F, -3.0F, -0.5F, 1, 3, 1,
                PartPose.offsetAndRotation(2.0F, -8.0F, 0.0F, 0.0F, 0.0F, 0.3F));
        add(root.getChild("body"), "cape", 0, 32, -5.0F, 0.0F, 2.0F, 10, 8, 1, PartPose.ZERO);
        add(root.getChild("left_arm"), "plate", 24, 32, -1.5F, -3.5F, -2.5F, 5, 3, 5, PartPose.ZERO);
    }

    /** A bulging loot sack on the back. UV: sack (0,32). */
    private static void thug(PartDefinition root) {
        add(root.getChild("body"), "sack", 0, 32, -3.0F, 1.0F, 2.0F, 6, 8, 3, PartPose.ZERO);
    }
}
