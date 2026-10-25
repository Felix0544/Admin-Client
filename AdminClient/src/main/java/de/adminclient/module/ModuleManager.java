package de.adminclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public final class ModuleManager {
    private ModuleManager() {}

    public static final List<Module> ALL = new ArrayList<>();

    public static void init() {
        ALL.clear();
        // PvP
        ALL.add(new AutoAnchorModule());
        ALL.add(new KillAuraModule());
        ALL.add(new BowAimModule());
        // Bases
        ALL.add(new XRayModule());
        ALL.add(new ChestEspModule());
        ALL.add(new FullbrightModule());
        // Movement
        ALL.add(new FlyModule());
        ALL.add(new SpeedModule());
        ALL.add(new FreecamModule());
    }

    @SuppressWarnings("unchecked")
    public static <T extends Module> T get(Class<T> type) {
        for (Module m : ALL) if (type.isInstance(m)) return (T) m;
        return null;
    }

    public static List<Module> of(Module.Category cat) {
        List<Module> out = new ArrayList<>();
        for (Module m : ALL) if (m.category == cat) out.add(m);
        return out;
    }

    public static void tick(MinecraftClient c, ClientPlayerEntity p) {
        for (Module m : ALL) if (m.isEnabled()) m.onTick(c, p);
    }

    public static void disableAll() {
        for (Module m : ALL) m.setEnabled(false);
    }

    public static void hud(DrawContext ctx, MinecraftClient c) {
        if (c.player == null || c.options.hudHidden) return;
        for (Module m : ALL) if (m.isEnabled()) m.onHud(ctx, c);

        // kleine Liste der aktiven Module oben rechts
        TextRenderer tr = c.textRenderer;
        int w = c.getWindow().getScaledWidth();
        int y = 4;
        for (Module m : ALL) {
            if (!m.isEnabled()) continue;
            ctx.drawText(tr, m.name, w - tr.getWidth(m.name) - 4, y, 0xFF55FF55, true);
            y += 10;
        }
    }
}
