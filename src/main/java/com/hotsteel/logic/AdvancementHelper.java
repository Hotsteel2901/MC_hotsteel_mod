package com.hotsteel.logic;

import com.hotsteel.HotSteel;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/** Small helper for awarding mod advancements from server-side logic. */
public final class AdvancementHelper {

    private AdvancementHelper() {}

    /**
     * Awards the advancement at {@code data/hotsteel/advancement/<path>.json} using
     * the given criterion (the JSON must define an "impossible" trigger with that name).
     */
    public static void award(ServerPlayer player, String path, String criterion) {
        MinecraftServer server = player.serverLevel().getServer();
        AdvancementHolder holder = server.getAdvancements().get(HotSteel.id(path));
        if (holder != null) {
            player.getAdvancements().award(holder, criterion);
        }
    }
}
