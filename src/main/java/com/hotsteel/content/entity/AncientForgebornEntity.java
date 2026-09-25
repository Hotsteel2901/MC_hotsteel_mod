package com.hotsteel.content.entity;

import java.util.List;

import com.hotsteel.logic.AdvancementHelper;
import com.hotsteel.registry.ModEntities;
import com.hotsteel.registry.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 「远古熔铸者」— the final boss of the Molten Age, summoned at the Molten Altar.
 *
 * <p>Three phases, driven by remaining health:
 * <ol>
 *   <li><b>熔铸之怒</b> — melee bruiser; periodically calls Slag Crawlers.</li>
 *   <li><b>烈焰喷发</b> — briefly invulnerable while it re-centres, then hammers the
 *       area with a 5-shot fireball fan and summons Ember Wisps.</li>
 *   <li><b>岩浆狂潮</b> — enraged: faster, hits harder, and floods the ground with
 *       lava pools around its target.</li>
 * </ol>
 */
public class AncientForgebornEntity extends Monster {

    private static final double PHASE_2_THRESHOLD = 0.66;
    private static final double PHASE_3_THRESHOLD = 0.33;
    private static final int TRANSITION_TICKS = 60;
    private static final int MAX_MINIONS = 6;

    private final ServerBossEvent bossEvent;
    private int phase = 1;
    private int transitionTicks = 0;
    private int summonCooldown = 80;
    private int barrageCooldown = 100;
    private int lavaCooldown = 120;
    private int fanVolley = 0;

