package com.plusultra.client;

import com.plusultra.PlusUltra;
import com.plusultra.entity.VillainEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;

/** Draws a villain's eyes fullbright, like a spider's eyes. Texture: textures/entity/<name>_eyes.png. */
public class VillainEyesLayer extends EyesLayer<VillainEntity, VillainModel> {
    private final RenderType eyes;

    public VillainEyesLayer(RenderLayerParent<VillainEntity, VillainModel> parent, String textureName) {
        super(parent);
        this.eyes = RenderType.eyes(PlusUltra.id("textures/entity/" + textureName + "_eyes.png"));
    }

    @Override
    public RenderType renderType() {
        return eyes;
    }
}
