package com.hotsteel.content.item;

import com.hotsteel.HotSteel;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

/**
 * 「热钢手册」— the in-game chronicle of the Molten Age.
 * <p>
 * Right-clicking it opens the chapter viewer. On a server the player also gets a
 * quick summary in chat so the item is still useful without the GUI.
 */
public class ChronicleCodexItem extends Item {

    /** Advancement id path of each chapter, index 0 = chapter I. */
    public static final String[] CHAPTER_PATHS = {
        "chapter_1", "chapter_2", "chapter_3", "chapter_4", "chapter_5"
    };

    public ChronicleCodexItem(Properties properties) {
        super(properties.rarity(Rarity.RARE).stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            com.hotsteel.lore.Chronicle.openScreen(player);
            return InteractionResultHolder.success(stack);
        }
        int unlocked = 0;
        for (String path : CHAPTER_PATHS) {
            if (hasChapter(player, path)) {
                unlocked++;
            }
        }
        player.displayClientMessage(
            Component.translatable("message.hotsteel.codex_progress", unlocked, CHAPTER_PATHS.length),
            false);
        return InteractionResultHolder.success(stack);
    }

    /** True if the player has already been awarded the given chapter advancement. */
    public static boolean hasChapter(Player player, String path) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        MinecraftServer server = serverPlayer.serverLevel().getServer();
        AdvancementHolder holder = server.getAdvancements().get(HotSteel.id(path));
        return holder != null && serverPlayer.getAdvancements().getOrStartProgress(holder).isDone();
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
