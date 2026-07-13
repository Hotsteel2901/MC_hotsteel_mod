#!/usr/bin/env python3
"""Generate proper 64x32 armor layer textures for Hot Steel mod.

The previous PNGs were 32x16 with opaque pixels concentrated in the top-left
quadrant, which caused only some armor pieces to render on the player model
(the renderer samples UV coords designed for a 64x32 texture).

This script produces 64x32 RGBA PNGs with a uniform steel-like fill so all
armor parts (helmet, chestplate, sleeves, legs, boots) render correctly.
"""
from PIL import Image
import os

# Steel palette (matches the SVG placeholder color)
BASE = (0x6f, 0x7c, 0x8a, 255)   # main gray-steel
DARK = (0x3e, 0x47, 0x50, 255)   # shadow / edge

OUT_DIR = "/workspace/src/main/resources/assets/hotsteel/textures/models/armor"


def armor_texture(w, h):
    """Create a 64x32 armor texture with subtle steel shading."""
    im = Image.new("RGBA", (w, h), BASE)
    px = im.load()
    # Add subtle horizontal banding for a "plate" look and a darker edge.
    for y in range(h):
        for x in range(w):
            # darker border (1px)
            if x == 0 or y == 0 or x == w - 1 or y == h - 1:
                px[x, y] = DARK
            # subtle vertical highlight every 8px column
            elif x % 8 == 0:
                r, g, b, a = BASE
                px[x, y] = (min(255, r + 12), min(255, g + 12), min(255, b + 12), a)
            # subtle horizontal shade line every 8px row
            elif y % 8 == 0:
                r, g, b, a = BASE
                px[x, y] = (max(0, r - 18), max(0, g - 18), max(0, b - 18), a)
    return im


def main():
    os.makedirs(OUT_DIR, exist_ok=True)
    for name in ("hot_steel_layer_1", "hot_steel_layer_2"):
        im = armor_texture(64, 32)
        path = os.path.join(OUT_DIR, name + ".png")
        im.save(path, "PNG", optimize=True)
        print(f"  wrote {path} ({im.size[0]}x{im.size[1]})")


if __name__ == "__main__":
    main()
