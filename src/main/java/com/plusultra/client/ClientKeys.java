package com.plusultra.client;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class ClientKeys {
    public static final String CATEGORY = "key.categories.plusultra";

    public static final KeyMapping[] ABILITY = {
            new KeyMapping("key.plusultra.ability1", GLFW.GLFW_KEY_Z, CATEGORY),
            new KeyMapping("key.plusultra.ability2", GLFW.GLFW_KEY_X, CATEGORY),
            new KeyMapping("key.plusultra.ability3", GLFW.GLFW_KEY_C, CATEGORY)
    };
    public static final KeyMapping TALENTS = new KeyMapping("key.plusultra.talents", GLFW.GLFW_KEY_V, CATEGORY);
    public static final KeyMapping SWAP = new KeyMapping("key.plusultra.swap", GLFW.GLFW_KEY_G, CATEGORY);
    public static final KeyMapping FUSION = new KeyMapping("key.plusultra.fusion", GLFW.GLFW_KEY_B, CATEGORY);

    private ClientKeys() {}
}
