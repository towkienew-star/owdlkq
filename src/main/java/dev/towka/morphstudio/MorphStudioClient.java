package dev.towka.morphstudio;

import dev.towka.morphstudio.gui.PlayerEditorScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class MorphStudioClient implements ClientModInitializer {
    public static final String MOD_ID = "morphstudio";

    @Override
    public void onInitializeClient() {
        ProfileStore.load();
        FakeManager.load();
        KeyBinding openKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.morphstudio.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, "category.morphstudio")
        );
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ProfileStore.sync(client);
            FakeManager.tick(client);
            while (openKey.wasPressed()) {
                if (client.world != null && client.currentScreen == null) {
                    client.setScreen(new PlayerEditorScreen());
                }
            }
        });
    }

    public static void openFakeEditor() {
        MinecraftClient.getInstance().setScreen(new dev.towka.morphstudio.gui.FakeEditorScreen());
    }
}
