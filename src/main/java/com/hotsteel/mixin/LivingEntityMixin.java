package com.hotsteel.mixin;

import com.hotsteel.logic.SuperFireResistanceHandler;
import com.hotsteel.registry.ModItems;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    /**
     * While Super Fire Resistance is active, treat lava as water inside {@code travel()} so the
     * player swims through lava with water physics instead of the sluggish lava branch.
     * Uses the body-touching-lava check so water physics stays applied while the player bobs at
     * the lava surface — keeps them moving smoothly instead of sink-rise-sink-rise cycling.
     */
    @Redirect(
        method = "travel",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isInWater()Z"))
    private boolean hotsteel$lavaAsWater(LivingEntity self) {
        if (self instanceof Player
            && SuperFireResistanceHandler.isActive(self)
            && SuperFireResistanceHandler.isBodyTouchingLava(self)) {
            return true;
        }
        return self.isInWater();
    }

    /**
     * While Super Fire Resistance is active in lava, drive the swim-amount interpolation from the
     * freshly-set swimming flag instead of {@code hasPose(SWIMMING)}. Vanilla calls
     * {@code updateSwimAmount()} BEFORE {@code updatePlayerPose()} in the tick, so the pose lags
     * the flag by one tick — without this redirect, the arms lower when you start sprinting and
     * keep rising the tick you stop sprinting, which reads as a twitch at every transition.
     */
    @Redirect(
        method = "updateSwimAmount",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isVisuallySwimming()Z"))
    private boolean hotsteel$lavaSwimAmountUseFlag(LivingEntity self) {
        if (self instanceof Player player
            && SuperFireResistanceHandler.isActive(self)
            && SuperFireResistanceHandler.isBodyTouchingLava(self)) {
            return player.isSwimming();
        }
        return self.isVisuallySwimming();
    }

    /** A Hot Steel shield sets the attacker on fire when it blocks a melee hit. */
    @Inject(method = "blockUsingShield", at = @At("TAIL"))
    private void hotsteel$igniteBlockedAttacker(LivingEntity attacker, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.level().isClientSide()
            && self.getUseItem().is(ModItems.HOT_STEEL_SHIELD)) {
            attacker.igniteForSeconds(5.0f);
        }
    }
}
