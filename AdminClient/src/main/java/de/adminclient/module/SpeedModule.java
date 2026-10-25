package de.adminclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class SpeedModule extends Module {
    private final Setting factor;

    public SpeedModule() {
        super("Speed", "Schneller laufen", Category.MOVEMENT);
        factor = addSetting("Faktor", 1.4, 1.1, 2.5, 0.1, null);
    }

    @Override
    public void onTick(MinecraftClient c, ClientPlayerEntity p) {
        if (!p.isOnGround()) return;
        if (p.input.movementForward == 0 && p.input.movementSideways == 0) return;
        Vec3d v = p.getVelocity();
        double h = Math.hypot(v.x, v.z);
        if (h < 0.01) return;
        double nh = Math.min(h * factor.value, 0.30 * factor.value);
        double k = nh / h;
        p.setVelocity(v.x * k, v.y, v.z * k);
    }
}
