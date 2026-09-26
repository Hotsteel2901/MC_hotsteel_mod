package com.hotsteel.content.entity;

import java.util.List;

import com.hotsteel.logic.AdvancementHelper;
import com.hotsteel.registry.ModEntities;
import com.hotsteel.registry.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
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
 * <p>The fight is built as three genuinely different creatures rather than one
 * creature with three numbers:
 *
 * <table>
 *   <tr><th>Phase</th><th>Form</th><th>Shape</th><th>How it fights</th></tr>
 *   <tr><td>1 · 熔铸之怒</td><td>armoured colossus</td><td>1.6 × 3.4</td>
 *       <td>closes in, charges, ground-slams, calls Slag Crawlers</td></tr>
 *   <tr><td>2 · 烈焰喷发</td><td>the Ascendant — wider, four arms, wings, halo</td>
 *       <td>1.9 × 3.2</td><td>keeps its distance, fireball fans, fire nova,
 *           flies (no gravity), calls Ember Wisps</td></tr>
 *   <tr><td>3 · 岩浆狂潮</td><td>the Molten Horror — hunched, huge claws, exposed heart</td>
 *       <td>2.1 × 2.6</td><td>faster and harder, sweeping claw combos, meteor
 *           fireballs from above, floods the ground with lava</td></tr>
 * </table>
 *
 * <p>Each form has its own model, its own texture, its own animation set (see
 * {@code AncientForgebornRenderer}) and its own hitbox, and the change itself is
 * a staged transformation: invulnerable rise, expanding shockwave, then the new
 * shape settles in.
 */
public class AncientForgebornEntity extends Monster {

    /** Fraction of health at which phase 2 / phase 3 begins. */
    private static final double PHASE_2_THRESHOLD = 0.70;
    private static final double PHASE_3_THRESHOLD = 0.35;
    /** Length of the transformation sequence. */
    private static final int TRANSITION_TICKS = 80;
    private static final int MAX_MINIONS = 8;

    private static final EntityDataAccessor<Integer> DATA_PHASE =
        SynchedEntityData.defineId(AncientForgebornEntity.class, EntityDataSerializers.INT);
    /** Non-zero while the boss is transforming; synced so the model can brace. */
    private static final EntityDataAccessor<Integer> DATA_TRANSITION =
        SynchedEntityData.defineId(AncientForgebornEntity.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossEvent;
    private int summonCooldown = 80;
    private int barrageCooldown = 100;
    private int lavaCooldown = 120;
    private int dashCooldown = 100;
    private int novaCooldown = 160;
    private int meteorCooldown = 200;
    private int sweepCooldown = 0;
    private int pendingPhase = 0;
    private double dashDirX;
    private double dashDirZ;
    private int dashTicks = 0;

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
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PHASE, 1);
        builder.define(DATA_TRANSITION, 0);
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

    // ---- Form / phase state ----------------------------------------------

    /** 1 = 熔铸之怒, 2 = 烈焰喷发, 3 = 岩浆狂潮. */
    public int getPhase() {
        return this.entityData.get(DATA_PHASE);
    }

    /** Ticks left in the current transformation (0 when not transforming). */
    public int getTransitionTicks() {
        return this.entityData.get(DATA_TRANSITION);
    }

    public boolean isTransforming() {
        return getTransitionTicks() > 0;
    }

