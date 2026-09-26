#!/usr/bin/env python3
"""Generate the three-form atlas of the Ancient Forgeborn boss.

The boss changes shape with every phase, so it needs three atlases:

* ``ancient_forgeborn.png``            -- phase 1, 熔铸之怒 (the armoured colossus)
* ``ancient_forgeborn_ascendant.png``  -- phase 2, 烈焰喷发 (the ascendant)
* ``ancient_forgeborn_horror.png``     -- phase 3, 岩浆狂潮 (the molten horror)

Each form is a list of ``(name, width, height, depth)`` boxes. The script packs
them into a 128x128 sheet with a deterministic shelf packer and prints the exact
``texOffs(u, v)`` values, so the Java model and the texture can never drift
apart. Every rectangle is filled with that part's own material (plate, charred
plate, molten membrane...) plus rivets / vents / glowing veins, and glowing
parts are drawn last so they stay readable.
"""
import os

from PIL import Image

OUT = "/workspace/src/main/resources/assets/hotsteel/textures/entity"
# Sheet is 128 wide (so the wrap-around of tall parts stays predictable) and
# tall enough for the largest form with room to spare. Non-square atlases are
# perfectly normal for entity textures.
SHEET_W = 128
SHEET_H = 256

# ---- palettes (dark -> bright) -------------------------------------------
STEEL = [(0x12, 0x14, 0x18), (0x1e, 0x22, 0x27), (0x2e, 0x33, 0x3a), (0x40, 0x47, 0x50),
         (0x56, 0x5e, 0x69), (0x6f, 0x79, 0x85), (0x8b, 0x96, 0xa2), (0xa9, 0xb5, 0xc0),
         (0xc9, 0xd4, 0xdd)]
CHAR = [(0x07, 0x07, 0x08), (0x0f, 0x0f, 0x10), (0x18, 0x17, 0x17), (0x23, 0x22, 0x21),
        (0x30, 0x2e, 0x2c), (0x3f, 0x3c, 0x39), (0x50, 0x4c, 0x48), (0x66, 0x61, 0x5c),
        (0x80, 0x7a, 0x74)]
GOLD = [(0x2a, 0x14, 0x02), (0x4d, 0x26, 0x05), (0x7a, 0x3d, 0x08), (0xa8, 0x5c, 0x0c),
        (0xd4, 0x82, 0x14), (0xee, 0xa8, 0x24), (0xff, 0xc9, 0x52), (0xff, 0xe6, 0xa0),
        (0xff, 0xf6, 0xdc)]
EMBER = [(0x2a, 0x0d, 0x02), (0x58, 0x1b, 0x04), (0x8c, 0x2f, 0x08), (0xc0, 0x4a, 0x0d),
         (0xe8, 0x6d, 0x16), (0xff, 0x93, 0x27), (0xff, 0xbb, 0x52), (0xff, 0xe0, 0x93),
         (0xff, 0xf6, 0xd8)]
LAVA = [(0x1a, 0x06, 0x02), (0x45, 0x10, 0x04), (0x7d, 0x1f, 0x05), (0xb8, 0x3a, 0x08),
        (0xea, 0x5f, 0x10), (0xff, 0x8b, 0x22), (0xff, 0xbd, 0x58), (0xff, 0xe4, 0xa0),
        (0xff, 0xfb, 0xe8)]


def _lcg(seed):
    state = [seed & 0xFFFFFFFF]

    def nxt():
        state[0] = (state[0] * 1103515245 + 12345) & 0x7FFFFFFF
        return state[0] / 0x7FFFFFFF

    return nxt


def ramp(ramp_list, t):
    t = 0.0 if t < 0.0 else (1.0 if t > 1.0 else t)
    return ramp_list[int(round(t * (len(ramp_list) - 1)))]


# ===========================================================================
# Shelf packer: deterministic, top-left origin, 1px gutter.
# ===========================================================================
def pack(parts):
    boxes = []
    for name, w, h, d in parts:
        bw, bh = 2 * (w + d), h + d
        boxes.append([name, w, h, d, bw, bh, None, None])
    boxes.sort(key=lambda b: (-b[5], -b[4], b[0]))
    x = y = 0
    row_h = 0
    for b in boxes:
        if x + b[4] + 1 > SHEET_W:
            x = 0
            y += row_h + 1
            row_h = 0
        if y + b[5] > SHEET_H:
            raise SystemExit(f"atlas overflow at {b[0]} ({b[4]}x{b[5]}) y={y}")
        b[6], b[7] = x, y
        x += b[4] + 1
        row_h = max(row_h, b[5])
    return boxes


