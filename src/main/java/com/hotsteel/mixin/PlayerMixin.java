package com.hotsteel.mixin;

import com.hotsteel.logic.SuperFireResistanceHandler;

import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Locks the player's swim flag and Pose while Super Fire Resistance is active
 * inside lava, so sprinting cleanly flips between SWIMMING and STANDING with no
 * twitching.
 * <p>
 * Symptom this fixes: in vanilla, lava is not a swim-able fluid, so the moment
 * the mod sets the SWIMMING pose / swim flag, vanilla logic elsewhere in the
 * tick (e.g. {@code Entity.updateSwimming} on the base class, or pose
 * re-evaluation) "corrects" it back to STANDING. The flag then re-toggles the
 * next tick, producing the rapid swim/not-swim twitch the player saw.
 * <p>
 * Strategy: as long as the player is in lava with Super Fire Resistance active,
 * we take full ownership of BOTH the swim flag and the Pose. Vanilla is not
 * allowed to touch either while we're driving.
 * <ul>
 *   <li>{@code updateSwimming} — cancel always; force
 *       {@code setSwimming(isSprinting())}.</li>
 *   <li>{@code updatePlayerPose} — cancel always; force SWIMMING pose when
 *       sprinting, STANDING pose otherwise.</li>
 * </ul>
 * The {@link com.hotsteel.mixin.LivingEntityMixin} still applies water-physics
 * to lava inside {@code travel()} and feeds the swim flag into
 * {@code updateSwimAmount()}, so movement and arm interpolation both stay in
 * sync with the locked pose.
 */
@Mixin(Player.class)
public abstract class PlayerMixin {

    /**
     * Owns the swim shared-flag while in lava + active. Vanilla's
     * {@code updateSwimming} checks {@code isInWater()} (false for lava) and
     * would otherwise keep resetting the flag to false every tick.
     */
    @Inject(method = "updateSwimming", at = @At("HEAD"), cancellable = true)
    private void hotsteel$lavaSwimFlag(CallbackInfo ci) {
        Player self = (Player) (Object) this;
        if (!self.getAbilities().flying
            && !self.isPassenger()
            && SuperFireResistanceHandler.isActive(self)
            && SuperFireResistanceHandler.isBodyTouchingLava(self)) {
            // Lock: sprint on -> swim flag on; sprint off -> swim flag off.
            self.setSwimming(self.isSprinting());
            ci.cancel();
        }
    }

    /**
     * Owns the Pose while in lava + active. This is the actual "lock" — even
     * if vanilla or another system tried to set a different pose, we override
     * it here on every tick. {@code updatePlayerPose} is the unique writer of
     * {@code Pose.SWIMMING} for the player, so cancelling it is sufficient.
     * <p>
     * Outside lava (or without the effect) we don't cancel, so vanilla behaviour
     * is fully preserved — water swimming, fall flying, spin attack,
     * crouching all work normally.
     */
    @Inject(method = "updatePlayerPose", at = @At("HEAD"), cancellable = true)
    private void hotsteel$lockLavaSwimPose(CallbackInfo ci) {
        Player self = (Player) (Object) this;
        if (!self.getAbilities().flying
            && !self.isPassenger()
            && SuperFireResistanceHandler.isActive(self)
            && SuperFireResistanceHandler.isBodyTouchingLava(self)) {
            if (self.isSprinting()) {
                // Sprint + lava + effect -> lock SWIMMING. Also re-sync the
                // flag in case something cleared it between updateSwimming
                // and here (defensive; the updateSwimming inject should
                // already keep it in sync).
                self.setSwimming(true);
                self.setPose(Pose.SWIMMING);
            } else {
                // Not sprinting -> lock STANDING. Vanilla would already pick
                // STANDING here (no water -> no swim), but forcing it removes
                // any chance of a 1-tick SWIMMING remnant during the
                // sprint-off transition.
                self.setSwimming(false);
                self.setPose(Pose.STANDING);
            }
            ci.cancel();
        }
    }
}
