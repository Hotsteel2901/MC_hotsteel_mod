package com.hotsteel.logic;

import com.hotsteel.registry.ModItems;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Effects for the Hot Steel bow and crossbow that work with <em>any</em>
 * ammunition, not just the mod's own arrows.
 *
 * <ul>
 *   <li><b>Hot Steel bow — 灼热之矢:</b> every arrow it launches ignites what it
 *       hits (and it already hits 1.5 harder at full draw).</li>
 *   <li><b>Hot Steel crossbow — 熔火爆矢:</b> bolts fly heavier (+2 damage) and
 *       detonate where they land, burning and shoving everything in a 2.5 block
 *       burst.</li>
 * </ul>
 *
 * <p>{@link AbstractArrow#getWeaponItem()} carries the launcher stack all the
 * way to the impact, so there is no flag to store and nothing to guess: the
 * projectile itself says which weapon fired it.
 */
public final class HotSteelRangedHandler {

    private HotSteelRangedHandler() {}

    /** Fire ticks applied by the bow's arrows (5s). */
    private static final int BOW_IGNITE_TICKS = 100;
    /** Fire ticks applied by the crossbow's burst (4s). */
    private static final int BOLT_IGNITE_TICKS = 80;
    /** Extra damage carried by a crossbow bolt. */
    private static final double BOLT_BONUS_DAMAGE = 2.0;
    private static final double BURST_RADIUS = 2.5;
    private static final float BURST_DAMAGE = 4.0f;

    public static void register() {
        // ALLOW_DAMAGE fires while the projectile is applying its hit, which is exactly
        // where an "on impact" effect belongs. The hit itself is never consumed.
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity.level().isClientSide()) {
                return true;
            }
            if (!(source.getDirectEntity() instanceof AbstractArrow arrow)) {
                return true;
            }
            ItemStack weapon = arrow.getWeaponItem();
            boolean hotBow = weapon.is(ModItems.HOT_STEEL_BOW);
            boolean hotCrossbow = weapon.is(ModItems.HOT_STEEL_CROSSBOW);
            if (!hotBow && !hotCrossbow
                && !arrow.getPickupItemStackOrigin().is(ModItems.HOT_STEEL_ARROW)) {
                return true;
            }
            if (!entity.fireImmune()) {
                entity.setRemainingFireTicks(Math.max(entity.getRemainingFireTicks(),
                    hotCrossbow ? BOLT_IGNITE_TICKS : BOW_IGNITE_TICKS));
            }
            if (hotCrossbow) {
                burst(arrow, entity, arrow.getOwner() instanceof LivingEntity owner ? owner : null);
            }
            return true;
        });
    }

    /**
     * Heavier bolts: called by {@code HotSteelCrossbowItem} the moment a bolt is
     * created, so the extra damage is part of the projectile from the start.
     */
    public static void heavyBolt(AbstractArrow arrow) {
        arrow.setBaseDamage(arrow.getBaseDamage() + BOLT_BONUS_DAMAGE);
    }

    /** The crossbow bolt's impact: a tight flame burst that shoves and burns nearby mobs. */
    private static void burst(AbstractArrow arrow, LivingEntity hit, LivingEntity shooter) {
        Level level = arrow.level();
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        Vec3 at = arrow.position();
        AABB area = new AABB(at.x - BURST_RADIUS, at.y - BURST_RADIUS, at.z - BURST_RADIUS,
            at.x + BURST_RADIUS, at.y + BURST_RADIUS, at.z + BURST_RADIUS);
        for (LivingEntity bystander : server.getEntitiesOfClass(LivingEntity.class, area)) {
            if (bystander == shooter || bystander == hit) {
                continue;
            }
            if (shooter != null && bystander.isAlliedTo(shooter)) {
                continue;
            }
            bystander.hurt(server.damageSources().explosion(arrow, shooter), BURST_DAMAGE);
            if (!bystander.fireImmune()) {
                bystander.setRemainingFireTicks(Math.max(bystander.getRemainingFireTicks(),
                    BOLT_IGNITE_TICKS));
            }
            Vec3 push = bystander.position().subtract(at);
            Vec3 flat = new Vec3(push.x, 0.0, push.z);
            if (flat.lengthSqr() > 1.0E-4) {
                flat = flat.normalize().scale(0.45);
                bystander.push(flat.x, 0.25, flat.z);
                bystander.hurtMarked = true;
            }
        }
        server.sendParticles(ParticleTypes.FLAME, at.x, at.y, at.z, 26, 0.45, 0.45, 0.45, 0.06);
        server.sendParticles(ParticleTypes.LAVA, at.x, at.y, at.z, 6, 0.3, 0.3, 0.3, 0.0);
        server.playSound(null, arrow.blockPosition(), SoundEvents.FIRECHARGE_USE,
            SoundSource.PLAYERS, 1.0f, 0.7f);
    }
}