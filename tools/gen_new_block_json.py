#!/usr/bin/env python3
"""Generate hand-written model + blockstate JSONs for hot steel forge, lantern,
smelter, door, trapdoor, fence and pressure plate. These live in
src/main/resources (not datagen-generated) so the datagen diff check stays clean."""
import json
import os

ROOT = "/workspace/src/main/resources/assets/hotsteel"
BLK = "hotsteel:block/hot_steel_block"
FORGE = "hotsteel:block/hot_steel_forge"
LANTERN = "hotsteel:block/hot_steel_lantern"
SMELTER = "hotsteel:block/hot_steel_smelter"

def write(path, data):
    full = os.path.join(ROOT, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)
    print("  wrote", full)

# ============================================================================
# HOT STEEL FORGE  (single cube, own texture)
# ============================================================================
write("models/block/hot_steel_forge.json", {
    "parent": "minecraft:block/cube_all",
    "textures": {"all": FORGE}})
write("blockstates/hot_steel_forge.json", {
    "variants": {"": {"model": "hotsteel:block/hot_steel_forge"}}})

# ============================================================================
# HOT STEEL SMELTER  (single cube, own texture)
# ============================================================================
write("models/block/hot_steel_smelter.json", {
    "parent": "minecraft:block/cube_all",
    "textures": {"all": SMELTER}})
write("blockstates/hot_steel_smelter.json", {
    "variants": {"": {"model": "hotsteel:block/hot_steel_smelter"}}})
write("models/item/hot_steel_smelter.json", {"parent": "hotsteel:block/hot_steel_smelter"})

# ============================================================================
# HOT STEEL LANTERN  (hanging + standing)
# ============================================================================
write("models/block/hot_steel_lantern.json", {
    "parent": "minecraft:block/template_lantern",
    "textures": {"lantern": LANTERN}})
write("models/block/hot_steel_lantern_hanging.json", {
    "parent": "minecraft:block/template_hanging_lantern",
    "textures": {"lantern": LANTERN}})
write("blockstates/hot_steel_lantern.json", {
    "variants": {
        "hanging=false": {"model": "hotsteel:block/hot_steel_lantern"},
        "hanging=true": {"model": "hotsteel:block/hot_steel_lantern_hanging"}}})
write("models/item/hot_steel_lantern.json", {"parent": "hotsteel:block/hot_steel_lantern"})

# ============================================================================
# HOT STEEL DOOR
# ============================================================================
T = "hotsteel:block/hot_steel_door"
write("models/block/hot_steel_door_bottom_left.json", {
    "parent": "minecraft:block/door_bottom_left", "textures": {
        "bottom": T + "_bottom", "top": T + "_top"}})
write("models/block/hot_steel_door_bottom_left_open.json", {
    "parent": "minecraft:block/door_bottom_left_open", "textures": {
        "bottom": T + "_bottom", "top": T + "_top"}})
write("models/block/hot_steel_door_bottom_right.json", {
    "parent": "minecraft:block/door_bottom_right", "textures": {
        "bottom": T + "_bottom", "top": T + "_top"}})
write("models/block/hot_steel_door_bottom_right_open.json", {
    "parent": "minecraft:block/door_bottom_right_open", "textures": {
        "bottom": T + "_bottom", "top": T + "_top"}})
write("models/block/hot_steel_door_top_left.json", {
    "parent": "minecraft:block/door_top_left", "textures": {
        "bottom": T + "_bottom", "top": T + "_top"}})
write("models/block/hot_steel_door_top_left_open.json", {
    "parent": "minecraft:block/door_top_left_open", "textures": {
        "bottom": T + "_bottom", "top": T + "_top"}})
write("models/block/hot_steel_door_top_right.json", {
    "parent": "minecraft:block/door_top_right", "textures": {
        "bottom": T + "_bottom", "top": T + "_top"}})
write("models/block/hot_steel_door_top_right_open.json", {
    "parent": "minecraft:block/door_top_right_open", "textures": {
        "bottom": T + "_bottom", "top": T + "_top"}})

def door_blockstate():
    variants = {}
    for facing in ("north", "south", "west", "east"):
        for half in ("lower", "upper"):
            for hinge in ("left", "right"):
                for open in ("false", "true"):
                    if half == "lower":
                        base = "bottom"
                    else:
                        base = "top"
                    model = f"hot_steel_door_{base}_{hinge}"
                    if open == "true":
                        model += "_open"
                    rot = {"north": 0, "south": 180, "west": 270, "east": 90}[facing]
                    variants[f"facing={facing},half={half},hinge={hinge},open={open}"] = {
                        "model": f"hotsteel:block/{model}", "y": rot}
    return {"variants": variants}

write("blockstates/hot_steel_door.json", door_blockstate())
write("models/item/hot_steel_door.json", {"parent": "minecraft:item/generated",
    "textures": {"layer0": "hotsteel:item/hot_steel_door"}})

# ============================================================================
# HOT STEEL TRAPDOOR
# ============================================================================
TT = "hotsteel:block/hot_steel_trapdoor"
write("models/block/hot_steel_trapdoor_bottom.json", {
    "parent": "minecraft:block/template_trapdoor_bottom", "textures": {"texture": TT}})
write("models/block/hot_steel_trapdoor_top.json", {
    "parent": "minecraft:block/template_trapdoor_top", "textures": {"texture": TT}})
write("models/block/hot_steel_trapdoor_open.json", {
    "parent": "minecraft:block/template_trapdoor_open", "textures": {"texture": TT}})
