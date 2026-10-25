package de.adminclient.module;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.Vec3d;

/**
 * Freecam: Die Kamera loest sich von deiner Figur. Die Figur bleibt stehen (Bewegungstasten werden
 * ihr weggenommen), die Kamera fliegt mit WASD / Leertaste / Shift frei herum.
 */
public class FreecamModule extends Module {
    private static OtherClientPlayerEntity cam;
    private final Setting speed;

    public FreecamModule() {
        super("Freecam", "Kamera frei bewegen", Category.MOVEMENT);
        speed = addSetting("Tempo", 1.0, 0.5, 5.0, 0.5, null);
    }

    @Override
    public void onEnable() {
        MinecraftClient c = MinecraftClient.getInstance();
        ClientPlayerEntity p = c.player;
        if (p == null || c.world == null) { setEnabled(false); return; }
        cam = new OtherClientPlayerEntity(c.world, p.getGameProfile());
        place(p.getX(), p.getY(), p.getZ(), p.getYaw(), p.getPitch());
        c.setCameraEntity(cam);
    }

    @Override
    public void onDisable() {
        MinecraftClient c = MinecraftClient.getInstance();
        cam = null;
        if (c.player != null) {
            c.setCameraEntity(c.player);
            KeyBinding.updatePressedStates();
        }
    }

    private static void place(double x, double y, double z, float yaw, float pitch) {
        cam.setPosition(x, y, z);
        cam.prevX = x; cam.prevY = y; cam.prevZ = z;
        cam.setYaw(yaw); cam.setPitch(pitch);
        cam.prevYaw = yaw; cam.prevPitch = pitch;
    }

    private static boolean down(MinecraftClient c, KeyBinding kb) {
        InputUtil.Key k = KeyBindingHelper.getBoundKeyOf(kb);
        return k.getCategory() == InputUtil.Type.KEYSYM && InputUtil.isKeyPressed(c.getWindow().getHandle(), k.getCode());
    }

    /** Wird vor jedem Client-Tick aufgerufen (vor der Spielerbewegung). */
    public static void preTick(MinecraftClient c) {
        if (cam == null || c.player == null) return;
        GameOptions o = c.options;

        double f = 0, s = 0, v = 0;
        if (c.currentScreen == null) {
            if (down(c, o.forwardKey)) f += 1;
            if (down(c, o.backKey)) f -= 1;
            if (down(c, o.leftKey)) s += 1;
            if (down(c, o.rightKey)) s -= 1;
            if (down(c, o.jumpKey)) v += 1;
            if (down(c, o.sneakKey)) v -= 1;
        }
        // Figur bekommt keine Bewegungstasten
        for (KeyBinding k : new KeyBinding[]{o.forwardKey, o.backKey, o.leftKey, o.rightKey, o.jumpKey, o.sneakKey, o.sprintKey}) {
            k.setPressed(false);
        }

        FreecamModule m = ModuleManager.get(FreecamModule.class);
        double sp = 0.5 * (m != null ? m.speed.value : 1.0);
        double yr = Math.toRadians(c.player.getYaw()), pr = Math.toRadians(c.player.getPitch());
        Vec3d fwd = new Vec3d(-Math.sin(yr) * Math.cos(pr), -Math.sin(pr), Math.cos(yr) * Math.cos(pr));
        Vec3d left = new Vec3d(Math.cos(yr), 0, Math.sin(yr));
        Vec3d mv = fwd.multiply(f).add(left.multiply(s)).add(0, v, 0);
        if (mv.lengthSquared() > 0) mv = mv.normalize().multiply(sp);

        cam.prevX = cam.getX(); cam.prevY = cam.getY(); cam.prevZ = cam.getZ();
        cam.setPosition(cam.getX() + mv.x, cam.getY() + mv.y, cam.getZ() + mv.z);
        syncRotation();
    }

    /** Blickrichtung der Kamera = Blickrichtung der Figur (Maus). Wird auch pro Frame aufgerufen. */
    public static void syncRotation() {
        MinecraftClient c = MinecraftClient.getInstance();
        if (cam == null || c.player == null) return;
        float yaw = c.player.getYaw(), pitch = c.player.getPitch();
        cam.setYaw(yaw); cam.setPitch(pitch);
        cam.prevYaw = yaw; cam.prevPitch = pitch;
    }
}
