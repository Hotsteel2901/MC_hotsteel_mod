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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
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
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0f, 0.7f);
        return InteractionResultHolder.success(stack);
    }

    private void releaseFlameWave(Level level, Player player) {
        Vec3 eye = player.getEyePosition(1.0f);
        Vec3 dir = player.getLookAngle();

        if (level instanceof ServerLevel serverLevel) {
            for (int step = 1; step <= RANGE; step++) {
                Vec3 point = eye.add(dir.scale(step));
                serverLevel.sendParticles(ParticleTypes.FLAME,
                    point.x, point.y, point.z, 3, 0.18, 0.18, 0.18, 0.02);
                if (step % 3 == 0) {
                    serverLevel.sendParticles(ParticleTypes.LAVA,
                        point.x, point.y, point.z, 1, 0.1, 0.1, 0.1, 0.0);
                }
            }
        }

        AABB corridor = player.getBoundingBox()
            .expandTowards(dir.scale(RANGE))
            .inflate(CORRIDOR_RADIUS);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, corridor)) {
            if (target == player || target.isAlliedTo(player)) {
                continue;
            }
            // Only burn what is actually in front of the wielder.
            Vec3 toTarget = target.position().subtract(eye);
            if (toTarget.dot(dir) < 0.0) {
                continue;
            }
            if (target instanceof AncientForgebornEntity) {
                continue; // the Forgeborn is immune to its own element
            }
            if (player instanceof Player attacker) {
                target.hurt(player.damageSources().playerAttack(attacker), WAVE_DAMAGE);
            }
            target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), WAVE_FIRE_TICKS));
        }
    }
}