# ===========================================================================
# Part painting
# ===========================================================================
def paint(img, box, style, seed):
    name, w, h, d, bw, bh, u, v = box
    px = img.load()
    rnd = _lcg(seed)
    base, glow, glowy = style
    for yy in range(v, v + bh):
        for xx in range(u, u + bw):
            px[xx, yy] = ramp(base, 0.26 + rnd() * 0.30) + (255,)
    # dark outline so the silhouette reads from far away
    for xx in range(u, u + bw):
        px[xx, v] = ramp(base, 0.06) + (255,)
        px[xx, v + bh - 1] = ramp(base, 0.06) + (255,)
    for yy in range(v, v + bh):
        px[u, yy] = ramp(base, 0.10) + (255,)
        px[u + bw - 1, yy] = ramp(base, 0.10) + (255,)
    # riveted plate grid
    for rx in range(u + 3, u + bw - 3, 9):
        for ry in range(v + 3, v + bh - 3, 9):
            px[rx, ry] = ramp(base, 0.92) + (255,)
            px[rx + 1, ry + 1] = ramp(base, 0.14) + (255,)
    if glowy:
        # a horizontal band of glow across the plate, plus vents
        gy = v + bh // 2
        for xx in range(u + 2, u + bw - 2):
            px[xx, gy] = ramp(glow, 0.62) + (255,)
            px[xx, gy + 1] = ramp(glow, 0.42) + (255,)
        for gx in range(u + 4, u + bw - 5, 7):
            px[gx, gy - 2] = ramp(glow, 0.88) + (255,)
            px[gx + 1, gy - 2] = ramp(glow, 0.70) + (255,)
        # a couple of white-hot nodes
        for _ in range(3):
            px[u + 2 + int(rnd() * (bw - 4)), v + 2 + int(rnd() * (bh - 4))] = ramp(glow, 0.99) + (255,)
    else:
        for _ in range(4):
            px[u + 2 + int(rnd() * (bw - 4)), v + 2 + int(rnd() * (bh - 4))] = ramp(glow, 0.70) + (255,)


# ===========================================================================
# Form 2 -- 烈焰喷发 / the Ascendant.
# Wider, floating, four arms, a crown of horns, wings and a halo ring.
# ===========================================================================
ASCENDANT = [
    ("skirt", 18, 12, 14),
    ("waist", 12, 6, 10),
    ("torso", 26, 22, 14),
    ("core", 12, 12, 3),
    ("head", 14, 9, 14),
    ("crown", 4, 7, 4),
    ("eye", 5, 3, 1),
    ("pauldron", 9, 7, 9),
    ("upper_arm", 6, 14, 6),
    ("forearm", 6, 13, 6),
    ("fist", 6, 7, 6),
    ("lower_arm", 5, 12, 5),
    ("wing", 3, 18, 14),
    ("halo", 22, 1, 22),
]

# ===========================================================================
# Form 3 -- 岩浆狂潮 / the Molten Horror.
# Hunched, low, huge arms with claws, back spikes, skull fused to the chest,
# and an exposed molten heart.
# ===========================================================================
HORROR = [
    ("leg", 9, 13, 9),
    ("foot", 10, 5, 10),
    ("torso", 28, 22, 16),
    ("back_plate", 16, 12, 4),
    ("skull", 13, 8, 13),
    ("eye", 4, 2, 1),
    ("heart", 15, 15, 4),
    ("upper_arm", 7, 19, 7),
    ("forearm", 7, 17, 7),
    ("claw", 8, 10, 8),
    ("spike", 4, 10, 4),
]

STYLES = {
    # name -> (base ramp, glow ramp, glowy)
    "ascendant": (GOLD, EMBER, True),
    "horror": (CHAR, LAVA, True),
}


def build(form_name, parts, seed_base, accent):
    """accents: parts that should read as bare fire instead of plate."""
    boxes = pack(parts)
    img = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))
    base_ramp, glow_ramp, _ = STYLES[form_name]
    order = {name: i for i, (name, *_rest) in enumerate(parts)}
    for i, box in enumerate(sorted(boxes, key=lambda b: order[b[0]])):
        name = box[0]
        if name in accent:
            style = (glow_ramp, glow_ramp, True)
        elif name in ("wing", "halo"):
            style = (base_ramp, glow_ramp, True)
        else:
            style = STYLES[form_name]
        paint(img, box, style, seed_base + 7 * i)
    # eyes / cores / hearts get a dedicated white-hot treatment
    for box in boxes:
        name = box[0]
        if name in ("eye", "core", "heart", "halo"):
            _draw_fiery(box, img, glow_ramp, name)
    path = os.path.join(OUT, f"ancient_forgeborn_{form_name}.png")
    os.makedirs(OUT, exist_ok=True)
    img.save(path, "PNG", optimize=True)
    print(f"wrote {path}")
    print(f"  --- {form_name} UV table ---")
    for box in sorted(boxes, key=lambda b: order[b[0]]):
        print(f'  {box[0]:12s} w={box[1]:2d} h={box[2]:2d} d={box[3]:2d}'
              f'  ->  texOffs({box[6]:3d}, {box[7]:3d})  rect {box[4]}x{box[5]}')
    return path


def _draw_fiery(box, img, glow_ramp, kind):
    name, w, h, d, bw, bh, u, v = box
    px = img.load()
    if kind == "eye":
        for yy in range(v, v + bh):
            for xx in range(u, u + bw):
                px[xx, yy] = ramp(glow_ramp, 0.99) + (255,)
        return
    # core / heart / halo: a fiery centre that fades to the rim
    cx, cy = (bw - 1) / 2.0, (bh - 1) / 2.0
    maxd = max(cx, cy) or 1.0
    for yy in range(v, v + bh):
        for xx in range(u, u + bw):
            d = max(abs(xx - u - cx), abs(yy - v - cy)) / maxd
            px[xx, yy] = ramp(glow_ramp, 0.98 - 0.62 * d) + (255,)


def main():
    build("ascendant", ASCENDANT, 401, accent={"lower_arm", "wing"})
    build("horror", HORROR, 701, accent={"heart", "spike", "claw"})


if __name__ == "__main__":
    main()