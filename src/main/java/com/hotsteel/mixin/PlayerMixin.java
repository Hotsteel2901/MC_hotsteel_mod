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
 */
@Mixin(Player.class)
public abstract class PlayerMixin {

    @Inject(method = "updateSwimming", at = @At("HEAD"), cancellable = true)
    private void hotsteel$lavaSwimPose(CallbackInfo ci) {
        Player self = (Player) (Object) this;
        if (!self.getAbilities().flying
            && self.isInLava()
            && SuperFireResistanceHandler.isActive(self)) {
            self.setSwimming(self.isSprinting() && !self.isPassenger());
            ci.cancel();
        }
    }
}
