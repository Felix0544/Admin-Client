package de.adminclient.module;

import de.adminclient.util.Projection;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.entity.TrappedChestBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;

/** Zeigt Kisten, Fass, Enderkiste & Shulker als Rahmen durch Waende (HUD-Projektion). */
public class ChestEspModule extends Module {
    private record Hit(BlockPos pos, int color, String label) {}

    private final List<Hit> hits = new ArrayList<>();
    private final Setting radius;

    public ChestEspModule() {
        super("Chest-ESP", "Kisten durch Waende sehen", Category.BASES);
        radius = addSetting("Radius", 48, 16, 128, 16, null);
    }

    @Override
    public void onDisable() { hits.clear(); }

    @Override
    public void onTick(MinecraftClient c, ClientPlayerEntity p) {
        if (p.age % 10 != 0) return;
        hits.clear();
        int r = (int) radius.value;
        double r2 = (double) r * r;
        int cr = (r >> 4) + 1;
        int pcx = p.getBlockX() >> 4;
        int pcz = p.getBlockZ() >> 4;
        for (int dx = -cr; dx <= cr; dx++) {
            for (int dz = -cr; dz <= cr; dz++) {
                Chunk ch = c.world.getChunk(pcx + dx, pcz + dz, ChunkStatus.FULL, false);
                if (!(ch instanceof WorldChunk wc)) continue;
                for (BlockEntity be : wc.getBlockEntities().values()) {
                    BlockPos bp = be.getPos();
                    double ddx = bp.getX() + 0.5 - p.getX(), ddy = bp.getY() + 0.5 - p.getY(), ddz = bp.getZ() + 0.5 - p.getZ();
                    if (ddx * ddx + ddy * ddy + ddz * ddz > r2) continue;
                    if (be instanceof TrappedChestBlockEntity) hits.add(new Hit(bp, 0xFFFF4040, "Trapped Chest"));
                    else if (be instanceof ChestBlockEntity) hits.add(new Hit(bp, 0xFFFFAA00, "Chest"));
                    else if (be instanceof EnderChestBlockEntity) hits.add(new Hit(bp, 0xFFB44DFF, "Ender Chest"));
                    else if (be instanceof BarrelBlockEntity) hits.add(new Hit(bp, 0xFFC08040, "Barrel"));
                    else if (be instanceof ShulkerBoxBlockEntity) hits.add(new Hit(bp, 0xFFFF66CC, "Shulker"));
                }
            }
        }
    }

    @Override
    public void onHud(DrawContext ctx, MinecraftClient c) {
        if (c.player == null) return;
        int drawn = 0;
        for (Hit h : hits) {
            if (drawn++ > 300) break;
            double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
            boolean ok = true;
            for (int i = 0; i < 8 && ok; i++) {
                Vec3d corner = new Vec3d(h.pos.getX() + (i & 1), h.pos.getY() + ((i >> 1) & 1), h.pos.getZ() + ((i >> 2) & 1));
                double[] s = Projection.project(c, corner);
                if (s == null) { ok = false; break; }
                minX = Math.min(minX, s[0]); maxX = Math.max(maxX, s[0]);
                minY = Math.min(minY, s[1]); maxY = Math.max(maxY, s[1]);
            }
            if (!ok) continue;
            int x = (int) minX, y = (int) minY, w = Math.max(2, (int) (maxX - minX)), hh = Math.max(2, (int) (maxY - minY));
            if (w > 600 || hh > 600) continue;
            ctx.drawBorder(x, y, w, hh, h.color);
            Vec3d cp = c.gameRenderer.getCamera().getPos();
            double ex = h.pos.getX() + 0.5 - cp.x, ey = h.pos.getY() + 0.5 - cp.y, ez = h.pos.getZ() + 0.5 - cp.z;
            double dist = Math.sqrt(ex * ex + ey * ey + ez * ez);
            ctx.drawText(c.textRenderer, h.label + " " + (int) dist + "m", x, y - 10, h.color, true);
        }
    }
}
