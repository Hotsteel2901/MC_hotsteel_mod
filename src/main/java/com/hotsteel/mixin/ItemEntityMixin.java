package com.hotsteel.mixin;

import com.hotsteel.registry.ModItems;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseFireBlock;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Turns a dropped Steel Ingot into a Hot Steel Ingot by heating it:
 * in lava it takes ~5s (100 ticks); in fire it takes twice as long (~10s / 200 ticks).
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    @Unique
    private static final int HEAT_THRESHOLD = 200;

    @Unique
    private int hotsteel$heatProgress = 0;

    @Inject(method = "tick", at = @At("TAIL"))
    private void hotsteel$heatSteel(CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (self.level().isClientSide()) {
            return;
        }
        ItemStack stack = self.getItem();
        if (!stack.is(ModItems.STEEL_INGOT)) {
            hotsteel$heatProgress = 0;
            return;
        }

        int add = 0;
        if (self.isInLava()) {
            add = 2;                              // lava: full speed -> 100 ticks
        } else if (hotsteel$inFire(self)) {
            add = 1;                              // fire: half speed -> 200 ticks
        }

        if (add > 0) {
            hotsteel$heatProgress += add;
            if (hotsteel$heatProgress >= HEAT_THRESHOLD) {
                hotsteel$heatProgress = 0;
                self.setItem(new ItemStack(ModItems.HOT_STEEL_INGOT, stack.getCount()));
                self.level().playSound(null, self.getX(), self.getY(), self.getZ(),
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6f, 1.4f);
                if (self.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.LAVA,
                        self.getX(), self.getY() + 0.2, self.getZ(), 10, 0.15, 0.15, 0.15, 0.0);
                }
            }
        } else {
            hotsteel$heatProgress = 0;
        }
    }

    @Unique
    private boolean hotsteel$inFire(ItemEntity self) {
        return self.level().getBlockState(self.blockPosition()).getBlock() instanceof BaseFireBlock;
    }
}
