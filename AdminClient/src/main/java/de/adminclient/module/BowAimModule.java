package de.adminclient.module;

import de.adminclient.util.ItemUtil;
import de.adminclient.util.Targets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BowItem;
import net.minecraft.util.math.Vec3d;

/** Zielt mit dem Bogen automatisch (inkl. Fallkurve & Vorhalt). Schiessen/Loslassen machst du selbst. */
public class BowAimModule extends Module {
    private final Setting reach;
    private final Setting targets;

    public BowAimModule() {
        super("Auto Bow Aim", "Zielt mit dem Bogen automatisch", Category.PVP);
        reach = addSetting("Reichweite", 60, 20, 100, 10, null);
        targets = addSetting("Ziele", 0, 0, 2, 1, Targets.MODES);
    }

    @Override
    public void onTick(MinecraftClient c, ClientPlayerEntity p) {
        if (!p.isUsingItem() || !ItemUtil.isItem(p.getActiveItem(), "bow")) return;
        double power = BowItem.getPullProgress(p.getItemUseTime());
        if (power < 0.1) return;
        double speed = power * 3.0;

        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity e : c.world.getEntities()) {
            if (!Targets.valid(e, p, (int) targets.value) || !p.canSee(e)) continue;
            double d = p.distanceTo(e);
            if (d <= reach.value && d < bestDist) {
                bestDist = d;
                best = (LivingEntity) e;
            }
        }
        if (best == null) return;

        Vec3d eye = p.getEyePos();
        Vec3d center = best.getBoundingBox().getCenter();
        Vec3d vel = new Vec3d(best.getX() - best.prevX, best.isOnGround() ? 0 : best.getY() - best.prevY, best.getZ() - best.prevZ);

        Vec3d aim = center;
        double[] sol = null;
        for (int i = 0; i < 4; i++) {
            sol = solve(eye, aim, speed);
            if (sol == null) return;
            aim = center.add(vel.multiply(sol[2]));
        }
        sol = solve(eye, aim, speed);
        if (sol == null) return;
        p.setYaw((float) sol[0]);
        p.setPitch((float) sol[1]);
    }

    /** @return {yaw, pitch, flugdauer in Ticks} oder null, wenn nicht erreichbar. */
    private static double[] solve(Vec3d eye, Vec3d target, double speed) {
        double dx = target.x - eye.x, dz = target.z - eye.z;
        double d = Math.hypot(dx, dz);
        double dy = target.y - (eye.y - 0.1);
        double yaw = Math.toDegrees(Math.atan2(-dx, dz));

        double prevDiff = Double.NaN, prevDeg = 0;
        for (double deg = -80; deg <= 80; deg += 0.5) {
            double[] r = simulate(Math.toRadians(deg), speed, d);
            if (r == null) { prevDiff = Double.NaN; continue; }
            double diff = r[0] - dy;
            if (!Double.isNaN(prevDiff) && prevDiff < 0 && diff >= 0) {
                double f = -prevDiff / (diff - prevDiff);
                double ang = prevDeg + f * 0.5;
                double[] rr = simulate(Math.toRadians(ang), speed, d);
                return new double[]{yaw, -ang, rr != null ? rr[1] : r[1]};
            }
            prevDiff = diff;
            prevDeg = deg;
        }
        return null;
    }

    /** Flugbahn eines Pfeils: {Hoehe bei Distanz d, Ticks}. */
    private static double[] simulate(double theta, double speed, double d) {
        double vx = speed * Math.cos(theta), vy = speed * Math.sin(theta);
        double x = 0, y = 0;
        for (int t = 1; t <= 300; t++) {
            double nx = x + vx, ny = y + vy;
            if (nx >= d) {
                double f = (d - x) / (nx - x);
                return new double[]{y + f * (ny - y), t - 1 + f};
            }
            x = nx;
            y = ny;
            vx *= 0.99;
            vy *= 0.99;
            vy -= 0.05;
        }
        return null;
    }
}
