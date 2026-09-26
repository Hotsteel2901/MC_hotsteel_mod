#!/usr/bin/env python3
"""Rebuild every hand-drawn Hot Steel / Molten-forged BLOCK and ARMOR texture.

Why this script exists
----------------------
Two classes of textures kept going wrong:

1. Blocks that reuse a vanilla *template* model (chain, lantern, door, trapdoor,
   ladder) or the vanilla armor model sample hard-coded UV rectangles. Free-hand
   art lands outside the sampled area, so the block looks empty or garbled.
   Fix: start from the vanilla texture (correct layout + alpha silhouette) and
   re-map it into the mod palette, *preserving* the vanilla shading gradient.

2. Simple cube blocks must be instantly tellable apart. Each one below gets its
   own structure (tiles / cracks / bricks / pitted / basin), not just a hue swap.

Everything is deterministic: the pseudo-random noise comes from an explicit LCG,
so two runs produce byte-identical PNGs.
"""
import io
import os
import zipfile

from PIL import Image

VANILLA_JAR = "/root/.gradle/caches/fabric-loom/1.21.1/minecraft-client.jar"
ASSETS = "/workspace/src/main/resources/assets/hotsteel/textures"
OUT_BLOCK = os.path.join(ASSETS, "block")
OUT_ARMOR = os.path.join(ASSETS, "models/armor")

# ===========================================================================
# Palette ramps: dark -> bright.
# ===========================================================================
# Warm quenched steel: the body colour of everything "hot steel".
RAMP_STEEL = [
    (0x12, 0x14, 0x18), (0x1e, 0x22, 0x27), (0x2e, 0x33, 0x3a), (0x40, 0x47, 0x50),
    (0x56, 0x5e, 0x69), (0x6f, 0x79, 0x85), (0x8b, 0x96, 0xa2), (0xa9, 0xb5, 0xc0),
    (0xc9, 0xd4, 0xdd),
]
# Cold blue-steel, used for the smooth "steel" tier and quench highlights.
RAMP_QUENCH = [
    (0x10, 0x17, 0x20), (0x1a, 0x25, 0x31), (0x28, 0x38, 0x49), (0x3a, 0x51, 0x68),
    (0x4e, 0x6b, 0x85), (0x66, 0x88, 0xa3), (0x85, 0xa7, 0xc0), (0xa9, 0xc7, 0xdc),
    (0xcd, 0xe5, 0xf5),
]
# Dull, dirty grey for crude steel.
RAMP_CRUDE = [
    (0x1a, 0x1c, 0x1d), (0x27, 0x2a, 0x2c), (0x35, 0x39, 0x3c), (0x44, 0x49, 0x4d),
    (0x53, 0x59, 0x5e), (0x66, 0x6c, 0x72), (0x7c, 0x83, 0x8a), (0x95, 0x9d, 0xa4),
    (0xb0, 0xb8, 0xbf),
]
# Soot / charcoal.
RAMP_CHAR = [
    (0x07, 0x07, 0x08), (0x0f, 0x0f, 0x10), (0x18, 0x17, 0x17), (0x23, 0x22, 0x21),
    (0x30, 0x2e, 0x2c), (0x3f, 0x3c, 0x39), (0x50, 0x4c, 0x48), (0x66, 0x61, 0x5c),
    (0x80, 0x7a, 0x74),
]
# Ember: the glow of hot steel.
RAMP_EMBER = [
    (0x2a, 0x0d, 0x02), (0x58, 0x1b, 0x04), (0x8c, 0x2f, 0x08), (0xc0, 0x4a, 0x0d),
    (0xe8, 0x6d, 0x16), (0xff, 0x93, 0x27), (0xff, 0xbb, 0x52), (0xff, 0xe0, 0x93),
    (0xff, 0xf6, 0xd8),
]
# Lava: the Molten-forged glow, hotter and more saturated.
RAMP_LAVA = [
    (0x1a, 0x06, 0x02), (0x45, 0x10, 0x04), (0x7d, 0x1f, 0x05), (0xb8, 0x3a, 0x08),
    (0xea, 0x5f, 0x10), (0xff, 0x8b, 0x22), (0xff, 0xbd, 0x58), (0xff, 0xe4, 0xa0),
    (0xff, 0xfb, 0xe8),
]


def _lcg(seed):
    """Deterministic pseudo-random stream in [0,1)."""
    state = [seed & 0xFFFFFFFF]

    def nxt():
        state[0] = (state[0] * 1103515245 + 12345) & 0x7FFFFFFF
        return state[0] / 0x7FFFFFFF

    return nxt


