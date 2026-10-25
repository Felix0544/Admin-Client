package de.adminclient.gui;

import de.adminclient.AdminClient;
import de.adminclient.module.Module;
import de.adminclient.module.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/** Menue mit Reitern PvP / Bases / Movement. Linksklick = an/aus, [-] [+] = Einstellungen. */
public class ClickGuiScreen extends Screen {
    private static final int W = 440;
    private static final int ROW = 30;
    private static final int HEAD = 58;
    private static final int ROWS = 4;
    private static final int SET_W = 120;

    private static Module.Category lastTab = Module.Category.PVP;

    public ClickGuiScreen() {
        super(Text.literal("AdminClient"));
    }

    @Override
    public boolean shouldPause() { return false; }

    private int panelH() { return HEAD + ROWS * ROW + 30; }
    private int px() { return (width - W) / 2; }
    private int py() { return (height - panelH()) / 2; }
    private int rowY(int i) { return py() + HEAD + i * ROW; }
    private int setX(int k, int n) { return px() + W - 14 - (n - k) * (SET_W + 4) + 4; }

    private static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private String fit(String s, int maxW) {
        while (s.length() > 1 && textRenderer.getWidth(s) > maxW) s = s.substring(0, s.length() - 1);
        return s;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);   // Hintergrund
        int x = px(), y = py(), h = panelH();

        ctx.fill(x - 2, y - 2, x + W + 2, y + h + 2, 0xFF3D5AFE);
        ctx.fill(x, y, x + W, y + h, 0xF01B1B1F);
        ctx.drawText(textRenderer, "AdminClient", x + 12, y + 9, 0xFF55FFFF, true);

        boolean ok = AdminClient.allowed(client);
        String status = ok ? "Berechtigt" : "Gesperrt";
        ctx.drawText(textRenderer, status, x + W - 12 - textRenderer.getWidth(status), y + 9, ok ? 0xFF55FF55 : 0xFFFF5555, true);

        // Reiter
        Module.Category[] cats = Module.Category.values();
        for (int i = 0; i < cats.length; i++) {
            int tx = x + 12 + i * 104, ty = y + 28;
            boolean sel = cats[i] == lastTab;
            boolean hov = in(mx, my, tx, ty, 100, 20);
            ctx.fill(tx, ty, tx + 100, ty + 20, sel ? 0xFF3D5AFE : (hov ? 0xFF3A3A44 : 0xFF2A2A31));
            String label = cats[i].label;
            ctx.drawText(textRenderer, label, tx + 50 - textRenderer.getWidth(label) / 2, ty + 6, 0xFFFFFFFF, true);
        }

        // Module
        List<Module> mods = ModuleManager.of(lastTab);
        for (int i = 0; i < mods.size(); i++) {
            Module m = mods.get(i);
            int ry = rowY(i), rx = x + 8, rw = W - 16;
            boolean hov = in(mx, my, rx, ry, rw, ROW - 4);
            int bg = m.isEnabled() ? (hov ? 0xFF3F8F43 : 0xFF2E7D32) : (hov ? 0xFF3A3A44 : 0xFF2A2A31);
            ctx.fill(rx, ry, rx + rw, ry + ROW - 4, bg);

            int n = m.settings.size();
            int nameW = rw - 16 - n * (SET_W + 4);
            ctx.drawText(textRenderer, fit(m.name, nameW), rx + 8, ry + 4, 0xFFFFFFFF, true);
            ctx.drawText(textRenderer, fit(m.description, nameW), rx + 8, ry + 15, 0xFFB8B8B8, false);

            for (int k = 0; k < n; k++) {
                Module.Setting s = m.settings.get(k);
                int sx = setX(k, n), sy = ry + 6;
                boolean hm = in(mx, my, sx, sy, 14, 14), hp = in(mx, my, sx + SET_W - 14, sy, 14, 14);
                ctx.fill(sx, sy, sx + 14, sy + 14, hm ? 0xFF6A6A78 : 0xFF45454F);
                ctx.fill(sx + SET_W - 14, sy, sx + SET_W, sy + 14, hp ? 0xFF6A6A78 : 0xFF45454F);
                ctx.drawText(textRenderer, "-", sx + 5, sy + 3, 0xFFFFFFFF, false);
                ctx.drawText(textRenderer, "+", sx + SET_W - 9, sy + 3, 0xFFFFFFFF, false);
                String label = fit(s.name + " " + s.display(), SET_W - 36);
                ctx.drawText(textRenderer, label, sx + SET_W / 2 - textRenderer.getWidth(label) / 2, sy + 3, 0xFFFFFFFF, false);
            }
        }

        String hint = "Linksklick: an/aus   [-] [+]: Einstellungen   G / ESC: schliessen";
        ctx.drawText(textRenderer, hint, x + W / 2 - textRenderer.getWidth(hint) / 2, y + h - 16, 0xFF808088, false);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) return super.mouseClicked(mx, my, button);
        int x = px(), y = py();

        Module.Category[] cats = Module.Category.values();
        for (int i = 0; i < cats.length; i++) {
            if (in(mx, my, x + 12 + i * 104, y + 28, 100, 20)) { lastTab = cats[i]; return true; }
        }

        List<Module> mods = ModuleManager.of(lastTab);
        for (int i = 0; i < mods.size(); i++) {
            Module m = mods.get(i);
            int ry = rowY(i), rx = x + 8, rw = W - 16;
            if (!in(mx, my, rx, ry, rw, ROW - 4)) continue;

            int n = m.settings.size();
            for (int k = 0; k < n; k++) {
                int sx = setX(k, n), sy = ry + 6;
                if (in(mx, my, sx, sy, 14, 14)) { m.settings.get(k).change(-1); return true; }
                if (in(mx, my, sx + SET_W - 14, sy, 14, 14)) { m.settings.get(k).change(+1); return true; }
            }
            if (AdminClient.allowed(client)) m.toggle();
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_G) { close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