write("blockstates/hot_steel_trapdoor.json", {
    "variants": {
        "facing=north,half=bottom,open=false": {"model": "hotsteel:block/hot_steel_trapdoor_bottom"},
        "facing=south,half=bottom,open=false": {"model": "hotsteel:block/hot_steel_trapdoor_bottom", "y": 180},
        "facing=east,half=bottom,open=false": {"model": "hotsteel:block/hot_steel_trapdoor_bottom", "y": 90},
        "facing=west,half=bottom,open=false": {"model": "hotsteel:block/hot_steel_trapdoor_bottom", "y": 270},
        "facing=north,half=top,open=false": {"model": "hotsteel:block/hot_steel_trapdoor_top"},
        "facing=south,half=top,open=false": {"model": "hotsteel:block/hot_steel_trapdoor_top", "y": 180},
        "facing=east,half=top,open=false": {"model": "hotsteel:block/hot_steel_trapdoor_top", "y": 90},
        "facing=west,half=top,open=false": {"model": "hotsteel:block/hot_steel_trapdoor_top", "y": 270},
        "facing=north,half=bottom,open=true": {"model": "hotsteel:block/hot_steel_trapdoor_open"},
        "facing=south,half=bottom,open=true": {"model": "hotsteel:block/hot_steel_trapdoor_open", "y": 180},
        "facing=east,half=bottom,open=true": {"model": "hotsteel:block/hot_steel_trapdoor_open", "y": 90},
        "facing=west,half=bottom,open=true": {"model": "hotsteel:block/hot_steel_trapdoor_open", "y": 270},
        "facing=north,half=top,open=true": {"model": "hotsteel:block/hot_steel_trapdoor_open", "x": 180, "y": 180},
        "facing=south,half=top,open=true": {"model": "hotsteel:block/hot_steel_trapdoor_open", "x": 180, "y": 0},
        "facing=east,half=top,open=true": {"model": "hotsteel:block/hot_steel_trapdoor_open", "x": 180, "y": 270},
        "facing=west,half=top,open=true": {"model": "hotsteel:block/hot_steel_trapdoor_open", "x": 180, "y": 90}}})
write("models/item/hot_steel_trapdoor.json", {"parent": "hotsteel:block/hot_steel_trapdoor_bottom"})

# ============================================================================
# HOT STEEL FENCE
# ============================================================================
write("models/block/hot_steel_fence_post.json", {
    "parent": "minecraft:block/fence_post", "textures": {"texture": BLK}})
write("models/block/hot_steel_fence_side.json", {
    "parent": "minecraft:block/fence_side", "textures": {"texture": BLK}})
write("models/block/hot_steel_fence_inventory.json", {
    "parent": "minecraft:block/fence_inventory", "textures": {"texture": BLK}})
write("blockstates/hot_steel_fence.json", {
    "multipart": [
        {"apply": {"model": "hotsteel:block/hot_steel_fence_post"}},
        {"when": {"north": "true"}, "apply": {"model": "hotsteel:block/hot_steel_fence_side", "uvlock": True}},
        {"when": {"east": "true"}, "apply": {"model": "hotsteel:block/hot_steel_fence_side", "y": 90, "uvlock": True}},
        {"when": {"south": "true"}, "apply": {"model": "hotsteel:block/hot_steel_fence_side", "y": 180, "uvlock": True}},
        {"when": {"west": "true"}, "apply": {"model": "hotsteel:block/hot_steel_fence_side", "y": 270, "uvlock": True}}]})
write("models/item/hot_steel_fence.json", {"parent": "hotsteel:block/hot_steel_fence_inventory"})

# ============================================================================
# HOT STEEL PRESSURE PLATE
# ============================================================================
PP = "hotsteel:block/hot_steel_pressure_plate"
write("models/block/hot_steel_pressure_plate.json", {
    "parent": "minecraft:block/pressure_plate_up", "textures": {"texture": BLK}})
write("models/block/hot_steel_pressure_plate_down.json", {
    "parent": "minecraft:block/pressure_plate_down", "textures": {"texture": BLK}})
write("blockstates/hot_steel_pressure_plate.json", {
    "variants": {
        "powered=false": {"model": "hotsteel:block/hot_steel_pressure_plate"},
        "powered=true": {"model": "hotsteel:block/hot_steel_pressure_plate_down"}}})
write("models/item/hot_steel_pressure_plate.json", {
    "parent": "hotsteel:block/hot_steel_pressure_plate"})

# ============================================================================
# HOT STEEL BRICKS item model (block model generated by datagen)
# ============================================================================
write("models/item/hot_steel_bricks.json", {"parent": "hotsteel:block/hot_steel_bricks"})

# ============================================================================
# ITEM MODELS for new plain items / spawn eggs
# ============================================================================
write("models/item/hot_steel_nugget.json", {
    "parent": "minecraft:item/generated",
    "textures": {"layer0": "hotsteel:item/hot_steel_nugget"}})
write("models/item/molten_core.json", {
    "parent": "minecraft:item/generated",
    "textures": {"layer0": "hotsteel:item/molten_core"}})
write("models/item/hot_steel_fishing_rod.json", {
    "parent": "minecraft:item/handheld_rod",
    "textures": {"layer0": "hotsteel:item/hot_steel_fishing_rod"}})
write("models/item/hot_steel_sickle.json", {
    "parent": "minecraft:item/handheld",
    "textures": {"layer0": "hotsteel:item/hot_steel_sickle"}})
write("models/item/fire_wraith_spawn_egg.json", {"parent": "minecraft:item/template_spawn_egg"})

print("\nDone.")
