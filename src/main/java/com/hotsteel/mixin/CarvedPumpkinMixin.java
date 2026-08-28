package com.hotsteel.mixin;

import com.hotsteel.entity.LavaGolemEntity;
import com.hotsteel.logic.AdvancementHelper;
import com.hotsteel.registry.ModBlocks;
import com.hotsteel.registry.ModEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lets players summon a Lava Golem exactly like an Iron Golem — build a
 * T-shape out of Hot Steel blocks and place a carved pumpkin on top. The whole
 * structure is consumed and a player-created Lava Golem appears. This makes the
 * golem fully obtainable in survival mode (no spawn egg needed).
 */
@Mixin(CarvedPumpkinBlock.class)
public abstract class CarvedPumpkinMixin {

    /**
     * Runs every time a carved pumpkin / jack o'lantern is placed. Mirrors the
     * vanilla iron-golem structure check but for Hot Steel blocks:
     * <pre>
     *   ~ ^ ~    top    (the pumpkin head being placed)
     *   # # #    middle (torso + arms)
     *   ~ # ~    bottom (legs)
     * </pre>
     */
    @Inject(method = "trySpawnGolem", at = @At("HEAD"), cancellable = true)
    private void hotsteel$trySpawnLavaGolem(Level level, BlockPos pos, CallbackInfo ci) {
        if (level.isClientSide) {
            return;
        }
        if (!isHotSteel(level, pos.below())
            || !isHotSteel(level, pos.below().east())
            || !isHotSteel(level, pos.below().west())
            || !isHotSteel(level, pos.below(2))) {
            return;
        }
        // Like vanilla, the air cells around the body must not be full of fluid.
        if (!isEmptyFluid(level, pos.below().north())
            || !isEmptyFluid(level, pos.below().south())
            || !isEmptyFluid(level, pos.below(2).east())
            || !isEmptyFluid(level, pos.below(2).west())) {
            return;
        }

        LavaGolemEntity golem = ModEntities.LAVA_GOLEM.create(level);
        if (golem == null) {
            return;
        }
        consumeStructure(level, pos);
        golem.setPlayerCreated(true);
        float yaw = Mth.wrapDegrees(level.random.nextFloat() * 360.0F);
        golem.moveTo(pos.getX() + 0.5, pos.below(2).getY() + 0.05, pos.getZ() + 0.5, yaw, 0.0F);
        level.addFreshEntity(golem);
        if (level instanceof ServerLevel serverLevel) {
            net.minecraft.world.entity.player.Player nearest = serverLevel.getNearestPlayer(golem, 12.0);
            if (nearest instanceof ServerPlayer serverPlayer) {
                AdvancementHelper.award(serverPlayer, "lava_golem", "summon_golem");
            }
        }
        ci.cancel();
    }

    @Unique
    private static boolean isHotSteel(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(ModBlocks.HOT_STEEL_BLOCK);
    }

    @Unique
    private static boolean isEmptyFluid(Level level, BlockPos pos) {
        return level.getFluidState(pos).isEmpty();
    }

    /** Remove the head + 4 body blocks with vanilla-style break effects. */
    @Unique
    private static void consumeStructure(Level level, BlockPos head) {
        BlockPos[] positions = {
            head, head.below(), head.below().east(), head.below().west(), head.below(2)
        };
        for (BlockPos p : positions) {
            BlockState state = level.getBlockState(p);
            level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
            level.globalLevelEvent(2001, p, Block.getId(state));
        }
    }
}
