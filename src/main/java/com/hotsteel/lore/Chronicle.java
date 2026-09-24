package com.hotsteel.lore;

import java.util.List;

import net.minecraft.world.entity.player.Player;

/**
 * 「熔炉纪元」编年史 — the static chapter table behind the Hot Steel Codex.
 * <p>
 * Every chapter is pure text: the title and body live in the language files, so
 * the story can be translated without touching code. The codex shows all
 * chapters; in-world progression is carried by the advancement tree and by
 * reading {@code scorched_page} fragments.
 */
public final class Chronicle {

    private Chronicle() {}

    /** A single chapter of the chronicle. */
    public record Chapter(String path, String titleKey, String bodyKey) {}

    public static final String[] CHAPTER_PATHS = {
        "chapter_1", "chapter_2", "chapter_3", "chapter_4", "chapter_5"
    };

    public static final List<Chapter> CHAPTERS = List.of(
        chapter("chapter_1"),
        chapter("chapter_2"),
        chapter("chapter_3"),
        chapter("chapter_4"),
        chapter("chapter_5"));

    private static Chapter chapter(String path) {
        return new Chapter(path,
            "hotsteel.chronicle." + path + ".title",
            "hotsteel.chronicle." + path + ".body");
    }

    /**
     * Opens the codex viewer. Only ever called from the client (guarded by
     * {@code level.isClientSide} at the call site), so the client-only screen
     * class is never resolved on a dedicated server.
     */
    public static void openScreen(Player player) {
        if (!player.level().isClientSide) {
            return;
        }
        net.minecraft.client.Minecraft.getInstance().setScreen(
            new com.hotsteel.client.screen.ChronicleScreen());
    }
}
