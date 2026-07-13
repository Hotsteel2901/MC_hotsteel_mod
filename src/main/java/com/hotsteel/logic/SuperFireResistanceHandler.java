package com.hotsteel.logic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.hotsteel.HotSteel;
import com.hotsteel.registry.ModEffects;
import com.hotsteel.registry.ModItems;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Drives "Super Fire Resistance": full Hot Steel armor set + fire environment (lava / on fire)
 * grants up to 60s of full fire/lava immunity and water-like lava swimming. Also announces the
 * set becoming complete/incomplete and awards the full-set advancement.
 */
public final class SuperFireResistanceHandler {

    private SuperFireResistanceHandler() {}

    private static final int MAX_TICKS = 1200; // 60 seconds
    private static final Map<UUID, Integer> TIMER = new HashMap<>();
    private static final Set<UUID> HAD_FULL_SET = new HashSet<>();

    /** Client-safe check: driven by the auto-synced marker effect. */
    public static boolean isActive(LivingEntity entity) {
        return entity.hasEffect(ModEffects.SUPER_FIRE_RESISTANCE);
    }

    /**
     * Returns true if any part of the entity's bounding box intersects a lava block.
     * <p>
     * Vanilla {@link LivingEntity#isInLava()} only checks the fluid at the entity's eye level,
     * which causes rapid on/off toggling when the player bobs at the lava surface (water physics
     * lifts them up, eyes leave lava, physics reverts, player sinks, eyes re-enter lava, ...).
     * Checking the whole bounding box keeps the swim flag, the marker effect, and the water-physics
     * redirect stable across that bobbing so the player smoothly swims in lava like in water.
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
        // swimming/pose mixin runs inside Player.tick(). Avoids a 1-tick "STANDING then
        // SWIMMING" snap the instant a player touches lava.
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                tick(player);
            }
        });

        // Cancel fire/lava damage the instant it would apply — no initial burn on first contact.
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayer player
                && source.is(DamageTypeTags.IS_FIRE)
                && hasFullSet(player)
                && TIMER.getOrDefault(player.getUUID(), 0) < MAX_TICKS) {
                return false;
            }
            return true;
        });
    }

    private static void tick(ServerPlayer player) {
        UUID id = player.getUUID();
        boolean fullSet = hasFullSet(player);

        // Announce set becoming complete / incomplete.
        boolean had = HAD_FULL_SET.contains(id);
        if (fullSet && !had) {
            HAD_FULL_SET.add(id);
            player.displayClientMessage(
                Component.translatable("message.hotsteel.super_fire_on").withStyle(ChatFormatting.GOLD), false);
            awardFullArmor(player);
        } else if (!fullSet && had) {
            HAD_FULL_SET.remove(id);
            player.displayClientMessage(
                Component.translatable("message.hotsteel.super_fire_off").withStyle(ChatFormatting.RED), false);
        }

        // Fire / lava protection. Use the body-touching check (not just eye level) so the
        // effect stays granted while the player bobs at the lava surface — otherwise the
        // rapid grant/remove cycle causes the swim pose to twitch every tick.
        boolean inFire = isBodyTouchingLava(player) || player.isOnFire();
        if (fullSet && inFire) {
            int elapsed = TIMER.getOrDefault(id, 0) + 1;
            TIMER.put(id, elapsed);
            if (elapsed <= MAX_TICKS) {
                player.clearFire();
                int remaining = MAX_TICKS - elapsed + 1;
                player.addEffect(new MobEffectInstance(ModEffects.SUPER_FIRE_RESISTANCE,
                    remaining, 0, true, false, true));
            } else {
                player.removeEffect(ModEffects.SUPER_FIRE_RESISTANCE);
            }
        } else {
            TIMER.remove(id);
            if (player.hasEffect(ModEffects.SUPER_FIRE_RESISTANCE)) {
                player.removeEffect(ModEffects.SUPER_FIRE_RESISTANCE);
            }
        }
    }

    private static void awardFullArmor(ServerPlayer player) {
        MinecraftServer server = player.serverLevel().getServer();
        AdvancementHolder holder = server.getAdvancements().get(HotSteel.id("full_armor"));
        if (holder != null) {
            player.getAdvancements().award(holder, "wear_full_set");
        }
    }

    private static boolean hasFullSet(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.HOT_STEEL_HELMET)
            && player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.HOT_STEEL_CHESTPLATE)
            && player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.HOT_STEEL_LEGGINGS)
            && player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.HOT_STEEL_BOOTS);
    }
}
