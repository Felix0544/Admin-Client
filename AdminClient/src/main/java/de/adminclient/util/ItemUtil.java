package de.adminclient.util;

import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

public final class ItemUtil {
    private ItemUtil() {}

    public static boolean isItem(ItemStack s, String path) {
        return s != null && !s.isEmpty() && Registries.ITEM.getId(s.getItem()).getPath().equals(path);
    }

    public static String blockPath(BlockState s) {
        return Registries.BLOCK.getId(s.getBlock()).getPath();
    }

    /** Hotbar-Slot (0-8) mit dem Item oder -1. */
    public static int findHotbar(ClientPlayerEntity p, String path) {
        for (int i = 0; i < 9; i++) {
            if (isItem(p.getInventory().getStack(i), path)) return i;
        }
        return -1;
    }

    /** Erster Hotbar-Slot, der NICHT das Item enthaelt. */
    public static int findHotbarNot(ClientPlayerEntity p, String path) {
        for (int i = 0; i < 9; i++) {
            if (!isItem(p.getInventory().getStack(i), path)) return i;
        }
        return -1;
    }
}
