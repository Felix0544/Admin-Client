package de.adminclient;

import de.adminclient.gui.ClickGuiScreen;
import de.adminclient.module.FreecamModule;
import de.adminclient.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * AdminClient: Menue mit G. Alle Funktionen sind nur im Singleplayer oder mit OP-Rechten (Level 2+) aktiv.
 */
public class AdminClient implements ClientModInitializer {

    private static KeyBinding openMenu;

    @Override
    public void onInitializeClient() {
        ModuleManager.init();
        openMenu = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.adminclient.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category.adminclient"));

        ClientTickEvents.START_CLIENT_TICK.register(FreecamModule::preTick);
        ClientTickEvents.END_CLIENT_TICK.register(AdminClient::tick);
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> ModuleManager.hud(ctx, MinecraftClient.getInstance()));
        WorldRenderEvents.START.register(context -> FreecamModule.syncRotation());
    }

    /** Die Sperre: nur Singleplayer ODER Admin (OP Level 2+). */
    public static boolean allowed(MinecraftClient c) {
        return c.player != null && (c.isInSingleplayer() || c.player.hasPermissionLevel(2));
    }

    private static void tick(MinecraftClient c) {
        ClientPlayerEntity p = c.player;
        if (p == null || c.world == null) {
            ModuleManager.disableAll();
            while (openMenu.wasPressed()) { /* verwerfen */ }
            return;
        }
        boolean ok = allowed(c);

        while (openMenu.wasPressed()) {
            if (c.currentScreen != null) continue;
            if (ok) c.setScreen(new ClickGuiScreen());
            else p.sendMessage(Text.literal("\u00a7c[AdminClient] Nur im Singleplayer oder mit OP-Rechten!"), true);
        }

        if (!ok) {            // Rechte verloren (deop / Servers gewechselt) -> alles aus
            ModuleManager.disableAll();
            return;
        }
        ModuleManager.tick(c, p);
    }
}
