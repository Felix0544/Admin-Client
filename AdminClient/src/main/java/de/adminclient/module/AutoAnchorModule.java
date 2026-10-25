package de.adminclient.module;

import de.adminclient.util.ItemUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * Auto Anchor: setzt einen Respawn-Anker neben den naechsten Spieler, ladet ihn mit Glowstone und zuendet ihn.
 * Braucht Respawn-Anker + Glowstone in der Hotbar. Explodiert nur in Overworld/End (im Nether nicht).
 */
public class AutoAnchorModule extends Module {
    private final Setting targetRange;
    private final Setting delay;
    private BlockPos anchor;
    private int timer;

    public AutoAnchorModule() {
        super("Auto Anchor", "Anker setzen, laden, zuenden", Category.PVP);
        targetRange = addSetting("Ziel", 5, 3, 8, 1, null);
        delay = addSetting("Delay", 3, 1, 10, 1, null);
    }

    @Override
    public void onDisable() { anchor = null; timer = 0; }

    @Override
    public void onTick(MinecraftClient c, ClientPlayerEntity p) {
        if (c.interactionManager == null || c.world == null) return;
        if (++timer < (int) delay.value) return;
        timer = 0;

        PlayerEntity target = nearestPlayer(c, p, targetRange.value);
        if (target == null) { anchor = null; return; }

        ClientWorld w = c.world;
        Vec3d eye = p.getEyePos();

        if (anchor == null) {
            int slot = ItemUtil.findHotbar(p, "respawn_anchor");
            if (slot < 0) return;
            BlockPos spot = findSpot(w, p, target, eye);
            if (spot == null) return;
            p.getInventory().selectedSlot = slot;
            BlockPos ground = spot.down();
            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(ground).add(0, 0.5, 0), Direction.UP, ground, false);
            ActionResult r = c.interactionManager.interactBlock(p, Hand.MAIN_HAND, hit);
            if (r.isAccepted()) {
                p.swingHand(Hand.MAIN_HAND);
                anchor = spot;
            }
            return;
        }

        BlockState st = w.getBlockState(anchor);
        if (!ItemUtil.blockPath(st).equals("respawn_anchor") || eye.distanceTo(Vec3d.ofCenter(anchor)) > 4.5) {
            anchor = null;
            return;
        }
        int charges = st.get(RespawnAnchorBlock.CHARGES);
        int slot = charges == 0 ? ItemUtil.findHotbar(p, "glowstone") : ItemUtil.findHotbarNot(p, "glowstone");
        if (slot < 0) return;
        p.getInventory().selectedSlot = slot;
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(anchor), Direction.UP, anchor, false);
        c.interactionManager.interactBlock(p, Hand.MAIN_HAND, hit);
        p.swingHand(Hand.MAIN_HAND);
        if (charges > 0) anchor = null; // gezuendet
    }

    private static PlayerEntity nearestPlayer(MinecraftClient c, ClientPlayerEntity self, double range) {
        PlayerEntity best = null;
        double bd = range;
        for (PlayerEntity o : c.world.getPlayers()) {
            if (o == self || o.isSpectator() || !o.isAlive()) continue;
            double d = self.distanceTo(o);
            if (d <= bd) { bd = d; best = o; }
        }
        return best;
    }

    /** Freies Feld neben dem Ziel (am Boden), in Reichweite, moeglichst weit von dir weg. */
    private static BlockPos findSpot(ClientWorld w, ClientPlayerEntity p, PlayerEntity target, Vec3d eye) {
        BlockPos t = target.getBlockPos();
        BlockPos best = null;
        double bestD = -1;
        for (Direction d : Direction.values()) {
            if (!d.getAxis().isHorizontal()) continue;
            BlockPos cand = t.offset(d);
            if (cand.equals(p.getBlockPos())) continue;
            if (!w.getBlockState(cand).isAir()) continue;
            if (!w.getBlockState(cand.down()).isSolidBlock(w, cand.down())) continue;
            Vec3d center = Vec3d.ofCenter(cand);
            if (eye.distanceTo(center) > 4.5) continue;
            double dist = center.distanceTo(p.getPos());
            if (dist > bestD) { bestD = dist; best = cand; }
        }
        return best;
    }
}
