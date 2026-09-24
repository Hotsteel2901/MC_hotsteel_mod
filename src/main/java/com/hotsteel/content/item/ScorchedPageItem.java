package com.hotsteel.content.item;

import com.hotsteel.logic.AdvancementHelper;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 「焦痕残页」— a scorched fragment of the old forge-logs. Reading one recovers
 * the next unrecovered chapter of the chronicle, so the story can be pieced
 * together even without triggering every milestone by hand.
 */
public class ScorchedPageItem extends Item {

    public ScorchedPageItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        String next = nextLockedChapter(player);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 0.8f);
        if (next == null) {
            player.displayClientMessage(
                Component.translatable("message.hotsteel.page_already_complete"), false);
            return InteractionResultHolder.fail(stack);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            AdvancementHelper.award(serverPlayer, next, "unlocked");
            player.displayClientMessage(
                Component.translatable("message.hotsteel.page_recovered",
                    Component.translatable("hotsteel.chronicle." + next + ".title")), false);
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.success(stack);
    }

    /** Lowest-numbered chapter the player has not unlocked yet, or null if all are read. */
    private static String nextLockedChapter(Player player) {
        for (String path : ChronicleCodexItem.CHAPTER_PATHS) {
            if (!ChronicleCodexItem.hasChapter(player, path)) {
                return path;
            }
        }
        return null;
    }
}
