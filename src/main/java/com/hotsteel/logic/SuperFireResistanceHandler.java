package com.hotsteel.logic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.hotsteel.registry.ModBlocks;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Drives the Hot Steel armor set bonuses and the Super Fire Resistance potion:
 * <ul>
 *   <li><b>2+ pieces — Flame Ward:</b> immune to fire damage (fire, magma, fireballs…),
 *       but lava still hurts without the full set.</li>
 *   <li><b>4 pieces + fire environment — Super Fire Resistance:</b> up to 60s of full
 *       fire/lava immunity plus water-like lava movement (Dolphin's Grace speed boost),
 *       with a HUD countdown effect. With the boots, the player can also walk ON lava.</li>
 *   <li><b>Potion of Super Fire Resistance:</b> grants the same immunity + lava movement
 *       without any armor (managed so we never strip a potion-applied effect).</li>
 * </ul>
 */
public final class SuperFireResistanceHandler {

    private SuperFireResistanceHandler() {}

    private static final int MAX_TICKS = 1200; // 60 seconds
    private static final Map<UUID, Integer> TIMER = new HashMap<>();
    private static final Set<UUID> HAD_FULL_SET = new HashSet<>();
    private static final Set<UUID> HAD_TWO_SET = new HashSet<>();
    /** Players whose SUPER_FIRE_RESISTANCE effect was applied by us (armor). */
    private static final Set<UUID> MANAGED = new HashSet<>();

    /** Client-safe check: driven by the auto-synced marker effect. */
    public static boolean isActive(LivingEntity entity) {
        return entity.hasEffect(ModEffects.SUPER_FIRE_RESISTANCE);
    }

    /**
     * Returns true if any part of the entity's bounding box intersects a lava block.
     * Checks the whole bounding box (not just eye level) so the effect and speed
     * boost stay stable across bobbing at the lava surface.
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

    /** Top Y (block coordinate) of the highest lava block inside the entity's AABB, or null if none. */
    private static Integer lavaSurfaceY(LivingEntity entity) {
        AABB box = entity.getBoundingBox();
        Level level = entity.level();
        int top = Integer.MIN_VALUE;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = Mth.floor(box.minY); y <= Mth.floor(box.maxY); y++) {
            for (int x = Mth.floor(box.minX); x <= Mth.floor(box.maxX); x++) {
                for (int z = Mth.floor(box.minZ); z <= Mth.floor(box.maxZ); z++) {
                    if (level.getFluidState(pos.set(x, y, z)).is(FluidTags.LAVA)) {
                        top = Math.max(top, y);
                    }
                }
            }
        }
        return top == Integer.MIN_VALUE ? null : top;
    }

    public static void register() {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                tick(player);
            }
        });

        // Cancel fire/lava damage:
        //  - 2+ pieces: immune to all fire-tag damage EXCEPT direct lava (keeps the 4-piece meaningful).
        //  - full set (timer active) OR having the Super Fire Resistance effect (armor or potion):
        //    full immunity including lava.
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayer player && source.is(DamageTypeTags.IS_FIRE)) {
                // Blocking with a Hot Steel shield makes you fully fireproof and
                // instantly puts out any fire on you.
                if (player.isBlocking()
                    && player.getUseItem().is(ModItems.HOT_STEEL_SHIELD)) {
                    player.clearFire();
                    return false;
                }
                int pieces = countPieces(player);
                boolean hasSuperEffect = player.hasEffect(ModEffects.SUPER_FIRE_RESISTANCE);
                boolean fullSuper = hasSuperEffect
                    || (pieces >= 4 && TIMER.getOrDefault(player.getUUID(), 0) < MAX_TICKS);
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

        // Armor-driven Super Fire Resistance timer.
        boolean inLava = isBodyTouchingLava(player);
        boolean inFire = inLava || player.isOnFire();
        if (fullSet && inFire) {
            int elapsed = TIMER.getOrDefault(id, 0) + 1;
            TIMER.put(id, elapsed);
            if (elapsed <= MAX_TICKS) {
                player.clearFire();
                MANAGED.add(id);
                int remaining = MAX_TICKS - elapsed + 1;
                MobEffectInstance existing = player.getEffect(ModEffects.SUPER_FIRE_RESISTANCE);
                // Don't overwrite a strong potion with the weaker armor-level effect.
                boolean keepStrong = existing != null && existing.getAmplifier() >= 1;
                if (!keepStrong) {
                    player.addEffect(new MobEffectInstance(ModEffects.SUPER_FIRE_RESISTANCE,
                        remaining, 0, true, false, true));
                }
            } else {
                MANAGED.remove(id);
                player.removeEffect(ModEffects.SUPER_FIRE_RESISTANCE);
            }
        } else {
            TIMER.remove(id);
            if (MANAGED.remove(id)) {
                if (player.hasEffect(ModEffects.SUPER_FIRE_RESISTANCE)) {
                    player.removeEffect(ModEffects.SUPER_FIRE_RESISTANCE);
                }
            }
        }

        // Lava speed boost is driven by the actual effect (armor OR potion):
        // Dolphin's Grace amplifier = 1 + effect level, so the Strong potion is faster.
        MobEffectInstance superEffect = player.getEffect(ModEffects.SUPER_FIRE_RESISTANCE);
        if (superEffect != null && inLava) {
            int amp = superEffect.getAmplifier();
            player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE,
                superEffect.getDuration(), 1 + amp, false, false, false));
        } else if (superEffect == null) {
            player.removeEffect(MobEffects.DOLPHINS_GRACE);
        }

        // Lava Walker: full set + boots lets the player walk ON the lava surface.
        if (fullSet && isActive(player) && inLava
            && player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.HOT_STEEL_BOOTS)) {
            Integer top = lavaSurfaceY(player);
            if (top != null) {
                double surface = top + 1.0;
                boolean headAboveSurface = player.getY() + player.getBbHeight() - 0.3 > surface;
                if (headAboveSurface) {
                    // stop sinking; stay on top of the lava
                    if (player.getDeltaMovement().y < 0) {
                        player.setDeltaMovement(player.getDeltaMovement().x, 0.0, player.getDeltaMovement().z);
                    }
                    if (player.getY() <= surface) {
                        player.setOnGround(true);
                    }
                }
            }
        }

        // Molten Aura: with the full set in lava, nearby mobs are scorched.
        if (fullSet && isActive(player) && inLava) {
            AABB aura = player.getBoundingBox().inflate(3.0);
            for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, aura)) {
                if (target != player && !target.fireImmune()) {
                    target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), 40));
                }
            }
        }

        // Auto-Repair: with the full set in lava, worn Hot Steel gear repairs itself
        // (1 durability per 4 ticks).
        if (fullSet && inLava) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = player.getItemBySlot(slot);
                if (isHotSteelGear(stack) && stack.isDamaged()
                    && player.tickCount % 4 == 0) {
                    stack.setDamageValue(stack.getDamageValue() - 1);
                }
            }
        }

        // Warm Hearth: standing near a Block of Hot Steel grants passive Fire
        // Resistance (refreshed every tick while close, fades 3s after leaving).
        if (nearHotSteelBlock(player)) {
            if (!player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,
                    60, 0, false, false, false));
            }
        }
    }

    /** True if the player is within a couple of blocks of a Block of Hot Steel. */
    private static boolean nearHotSteelBlock(Player player) {
        Level level = player.level();
        BlockPos center = player.blockPosition();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (level.getBlockState(
                        pos.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz))
                        .is(ModBlocks.HOT_STEEL_BLOCK)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** Hot Steel gear that self-repairs while the full set is worn in lava. */
    private static final java.util.Set<net.minecraft.world.item.Item> HOT_STEEL_GEAR =
        java.util.Set.of(
            ModItems.HOT_STEEL_HELMET, ModItems.HOT_STEEL_CHESTPLATE,
            ModItems.HOT_STEEL_LEGGINGS, ModItems.HOT_STEEL_BOOTS,
            ModItems.HOT_STEEL_SWORD, ModItems.HOT_STEEL_MACE, ModItems.HOT_STEEL_KNIFE,
            ModItems.HOT_STEEL_SICKLE, ModItems.HOT_STEEL_PICKAXE, ModItems.HOT_STEEL_AXE,
            ModItems.HOT_STEEL_SHOVEL, ModItems.HOT_STEEL_HOE, ModItems.HOT_STEEL_BOW,
            ModItems.HOT_STEEL_CROSSBOW, ModItems.HOT_STEEL_TRIDENT, ModItems.HOT_STEEL_SHIELD);

    /** True if the stack is one of the mod's repairable Hot Steel items. */
    private static boolean isHotSteelGear(ItemStack stack) {
        return HOT_STEEL_GEAR.contains(stack.getItem());
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
