package com.hotsteel.content.item;

import com.hotsteel.content.entity.AncientForgebornEntity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 「熔铸钢剑」— unlike the Hot Steel sword (which strikes a single point of fire),
 * this one releases a <b>piercing wave of molten flame</b>: every enemy caught in
 * the corridor in front of the wielder is scorched and set alight. Longer reach,
 * longer cooldown, much wider punishment.
 */
public class MoltenSteelSwordItem extends HotSteelSwordItem {

    private static final int RANGE = 12;
    private static final int COOLDOWN_TICKS = 45;
    private static final int DURABILITY_COST = 6;
    private static final float WAVE_DAMAGE = 7.0f;
    private static final int WAVE_FIRE_TICKS = 100;
    private static final double CORRIDOR_RADIUS = 1.3;

    public MoltenSteelSwordItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide) {
            releaseFlameWave(level, player);
            if (!player.getAbilities().instabuild) {
                stack.hurtAndBreak(DURABILITY_COST, player, EquipmentSlot.MAINHAND);
            }
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
            // Server-side only: playing it on both sides made the client hear it twice.
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0f, 0.7f);
        }
        return InteractionResultHolder.success(stack);
    }

    private void releaseFlameWave(Level level, Player player) {
        Vec3 origin = player.getEyePosition(1.0f);
        Vec3 dir = player.getLookAngle().normalize();

        // The wave stops at the first wall, so you cannot burn enemies through blocks.
        BlockHitResult blockHit = level.clip(new ClipContext(origin,
            origin.add(dir.scale(RANGE)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        double reach = blockHit.getType() == HitResult.Type.BLOCK
            ? Math.max(0.5, origin.distanceTo(blockHit.getLocation()))
            : RANGE;

        if (level instanceof ServerLevel serverLevel) {
            int steps = (int) Math.ceil(reach);
            for (int step = 1; step <= steps; step++) {
                Vec3 point = origin.add(dir.scale(Math.min(step, reach)));
                serverLevel.sendParticles(ParticleTypes.FLAME,
                    point.x, point.y, point.z, 3, 0.16, 0.16, 0.16, 0.02);
                if (step % 3 == 0) {
                    serverLevel.sendParticles(ParticleTypes.LAVA,
                        point.x, point.y, point.z, 1, 0.1, 0.1, 0.1, 0.0);
                }
            }
        }

        // A real ray corridor: an entity is hit only if it sits close to the ray and in
        // front of the caster. (The old version used one big inflated AABB, which also
        // caught targets well off to the side of a diagonal aim.)
        AABB search = new AABB(origin, origin.add(dir.scale(reach))).inflate(CORRIDOR_RADIUS);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, search)) {
            if (target == player || target.isAlliedTo(player)) {
                continue;
            }
            if (target instanceof AncientForgebornEntity) {
                continue; // the Forgeborn is immune to its own element
            }
            Vec3 toTarget = target.getBoundingBox().getCenter().subtract(origin);
            double along = toTarget.dot(dir);
            if (along < 0.0 || along > reach) {
                continue;
            }
            double perpendicular = toTarget.subtract(dir.scale(along)).length();
            if (perpendicular > CORRIDOR_RADIUS) {
                continue;
            }
            target.hurt(player.damageSources().playerAttack(player), WAVE_DAMAGE);
            target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), WAVE_FIRE_TICKS));
        }
    }
}
