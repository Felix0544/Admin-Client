package de.adminclient.util;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class Targets {
    private Targets() {}

    public static final String[] MODES = {"Spieler", "Monster", "Alle"};

    public static boolean valid(Entity e, ClientPlayerEntity self, int mode) {
        if (!(e instanceof LivingEntity le) || e == self || !le.isAlive() || e instanceof ArmorStandEntity) return false;
        if (le.isSpectator()) return false;
        return switch (mode) {
            case 0 -> e instanceof PlayerEntity;
            case 1 -> e instanceof Monster;
            default -> true;
        };
    }

    public static double distToBox(Vec3d p, Box b) {
        double dx = Math.max(Math.max(b.minX - p.x, 0), p.x - b.maxX);
        double dy = Math.max(Math.max(b.minY - p.y, 0), p.y - b.maxY);
        double dz = Math.max(Math.max(b.minZ - p.z, 0), p.z - b.maxZ);
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
