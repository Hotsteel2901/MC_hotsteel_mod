package com.hotsteel.logic;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Shared aiming helper for everything the mod launches (fire, arrows, bottles,
 * fireballs, tridents, wisp bolts).
 * <p>
 * Vanilla projectiles are positioned at {@code eyeY - 0.1}, i.e. effectively
 * inside the shooter's head. Anything that also does an area effect on impact
 * (explosions, lava, flame waves) then goes off in the shooter's own face when
 * they aim downwards or stand against a wall. Launching from a point clearly in
 * front of the eyes keeps the origin readable and the blast away from the caster.
 */
public final class LaunchHelper {

    private LaunchHelper() {}

    /** The muzzle of a projectile fired by {@code shooter}: slightly ahead of the eyes. */
    public static Vec3 muzzle(LivingEntity shooter, double forward) {
        return shooter.getEyePosition(1.0f).add(shooter.getLookAngle().scale(forward));
    }

    /** Muzzle height for a mob, placed at the middle of its body. */
    public static double mobMuzzleY(LivingEntity mob) {
        return mob.getY() + mob.getBbHeight() * 0.6;
    }
}
