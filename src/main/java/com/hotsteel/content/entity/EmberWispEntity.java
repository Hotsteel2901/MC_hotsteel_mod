package com.hotsteel.content.entity;

import com.hotsteel.registry.ModItems;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 「火翼灵」— a drifting ember spirit that keeps its distance and snipes with
 * small fireballs. Fire-proof, it drops Molten Shards and glowstone dust.
 */
public class EmberWispEntity extends Monster implements RangedAttackMob {

    public EmberWispEntity(EntityType<? extends EmberWispEntity> type, Level level) {
        super(type, level);
        this.xpReward = 12;
        this.moveControl = new FlyingMoveControl(this, 10, true);
        this.navigation = new FlyingPathNavigation(this, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.3)
            .add(Attributes.FLYING_SPEED, 0.5)
            .add(Attributes.ATTACK_DAMAGE, 5.0)
            .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, new RangedAttackGoal(this, 1.0, 40, 12.0f));
        this.goalSelector.addGoal(6, new RandomFlyGoal(this));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        if (this.level().isClientSide) {
            return;
        }
        Vec3 aim = target.getEyePosition().subtract(
            this.getX(), this.getY() + 0.5, this.getZ());
        SmallFireball fireball = new SmallFireball(this.level(), this, aim);
        fireball.setPos(this.getX(), this.getY() + 0.5, this.getZ());
        this.level().addFreshEntity(fireball);
        this.level().playSound(null, this.blockPosition(), SoundEvents.BLAZE_SHOOT,
            SoundSource.HOSTILE, 0.8f, 1.4f);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level().isClientSide) {
            return;
        }
        if (this.random.nextFloat() < 0.6f) {
            this.spawnAtLocation(new ItemStack(ModItems.MOLTEN_SHARD));
        }
        this.spawnAtLocation(new ItemStack(Items.GLOWSTONE_DUST, 1 + this.random.nextInt(2)));
    }

    /** Drifts to a random nearby point so the wisp hovers around rather than sitting still. */
    private static final class RandomFlyGoal extends Goal {

        private final EmberWispEntity wisp;

        private RandomFlyGoal(EmberWispEntity wisp) {
            this.wisp = wisp;
        }

        @Override
        public boolean canUse() {
            return this.wisp.getRandom().nextInt(40) == 0;
        }

        @Override
        public void start() {
            double x = this.wisp.getX() + (this.wisp.getRandom().nextDouble() * 2.0 - 1.0) * 8.0;
            double y = this.wisp.getY() + (this.wisp.getRandom().nextDouble() * 2.0 - 1.0) * 4.0;
            double z = this.wisp.getZ() + (this.wisp.getRandom().nextDouble() * 2.0 - 1.0) * 8.0;
            this.wisp.getMoveControl().setWantedPosition(x, y, z, 0.6);
        }
    }
}
