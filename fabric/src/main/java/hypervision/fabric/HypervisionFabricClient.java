package hypervision.fabric;

import baritone.api.BaritoneAPI;
import baritone.utils.gui.HypervisionMenuScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class HypervisionFabricClient implements ClientModInitializer {

    private static final KeyMapping OPEN_MENU = new KeyMapping(
            "key.hypervision.open_menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F8,
            "key.categories.hypervision"
    );

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(OPEN_MENU);
        ClientTickEvents.END_CLIENT_TICK.register(this::handleTick);
    }

    private void handleTick(Minecraft client) {
        while (OPEN_MENU.consumeClick()) {
            if (client.player == null) {
                return;
            }
            if (client.screen instanceof HypervisionMenuScreen) {
                client.setScreen(null);
            } else {
                BaritoneAPI.getProvider().getPrimaryBaritone().openCommandMenu();
            }
        }
    }
}
