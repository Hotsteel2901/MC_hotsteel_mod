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
     * player moves through lava with water physics (buoyancy + horizontal momentum) instead of the
     * sluggish vanilla lava physics. A Dolphin's-Grace effect applied by
     * {@link SuperFireResistanceHandler} then makes the movement noticeably faster than water.
     * <p>
     * The redirect is scoped to {@code travel()} only, so vanilla {@code updateSwimming()} and
     * {@code updatePlayerPose()} still see the real {@code isInWater()} (false for lava) — the
     * player keeps a normal standing pose in lava, no swim-pose twitch.
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
