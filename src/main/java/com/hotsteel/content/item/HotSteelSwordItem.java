package com.hotsteel.content.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Hot Steel sword: right-click to strike a burst of fire at whatever block you
 * are looking at (costs durability, short cooldown). Melee hits already ignite
 * targets via the hot-steel melee mixin.
 */
public class HotSteelSwordItem extends SwordItem {

    private static final int COOLDOWN_TICKS = 30;
    private static final int RANGE = 6;
    private static final int DURABILITY_COST = 4;

    public HotSteelSwordItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        boolean struck = false;
        if (!level.isClientSide) {
            struck = strikeFire(level, player);
            if (struck) {
                if (!player.getAbilities().instabuild) {
                    stack.hurtAndBreak(DURABILITY_COST, player, EquipmentSlot.MAINHAND);
                }
                // Server-side only: playing it on both sides made the client hear it twice.
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.6f, 1.0f);
            }
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        }
        return InteractionResultHolder.success(stack);
    }

    /**
     * Lights a fire where the player is actually aiming.
     * <p>
     * The previous version fell back to {@code blockPosition().relative(look)} whenever the
     * ray hit nothing — i.e. it dropped the fire in the block right next to the caster's own
     * feet, which could set them alight. The fallback is now the far end of the aim ray, and
     * the caster's own two blocks are always excluded.
     */
    private boolean strikeFire(Level level, Player player) {
        Vec3 start = player.getEyePosition(1.0f);
        Vec3 end = start.add(player.getLookAngle().scale(RANGE));
        BlockHitResult hit = level.clip(new ClipContext(start, end,
            ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        BlockPos pos = hit.getType() == HitResult.Type.BLOCK
            ? hit.getBlockPos().relative(hit.getDirection())
            : BlockPos.containing(end);
        return igniteAt(level, player, pos);
    }

    /** Places a fire at {@code pos} unless that spot is inside the caster's own body. */
    public static boolean igniteAt(Level level, Player player, BlockPos pos) {
        BlockPos feet = player.blockPosition();
        if (pos.equals(feet) || pos.equals(feet.above())) {
            return false;
        }
        if (!level.getBlockState(pos).isAir()) {
            return false;
        }
        if (!BaseFireBlock.canBePlacedAt(level, pos, Direction.UP)) {
            return false;
        }
        level.setBlock(pos, BaseFireBlock.getState(level, pos), 3);
        return true;
    }
}
