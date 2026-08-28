#!/usr/bin/env python3
"""Generate hand-written model + blockstate JSONs for hot steel stairs/slab/wall
and item models for the new blocks/items. These live in src/main/resources
(not datagen-generated) so the datagen diff check stays clean."""
import json
import os

ROOT = "/workspace/src/main/resources/assets/hotsteel"
TEX = "hotsteel:block/hot_steel_block"

def write(path, data):
    full = os.path.join(ROOT, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)
    print("  wrote", full)

# ---- Block models ----
write("models/block/hot_steel_stairs.json", {
    "parent": "minecraft:block/stairs",
    "textures": {"bottom": TEX, "top": TEX, "side": TEX}})
write("models/block/hot_steel_stairs_inner.json", {
    "parent": "minecraft:block/inner_stairs",
    "textures": {"bottom": TEX, "top": TEX, "side": TEX}})
write("models/block/hot_steel_stairs_outer.json", {
    "parent": "minecraft:block/outer_stairs",
    "textures": {"bottom": TEX, "top": TEX, "side": TEX}})
write("models/block/hot_steel_slab.json", {
    "parent": "minecraft:block/slab",
    "textures": {"bottom": TEX, "top": TEX, "side": TEX}})
write("models/block/hot_steel_slab_top.json", {
    "parent": "minecraft:block/slab_top",
    "textures": {"bottom": TEX, "top": TEX, "side": TEX}})
write("models/block/hot_steel_wall_post.json", {
    "parent": "minecraft:block/template_wall_post",
    "textures": {"wall": TEX}})
write("models/block/hot_steel_wall_side.json", {
    "parent": "minecraft:block/template_wall_side",
    "textures": {"wall": TEX}})
write("models/block/hot_steel_wall_side_tall.json", {
    "parent": "minecraft:block/template_wall_side_tall",
    "textures": {"wall": TEX}})
write("models/block/hot_steel_wall_inventory.json", {
    "parent": "minecraft:block/wall_inventory",
    "textures": {"wall": TEX}})

# ---- Item models ----
write("models/item/hot_steel_stairs.json", {"parent": "hotsteel:block/hot_steel_stairs"})
write("models/item/hot_steel_slab.json", {"parent": "hotsteel:block/hot_steel_slab"})
write("models/item/hot_steel_wall.json", {"parent": "hotsteel:block/hot_steel_wall_inventory"})
write("models/item/hot_steel_forge.json", {"parent": "hotsteel:block/hot_steel_forge"})
write("models/item/lava_bottle.json", {
    "parent": "minecraft:item/generated",
    "textures": {"layer0": "hotsteel:item/lava_bottle"}})
write("models/item/hot_steel_golem_spawn_egg.json", {"parent": "minecraft:item/template_spawn_egg"})

# ---- Blockstates ----
def stairs_blockstate():
    variants = {}
    for facing, fr in (("north", 180), ("south", 0), ("west", 270), ("east", 90)):
        for half, hy in (("top", 180), ("bottom", 0)):
            for shape, model in (("straight", "hot_steel_stairs"),
                                 ("inner_left", "hot_steel_stairs_inner"),
                                 ("inner_right", "hot_steel_stairs_inner"),
                                 ("outer_left", "hot_steel_stairs_outer"),
                                 ("outer_right", "hot_steel_stairs_outer")):
                rot = fr
                if shape == "inner_right":
                    rot = fr + 90
                elif shape == "outer_left":
                    rot = fr + 270
                elif shape == "outer_right":
                    rot = fr
                variants[f"facing={facing},half={half},shape={shape}"] = {
                    "model": f"hotsteel:block/{model}",
                    "y": rot % 360,
                    "uvlock": True}
    return {"variants": variants}

write("blockstates/hot_steel_stairs.json", stairs_blockstate())

write("blockstates/hot_steel_slab.json", {
    "variants": {
        "type=bottom": {"model": "hotsteel:block/hot_steel_slab"},
        "type=double": {"model": "hotsteel:block/hot_steel_block"},
        "type=top": {"model": "hotsteel:block/hot_steel_slab_top"}}})

def wall_blockstate():
    return {"multipart": [{"when": {"up": "true"},
                           "apply": {"model": "hotsteel:block/hot_steel_wall_post"}},
                          {"when": {"north": "true"},
                           "apply": {"model": "hotsteel:block/hot_steel_wall_side", "uvlock": True}},
                          {"when": {"north": "true", "up": "true"},
                           "apply": {"model": "hotsteel:block/hot_steel_wall_side_tall", "uvlock": True}},
                          {"when": {"east": "true"},
                           "apply": {"model": "hotsteel:block/hot_steel_wall_side", "y": 90, "uvlock": True}},
                          {"when": {"east": "true", "up": "true"},
                           "apply": {"model": "hotsteel:block/hot_steel_wall_side_tall", "y": 90, "uvlock": True}},
                          {"when": {"south": "true"},
                           "apply": {"model": "hotsteel:block/hot_steel_wall_side", "y": 180, "uvlock": True}},
                          {"when": {"south": "true", "up": "true"},
                           "apply": {"model": "hotsteel:block/hot_steel_wall_side_tall", "y": 180, "uvlock": True}},
                          {"when": {"west": "true"},
                           "apply": {"model": "hotsteel:block/hot_steel_wall_side", "y": 270, "uvlock": True}},
                          {"when": {"west": "true", "up": "true"},
                           "apply": {"model": "hotsteel:block/hot_steel_wall_side_tall", "y": 270, "uvlock": True}}]}

write("blockstates/hot_steel_wall.json", wall_blockstate())

print("\nDone.")
