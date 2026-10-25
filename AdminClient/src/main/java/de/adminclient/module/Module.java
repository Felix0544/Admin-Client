package de.adminclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class Module {

    public enum Category {
        PVP("PvP"), BASES("Bases"), MOVEMENT("Movement");
        public final String label;
        Category(String label) { this.label = label; }
    }

    /** Einstellbarer Wert (Zahl oder Auswahl) eines Moduls. */
    public static final class Setting {
        public final String name;
        public double value;
        public final double min, max, step;
        public final String[] options;

        Setting(String name, double value, double min, double max, double step, String[] options) {
            this.name = name; this.value = value; this.min = min; this.max = max; this.step = step; this.options = options;
        }

        public String display() {
            if (options != null) return options[(int) value];
            if (value == Math.rint(value)) return String.valueOf((int) value);
            return String.format(Locale.ROOT, "%.1f", value);
        }

        public void change(int dir) {
            double v = Math.round((value + dir * step) * 100.0) / 100.0;
            value = Math.max(min, Math.min(max, v));
        }
    }

    public final String name;
    public final String description;
    public final Category category;
    public final List<Setting> settings = new ArrayList<>();
    private boolean enabled;

    protected Module(String name, String description, Category category) {
        this.name = name; this.description = description; this.category = category;
    }

    protected Setting addSetting(String name, double def, double min, double max, double step, String[] options) {
        Setting s = new Setting(name, def, min, max, step, options);
        settings.add(s);
        return s;
    }

    public boolean isEnabled() { return enabled; }
    public void toggle() { setEnabled(!enabled); }

    public void setEnabled(boolean e) {
        if (e == enabled) return;
        enabled = e;
        if (e) onEnable(); else onDisable();
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick(MinecraftClient c, ClientPlayerEntity p) {}
    public void onHud(DrawContext ctx, MinecraftClient c) {}
}
