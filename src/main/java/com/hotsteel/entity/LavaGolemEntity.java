package com.hotsteel.entity;

import com.hotsteel.logic.AdvancementHelper;
import com.hotsteel.registry.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import org.jetbrains.annotations.Nullable;

/**
 * Lava Golem — a molten guardian forged from Hot Steel. It is immune to fire and
 * lava, floats on the surface of lava, burns whatever damages it, and drops Hot
 * Steel ingots when it dies. Crafted by a player, it stays peaceful unless provoked.
 */
public class LavaGolemEntity extends IronGolem {

    public LavaGolemEntity(EntityType<? extends LavaGolemEntity> type, Level level) {
        super(type, level);
        this.setPlayerCreated(true);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 80.0)
            .add(Attributes.MOVEMENT_SPEED, 0.26)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
            .add(Attributes.ATTACK_DAMAGE, 10.0)
            .add(Attributes.ARMOR, 6.0);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnData);
        if (spawnType == MobSpawnType.SPAWN_EGG && level instanceof ServerLevel serverLevel) {
            net.minecraft.world.entity.player.Player nearest = serverLevel.getNearestPlayer(this, 12.0);
            if (nearest instanceof ServerPlayer serverPlayer) {
                AdvancementHelper.award(serverPlayer, "lava_golem", "summon_golem");
            }
        }
        return data;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide) {
            if (source.getDirectEntity() instanceof LivingEntity attacker && attacker != this) {
                attacker.setRemainingFireTicks(Math.max(attacker.getRemainingFireTicks(), 100));
            }
        }
        return hurt;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            return;
        }
        // Float on the surface of lava like a buoyant forge-heart, and slowly
        // mend itself while submerged in the molten rock.
        if (this.isInLava()) {
            if (this.tickCount % 20 == 0 && this.getHealth() < this.getMaxHealth()) {
                this.heal(1.0f);
            }
            this.setDeltaMovement(this.getDeltaMovement().x, 0.16, this.getDeltaMovement().z);
            if (this.random.nextInt(4) == 0) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.4f, 1.2f);
            }
            if (this.level() instanceof ServerLevel serverLevel) {
                BlockPos pos = this.blockPosition();
                serverLevel.sendParticles(ParticleTypes.LAVA,
                    this.getX(), this.getY() + 0.5, this.getZ(), 3, 0.4, 0.2, 0.4, 0.0);
                serverLevel.sendParticles(ParticleTypes.SMOKE,
                    this.getX(), this.getY() + this.getBbHeight(), this.getZ(), 2, 0.3, 0.1, 0.3, 0.0);
            }
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide) {
            int count = 2 + this.random.nextInt(3);
            for (int i = 0; i < count; i++) {
                this.spawnAtLocation(new ItemStack(ModItems.HOT_STEEL_INGOT));
            }
        }
    }
}
