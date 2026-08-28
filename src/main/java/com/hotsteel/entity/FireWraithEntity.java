package com.hotsteel.entity;

import com.hotsteel.logic.AdvancementHelper;
import com.hotsteel.registry.ModItems;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Fire Wraith — a blazing spirit haunting the Nether. It behaves like a Blaze
 * (hovers, shoots fireballs, immune to fire) but burns brighter: it is stronger,
 * drops Molten Cores and Hot Steel ingots, and its fireballs set the ground alight.
 */
public class FireWraithEntity extends Blaze {

    public FireWraithEntity(EntityType<? extends FireWraithEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide) {
            int cores = 1 + this.random.nextInt(2);
            for (int i = 0; i < cores; i++) {
                this.spawnAtLocation(new ItemStack(ModItems.MOLTEN_CORE));
            }
            if (this.random.nextInt(2) == 0) {
                this.spawnAtLocation(new ItemStack(ModItems.HOT_STEEL_INGOT));
            }
            if (source.getEntity() instanceof ServerPlayer killer
                && this.level() instanceof ServerLevel) {
                AdvancementHelper.award(killer, "fire_wraith", "kill_wraith");
            }
        }
    }
}
