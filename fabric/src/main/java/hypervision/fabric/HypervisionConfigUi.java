package hypervision.fabric;

import baritone.Baritone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class HypervisionConfigUi {

    private HypervisionConfigUi() {
    }

    public static Screen createScreen() {
        HypervisionOneConfig.INSTANCE.syncFromBaritone();
        try {
            Class<?> screensKt = Class.forName("org.polyfrost.oneconfig.utils.v1.dsl.ScreensKt");
            Class<?> configClass = Class.forName("org.polyfrost.oneconfig.api.config.v1.Config");
            Method createScreen = screensKt.getMethod("createScreen", configClass);
            return (Screen) createScreen.invoke(null, HypervisionOneConfig.INSTANCE);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to create Hypervision OneConfig screen", e);
        }
    }

    public static void open() {
        Minecraft.getInstance().setScreen(createScreen());
    }

    public static void open(Baritone ignored) {
        open();
    }
}
