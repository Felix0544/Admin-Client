package de.adminclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class FullbrightModule extends Module {

    public FullbrightModule() {
        super("Fullbright", "Immer volle Helligkeit", Category.BASES);
    }

    @Override
    public void onTick(MinecraftClient c, ClientPlayerEntity p) {
        StatusEffectInstance cur = p.getStatusEffect(StatusEffects.NIGHT_VISION);
        if (cur == null || cur.getDuration() < 400) {
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1000, 0, false, false, false));
        }
    }

    @Override
    public void onDisable() {
        ClientPlayerEntity p = MinecraftClient.getInstance().player;
        if (p != null) p.removeStatusEffect(StatusEffects.NIGHT_VISION);
    }
}
