package com.plusultra.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.plusultra.PlusUltra;
import com.plusultra.entity.VillainEntity;
import com.plusultra.entity.VillainEntity.Kind;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/** One renderer for every villain kind; the kind picks the model layer, skin, eyes and size. */
public class VillainRenderer extends HumanoidMobRenderer<VillainEntity, VillainModel> {
    private final ResourceLocation texture;
    private final Kind kind;

    public VillainRenderer(EntityRendererProvider.Context context, Kind kind) {
        super(context, new VillainModel(context.bakeLayer(VillainModel.layer(kind))), 0.6f * kind.scale);
        this.kind = kind;
        this.texture = PlusUltra.id("textures/entity/" + kind.texture + ".png");
        this.addLayer(new VillainEyesLayer(this, kind.texture));
    }

    @Override
    protected void scale(VillainEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(kind.scale, kind.scale, kind.scale);
    }

    @Override
    public ResourceLocation getTextureLocation(VillainEntity entity) {
        return texture;
    }
}
