/*
 * This file is part of hypervision.
 *
 * Hypervision is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Hypervision is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with hypervision.  If not, see <https://www.gnu.org/licenses/>.
 */

package hypervision.api.utils.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class HypervisionToast implements Toast {
    private static final int TITLE_COLOR = 0xC62828;
    private static final int SUBTITLE_COLOR = 0x4B5563;

    private String title;
    private String subtitle;
    private long firstDrawTime;
    private boolean newDisplay;
    private long totalShowTime;

    public HypervisionToast(Component titleComponent, Component subtitleComponent, long totalShowTime) {
        this.title = titleComponent.getString();
        this.subtitle = subtitleComponent == null ? null : subtitleComponent.getString();
        this.totalShowTime = totalShowTime;
    }

    public Visibility render(PoseStack matrixStack, ToastComponent toastGui, long delta) {
        if (this.newDisplay) {
            this.firstDrawTime = delta;
            this.newDisplay = false;
        }


        //TODO: check
        toastGui.getMinecraft().getTextureManager().bindForSetup(new ResourceLocation("textures/gui/toasts.png"));
        //GlStateManager._color4f(1.0F, 1.0F, 1.0F, 255.0F);
        toastGui.blit(matrixStack, 0, 0, 0, 32, 160, 32);

        if (this.subtitle == null) {
            toastGui.getMinecraft().font.draw(matrixStack, this.title, 18, 12, TITLE_COLOR);
        } else {
            toastGui.getMinecraft().font.draw(matrixStack, this.title, 18, 7, TITLE_COLOR);
            toastGui.getMinecraft().font.draw(matrixStack, this.subtitle, 18, 18, SUBTITLE_COLOR);
        }

        return delta - this.firstDrawTime < totalShowTime ? Visibility.SHOW : Visibility.HIDE;
    }

    public void setDisplayedText(Component titleComponent, Component subtitleComponent) {
        this.title = titleComponent.getString();
        this.subtitle = subtitleComponent == null ? null : subtitleComponent.getString();
        this.newDisplay = true;
    }

    public static void addOrUpdate(ToastComponent toast, Component title, Component subtitle, long totalShowTime) {
        HypervisionToast HypervisionToast = toast.getToast(HypervisionToast.class, new Object());

        if (HypervisionToast == null) {
            toast.addToast(new HypervisionToast(title, subtitle, totalShowTime));
        } else {
            HypervisionToast.setDisplayedText(title, subtitle);
        }
    }

    public static void addOrUpdate(Component title, Component subtitle) {
        addOrUpdate(Minecraft.getInstance().getToasts(), title, subtitle, hypervision.api.HypervisionAPI.getSettings().toastTimer.value);
    }
}

