package com.hotsteel.mixin;

import java.util.Set;

import com.hotsteel.logic.SuperFireResistanceHandler;
import com.hotsteel.registry.ModItems;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    /** All Hot Steel and Molten-forged melee weapons ignite targets on hit. */
    @Unique
    private static final Set<Item> HOT_MELEE = Set.of(
        ModItems.HOT_STEEL_SWORD, ModItems.HOT_STEEL_KNIFE, ModItems.HOT_STEEL_AXE,
        ModItems.HOT_STEEL_MACE, ModItems.HOT_STEEL_TRIDENT,
        ModItems.MOLTEN_STEEL_SWORD, ModItems.MOLTEN_STEEL_SCYTHE, ModItems.MOLTEN_STEEL_AXE);

    /** Fire ticks applied to a target hit by a Hot Steel melee weapon (4s). */
    @Unique
    private static final int IGNITE_TICKS = 80;

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

    /** Hot Steel melee weapons set the target on fire. Fires only on actual melee damage
     * (direct attacker is a Player holding a Hot Steel weapon, damage type is melee) and only
     * when the hit actually landed (return value true). */
    @Inject(method = "hurt", at = @At("TAIL"))
    private void hotsteel$igniteOnHotSteelHit(DamageSource source, float amount,
                                              CallbackInfoReturnable<Boolean> cir) {
        if (!Boolean.TRUE.equals(cir.getReturnValue())) {
            return; // damage wasn't actually applied
        }
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide()
            || !source.is(DamageTypeTags.IS_PLAYER_ATTACK)
            || !(source.getDirectEntity() instanceof Player attacker)
            || !HOT_MELEE.contains(attacker.getMainHandItem().getItem())
            || self == attacker) {
            return;
        }
        self.setRemainingFireTicks(Math.max(self.getRemainingFireTicks(), IGNITE_TICKS));
    }

    /** Wearing the Hot Steel chestplate sets whoever melee-hits you on fire (fiery thorns). */
    @Inject(method = "hurt", at = @At("TAIL"))
    private void hotsteel$igniteOnChestplateHit(DamageSource source, float amount,
                                                CallbackInfoReturnable<Boolean> cir) {
        if (!Boolean.TRUE.equals(cir.getReturnValue())) {
            return;
        }
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide()
            || !self.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.HOT_STEEL_CHESTPLATE)) {
            return;
        }
        if (source.getDirectEntity() instanceof LivingEntity attacker && attacker != self) {
            attacker.setRemainingFireTicks(Math.max(attacker.getRemainingFireTicks(), IGNITE_TICKS));
        }
    }

    /**
     * 「熔炉壁垒」— a Hot Steel shield does more than block. The barrier of molten
     * steel throws the attacker back and coats the wielder in a brief shell of
     * heat-resistance on every block, so holding the line against fire is a real
     * tactic rather than a flat damage reduction.
     */
    @Inject(method = "blockUsingShield", at = @At("TAIL"))
    private void hotsteel$shieldWard(LivingEntity attacker, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide()
            || !self.getUseItem().is(ModItems.HOT_STEEL_SHIELD)) {
            return;
        }
        // Set the attacker alight...
        attacker.setRemainingFireTicks(Math.max(attacker.getRemainingFireTicks(), IGNITE_TICKS));
        // ...and shove them off the shield.
        Vec3 push = attacker.position().subtract(self.position());
        Vec3 flat = new Vec3(push.x, 0.0, push.z);
        if (flat.lengthSqr() > 1.0E-4) {
            flat = flat.normalize().scale(0.9);
            attacker.push(flat.x, 0.36, flat.z);
            attacker.hurtMarked = true;
        }
        // The block itself leaves the wielder briefly warded against fire.
        self.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, false, false, false));
    }
}