    /**
     * Each form has its own real hitbox: the Ascendant is wider and shorter
     * (it hovers), the Horror is lower and broader (it hunches). The renderer
     * derives its scale from the same numbers.
     */
    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        return switch (getPhase()) {
            case 2 -> EntityDimensions.scalable(1.9f, 3.2f);
            case 3 -> EntityDimensions.scalable(2.1f, 2.6f);
            default -> EntityDimensions.scalable(1.6f, 3.4f);
        };
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
        return effect.getEffect() != MobEffects.WITHER && super.canBeAffected(effect);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return isTransforming() || super.isInvulnerableTo(source);
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    // ---- Persistence ------------------------------------------------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("HotSteelPhase", getPhase());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        int phase = tag.contains("HotSteelPhase") ? tag.getInt("HotSteelPhase") : 1;
        setPhase(phase);
        applyPhaseAttributes(phase);
    }

    // ---- Phase logic ------------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            // Client-side flavour: each form breathes its own particles.
            clientFormParticles();
            return;
        }
        if (this.bossEvent != null) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        }

        if (isTransforming()) {
            tickTransition();
            return;
        }

        float fraction = this.getHealth() / this.getMaxHealth();
        if (fraction <= PHASE_3_THRESHOLD && getPhase() < 3) {
            beginTransition(3);
            return;
        }
        if (fraction <= PHASE_2_THRESHOLD && getPhase() < 2) {
            beginTransition(2);
            return;
        }

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        switch (getPhase()) {
            case 2 -> tickAscendant(target);
            case 3 -> tickHorror(target);
            default -> tickColossus(target);
        }
    }

    /** Phase 1 — 熔铸之怒: bruiser. Charges, slams, summons crawlers. */
    private void tickColossus(LivingEntity target) {
        if (--this.summonCooldown <= 0) {
            this.summonCooldown = 200;
            summonMinions(target, false);
        }
        if (this.dashTicks > 0) {
            tickDash(target);
            return;
        }
        if (--this.dashCooldown <= 0 && this.distanceToSqr(target) < 200.0) {
            startDash(target);
        }
    }

    /** Phase 2 — 烈焰喷发: artillery. Keeps range, fans fireballs, novas, hovers. */
    private void tickAscendant(LivingEntity target) {
        double distance = Math.sqrt(this.distanceToSqr(target));
        // Artillery behaviour: back off when the player closes in.
        if (distance < 6.0) {
            Vec3 away = this.position().subtract(target.position()).normalize().scale(0.24);
            this.setDeltaMovement(this.getDeltaMovement().add(away.x, 0.02, away.z));
            this.hurtMarked = true;
        }
        if (--this.summonCooldown <= 0) {
            this.summonCooldown = 220;
            summonMinions(target, true);
        }
        if (--this.barrageCooldown <= 0) {
            this.barrageCooldown = 70;
            fireFan(target);
        }
        if (--this.novaCooldown <= 0) {
            this.novaCooldown = 200;
            fireNova();
        }
    }

    /** Phase 3 — 岩浆狂潮: berserker. Combo swipes, meteors, lava, minions. */
    private void tickHorror(LivingEntity target) {
        if (--this.summonCooldown <= 0) {
            this.summonCooldown = 180;
            summonMinions(target, true);
        }
        if (--this.sweepCooldown <= 0 && this.distanceToSqr(target) < 36.0) {
            this.sweepCooldown = 90;
            clawSweep(target);
        }
        if (--this.meteorCooldown <= 0) {
            this.meteorCooldown = 160;
            meteorRain(target);
        }
        if (--this.lavaCooldown <= 0) {
            this.lavaCooldown = 110;
            floodLava(target);
        }
    }

    /** Client-side: each form gets a distinct idle signature so they read differently. */
    private void clientFormParticles() {
        if (this.random.nextInt(getPhase() >= 3 ? 2 : 4) != 0) {
            return;
        }
        double y = this.getY() + this.getBbHeight() * (getPhase() == 3 ? 0.35 : 0.7);
        switch (getPhase()) {
            case 2 -> this.level().addParticle(ParticleTypes.FLAME,
                this.getRandomX(0.9), y, this.getRandomZ(0.9),
                (this.random.nextDouble() - 0.5) * 0.02, 0.03, (this.random.nextDouble() - 0.5) * 0.02);
            case 3 -> this.level().addParticle(ParticleTypes.LAVA,
                this.getRandomX(1.1), this.getY() + this.getBbHeight() * 0.3, this.getRandomZ(1.1),
                0.0, 0.0, 0.0);
            default -> this.level().addParticle(ParticleTypes.SMOKE,
                this.getRandomX(0.7), y, this.getRandomZ(0.7), 0.0, 0.01, 0.0);
        }
    }

    // ---- Transformation ---------------------------------------------------

    private void beginTransition(int newPhase) {
        this.pendingPhase = newPhase;
        this.entityData.set(DATA_TRANSITION, TRANSITION_TICKS);
        this.setDeltaMovement(Vec3.ZERO);
        this.getNavigation().stop();
        this.removeAllEffects();
        this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, TRANSITION_TICKS, 5, false, false));
        if (this.bossEvent != null) {
            this.bossEvent.setColor(newPhase >= 3 ? BossEvent.BossBarColor.RED
                : BossEvent.BossBarColor.YELLOW);
            this.bossEvent.setName(Component.translatable(
                newPhase >= 3 ? "entity.hotsteel.ancient_forgeborn.phase3"
                    : "entity.hotsteel.ancient_forgeborn.phase2"));
        }
        if (this.level() instanceof ServerLevel server) {
            server.playSound(null, this.blockPosition(), SoundEvents.WITHER_SPAWN,
                SoundSource.HOSTILE, 1.6f, 0.55f + newPhase * 0.15f);
        }
    }

    /** The staged transformation: rise -> roar -> shockwave -> new shape settles in. */
    private void tickTransition() {
        int left = this.entityData.get(DATA_TRANSITION) - 1;
        this.entityData.set(DATA_TRANSITION, left);
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        int elapsed = TRANSITION_TICKS - left;
        double cx = this.getX();
        double cz = this.getZ();
        double baseY = this.getY();

        // Stage 1 (0-30): a rising vortex of flame while the old shell burns off.
        if (elapsed <= 30) {
            double angle = elapsed * 0.55;
            double radius = 1.2 + elapsed * 0.06;
            for (int arm = 0; arm < 4; arm++) {
                double a = angle + arm * (Math.PI / 2.0);
                server.sendParticles(ParticleTypes.FLAME,
                    cx + Math.cos(a) * radius, baseY + elapsed * 0.07, cz + Math.sin(a) * radius,
                    3, 0.05, 0.05, 0.05, 0.02);
            }
            server.sendParticles(ParticleTypes.LARGE_SMOKE,
                cx, baseY + this.getBbHeight() * 0.5, cz, 4, 0.8, 1.0, 0.8, 0.02);
        }
        // Stage 2 (30-55): the shell cracks; ash and embers burst outwards.
        if (elapsed > 30 && elapsed <= 55) {
            server.sendParticles(ParticleTypes.LAVA,
                cx, baseY + this.getBbHeight() * 0.5, cz, 14, 1.1, 1.2, 1.1, 0.15);
            server.sendParticles(ParticleTypes.ASH,
                cx, baseY + this.getBbHeight() * 0.4, cz, 12, 1.4, 1.0, 1.4, 0.05);
        }
        // Stage 3 (55-70): the shockwave — shoved back, and the bar flares.
        if (elapsed == 56) {
            server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, cx, baseY + 0.4, cz, 3, 1.0, 0.2, 1.0, 0.0);
            server.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.HOSTILE, 1.6f, 0.6f);
            AABB area = this.getBoundingBox().inflate(9.0);
            for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, area)) {
                if (victim == this || victim.isAlliedTo(this)) {
                    continue;
                }
                Vec3 push = victim.position().subtract(this.position());
                Vec3 flat = new Vec3(push.x, 0.0, push.z);
                if (flat.lengthSqr() > 1.0E-4) {
                    flat = flat.normalize().scale(1.5);
                    victim.push(flat.x, 0.6, flat.z);
                    victim.hurtMarked = true;
                }
                if (!victim.fireImmune()) {
                    victim.setRemainingFireTicks(Math.max(victim.getRemainingFireTicks(), 100));
                }
            }
        }
        // Stage 4 (70-80): the new form settles.
        if (elapsed > 70) {
            server.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                cx, baseY + 0.2, cz, 6, 0.7, 0.1, 0.7, 0.01);
        }

        if (left == 0) {
            setPhase(this.pendingPhase);
            applyPhaseAttributes(this.pendingPhase);
            // The new form announces itself.
            if (this.bossEvent != null) {
                this.bossEvent.setColor(this.pendingPhase >= 3
                    ? BossEvent.BossBarColor.RED : BossEvent.BossBarColor.YELLOW);
            }
            server.playSound(null, this.blockPosition(),
                SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.8f, 0.5f);
            if (this.pendingPhase >= 2) {
                summonMinions(this.getTarget(), true);
            }
        }
    }

    private void setPhase(int phase) {
        this.entityData.set(DATA_PHASE, Math.max(1, Math.min(3, phase)));
        // The hitbox genuinely changes: re-fit the entity into the world.
        this.refreshDimensions();
    }

    private void applyPhaseAttributes(int phase) {
        if (phase >= 3) {
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.40);
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(19.0);
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0);
        } else if (phase >= 2) {
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.30);
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(16.0);
            this.getAttribute(Attributes.ARMOR).setBaseValue(13.0);
        }
    }

    // ---- Phase 1 actions --------------------------------------------------

    private void startDash(LivingEntity target) {
        Vec3 dir = target.position().subtract(this.position());
        Vec3 flat = new Vec3(dir.x, 0.0, dir.z);
        if (flat.lengthSqr() < 1.0E-4) {
            return;
        }
        flat = flat.normalize();
        this.dashDirX = flat.x;
        this.dashDirZ = flat.z;
        this.dashTicks = 22;
        this.dashCooldown = 140;
        if (this.level() instanceof ServerLevel server) {
            server.playSound(null, this.blockPosition(), SoundEvents.RAVAGER_ROAR,
                SoundSource.HOSTILE, 1.4f, 0.7f);
        }
    }

    private void tickDash(LivingEntity target) {
        this.dashTicks--;
        this.setDeltaMovement(this.dashDirX * 0.72, this.getDeltaMovement().y, this.dashDirZ * 0.72);
        this.hurtMarked = true;
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.CLOUD,
                this.getX(), this.getY() + 0.2, this.getZ(), 4, 0.4, 0.1, 0.4, 0.02);
        }
        // Impact: anything it ploughs into is launched.
        AABB area = this.getBoundingBox().inflate(1.3);
        for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class, area)) {
            if (victim == this || victim.isAlliedTo(this)) {
                continue;
            }
            victim.hurt(this.damageSources().mobAttack(this), 9.0f);
            victim.push(this.dashDirX * 1.6, 0.5, this.dashDirZ * 1.6);
            victim.hurtMarked = true;
            this.dashTicks = 0;
            groundSlam();
            return;
        }
        if (this.dashTicks == 0) {
            groundSlam();
        }
    }

    /** Phase 1's finisher: a ring of broken ground that bruises everything nearby. */
    private void groundSlam() {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        double cx = this.getX();
        double cy = this.getY() + 0.2;
        double cz = this.getZ();
        for (int i = 0; i < 24; i++) {
            double a = i * (Math.PI / 12.0);
            server.sendParticles(ParticleTypes.CLOUD,
                cx + Math.cos(a) * 2.6, cy, cz + Math.sin(a) * 2.6, 2, 0.1, 0.05, 0.1, 0.01);
            server.sendParticles(ParticleTypes.LAVA,
                cx + Math.cos(a) * 3.1, cy, cz + Math.sin(a) * 3.1, 1, 0.0, 0.0, 0.0, 0.0);
        }
        server.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(),
            SoundSource.HOSTILE, 1.2f, 0.8f);
        AABB area = this.getBoundingBox().inflate(4.0);
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, area)) {
            if (victim == this || victim.isAlliedTo(this)) {
                continue;
            }
            victim.hurt(this.damageSources().mobAttack(this), 7.0f);
            Vec3 push = victim.position().subtract(this.position());
            Vec3 flat = new Vec3(push.x, 0.0, push.z);
            if (flat.lengthSqr() > 1.0E-4) {
                flat = flat.normalize().scale(0.8);
                victim.push(flat.x, 0.5, flat.z);
                victim.hurtMarked = true;
            }
        }
    }

    // ---- Phase 2 actions --------------------------------------------------

    /** Fires a five-shot fan of small fireballs at the target. */
    private void fireFan(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        Vec3 origin = new Vec3(this.getX(), this.getY() + this.getBbHeight() * 0.8, this.getZ());
        Vec3 aim = new Vec3(target.getX(), target.getEyeY(), target.getZ()).subtract(origin);
        Vec3 base = aim.normalize();
        Vec3 muzzle = origin.add(base.scale(1.5));
        double spread = 0.18;
        for (int i = -2; i <= 2; i++) {
            Vec3 offset = new Vec3(base.z, 0.0, -base.x).normalize().scale(spread * i);
            Vec3 dir = base.add(offset).normalize().scale(1.25);
            SmallFireball fireball = new SmallFireball(server, this, dir);
            fireball.setPos(muzzle.x, muzzle.y, muzzle.z);
            server.addFreshEntity(fireball);
        }
        server.playSound(null, this.blockPosition(), SoundEvents.BLAZE_SHOOT,
            SoundSource.HOSTILE, 1.4f, 0.8f);
    }

    /** Phase 2's panic button: a ring of flame that pushes players out of melee range. */
    private void fireNova() {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        double cx = this.getX();
        double cy = this.getY() + this.getBbHeight() * 0.5;
        double cz = this.getZ();
        for (int ring = 0; ring < 3; ring++) {
            double radius = 1.5 + ring * 1.3;
            for (int i = 0; i < 20; i++) {
                double a = i * (Math.PI / 10.0);
                server.sendParticles(ParticleTypes.FLAME,
                    cx + Math.cos(a) * radius, cy, cz + Math.sin(a) * radius,
                    2, 0.05, 0.15, 0.05, 0.01);
            }
        }
        server.playSound(null, this.blockPosition(), SoundEvents.BLAZE_SHOOT,
            SoundSource.HOSTILE, 1.5f, 0.6f);
        AABB area = this.getBoundingBox().inflate(6.0);
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, area)) {
            if (victim == this || victim.isAlliedTo(this)) {
                continue;
            }
            victim.hurt(this.damageSources().mobAttack(this), 6.0f);
            if (!victim.fireImmune()) {
                victim.setRemainingFireTicks(Math.max(victim.getRemainingFireTicks(), 120));
            }
            Vec3 push = victim.position().subtract(this.position());
            Vec3 flat = new Vec3(push.x, 0.0, push.z);
            if (flat.lengthSqr() > 1.0E-4) {
                flat = flat.normalize().scale(1.1);
                victim.push(flat.x, 0.45, flat.z);
                victim.hurtMarked = true;
            }
        }
    }

    // ---- Phase 3 actions --------------------------------------------------

    /** A double claw sweep: two damage windows with a short gap, hitting all around. */
    private void clawSweep(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        server.playSound(null, this.blockPosition(), SoundEvents.RAVAGER_ATTACK,
            SoundSource.HOSTILE, 1.5f, 0.6f);
        double cx = this.getX();
        double cy = this.getY() + this.getBbHeight() * 0.5;
        double cz = this.getZ();
        Vec3 facing = target.position().subtract(this.position()).normalize();
        double baseAngle = Math.atan2(facing.z, facing.x);
        for (int step = 0; step < 12; step++) {
            double a = baseAngle - 1.2 + step * 0.2;
            server.sendParticles(ParticleTypes.SWEEP_ATTACK,
                cx + Math.cos(a) * 2.6, cy, cz + Math.sin(a) * 2.6, 1, 0.0, 0.0, 0.0, 0.0);
            server.sendParticles(ParticleTypes.FLAME,
                cx + Math.cos(a) * 3.0, cy - 0.4, cz + Math.sin(a) * 3.0, 1, 0.05, 0.05, 0.05, 0.01);
        }
        AABB area = this.getBoundingBox().inflate(3.6);
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, area)) {
            if (victim == this || victim.isAlliedTo(this)) {
                continue;
            }
            victim.hurt(this.damageSources().mobAttack(this), 10.0f);
            if (!victim.fireImmune()) {
                victim.setRemainingFireTicks(Math.max(victim.getRemainingFireTicks(), 140));
            }
            Vec3 push = victim.position().subtract(this.position());
            Vec3 flat = new Vec3(push.x, 0.0, push.z);
            if (flat.lengthSqr() > 1.0E-4) {
                flat = flat.normalize().scale(0.9);
                victim.push(flat.x, 0.7, flat.z);
                victim.hurtMarked = true;
            }
        }
    }

    /** Phase 3: fireballs rain down on the target from above. */
    private void meteorRain(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        for (int i = 0; i < 4; i++) {
            double ox = (this.random.nextDouble() - 0.5) * 7.0;
            double oz = (this.random.nextDouble() - 0.5) * 7.0;
            double sx = target.getX() + ox;
            double sy = target.getY() + 12.0 + this.random.nextDouble() * 4.0;
            double sz = target.getZ() + oz;
            Vec3 dir = new Vec3(target.getX() - sx, target.getY() + 0.5 - sy, target.getZ() - sz)
                .normalize().scale(1.1);
            SmallFireball fireball = new SmallFireball(server, this, dir);
            fireball.setPos(sx, sy, sz);
            server.addFreshEntity(fireball);
        }
        server.sendParticles(ParticleTypes.LAVA,
            target.getX(), target.getY() + 3.0, target.getZ(), 12, 2.5, 2.0, 2.5, 0.0);
        server.playSound(null, this.blockPosition(), SoundEvents.BLAZE_SHOOT,
            SoundSource.HOSTILE, 1.3f, 1.1f);
    }

    /** Phase 3 only: opens lava pools around the target. */
    private void floodLava(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        BlockPos around = target.blockPosition();
        int placed = 0;
        for (int attempt = 0; attempt < 12 && placed < 5; attempt++) {
            int dx = this.random.nextInt(7) - 3;
            int dz = this.random.nextInt(7) - 3;
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

    // ---- Minions ----------------------------------------------------------

    private void summonMinions(LivingEntity target, boolean mixed) {
        if (!(this.level() instanceof ServerLevel server) || target == null) {
            return;
        }
        AABB area = this.getBoundingBox().inflate(24.0);
        List<Monster> nearby = server.getEntitiesOfClass(Monster.class, area,
            mob -> mob != this && (mob instanceof SlagCrawlerEntity || mob instanceof EmberWispEntity));
        if (nearby.size() >= MAX_MINIONS) {
            return;
        }
        int count = getPhase() >= 3 ? 3 : 2;
        for (int i = 0; i < count; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            double radius = 3.0 + this.random.nextDouble() * 2.0;
            double x = this.getX() + Math.cos(angle) * radius;
            double z = this.getZ() + Math.sin(angle) * radius;
            boolean wisp = mixed && this.random.nextBoolean();
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

    // ---- Death ------------------------------------------------------------

    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                this.getX(), this.getY() + this.getBbHeight() * 0.5, this.getZ(),
                4, 1.0, 1.0, 1.0, 0.0);
            // Everything it drops is fireproof, and lava is handled explicitly: the
            // heart used to be able to land in the pool the boss itself created and
            // vanish with the whole reward. Drops now always surface.
            dropSafely(server, new ItemStack(ModItems.FORGE_HEART));
            int ingots = 4 + this.random.nextInt(5); // 4-8
            dropSafely(server, new ItemStack(ModItems.MOLTEN_STEEL_INGOT, ingots));
            int cores = 1 + this.random.nextInt(3);
            dropSafely(server, new ItemStack(ModItems.MOLTEN_CORE, cores));
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

    /**
     * Spawns a drop, lifting it out of any lava first so a boss killed inside the
     * molten pool it created cannot destroy its own loot.
     */
    private void dropSafely(ServerLevel server, ItemStack stack) {
        double x = this.getX();
        double y = this.getY() + 0.5;
        double z = this.getZ();
        BlockPos pos = BlockPos.containing(x, y, z);
        if (server.getFluidState(pos).is(FluidTags.LAVA)) {
            for (int dy = 1; dy <= 12; dy++) {
                BlockPos candidate = pos.above(dy);
                if (!server.getFluidState(candidate).is(FluidTags.LAVA)) {
                    y = candidate.getY() + 0.1;
                    break;
                }
            }
        }
        ItemEntity entity = new ItemEntity(server, x, y, z, stack);
        entity.setDefaultPickUpDelay();
        entity.setInvulnerable(true);
        server.addFreshEntity(entity);
    }
}