def jar():
    return zipfile.ZipFile(VANILLA_JAR)


def vanilla(rel, crop_first_frame=True):
    with jar() as z:
        raw = z.read("assets/minecraft/textures/" + rel)
    im = Image.open(io.BytesIO(raw)).convert("RGBA")
    if crop_first_frame and im.height > im.width:
        im = im.crop((0, 0, im.width, im.width))
    return im


def lum(px):
    return (px[0] * 299 + px[1] * 587 + px[2] * 114) // 1000


def ramp_at(ramp, t):
    if t < 0.0:
        t = 0.0
    elif t > 1.0:
        t = 1.0
    return ramp[int(round(t * (len(ramp) - 1)))]


def save(im, name, folder=OUT_BLOCK):
    os.makedirs(folder, exist_ok=True)
    path = os.path.join(folder, name)
    im.save(path, "PNG", optimize=True)
    return name


# ===========================================================================
# 1. Vanilla-template blocks.
#
# `remap` keeps the vanilla shading instead of flattening it: the source
# luminance drives a position *inside* the target ramp, and only pixels brighter
# than `glow_from` additionally bleed towards the glow ramp — so frames stay
# dark, glass keeps its gradient, and only the flame core goes white-hot.
# ===========================================================================
def remap(src, base, glow=None, glow_from=1.1, glow_mix=1.0, base_lo=0.0, base_hi=1.0):
    out = Image.new("RGBA", src.size, (0, 0, 0, 0))
    sp, op = src.load(), out.load()
    for y in range(src.height):
        for x in range(src.width):
            px = sp[x, y]
            if px[3] == 0:
                continue
            t = lum(px) / 255.0
            colour = ramp_at(base, base_lo + t * (base_hi - base_lo))
            if glow is not None and t > glow_from:
                k = min(1.0, (t - glow_from) / max(1e-6, 1.0 - glow_from)) * glow_mix
                g = ramp_at(glow, 0.45 + 0.55 * min(1.0, (t - glow_from) / 0.35))
                colour = tuple(int(colour[i] + (g[i] - colour[i]) * k) for i in range(3))
            op[x, y] = (colour[0], colour[1], colour[2], px[3])
    return out


# ===========================================================================
# 2. Simple cube blocks -- each gets its own structure.
# ===========================================================================
def fill(im, colour, box=None):
    p = im.load()
    x0, y0, x1, y1 = box or (0, 0, im.width, im.height)
    for y in range(y0, y1):
        for x in range(x0, x1):
            p[x, y] = colour


def build_crude_steel_block():
    """Pitted, matte, dirty: the first bloom off the furnace."""
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    rnd = _lcg(11)
    p = im.load()
    for y in range(16):
        for x in range(16):
            t = 0.18 + rnd() * 0.30
            p[x, y] = ramp_at(RAMP_CRUDE, t) + (255,)
    # irregular pits
    for _ in range(26):
        x, y = int(rnd() * 16), int(rnd() * 16)
        shade = 0.03 + rnd() * 0.08
        p[x, y] = ramp_at(RAMP_CRUDE, shade) + (255,)
    # a few oxidised flecks so it reads as "unfinished"
    for _ in range(7):
        x, y = int(rnd() * 16), int(rnd() * 16)
        p[x, y] = (0x5a, 0x42, 0x2c, 255)
    # chipped border to break up the cube silhouette
    for i in range(16):
        p[i, 0] = ramp_at(RAMP_CRUDE, 0.12) + (255,)
        p[i, 15] = ramp_at(RAMP_CRUDE, 0.12) + (255,)
        p[0, i] = ramp_at(RAMP_CRUDE, 0.12) + (255,)
        p[15, i] = ramp_at(RAMP_CRUDE, 0.12) + (255,)
    return save(im, "crude_steel_block.png")


