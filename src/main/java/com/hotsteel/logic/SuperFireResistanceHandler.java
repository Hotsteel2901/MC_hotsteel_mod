package com.hotsteel.logic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.hotsteel.registry.ModEffects;
import com.hotsteel.registry.ModItems;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Drives the Hot Steel armor set bonuses:
 * <ul>
 *   <li><b>2+ pieces — Flame Ward:</b> immune to fire damage (fire, magma, fireballs…),
 *       but lava still hurts without the full set.</li>
 *   <li><b>4 pieces + fire environment — Super Fire Resistance:</b> up to 60s of full
 *       fire/lava immunity plus water-like lava movement (Dolphin's Grace speed boost),
 *       with a HUD countdown effect.</li>
 * </ul>
 * Also announces set bonus activation and awards the matching advancements.
 */
public final class SuperFireResistanceHandler {

    private SuperFireResistanceHandler() {}

    private static final int MAX_TICKS = 1200; // 60 seconds
    private static final Map<UUID, Integer> TIMER = new HashMap<>();
    private static final Set<UUID> HAD_FULL_SET = new HashSet<>();
    private static final Set<UUID> HAD_TWO_SET = new HashSet<>();

    /** Client-safe check: driven by the auto-synced marker effect. */
    public static boolean isActive(LivingEntity entity) {
        return entity.hasEffect(ModEffects.SUPER_FIRE_RESISTANCE);
    }

    /**
     * Returns true if any part of the entity's bounding box intersects a lava block.
     * <p>
     * Vanilla {@link LivingEntity#isInLava()} only checks the fluid at the entity's eye level,
     * which causes rapid on/off toggling when the player bobs at the lava surface. Checking the
     * whole bounding box keeps the effect and speed boost stable across that bobbing.
     */
    public static boolean isBodyTouchingLava(LivingEntity entity) {
        if (entity.isInLava()) {
            return true; // fast path: eye-level check is enough when fully submerged
        }
        AABB box = entity.getBoundingBox();
        Level level = entity.level();
        int minX = Mth.floor(box.minX);
        int minY = Mth.floor(box.minY);
        int minZ = Mth.floor(box.minZ);
        int maxX = Mth.floor(box.maxX);
        int maxY = Mth.floor(box.maxY);
        int maxZ = Mth.floor(box.maxZ);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (level.getFluidState(pos.set(x, y, z)).is(FluidTags.LAVA)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void register() {
        // Run BEFORE entity ticks so the marker effect is already present when the
        // swimming/pose mixin runs inside Player.tick().
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                tick(player);
            }
        });

        // Cancel fire/lava damage:
        //  - 2+ pieces: immune to all fire-tag damage EXCEPT direct lava (lava still burns
        //    without the full set, keeping the 4-piece super meaningful).
        //  - 4 pieces + timer active: full immunity including lava.
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayer player && source.is(DamageTypeTags.IS_FIRE)) {
                int pieces = countPieces(player);
                boolean fullSuper = pieces >= 4 && TIMER.getOrDefault(player.getUUID(), 0) < MAX_TICKS;
                boolean flameWard = pieces >= 2 && !"lava".equals(source.type().msgId());
                if (fullSuper || flameWard) {
                    return false;
                }
            }
            return true;
        });
    }

    private static void tick(ServerPlayer player) {
        UUID id = player.getUUID();
        int pieces = countPieces(player);

        // Announce the 2-piece Flame Ward becoming active / inactive.
        boolean hadTwo = HAD_TWO_SET.contains(id);
        if (pieces >= 2 && !hadTwo) {
            HAD_TWO_SET.add(id);
            player.displayClientMessage(
                Component.translatable("message.hotsteel.flame_ward_on").withStyle(ChatFormatting.GOLD),
                false);
            AdvancementHelper.award(player, "set_bonus_2", "wear_two_pieces");
        } else if (pieces < 2 && hadTwo) {
            HAD_TWO_SET.remove(id);
            player.displayClientMessage(
                Component.translatable("message.hotsteel.flame_ward_off").withStyle(ChatFormatting.RED),
                false);
        }

        // Announce the 4-piece Super Fire Resistance set becoming complete / incomplete.
        boolean fullSet = pieces >= 4;
        boolean hadFull = HAD_FULL_SET.contains(id);
        if (fullSet && !hadFull) {
            HAD_FULL_SET.add(id);
            player.displayClientMessage(
                Component.translatable("message.hotsteel.super_fire_on").withStyle(ChatFormatting.GOLD),
                false);
            AdvancementHelper.award(player, "full_armor", "wear_full_set");
        } else if (!fullSet && hadFull) {
            HAD_FULL_SET.remove(id);
            player.displayClientMessage(
                Component.translatable("message.hotsteel.super_fire_off").withStyle(ChatFormatting.RED),
                false);
        }

        // Super Fire Resistance timer + lava speed boost (full set + fire environment).
        boolean inLava = isBodyTouchingLava(player);
        boolean inFire = inLava || player.isOnFire();
        if (fullSet && inFire) {
            int elapsed = TIMER.getOrDefault(id, 0) + 1;
            TIMER.put(id, elapsed);
            if (elapsed <= MAX_TICKS) {
                player.clearFire();
                int remaining = MAX_TICKS - elapsed + 1;
                player.addEffect(new MobEffectInstance(ModEffects.SUPER_FIRE_RESISTANCE,
                    remaining, 0, true, false, true));
                // Invisible speed boost so the player moves through lava faster than water.
                if (inLava) {
                    player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE,
                        remaining, 1, false, false, false));
                }
            } else {
                player.removeEffect(ModEffects.SUPER_FIRE_RESISTANCE);
                player.removeEffect(MobEffects.DOLPHINS_GRACE);
            }
        } else {
            TIMER.remove(id);
            if (player.hasEffect(ModEffects.SUPER_FIRE_RESISTANCE)) {
                player.removeEffect(ModEffects.SUPER_FIRE_RESISTANCE);
            }
            if (player.hasEffect(MobEffects.DOLPHINS_GRACE)) {
                player.removeEffect(MobEffects.DOLPHINS_GRACE);
            }
        }
    }

    /** Number of Hot Steel armor pieces currently worn (0–4). */
    private static int countPieces(Player player) {
        int count = 0;
        if (player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.HOT_STEEL_HELMET)) count++;
        if (player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.HOT_STEEL_CHESTPLATE)) count++;
        if (player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.HOT_STEEL_LEGGINGS)) count++;
        if (player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.HOT_STEEL_BOOTS)) count++;
        return count;
    }
}
