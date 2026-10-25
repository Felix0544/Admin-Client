package de.adminclient.module;

import de.adminclient.util.ItemUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** X-Ray ueber BlockMixin (Block.shouldDrawSide). Funktioniert ohne Sodium. */
public class XRayModule extends Module {
    public static volatile boolean active = false;

    private static final Map<Block, Boolean> CACHE = new ConcurrentHashMap<>();
    private static final Set<String> EXTRA = Set.of(
            "ancient_debris", "spawner", "chest", "trapped_chest", "ender_chest", "barrel",
            "end_portal_frame", "lava");

    public XRayModule() {
        super("X-Ray", "Nur Erze sichtbar (ohne Sodium)", Category.BASES);
    }

    public static boolean isTarget(BlockState state) {
        return CACHE.computeIfAbsent(state.getBlock(), b -> {
            String id = ItemUtil.blockPath(state);
            return id.endsWith("_ore") || EXTRA.contains(id);
        });
    }

    @Override
    public void onEnable() {
        active = true;
        CACHE.clear();
        FullbrightModule fb = ModuleManager.get(FullbrightModule.class);
        if (fb != null) fb.setEnabled(true); // ohne Licht sind Erze im Fels sonst dunkel
        reload();
    }

    @Override
    public void onDisable() {
        active = false;
        reload();
    }

    private static void reload() {
        MinecraftClient c = MinecraftClient.getInstance();
        if (c.worldRenderer != null && c.world != null) c.worldRenderer.reload();
    }
}
