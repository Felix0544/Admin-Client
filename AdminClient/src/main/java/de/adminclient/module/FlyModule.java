package de.adminclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public class FlyModule extends Module {
    private final Setting speed;

    public FlyModule() {
        super("Fly", "Fliegen wie im Creative (2x Leertaste)", Category.MOVEMENT);
        speed = addSetting("Tempo", 1.0, 0.5, 5.0, 0.5, null);
    }

    @Override
    public void onTick(MinecraftClient c, ClientPlayerEntity p) {
        p.getAbilities().allowFlying = true;
        p.getAbilities().setFlySpeed((float) (0.05 * speed.value));
    }

    @Override
    public void onDisable() {
        ClientPlayerEntity p = MinecraftClient.getInstance().player;
        if (p == null) return;
        p.getAbilities().setFlySpeed(0.05f);
        if (!p.isCreative() && !p.isSpectator()) {
            p.getAbilities().allowFlying = false;
            p.getAbilities().flying = false;
        }
    }
}