def build_steel_block():
    """Smooth, polished, cold: a proper storage block."""
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    rnd = _lcg(23)
    p = im.load()
    for y in range(16):
        for x in range(16):
            # gentle diagonal sheen across the face
            t = 0.30 + 0.28 * ((x + y) / 30.0) + (rnd() - 0.5) * 0.05
            p[x, y] = ramp_at(RAMP_QUENCH, t) + (255,)
    # inset bevel frame
    for i in range(16):
        p[i, 0] = ramp_at(RAMP_QUENCH, 0.62) + (255,)
        p[0, i] = ramp_at(RAMP_QUENCH, 0.62) + (255,)
        p[i, 15] = ramp_at(RAMP_QUENCH, 0.10) + (255,)
        p[15, i] = ramp_at(RAMP_QUENCH, 0.10) + (255,)
    # quarter seams
    for i in range(1, 15):
        p[8, i] = ramp_at(RAMP_QUENCH, 0.16) + (255,)
        p[i, 8] = ramp_at(RAMP_QUENCH, 0.16) + (255,)
    # corner rivets
    for (cx, cy) in ((3, 3), (12, 3), (3, 12), (12, 12)):
        p[cx, cy] = ramp_at(RAMP_QUENCH, 0.95) + (255,)
        p[cx + 1, cy] = ramp_at(RAMP_QUENCH, 0.55) + (255,)
        p[cx, cy + 1] = ramp_at(RAMP_QUENCH, 0.45) + (255,)
    return save(im, "steel_block.png")


def build_hot_steel_block():
    """Four glowing plates welded into one block: dark steel, ember seams."""
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    rnd = _lcg(37)
    p = im.load()
    for y in range(16):
        for x in range(16):
            p[x, y] = ramp_at(RAMP_STEEL, 0.30 + rnd() * 0.22) + (255,)
    # 1px ember seams on the tile grid (dark -> hot, so the light bleeds out)
    for i in range(16):
        p[i, 0] = ramp_at(RAMP_EMBER, 0.55) + (255,)
        p[0, i] = ramp_at(RAMP_EMBER, 0.55) + (255,)
    for i in range(1, 16):
        p[i, 8] = ramp_at(RAMP_EMBER, 0.72) + (255,)
        p[8, i] = ramp_at(RAMP_EMBER, 0.72) + (255,)
    # the seam corners get the brightest bleed
    for (x, y) in ((0, 0), (8, 0), (0, 8), (8, 8)):
        p[x, y] = ramp_at(RAMP_EMBER, 0.92) + (255,)
    # rivets in the middle of each plate
    for (cx, cy) in ((3, 3), (12, 3), (3, 12), (12, 12)):
        p[cx, cy] = ramp_at(RAMP_STEEL, 0.90) + (255,)
        p[cx + 1, cy + 1] = ramp_at(RAMP_STEEL, 0.16) + (255,)
    # heat halo around each rivet
    for (cx, cy) in ((3, 3), (12, 3), (3, 12), (12, 12)):
        for dx, dy in ((0, 1), (1, 0), (2, 1), (1, 2)):
            x, y = cx + dx, cy + dy
            if 0 <= x < 16 and 0 <= y < 16:
                r, g, b, a = p[x, y]
                p[x, y] = (min(255, r + 70), min(255, g + 34), b, a)
    return save(im, "hot_steel_block.png")


def build_molten_steel_block():
    """Black plate split by thin lava cracks around a molten stud."""
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    rnd = _lcg(53)
    p = im.load()
    for y in range(16):
        for x in range(16):
            p[x, y] = ramp_at(RAMP_CHAR, 0.16 + rnd() * 0.20) + (255,)
    # two crossing lava cracks, jagged but only 1px wide
    crack = set()
    for i in range(16):
        crack.add((i, max(0, min(15, int(3 + i * 0.60 + (rnd() - 0.5) * 1.6)))))
        crack.add((max(0, min(15, int(2 + i * 0.55 + (rnd() - 0.5) * 1.6))), i))
    for (x, y) in crack:
        p[x, y] = ramp_at(RAMP_LAVA, 0.82) + (255,)
        # a very light ember bleed only where the crack runs, not a fat halo
        for dx, dy in ((0, 1), (1, 0)):
            nx, ny = x + dx, y + dy
            if 0 <= nx < 16 and 0 <= ny < 16 and (nx, ny) not in crack:
                r, g, b, a = p[nx, ny]
                p[nx, ny] = (min(255, r + 46), min(255, g + 18), b, a)
    # central molten stud
    for y in range(6, 10):
        for x in range(6, 10):
            d = abs(x - 7.5) + abs(y - 7.5)
            p[x, y] = ramp_at(RAMP_LAVA, 0.95 if d < 1.5 else 0.72) + (255,)
    return save(im, "molten_steel_block.png")


