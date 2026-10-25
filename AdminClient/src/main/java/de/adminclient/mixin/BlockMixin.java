package de.adminclient.mixin;

import de.adminclient.module.XRayModule;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** X-Ray: Nur Erze & Co. werden gezeichnet, alles andere wird ausgeblendet. */
@Mixin(Block.class)
public abstract class BlockMixin {
    @Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true)
    private static void adminclient$xray(BlockState state, BlockState otherState, Direction side,
                                         CallbackInfoReturnable<Boolean> cir) {
        if (XRayModule.active) {
            cir.setReturnValue(XRayModule.isTarget(state));
        }
    }
}
