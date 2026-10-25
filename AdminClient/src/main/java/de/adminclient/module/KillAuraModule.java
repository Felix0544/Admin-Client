package de.adminclient.module;

import de.adminclient.util.Targets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class KillAuraModule extends Module {
    private final Setting range;
    private final Setting targets;

    public KillAuraModule() {
        super("Kill Aura", "Greift Ziele in Reichweite an", Category.PVP);
        range = addSetting("Range", 3.0, 2.0, 6.0, 0.5, null);
        targets = addSetting("Ziele", 0, 0, 2, 1, Targets.MODES);
    }

    @Override
    public void onTick(MinecraftClient c, ClientPlayerEntity p) {
        if (c.interactionManager == null || p.isUsingItem()) return;
        if (p.getAttackCooldownProgress(0.5f) < 1.0f) return; // erst wenn der Schlag voll geladen ist

        Vec3d eye = p.getEyePos();
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity e : c.world.getEntities()) {
            if (!Targets.valid(e, p, (int) targets.value)) continue;
            double d = Targets.distToBox(eye, e.getBoundingBox());
            if (d <= range.value && d < bestDist) {
                bestDist = d;
                best = (LivingEntity) e;
            }
        }
        if (best != null) {
            c.interactionManager.attackEntity(p, best);
            p.swingHand(Hand.MAIN_HAND);
        }
    }
}