def build_bricks(name, brick_ramp, mortar_ramp, ember=False, seed=7):
    """Offset brick courses; mortar is a 1px dark grid, bricks carry the colour."""
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    rnd = _lcg(seed)
    p = im.load()
    row_h = 4
    for y in range(16):
        course = y // row_h
        for x in range(16):
            offset = 0 if course % 2 == 0 else 4
            local = (x + offset) % 8
            is_mortar = (y % row_h == 0) or (local == 0)
            if is_mortar:
                t = 0.10 + rnd() * 0.10
                if ember and rnd() < 0.22:
                    p[x, y] = ramp_at(brick_ramp, 0.68 + rnd() * 0.25) + (255,)
                else:
                    p[x, y] = ramp_at(mortar_ramp, t) + (255,)
            else:
                # keep adjacent bricks close in tone so the courses still read as
                # bricks instead of a bag of loose stones
                base = 0.60 + rnd() * 0.16
                p[x, y] = ramp_at(brick_ramp, base) + (255,)
    if ember:
        # faint glow on the upper edge of each course
        for y in range(16):
            if y % row_h == 1:
                for x in range(16):
                    r, g, b, a = p[x, y]
                    p[x, y] = (min(255, r + 34), min(255, g + 14), b, a)
        # a couple of white-hot mortar specks
        for _ in range(5):
            x, y = int(rnd() * 16), int(rnd() * 16)
            if y % row_h == 0:
                p[x, y] = ramp_at(brick_ramp, 0.98) + (255,)
    return save(im, name)


def build_hot_steel_forge():
    """Furnace face: bolts, a dark mouth and a white-hot grate."""
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    rnd = _lcg(71)
    p = im.load()
    for y in range(16):
        for x in range(16):
            p[x, y] = ramp_at(RAMP_STEEL, 0.34 + rnd() * 0.20) + (255,)
    # darker plate border
    for i in range(16):
        p[i, 0] = ramp_at(RAMP_STEEL, 0.12) + (255,)
        p[i, 15] = ramp_at(RAMP_STEEL, 0.12) + (255,)
        p[0, i] = ramp_at(RAMP_STEEL, 0.12) + (255,)
        p[15, i] = ramp_at(RAMP_STEEL, 0.12) + (255,)
    # corner bolts
    for (cx, cy) in ((2, 2), (13, 2), (2, 13), (13, 13)):
        p[cx, cy] = ramp_at(RAMP_STEEL, 0.95) + (255,)
        p[cx, cy + 1] = ramp_at(RAMP_STEEL, 0.30) + (255,)
    # furnace mouth
    for y in range(5, 13):
        for x in range(4, 12):
            p[x, y] = ramp_at(RAMP_CHAR, 0.05) + (255,)
    # grate bars
    for x in range(4, 12):
        if (x - 4) % 3 == 0:
            p[x, 10] = ramp_at(RAMP_STEEL, 0.55) + (255,)
    # fire inside, hottest at the bottom
    for y in range(10, 13):
        for x in range(5, 11):
            k = (y - 9) / 4.0
            p[x, y] = ramp_at(RAMP_EMBER, 0.45 + k * 0.5) + (255,)
    for x in range(6, 10):
        p[x, 12] = ramp_at(RAMP_EMBER, 0.98) + (255,)
    # vent slits on the shoulder
    for x in range(5, 11, 2):
        p[x, 3] = ramp_at(RAMP_EMBER, 0.62) + (255,)
    return save(im, "hot_steel_forge.png")


def build_hot_steel_smelter():
    """A basin: dark rim, molten pool with bubbles, glow bleeding inward."""
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    rnd = _lcg(89)
    p = im.load()
    # rim
    for y in range(16):
        for x in range(16):
            edge = min(x, y, 15 - x, 15 - y)
            if edge < 2:
                p[x, y] = ramp_at(RAMP_STEEL, 0.30 + rnd() * 0.22) + (255,)
            elif edge == 2:
                p[x, y] = ramp_at(RAMP_EMBER, 0.38) + (255,)
            else:
                p[x, y] = ramp_at(RAMP_LAVA, 0.66 + rnd() * 0.24) + (255,)
    # bubbles that pop brighter at the centre
    for _ in range(12):
        x = 5 + int(rnd() * 6)
        y = 5 + int(rnd() * 6)
        p[x, y] = ramp_at(RAMP_LAVA, 0.99) + (255,)
        for dx, dy in ((1, 0), (0, 1)):
            if rnd() < 0.5:
                p[x + dx, y + dy] = ramp_at(RAMP_LAVA, 0.85) + (255,)
    # bolts on the rim
    for (cx, cy) in ((1, 1), (14, 1), (1, 14), (14, 14)):
        p[cx, cy] = ramp_at(RAMP_STEEL, 0.92) + (255,)
    return save(im, "hot_steel_smelter.png")


