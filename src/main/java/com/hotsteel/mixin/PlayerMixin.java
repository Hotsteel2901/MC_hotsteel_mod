package com.hotsteel.mixin;

import com.hotsteel.logic.SuperFireResistanceHandler;

import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes lava swimming behave exactly like water swimming while Super Fire Resistance is active:
 * normal (standing) pose by default, and only entering the swimming pose while sprinting.
 * <p>
 * Uses the body-touching-lava check (rather than vanilla {@code isInLava()}, which is eye-level
 * only) so the swim flag stays stable while the player bobs at the lava surface. Without this,
 * every time water physics lifts the player's eyes above the surface, vanilla logic flips the
 * swim flag off and the player sinks back down — causing the visible "twitch between swimming and
 * not swimming" the player sees.
 */
@Mixin(Player.class)
public abstract class PlayerMixin {

    @Inject(method = "updateSwimming", at = @At("HEAD"), cancellable = true)
    private void hotsteel$lavaSwimPose(CallbackInfo ci) {
        Player self = (Player) (Object) this;
        if (!self.getAbilities().flying
            && !self.isPassenger()
            && SuperFireResistanceHandler.isActive(self)
            && SuperFireResistanceHandler.isBodyTouchingLava(self)) {
            self.setSwimming(self.isSprinting());
            ci.cancel();
        }
    }
}
