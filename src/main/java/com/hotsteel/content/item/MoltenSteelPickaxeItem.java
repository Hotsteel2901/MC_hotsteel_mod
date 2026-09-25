package com.hotsteel.content.item;

import com.hotsteel.logic.AutoSmeltHelper;
import com.hotsteel.registry.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 「熔铸钢镐」— inherits the Hot Steel pickaxe's auto-smelting, and adds
 * <b>slag reclamation</b>: cracking ore occasionally yields a Molten Shard on
 * top of the smelted ingot, which is the steady supply line for Molten Cores.
 */
public class MoltenSteelPickaxeItem extends PickaxeItem {

    private static final float SHARD_CHANCE = 0.15f;

    public MoltenSteelPickaxeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos,
                             LivingEntity miner) {
        boolean broken = super.mineBlock(stack, level, state, pos, miner);
        if (!level.isClientSide
            && miner instanceof Player
            && level.random.nextFloat() < SHARD_CHANCE
            && AutoSmeltHelper.SMELT_MAP.containsKey(state.getBlock().asItem())) {
            Block.popResource(level, pos, new ItemStack(ModItems.MOLTEN_SHARD));
        }
        return broken;
    }
}