    public AncientForgebornEntity(EntityType<? extends AncientForgebornEntity> type, Level level) {
        super(type, level);
        this.xpReward = 300;
        this.setPersistenceRequired();
        this.bossEvent = !level.isClientSide
            ? new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.RED,
                BossEvent.BossBarOverlay.PROGRESS)
            : null;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 400.0)
            .add(Attributes.MOVEMENT_SPEED, 0.28)
            .add(Attributes.ATTACK_DAMAGE, 14.0)
            .add(Attributes.ARMOR, 12.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
            .add(Attributes.ATTACK_KNOCKBACK, 2.0)
            .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 24.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // ---- Boss bar plumbing -------------------------------------------------

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (this.bossEvent != null) {
            this.bossEvent.addPlayer(player);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        if (this.bossEvent != null) {
            this.bossEvent.removePlayer(player);
        }
    }

    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        if (this.bossEvent != null) {
            this.bossEvent.setName(this.getDisplayName());
        }
    }

    // ---- Traits -----------------------------------------------------------

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        // The Forgeborn shrugs off wither; everything else applies normally.
        return effect.getEffect() != MobEffects.WITHER && super.canBeAffected(effect);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return this.transitionTicks > 0 || super.isInvulnerableTo(source);
    }

    // ---- Phase logic ------------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            return;
        }
        if (this.bossEvent != null) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        }

        if (this.transitionTicks > 0) {
            this.transitionTicks--;
            if (this.transitionTicks == 0) {
                this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 60, 0, false, false));
            }
            return;
        }

        float fraction = this.getHealth() / this.getMaxHealth();
        if (fraction <= PHASE_3_THRESHOLD && this.phase < 3) {
            enterPhase(3);
        } else if (fraction <= PHASE_2_THRESHOLD && this.phase < 2) {
            enterPhase(2);
        }

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        if (--this.summonCooldown <= 0) {
            this.summonCooldown = this.phase >= 2 ? 160 : 200;
            summonMinions(target);
        }

        if (this.phase >= 2 && --this.barrageCooldown <= 0) {
            this.barrageCooldown = this.phase >= 3 ? 60 : 90;
            fireFan(target);
        }

        if (this.phase >= 3 && --this.lavaCooldown <= 0) {
            this.lavaCooldown = 110;
            floodLava(target);
        }
    }

    private void enterPhase(int newPhase) {
        this.phase = newPhase;
        this.transitionTicks = TRANSITION_TICKS;
        this.setDeltaMovement(Vec3.ZERO);
        this.getNavigation().stop();
        this.removeAllEffects();
        this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, TRANSITION_TICKS, 3, false, false));

        if (newPhase >= 3) {
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.392);
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(18.0);
        }
        if (newPhase >= 2) {
            summonMinions(this.getTarget());
        }

        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.LAVA,
                this.getX(), this.getY() + this.getBbHeight() * 0.5, this.getZ(),
                120, 1.2, 1.4, 1.2, 0.2);
            server.sendParticles(ParticleTypes.EXPLOSION,
                this.getX(), this.getY() + this.getBbHeight() * 0.5, this.getZ(),
                8, 1.0, 1.0, 1.0, 0.0);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.WITHER_SPAWN,
            SoundSource.HOSTILE, 1.5f, 0.6f + newPhase * 0.15f);
        this.level().playSound(null, this.blockPosition(), SoundEvents.LAVA_EXTINGUISH,
            SoundSource.HOSTILE, 1.2f, 0.8f);
    }

    private void summonMinions(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        AABB area = this.getBoundingBox().inflate(24.0);
        List<Monster> nearby = server.getEntitiesOfClass(Monster.class, area,
            mob -> mob != this && (mob instanceof SlagCrawlerEntity || mob instanceof EmberWispEntity));
        if (nearby.size() >= MAX_MINIONS) {
            return;
        }
        int count = this.phase >= 3 ? 3 : 2;
        for (int i = 0; i < count; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            double radius = 2.5 + this.random.nextDouble() * 2.0;
            double x = this.getX() + Math.cos(angle) * radius;
            double z = this.getZ() + Math.sin(angle) * radius;
            boolean wisp = this.phase >= 2 && this.random.nextBoolean();
            EntityType<? extends Monster> type = wisp
                ? ModEntities.EMBER_WISP : ModEntities.SLAG_CRAWLER;
            Monster minion = type.create(server);
            if (minion == null) {
                continue;
            }
            minion.moveTo(x, this.getY() + 0.5, z, this.random.nextFloat() * 360.0f, 0.0f);
            if (minion instanceof SlagCrawlerEntity && target instanceof Player player) {
                minion.setTarget(player);
            }
            server.addFreshEntity(minion);
            server.sendParticles(ParticleTypes.FLAME, x, this.getY() + 0.5, z,
                12, 0.3, 0.3, 0.3, 0.02);
        }
    }

    /** Fires a five-shot fan of small fireballs at the target. */
    private void fireFan(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        Vec3 origin = new Vec3(this.getX(), this.getY() + this.getBbHeight() * 0.7, this.getZ());
        Vec3 aim = new Vec3(target.getX(), target.getEyeY(), target.getZ()).subtract(origin);
        Vec3 base = aim.normalize();
        // Step the volley out of the boss's own hitbox: fired from the body centre the
        // fan detonated against the Forgeborn itself on the first tick.
        Vec3 muzzle = origin.add(base.scale(1.4));
        this.fanVolley++;
        double spread = 0.16;
        for (int i = -2; i <= 2; i++) {
            // Nudge the aim horizontally to create the fan.
            Vec3 offset = new Vec3(base.z, 0.0, -base.x).normalize().scale(spread * i);
            Vec3 dir = base.add(offset).normalize().scale(1.2);
            SmallFireball fireball = new SmallFireball(server, this, dir);
            fireball.setPos(muzzle.x, muzzle.y, muzzle.z);
            server.addFreshEntity(fireball);
        }
        server.playSound(null, this.blockPosition(), SoundEvents.BLAZE_SHOOT,
            SoundSource.HOSTILE, 1.4f, 0.8f);
    }

    /** Phase 3 only: opens lava pools around the target. */
    private void floodLava(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        BlockPos around = target.blockPosition();
        int placed = 0;
        for (int attempt = 0; attempt < 10 && placed < 4; attempt++) {
            int dx = this.random.nextInt(5) - 2;
            int dz = this.random.nextInt(5) - 2;
            BlockPos pos = around.offset(dx, 0, dz);
            for (int dy = 1; dy >= -2; dy--) {
                BlockPos candidate = pos.above(dy);
                if (server.getBlockState(candidate).isAir()
                    && !server.getBlockState(candidate.below()).isAir()
                    && server.getBlockState(candidate).getFluidState().isEmpty()) {
                    server.setBlockAndUpdate(candidate, Blocks.LAVA.defaultBlockState());
                    server.sendParticles(ParticleTypes.LAVA,
                        candidate.getX() + 0.5, candidate.getY() + 0.5, candidate.getZ() + 0.5,
                        10, 0.3, 0.3, 0.3, 0.0);
                    placed++;
                    break;
                }
            }
        }
        if (placed > 0) {
            server.playSound(null, this.blockPosition(), SoundEvents.LAVA_POP,
                SoundSource.HOSTILE, 1.2f, 0.7f);
        }
    }

    // ---- Death ------------------------------------------------------------

    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                this.getX(), this.getY() + this.getBbHeight() * 0.5, this.getZ(),
                4, 1.0, 1.0, 1.0, 0.0);
            this.spawnAtLocation(new ItemStack(ModItems.FORGE_HEART));
            int ingots = 4 + this.random.nextInt(5); // 4–8
            this.spawnAtLocation(new ItemStack(ModItems.MOLTEN_STEEL_INGOT, ingots));
            if (source.getEntity() instanceof ServerPlayer player) {
                AdvancementHelper.award(player, "chapter_5", "unlocked");
            } else if (server.getNearestPlayer(this, 32.0) instanceof ServerPlayer player) {
                AdvancementHelper.award(player, "chapter_5", "unlocked");
            }
            for (ServerPlayer player : server.getPlayers(p -> p.distanceTo(this) < 64.0)) {
                player.displayClientMessage(
                    Component.translatable("message.hotsteel.forgeborn_defeated"), false);
            }
        }
        super.die(source);
    }
}
