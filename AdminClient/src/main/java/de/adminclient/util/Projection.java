package de.adminclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;

/** Rechnet Weltkoordinaten in GUI-Bildschirmkoordinaten um (fuer ESP durch Waende). */
public final class Projection {
    private Projection() {}

    /** @return {x, y} in skalierten GUI-Pixeln oder null, wenn der Punkt hinter der Kamera liegt. */
    public static double[] project(MinecraftClient c, Vec3d world) {
        Camera cam = c.gameRenderer.getCamera();
        Vec3d cp = cam.getPos();
        double yaw = Math.toRadians(cam.getYaw());
        double pitch = Math.toRadians(cam.getPitch());

        Vec3d f = new Vec3d(-Math.sin(yaw) * Math.cos(pitch), -Math.sin(pitch), Math.cos(yaw) * Math.cos(pitch));
        Vec3d r = new Vec3d(-Math.cos(yaw), 0, -Math.sin(yaw));
        Vec3d u = r.crossProduct(f);

        Vec3d d = world.subtract(cp);
        double z = d.dotProduct(f);
        if (z < 0.05) return null;

        double fov = c.options.getFov().getValue();
        double tan = Math.tan(Math.toRadians(fov) / 2.0);
        double w = c.getWindow().getScaledWidth();
        double h = c.getWindow().getScaledHeight();

        double nx = d.dotProduct(r) / z;
        double ny = d.dotProduct(u) / z;
        double sx = w / 2.0 + nx / (tan * (w / h)) * (w / 2.0);
        double sy = h / 2.0 - ny / tan * (h / 2.0);
        return new double[]{sx, sy};
    }
}