# ===========================================================================
# 3. Armor layers.
# ===========================================================================
def build_armor():
    made = []
    for layer in (1, 2):
        src = vanilla(f"models/armor/iron_layer_{layer}.png")
        made.append(save(remap(src, RAMP_STEEL, RAMP_EMBER, glow_from=0.86, glow_mix=0.55),
                         f"hot_steel_layer_{layer}.png", OUT_ARMOR))
        made.append(save(remap(src, RAMP_CHAR, RAMP_LAVA, glow_from=0.70, glow_mix=0.9,
                               base_lo=0.10, base_hi=0.78),
                         f"molten_steel_layer_{layer}.png", OUT_ARMOR))
    return made


# ===========================================================================
# 4. Vanilla-template decorative blocks.
# ===========================================================================
def build_template_blocks():
    made = []
    # Lanterns: frame stays dark steel, the glass keeps its own gradient and
    # only the flame core goes white-hot -- the earlier version flattened the
    # whole glass into one solid orange square, which read as a dead box.
    made.append(save(remap(vanilla("block/lantern.png"), RAMP_STEEL, RAMP_EMBER,
                           glow_from=0.58, glow_mix=1.0, base_lo=0.0, base_hi=0.62),
                     "hot_steel_lantern.png"))
    made.append(save(remap(vanilla("block/lantern.png"), RAMP_CHAR, RAMP_LAVA,
                           glow_from=0.50, glow_mix=1.0, base_lo=0.0, base_hi=0.50),
                     "molten_lantern.png"))

    made.append(save(remap(vanilla("block/chain.png"), RAMP_STEEL, None, base_lo=0.05, base_hi=0.90),
                     "hot_steel_chain.png"))
    made.append(save(remap(vanilla("block/chain.png"), RAMP_CHAR, RAMP_LAVA,
                           glow_from=0.55, glow_mix=0.85, base_lo=0.05, base_hi=0.70),
                     "molten_steel_chain.png"))

    made.append(save(remap(vanilla("block/iron_door_top.png"), RAMP_STEEL, RAMP_EMBER,
                           glow_from=0.80, glow_mix=0.7, base_lo=0.06, base_hi=0.72),
                     "hot_steel_door_top.png"))
    made.append(save(remap(vanilla("block/iron_door_bottom.png"), RAMP_STEEL, RAMP_EMBER,
                           glow_from=0.80, glow_mix=0.7, base_lo=0.06, base_hi=0.72),
                     "hot_steel_door_bottom.png"))
    made.append(save(remap(vanilla("block/iron_trapdoor.png"), RAMP_STEEL, RAMP_EMBER,
                           glow_from=0.82, glow_mix=0.6, base_lo=0.06, base_hi=0.74),
                     "hot_steel_trapdoor.png"))
    made.append(save(remap(vanilla("block/ladder.png"), RAMP_STEEL, RAMP_EMBER,
                           glow_from=0.88, glow_mix=0.5, base_lo=0.06, base_hi=0.80),
                     "hot_steel_ladder.png"))
    return made


# ===========================================================================
# 5. Molten glass -- a thin pane you can actually see through.
# ===========================================================================
def build_molten_glass():
    S = 16
    im = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    p = im.load()
    for y in range(S):
        for x in range(S):
            edge = min(x, y, S - 1 - x, S - 1 - y)
            if edge == 0:
                p[x, y] = (0xb8, 0x3a, 0x08, 255)       # 1px hot frame
            elif edge == 1:
                p[x, y] = (0xc0, 0x4a, 0x0d, 70)        # whisper of inner heat
            else:
                a = 26 if (x + y) % 7 else 44
                p[x, y] = (0xff, 0xa8, 0x52, a)         # barely-there warm haze
    # small bright corner marks, not full white rivets
    for (x, y) in ((0, 0), (S - 1, 0), (0, S - 1), (S - 1, S - 1)):
        p[x, y] = (0xff, 0xbb, 0x52, 255)
    return [save(im, "molten_glass.png")]


def main():
    total = []
    total.append(build_crude_steel_block())
    total.append(build_steel_block())
    total.append(build_hot_steel_block())
    total.append(build_molten_steel_block())
    total.append(build_bricks("hot_steel_bricks.png", RAMP_EMBER, RAMP_STEEL, ember=True, seed=7))
    total.append(build_bricks("charred_bricks.png", RAMP_CHAR, RAMP_CHAR, ember=True, seed=13))
    total.append(build_hot_steel_forge())
    total.append(build_hot_steel_smelter())
    total += build_template_blocks()
    total += build_armor()
    total += build_molten_glass()
    print(f"wrote {len(total)} textures")
    for n in total:
        print("  ", n)


if __name__ == "__main__":
    main()
