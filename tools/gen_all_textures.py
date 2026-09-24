#!/usr/bin/env python3
"""Redraw every Hot Steel 2.0 ("Forge Era") mod texture as crisp pixel-art PNGs.

The script is fully procedural (Pillow only, no SVG/PNG intermediate), so the
output is deterministic 1:1 pixel art at the exact dimensions Minecraft needs.
Every colour comes from the single PALETTE table below; no function invents its
own colours. Shading is done inside a colour family with sh()/noise_shade(),
which are deterministic (hash based on pixel coordinates, never random()), so
re-running the script produces byte-identical files.

Families (each ordered deep -> dark -> mid -> base -> light -> high -> core):
  STEEL  steel grey      QUENCH quenched steel blue   CRUDE crude steel
  FIRE   molten orange   GOLD   molten-cast gold      WOOD   wooden handle
  STRING bowstring       CHAR   charcoal              PARCH  scorched parchment
  GREEN / RED accents    GLASS  tinted glass (fixed alpha)
"""
from PIL import Image
import os

TRANSPARENT = (0, 0, 0, 0)

# ===========================================================================
# PALETTE  -- the single, central source of every colour used below.
# ===========================================================================
PALETTE = {
    # ---- steel grey family -------------------------------------------------
    "steel_deep":  (0x24, 0x2a, 0x31),
    "steel_dark":  (0x3b, 0x45, 0x4f),
    "steel_mid":   (0x50, 0x5d, 0x6a),
    "steel_base":  (0x67, 0x76, 0x86),
    "steel_light": (0x96, 0xa6, 0xb6),
    "steel_high":  (0xc6, 0xd4, 0xe0),
    "steel_spec":  (0xe6, 0xef, 0xf6),

    # ---- crude (dull) steel ------------------------------------------------
    "crude_deep":  (0x1e, 0x21, 0x24),
    "crude_dark":  (0x33, 0x38, 0x3d),
    "crude_mid":   (0x47, 0x4d, 0x54),
    "crude_base":  (0x57, 0x5d, 0x65),
    "crude_light": (0x7f, 0x88, 0x92),
    "crude_high":  (0x9e, 0xa8, 0xb3),
    "crude_spec":  (0xc2, 0xcb, 0xd4),

    # ---- quenched / refined steel blue ------------------------------------
    "quench_deep":  (0x1c, 0x28, 0x34),
    "quench_dark":  (0x2d, 0x40, 0x52),
    "quench_mid":   (0x40, 0x60, 0x7c),
    "quench_base":  (0x54, 0x76, 0x93),
    "quench_light": (0x88, 0xac, 0xc8),
    "quench_high":  (0xba, 0xd6, 0xea),
    "quench_spec":  (0xe2, 0xf1, 0xfb),

    # ---- molten fire / emissive orange ------------------------------------
    "fire_deep":  (0x1a, 0x08, 0x02),
    "fire_dark":  (0x7a, 0x2c, 0x08),
    "fire_mid":   (0xa0, 0x3a, 0x10),
    "fire_base":  (0xe0, 0x61, 0x1f),
    "fire_light": (0xff, 0xc2, 0x4a),
    "fire_high":  (0xff, 0xe7, 0x9c),
    "fire_core":  (0xff, 0xfa, 0xe8),

    # ---- molten-cast gold (new) -------------------------------------------
    "gold_deep":  (0x3a, 0x22, 0x05),
    "gold_dark":  (0x8a, 0x52, 0x10),
    "gold_mid":   (0xb0, 0x6f, 0x18),
    "gold_base":  (0xd4, 0x88, 0x2a),
    "gold_light": (0xf2, 0xc2, 0x64),
    "gold_high":  (0xff, 0xe6, 0xa8),
    "gold_core":  (0xff, 0xf9, 0xe2),

    # ---- wood --------------------------------------------------------------
    "wood_deep":  (0x2a, 0x1c, 0x0f),
    "wood_dark":  (0x3f, 0x2b, 0x18),
    "wood_mid":   (0x55, 0x3a, 0x22),
    "wood_base":  (0x6b, 0x4a, 0x2b),
    "wood_light": (0x8b, 0x63, 0x3c),
    "wood_high":  (0xa8, 0x7c, 0x4e),
    "wood_core":  (0xc3, 0x9a, 0x68),

    # ---- bowstring ---------------------------------------------------------
    "str_deep":  (0x6e, 0x68, 0x58),
    "str_dark":  (0x8a, 0x84, 0x74),
    "str_mid":   (0xb0, 0xa9, 0x96),
    "str_base":  (0xd9, 0xd2, 0xc0),
    "str_light": (0xee, 0xe8, 0xd8),
    "str_high":  (0xff, 0xfd, 0xf2),
    "str_core":  (0xff, 0xff, 0xff),

    # ---- charcoal (burnt rock / charred wood) -----------------------------
    "char_deep":  (0x0e, 0x0d, 0x0c),
    "char_dark":  (0x1c, 0x1a, 0x18),
    "char_mid":   (0x26, 0x24, 0x22),
    "char_base":  (0x33, 0x2f, 0x2c),
    "char_light": (0x46, 0x41, 0x3d),
    "char_high":  (0x5c, 0x55, 0x4f),
    "char_core":  (0x74, 0x6b, 0x63),

    # ---- scorched parchment -----------------------------------------------
    "parch_deep":  (0x54, 0x38, 0x18),
    "parch_dark":  (0x76, 0x50, 0x24),
    "parch_mid":   (0x9c, 0x74, 0x38),
    "parch_base":  (0xc4, 0x9e, 0x58),
    "parch_light": (0xe0, 0xc6, 0x8a),
    "parch_high":  (0xf2, 0xe4, 0xba),
    "parch_core":  (0xff, 0xf8, 0xe6),

    # ---- small accents -----------------------------------------------------
    "green_deep":  (0x1c, 0x3a, 0x12),
    "green_dark":  (0x2f, 0x5c, 0x1c),
    "green_mid":   (0x43, 0x7a, 0x28),
    "green_base":  (0x57, 0x9a, 0x34),
    "green_light": (0x74, 0xbd, 0x4c),
    "green_high":  (0x9a, 0xda, 0x6e),
    "green_core":  (0xc4, 0xf0, 0xa0),

    "red_deep":  (0x5c, 0x0e, 0x08),
    "red_dark":  (0x8a, 0x1a, 0x10),
    "red_mid":   (0xb0, 0x25, 0x16),
    "red_base":  (0xd8, 0x32, 0x1e),
    "red_light": (0xf0, 0x5a, 0x34),
    "red_high":  (0xff, 0x8f, 0x6a),
    "red_core":  (0xff, 0xd0, 0xb8),
}

# --- colour families (only names taken from PALETTE, never literal colours) --
STEEL  = (PALETTE["steel_deep"],  PALETTE["steel_dark"],  PALETTE["steel_mid"],
          PALETTE["steel_base"],  PALETTE["steel_light"], PALETTE["steel_high"],
          PALETTE["steel_spec"])
CRUDE  = (PALETTE["crude_deep"],  PALETTE["crude_dark"],  PALETTE["crude_mid"],
          PALETTE["crude_base"],  PALETTE["crude_light"], PALETTE["crude_high"],
          PALETTE["crude_spec"])
QUENCH = (PALETTE["quench_deep"],  PALETTE["quench_dark"],  PALETTE["quench_mid"],
          PALETTE["quench_base"],  PALETTE["quench_light"], PALETTE["quench_high"],
          PALETTE["quench_spec"])
FIRE   = (PALETTE["fire_deep"],  PALETTE["fire_dark"],  PALETTE["fire_mid"],
          PALETTE["fire_base"],  PALETTE["fire_light"], PALETTE["fire_high"],
          PALETTE["fire_core"])
GOLD   = (PALETTE["gold_deep"],  PALETTE["gold_dark"],  PALETTE["gold_mid"],
          PALETTE["gold_base"],  PALETTE["gold_light"], PALETTE["gold_high"],
          PALETTE["gold_core"])
WOOD   = (PALETTE["wood_deep"],  PALETTE["wood_dark"],  PALETTE["wood_mid"],
          PALETTE["wood_base"],  PALETTE["wood_light"], PALETTE["wood_high"],
          PALETTE["wood_core"])
STRING = (PALETTE["str_deep"],  PALETTE["str_dark"],  PALETTE["str_mid"],
          PALETTE["str_base"],  PALETTE["str_light"], PALETTE["str_high"],
          PALETTE["str_core"])
CHAR   = (PALETTE["char_deep"],  PALETTE["char_dark"],  PALETTE["char_mid"],
          PALETTE["char_base"],  PALETTE["char_light"], PALETTE["char_high"],
          PALETTE["char_core"])
PARCH  = (PALETTE["parch_deep"],  PALETTE["parch_dark"],  PALETTE["parch_mid"],
          PALETTE["parch_base"],  PALETTE["parch_light"], PALETTE["parch_high"],
          PALETTE["parch_core"])
GREEN  = (PALETTE["green_deep"],  PALETTE["green_dark"],  PALETTE["green_mid"],
          PALETTE["green_base"],  PALETTE["green_light"], PALETTE["green_high"],
          PALETTE["green_core"])
RED    = (PALETTE["red_deep"],  PALETTE["red_dark"],  PALETTE["red_mid"],
          PALETTE["red_base"],  PALETTE["red_light"], PALETTE["red_high"],
          PALETTE["red_core"])

# glass is special: constant alpha, never a gradient (keys = solid / frame)
GLASS_BASE = (0x3a, 0x4a, 0x58, 165)   # dark tinted body (alpha ~165)
GLASS_EDGE = (0x24, 0x30, 0x3c, 178)   # frame (alpha ~178)
GLASS_SHINE = (0xdc, 0xee, 0xf6, 150)  # fixed-alpha highlight
BOTTLE_BODY = (0xb8, 0xd0, 0xd8, 180)  # lava-bottle glass (original alpha kept)
BOTTLE_EDGE = (0x8a, 0x9f, 0xa8, 200)


# ===========================================================================
# Deterministic shading helpers (no randomness, no anti-aliasing)
# ===========================================================================
def _lerp3(a, b, t):
    return (int(round(a[0] + (b[0] - a[0]) * t)),
            int(round(a[1] + (b[1] - a[1]) * t)),
            int(round(a[2] + (b[2] - a[2]) * t)))


def sh(fam, t):
    """Sample a colour inside a 7-step family. t in [-1,1]: -1=deep, +1=core."""
    n = len(fam) - 1
    t = -1.0 if t < -1.0 else (1.0 if t > 1.0 else t)
    p = (t + 1.0) / 2.0 * n
    i = int(p)
    if i >= n:
        return fam[n]
    return _lerp3(fam[i], fam[i + 1], p - i)


def noise_shade(fam, x, y, seed=0, amp=0.3):
    """Deterministic per-pixel metal-plate shading (hash of pixel coords)."""
    h = ((x * 73856093) ^ (y * 19349663) ^ ((seed + 1) * 83492791)) & 0x7fffffff
    r = (h % 2001) / 1000.0 - 1.0
    return sh(fam, r * amp)


# ===========================================================================
# Tiny pixel-art canvas helper
# ===========================================================================
class Canvas:
    def __init__(self, w, h):
        self.w, self.h = w, h
        self.im = Image.new("RGBA", (w, h), TRANSPARENT)
        self.px = self.im.load()

    def set(self, x, y, color):
        if 0 <= x < self.w and 0 <= y < self.h and color is not None:
            if len(color) == 3:
                color = (*color, 255)
            self.px[x, y] = color

    def get(self, x, y):
        return self.px[x, y]

    def fill(self, color):
        for y in range(self.h):
            for x in range(self.w):
                self.set(x, y, color)

    def rect(self, x0, y0, x1, y1, color):      # inclusive
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                self.set(x, y, color)

    def hline(self, x0, x1, y, color):
        self.rect(x0, y, x1, y, color)

    def vline(self, x, y0, y1, color):
        self.rect(x, y0, x, y1, color)

    def line(self, x0, y0, x1, y1, color):      # Bresenham
        x0, y0, x1, y1 = int(x0), int(y0), int(x1), int(y1)
        dx = abs(x1 - x0); dy = -abs(y1 - y0)
        sx = 1 if x0 < x1 else -1
        sy = 1 if y0 < y1 else -1
        err = dx + dy
        while True:
            self.set(x0, y0, color)
            if x0 == x1 and y0 == y1:
                break
            e2 = 2 * err
            if e2 >= dy:
                err += dy; x0 += sx
            if e2 <= dx:
                err += dx; y0 += sy

    def outline(self, pts, color):
        for i in range(len(pts)):
            a = pts[i]; b = pts[(i + 1) % len(pts)]
            self.line(int(round(a[0])), int(round(a[1])),
                      int(round(b[0])), int(round(b[1])), color)

    def poly(self, pts, color):                 # scanline fill
        ys = [p[1] for p in pts]
        y0 = max(0, int(min(ys))); y1 = min(self.h - 1, int(max(ys)))
        n = len(pts)
        for y in range(y0, y1 + 1):
            xs = []
            for i in range(n):
                xa, ya = pts[i]; xb, yb = pts[(i + 1) % n]
                if (ya <= y < yb) or (yb <= y < ya):
                    t = (y - ya) / (yb - ya)
                    xs.append(xa + t * (xb - xa))
            xs.sort()
            for i in range(0, len(xs) - 1, 2):
                for x in range(int(round(xs[i])), int(round(xs[i + 1])) + 1):
                    self.set(x, y, color)

    def disc(self, cx, cy, r, color):
        for y in range(cy - r, cy + r + 1):
            for x in range(cx - r, cx + r + 1):
                if (x - cx) ** 2 + (y - cy) ** 2 <= r * r + r * 0.4:
                    self.set(x, y, color)

    def ring(self, cx, cy, r, color):
        for y in range(cy - r, cy + r + 1):
            for x in range(cx - r, cx + r + 1):
                d = (x - cx) ** 2 + (y - cy) ** 2
                if (r - 1) ** 2 < d <= r * r + r * 0.4:
                    self.set(x, y, color)

    def elli(self, cx, cy, rx, ry, color):
        for y in range(cy - ry, cy + ry + 1):
            for x in range(cx - rx, cx + rx + 1):
                if rx and ry and ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2 <= 1.12:
                    self.set(x, y, color)

    def save(self, path):
        os.makedirs(os.path.dirname(path), exist_ok=True)
        self.im.save(path, "PNG", optimize=True)


# ===========================================================================
# Shared primitives
# ===========================================================================
def draw_handle(c, x=7, y0=7, y1=14, fam=WOOD):
    """3px-wide vertical handle: lit left edge, body, shadowed right edge."""
    d, dk, m, b, l, h, co = fam
    for y in range(y0, y1 + 1):
        c.set(x, y, l)
        c.set(x + 1, y, b)
        c.set(x + 2, y, dk)
    c.set(x, y0, m); c.set(x + 2, y0, d)
    c.set(x, y1, d); c.set(x + 1, y1, d); c.set(x + 2, y1, d)
    for y in range(y0 + 3, y1, 3):
        c.set(x, y, dk); c.set(x + 1, y, dk); c.set(x + 2, y, d)
    for y in range(y0 + 1, y1, 3):
        c.set(x + 1, y, m)


def panel(c, x0, y0, x1, y1, fam):
    """Beveled metal plate + dark outline."""
    d, dk, m, b, l, h, co = fam
    c.rect(x0, y0, x1, y1, b)
    c.hline(x0 + 1, x1 - 1, y0, h)
    c.vline(x0, y0 + 1, y1 - 1, l)
    c.hline(x0 + 1, x1 - 1, y1, d)
    c.vline(x1, y0 + 1, y1 - 1, dk)
    c.outline([(x0, y0), (x1, y0), (x1, y1), (x0, y1)], d)


def rivet(c, x, y, fam):
    d, dk, m, b, l, h, co = fam
    c.set(x, y, h); c.set(x + 1, y, m)
    c.set(x, y + 1, m); c.set(x + 1, y + 1, d)


def ember(c, x, y):
    """A tiny emissive ember pixel (bright)."""
    c.set(x, y, FIRE[4]); c.set(x, y + 1, FIRE[3])


def draw_ingot(c, fam, glow=False):
    """Beveled cast ingot: lit top slab, front face, shadowed end cap."""
    d, dk, m, b, l, h, co = fam
    # front face
    c.poly([(2, 8), (11, 8), (11, 13), (2, 13)], b)
    c.hline(3, 10, 8, l)
    c.hline(3, 11, 12, m)
    c.hline(3, 11, 13, d)
    c.vline(2, 8, 13, d)
    # end cap
    c.poly([(11, 8), (13, 6), (13, 11), (11, 13)], dk)
    c.vline(13, 6, 11, d)
    c.line(11, 8, 13, 6, d)
    c.set(12, 8, m)
    # top slab
    c.poly([(4, 6), (13, 6), (11, 8), (2, 8)], l)
    c.hline(4, 13, 6, d)
    c.hline(5, 12, 7, h)
    c.line(2, 8, 11, 8, m)
    # embossed rivets on the front
    c.set(5, 10, co); c.set(5, 11, m); c.set(8, 10, co); c.set(8, 11, m)
    if glow:
        c.hline(4, 9, 10, FIRE[4])
        c.set(6, 12, FIRE[5]); c.set(9, 12, FIRE[3])
        c.set(3, 7, FIRE[5]); c.set(12, 9, FIRE[4]); c.set(8, 5, FIRE[5])
        c.hline(3, 12, 7, FIRE[4])
    return c


# ===========================================================================
# Tools (steel / molten-cast variants share one routine)
# ===========================================================================
def tool_sword(c, fam, glow=False):
    d, dk, m, b, l, h, co = fam
    e_l = FIRE[6] if glow else h
    e_m = FIRE[5] if glow else l
    c.poly([(7, 2), (9, 2), (9, 10), (7, 10)], b)
    c.vline(7, 2, 10, e_l)
    c.vline(8, 2, 10, e_m)
    c.vline(9, 2, 10, dk)
    c.set(8, 1, e_l); c.set(8, 0, e_l)
    c.vline(6, 2, 10, d)
    c.vline(10, 2, 10, d)
    c.set(6, 1, d); c.set(9, 0, d)
    c.rect(4, 11, 12, 11, dk)
    c.rect(5, 12, 11, 12, m)
    c.hline(4, 12, 11, FIRE[4] if glow else h)
    draw_handle(c, 6, 13, 14)
    c.rect(6, 15, 10, 15, dk)
    c.hline(6, 10, 15, d)
    c.set(7, 15, FIRE[5] if glow else h)
    c.set(8, 15, FIRE[5] if glow else co)
    if glow:
        c.set(8, 5, FIRE[6]); c.set(7, 7, FIRE[5]); c.set(9, 4, FIRE[5])
    return c


def tool_pickaxe(c, fam, glow=False):
    d, dk, m, b, l, h, co = fam
    e_l = FIRE[5] if glow else h
    e_m = FIRE[4] if glow else l
    draw_handle(c, 7, 7, 14)
    pts = [(1, 6), (3, 3), (5, 2), (8, 2), (11, 2), (13, 3), (15, 6),
           (12, 6), (10, 5), (8, 4), (6, 5), (4, 6)]
    c.poly(pts, b)
    c.hline(5, 11, 2, e_l)
    c.set(4, 3, e_m); c.set(12, 3, e_m)
    c.hline(6, 10, 4, m)
    c.set(8, 3, d)
    c.outline(pts, d)
    c.set(7, 6, dk); c.set(8, 7, dk)
    if glow:
        c.set(8, 2, FIRE[6]); c.set(3, 4, FIRE[5]); c.set(13, 4, FIRE[5])
    return c


def tool_axe(c, fam, glow=False):
    d, dk, m, b, l, h, co = fam
    edge = FIRE[5] if glow else h
    draw_handle(c, 7, 6, 14)
    head = [(7, 3), (14, 3), (15, 6), (15, 9), (14, 11), (8, 11), (7, 9)]
    c.poly(head, b)
    c.hline(9, 14, 3, h)
    c.hline(9, 14, 4, l)
    c.vline(7, 3, 9, d)
    c.vline(15, 6, 9, edge)
    c.vline(14, 5, 10, FIRE[4] if glow else l)
    c.outline(head, d)
    c.set(8, 5, m); c.set(8, 8, m)
    if glow:
        c.set(15, 6, FIRE[6]); c.set(15, 8, FIRE[5]); c.set(10, 5, FIRE[4])
    return c


def tool_shovel(c, fam, glow=False):
    d, dk, m, b, l, h, co = fam
    draw_handle(c, 7, 6, 14)
    head = [(5, 2), (11, 2), (11, 6), (9, 7), (7, 7), (5, 6)]
    c.poly(head, b)
    c.hline(5, 11, 2, h)
    c.vline(5, 3, 6, l)
    c.vline(11, 3, 6, dk)
    c.hline(6, 10, 7, m)
    c.outline(head, d)
    c.set(8, 4, co)
    if glow:
        c.hline(5, 11, 3, FIRE[4]); c.set(8, 6, FIRE[5]); c.set(6, 3, FIRE[5])
    return c


def tool_hoe(c, fam, glow=False):
    d, dk, m, b, l, h, co = fam
    draw_handle(c, 7, 6, 14)
    head = [(3, 4), (13, 4), (13, 6), (9, 6), (9, 7), (7, 7), (7, 6), (3, 6)]
    c.poly(head, b)
    c.hline(3, 13, 4, h)
    c.hline(3, 13, 5, l)
    c.hline(3, 6, 6, dk)
    c.hline(9, 13, 6, dk)
    c.outline(head, d)
    if glow:
        c.hline(3, 12, 5, FIRE[4]); c.set(13, 5, FIRE[5]); c.set(4, 5, FIRE[5])
    return c


def tool_knife(c, fam, glow=False):
    d, dk, m, b, l, h, co = fam
    e_l = FIRE[6] if glow else h
    e_m = FIRE[5] if glow else l
    c.poly([(8, 3), (12, 3), (12, 9), (8, 9)], b)
    c.hline(8, 12, 3, e_l)
    c.vline(8, 4, 9, e_m)
    c.vline(12, 3, 9, dk)
    c.set(13, 3, e_l); c.set(13, 4, e_l)
    c.outline([(8, 3), (12, 3), (12, 9), (8, 9)], d)
    c.rect(6, 10, 12, 11, dk)
    c.hline(6, 12, 10, FIRE[4] if glow else m)
    draw_handle(c, 6, 11, 14)
    c.set(6, 15, d); c.set(7, 15, d)
    return c


def tool_scythe(c, fam, glow=False):
    d, dk, m, b, l, h, co = fam
    edge = FIRE[5] if glow else h
    # long handle (diagonal)
    c.line(3, 14, 9, 6, WOOD[3]); c.line(4, 14, 10, 6, WOOD[4])
    c.line(2, 14, 8, 6, WOOD[1]); c.line(5, 15, 10, 8, WOOD[1])
    c.set(3, 14, WOOD[0]); c.set(2, 14, WOOD[0])
    # grip wrap
    for i in range(3):
        c.set(3 + i, 13 - i, WOOD[1]); c.set(4 + i, 13 - i, WOOD[1])
    # big curved blade
    blade = [(6, 8), (8, 4), (11, 2), (14, 2), (15, 4), (14, 5), (12, 4), (10, 4), (8, 6)]
    c.poly(blade, b)
    c.line(6, 8, 9, 3, edge)
    c.line(9, 3, 15, 3, edge)
    c.outline(blade, d)
    c.set(15, 2, edge); c.set(13, 2, FIRE[4] if glow else h)
    if glow:
        c.set(10, 4, FIRE[5]); c.set(7, 6, FIRE[4])
    return c


def tool_mace(c, fam, glow=False):
    d, dk, m, b, l, h, co = fam
    draw_handle(c, 7, 7, 14)
    head = [(5, 2), (11, 2), (12, 4), (11, 6), (5, 6), (4, 4)]
    c.poly(head, b)
    c.hline(5, 11, 2, h)
    c.hline(5, 11, 3, l)
    c.outline(head, d)
    c.set(5, 1, d); c.set(8, 1, d); c.set(11, 1, d)
    c.set(6, 1, l); c.set(10, 1, l)
    c.hline(4, 12, 5, FIRE[3] if glow else m)
    c.set(4, 4, FIRE[5] if glow else m); c.set(12, 4, FIRE[5] if glow else m)
    c.set(7, 3, co); c.set(8, 4, co)
    c.set(7, 15, WOOD[1]); c.set(8, 15, WOOD[0])
    return c


def t_paxel():
    """16x16 hot-steel paxel: pick head + axe blade + shovel scoop combined."""
    c = Canvas(16, 16)
    d, dk, m, b, l, h, co = STEEL
    draw_handle(c, 7, 6, 14)
    pick = [(2, 6), (4, 4), (12, 4), (14, 6), (13, 7), (8, 5), (3, 7)]
    c.poly(pick, b)
    c.hline(4, 12, 4, h)
    c.set(3, 5, dk); c.set(13, 5, dk); c.set(2, 6, dk); c.set(14, 6, dk)
    c.outline(pick, d)
    axe = [(7, 4), (13, 4), (14, 7), (13, 9), (8, 9), (7, 8)]
    c.poly(axe, FIRE[3])
    c.hline(8, 13, 4, FIRE[5]); c.vline(13, 5, 8, FIRE[5])
    c.outline(axe, FIRE[1])
    scoop = [(2, 6), (6, 6), (6, 9), (5, 10), (3, 10), (2, 9)]
    c.poly(scoop, b)
    c.hline(3, 5, 6, l)
    c.outline(scoop, d)
    c.set(8, 5, FIRE[6])
    return c


def t_tool(kind, fam, glow=False):
    c = Canvas(16, 16)
    {"sword": tool_sword, "pickaxe": tool_pickaxe, "axe": tool_axe,
     "shovel": tool_shovel, "hoe": tool_hoe, "knife": tool_knife,
     "scythe": tool_scythe, "mace": tool_mace}[kind](c, fam, glow)
    return c


# ===========================================================================
# Ingots / nuggets / ore bits
# ===========================================================================
def t_ingot(fam, glow=False):
    return draw_ingot(Canvas(16, 16), fam, glow)


def t_nugget(fam, glow=False):
    c = Canvas(16, 16)
    d, dk, m, b, l, h, co = fam
    pts = [(5, 8), (6, 6), (9, 5), (12, 6), (13, 8), (12, 11), (9, 12), (6, 11)]
    c.poly(pts, b)
    c.poly([(7, 7), (9, 6), (11, 7), (9, 8)], l)
    c.poly([(8, 8), (9, 7), (10, 8), (9, 9)], h)
    c.outline(pts, d)
    c.set(5, 11, dk); c.set(12, 11, dk)
    if glow:
        c.set(9, 7, FIRE[6]); c.set(11, 8, FIRE[4]); c.set(7, 9, FIRE[4])
    return c


def t_molten_shard():
    c = Canvas(16, 16)
    shard = [(4, 12), (3, 7), (6, 3), (10, 2), (13, 6), (12, 11), (8, 14)]
    c.poly(shard, CHAR[2])
    c.poly([(5, 11), (5, 7), (7, 4), (10, 3), (12, 7), (11, 10), (8, 12)], CHAR[3])
    c.outline(shard, CHAR[0])
    c.poly([(7, 6), (10, 6), (11, 9), (9, 11), (6, 9)], FIRE[3])
    c.poly([(8, 7), (10, 7), (10, 9), (8, 10)], FIRE[4])
    c.set(9, 8, FIRE[5]); c.set(8, 8, FIRE[6])
    c.line(6, 5, 7, 7, FIRE[4]); c.line(11, 6, 10, 8, FIRE[4])
    c.set(3, 4, FIRE[5]); c.set(13, 3, FIRE[4]); c.set(4, 13, FIRE[4])
    return c


def t_forge_heart():
    c = Canvas(16, 16)
    heart = [(8, 14), (2, 8), (2, 5), (4, 3), (6, 3), (8, 5),
             (10, 3), (12, 3), (14, 5), (14, 8)]
    c.poly(heart, FIRE[2])
    inner1 = [(8, 12), (4, 8), (4, 5), (5, 4), (6, 4), (8, 6),
              (10, 4), (11, 4), (12, 5), (12, 8)]
    c.poly(inner1, FIRE[3])
    inner2 = [(8, 11), (5, 8), (5, 6), (6, 5), (8, 7), (10, 5), (11, 6), (11, 8)]
    c.poly(inner2, FIRE[4])
    inner3 = [(8, 10), (6, 8), (6, 7), (8, 8), (10, 7), (10, 8)]
    c.poly(inner3, FIRE[5])
    c.set(8, 9, FIRE[6]); c.set(7, 9, FIRE[5]); c.set(9, 9, FIRE[5])
    c.outline(heart, FIRE[1])
    for (dx, dy) in [(1, 4), (14, 4), (1, 8), (14, 8), (4, 13),
                     (11, 13), (8, 15), (2, 11), (13, 11)]:
        c.set(dx, dy, GOLD[4]); c.set(dx, dy + 1, GOLD[2])
    return c


def t_molten_core():
    c = Canvas(16, 16)
    shell = [(4, 6), (6, 3), (10, 3), (12, 6), (12, 10), (10, 13), (6, 13), (4, 10)]
    c.poly(shell, CHAR[2])
    c.poly([(5, 7), (7, 4), (9, 4), (11, 7), (11, 10), (9, 12), (7, 12), (5, 10)], CHAR[3])
    c.outline(shell, CHAR[0])
    heart = [(6, 6), (8, 5), (10, 6), (10, 9), (8, 11), (6, 9)]
    c.poly(heart, FIRE[3])
    c.poly([(7, 7), (8, 6), (9, 7), (8, 9)], FIRE[4])
    c.poly([(8, 8), (9, 8), (9, 9), (8, 9)], FIRE[5])
    c.set(8, 7, FIRE[6])
    c.line(6, 4, 4, 6, FIRE[4]); c.line(10, 4, 12, 6, FIRE[4])
    c.set(3, 3, FIRE[5]); c.set(13, 3, FIRE[5])
    c.set(2, 8, FIRE[3]); c.set(14, 9, FIRE[3])
    c.set(5, 14, FIRE[4]); c.set(11, 14, FIRE[4])
    return c


# ===========================================================================
# Books / pages
# ===========================================================================
def t_hot_steel_codex():
    c = Canvas(16, 16)
    cover = [(3, 2), (12, 2), (13, 3), (13, 14), (3, 14), (2, 13), (2, 3)]
    c.poly(cover, CHAR[2])
    c.poly([(3, 3), (12, 3), (12, 13), (3, 13)], CHAR[3])
    c.rect(2, 2, 4, 14, CHAR[1])
    c.vline(4, 3, 13, CHAR[0])
    c.rect(13, 4, 14, 13, PARCH[3]); c.vline(14, 4, 13, PARCH[2])
    c.rect(7, 6, 11, 6, FIRE[4]); c.rect(8, 7, 10, 7, FIRE[3]); c.set(9, 6, FIRE[6])
    c.hline(6, 11, 9, FIRE[5])
    c.vline(9, 10, 12, GOLD[5])
    c.set(12, 8, GOLD[5]); c.set(12, 9, GOLD[4])
    c.set(3, 3, GOLD[5]); c.set(3, 13, GOLD[5])
    c.outline(cover, CHAR[0])
    return c


def t_scorched_page():
    c = Canvas(16, 16)
    page = [(3, 2), (12, 2), (13, 4), (12, 13), (4, 14), (3, 12), (2, 8), (3, 5)]
    c.poly(page, PARCH[3])
    c.poly([(4, 3), (11, 3), (12, 5), (11, 12), (5, 13), (4, 11), (3, 8), (4, 5)], PARCH[4])
    c.outline(page, CHAR[1])
    c.poly([(3, 2), (6, 2), (4, 4), (3, 4)], CHAR[2])
    c.poly([(12, 13), (13, 12), (12, 11), (11, 13)], CHAR[2])
    c.set(6, 5, FIRE[4]); c.set(8, 5, FIRE[5]); c.set(10, 6, FIRE[4])
    c.vline(7, 7, 10, FIRE[3]); c.hline(6, 9, 9, FIRE[4])
    c.set(8, 11, FIRE[5]); c.set(6, 10, FIRE[4])
    c.hline(5, 10, 7, PARCH[1]); c.hline(5, 11, 9, PARCH[0])
    c.set(9, 2, CHAR[2]); c.set(11, 4, CHAR[2])
    return c


# ===========================================================================
# Armour items
# ===========================================================================
def armor_helmet(c, fam, glow=False):
    d, dk, m, b, l, h, co = fam
    dome = [(2, 9), (3, 5), (5, 3), (8, 2), (11, 3), (13, 5), (14, 9), (14, 11), (2, 11)]
    c.poly(dome, b)
    c.hline(4, 11, 4, h)
    c.set(8, 2, co)
    c.line(3, 5, 5, 3, l); c.line(11, 3, 13, 5, l)
    c.rect(5, 7, 11, 10, TRANSPARENT)
    c.outline(dome, d)
    c.vline(8, 7, 11, dk); c.set(8, 7, m)
    c.hline(2, 14, 11, d); c.hline(2, 14, 10, m)
    c.set(4, 9, co); c.set(12, 9, co)
    c.hline(6, 10, 3, co)
    if glow:
        c.hline(5, 7, 8, FIRE[5]); c.hline(9, 11, 8, FIRE[5])
        c.hline(3, 13, 10, FIRE[4]); c.set(8, 2, FIRE[6])
    return c


def armor_chestplate(c, fam, glow=False):
    d, dk, m, b, l, h, co = fam
    body = [(3, 4), (5, 3), (7, 4), (9, 4), (11, 3), (13, 4), (13, 14), (3, 14)]
    c.poly(body, b)
    c.rect(3, 3, 5, 5, l); c.rect(11, 3, 13, 5, l)
    c.poly([(7, 4), (9, 4), (9, 6), (7, 6)], dk)
    c.vline(8, 6, 13, d)
    c.vline(4, 6, 13, h); c.vline(5, 6, 13, l)
    c.vline(11, 6, 13, dk)
    c.hline(3, 13, 13, d); c.hline(3, 13, 12, m)
    c.set(4, 7, co); c.set(11, 7, co); c.set(4, 10, co); c.set(11, 10, co)
    c.outline(body, d)
    if glow:
        c.vline(8, 6, 12, FIRE[4])
        c.hline(5, 11, 8, FIRE[5]); c.set(8, 9, FIRE[6])
        c.hline(3, 5, 4, FIRE[4]); c.hline(11, 13, 4, FIRE[4])
    return c


def armor_leggings(c, fam, glow=False):
    d, dk, m, b, l, h, co = fam
    pts = [(3, 3), (13, 3), (13, 14), (9, 14), (8, 8), (7, 14), (3, 14)]
    c.poly(pts, b)
    c.hline(3, 13, 3, l); c.hline(3, 13, 4, d)
    c.set(7, 3, co); c.set(8, 3, co)
    c.vline(4, 5, 13, h); c.vline(5, 5, 13, l)
    c.vline(10, 5, 13, dk); c.vline(11, 5, 13, m)
    c.vline(8, 8, 14, d)
    c.set(5, 10, co); c.set(10, 10, co)
    c.outline(pts, d)
    if glow:
        c.vline(5, 6, 13, FIRE[4]); c.vline(11, 6, 13, FIRE[4])
        c.hline(3, 13, 4, FIRE[5])
    return c


def armor_boots(c, fam, glow=False):
    d, dk, m, b, l, h, co = fam
    pts = [(3, 5), (8, 5), (8, 10), (13, 10), (13, 14), (3, 14)]
    c.poly(pts, b)
    c.vline(4, 6, 10, h); c.vline(5, 6, 10, l)
    c.hline(3, 8, 5, d); c.hline(3, 8, 6, m)
    c.hline(7, 13, 10, d); c.hline(7, 13, 9, l)
    c.hline(3, 13, 13, d); c.hline(3, 13, 14, d)
    c.vline(3, 8, 14, d)
    c.set(12, 11, co); c.set(12, 12, h)
    c.outline(pts, d)
    if glow:
        c.hline(3, 8, 6, FIRE[5]); c.hline(8, 13, 9, FIRE[4])
    return c


def t_armor(kind, fam, glow=False):
    c = Canvas(16, 16)
    {"helmet": armor_helmet, "chestplate": armor_chestplate,
     "leggings": armor_leggings, "boots": armor_boots}[kind](c, fam, glow)
    return c


# ===========================================================================
# Ranged weapons
# ===========================================================================
def t_bow(pull=0):
    c = Canvas(16, 16)
    d, dk, m, b, l, h, co = WOOD
    pts = [(13, 1), (10, 3), (8, 6), (8, 10), (10, 13), (13, 15)]
    for i in range(len(pts) - 1):
        c.line(*pts[i], *pts[i + 1], b)
        c.line(pts[i][0] + 1, pts[i][1], pts[i + 1][0] + 1, pts[i + 1][1], dk)
    hl = [(13, 2), (11, 4), (9, 7), (9, 9), (11, 12), (13, 14)]
    for i in range(len(hl) - 1):
        c.line(*hl[i], *hl[i + 1], l)
    c.set(13, 0, m); c.set(13, 15, d)
    c.rect(7, 7, 8, 9, dk); c.set(7, 7, b)
    sx = 8 - pull
    sy = 8 + (1 if pull >= 2 else 0)
    c.line(13, 1, sx, sy, STRING[3])
    c.line(13, 15, sx, sy, STRING[3])
    if pull >= 1:
        c.line(13, 2, sx, sy, STRING[4])
    if pull >= 2:
        c.line(sx - 1, 8, 15, 8, WOOD[2])
        c.line(sx - 1, 8, 15, 8, WOOD[3])
        c.set(15, 8, STEEL[5]); c.set(14, 7, STEEL[4]); c.set(14, 9, STEEL[2])
        c.set(4, 7, STRING[2]); c.set(4, 9, STRING[2])
    return c


def _crossbow_base(c):
    d, dk, m, b, l, h, co = WOOD
    c.rect(2, 7, 13, 8, b)
    c.hline(2, 13, 7, l); c.hline(2, 13, 8, dk)
    c.outline([(2, 7), (13, 7), (13, 8), (2, 8)], d)
    c.line(2, 7, 1, 4, m); c.hline(1, 3, 4, m); c.set(1, 4, dk)
    c.line(2, 8, 1, 11, m); c.hline(1, 3, 11, m); c.set(1, 11, dk)
    c.set(2, 4, l); c.set(2, 11, l)


def t_crossbow(pull=None):
    c = Canvas(16, 16)
    _crossbow_base(c)
    if pull is None:
        c.vline(1, 4, 11, STRING[3]); c.set(1, 7, STRING[4])
    else:
        px = 6 - pull
        c.line(1, 4, px, 7, STRING[3])
        c.line(1, 11, px, 8, STRING[3])
    c.set(11, 9, WOOD[1]); c.set(11, 10, WOOD[0])
    if pull == 2:
        c.line(4, 7, 14, 7, WOOD[3]); c.line(4, 8, 14, 8, WOOD[2])
        c.set(14, 7, STEEL[5]); c.set(13, 6, STEEL[4]); c.set(13, 8, STEEL[2])
        c.set(4, 6, STRING[2]); c.set(4, 9, STRING[2])
    return c


def t_crossbow_arrow():
    c = Canvas(16, 16)
    _crossbow_base(c)
    c.vline(1, 4, 11, STRING[3])
    c.line(3, 7, 14, 7, WOOD[3]); c.line(3, 8, 14, 8, WOOD[2])
    c.set(14, 7, STEEL[5]); c.set(13, 6, STEEL[4]); c.set(13, 8, STEEL[2])
    c.set(4, 6, STRING[2]); c.set(5, 6, STRING[2])
    c.set(4, 9, STRING[2]); c.set(5, 9, STRING[2])
    return c


def t_crossbow_firework():
    c = Canvas(16, 16)
    _crossbow_base(c)
    c.vline(1, 4, 11, STRING[3])
    c.rect(11, 4, 13, 9, FIRE[3])
    c.hline(11, 13, 4, FIRE[5]); c.hline(11, 13, 9, FIRE[1])
    c.vline(11, 4, 9, FIRE[4]); c.vline(13, 4, 9, FIRE[1])
    c.set(12, 3, FIRE[5]); c.set(13, 3, FIRE[4])
    c.set(10, 5, FIRE[1]); c.set(10, 8, FIRE[1])
    c.set(12, 10, FIRE[6])
    return c


# ===========================================================================
# Trident / shield / arrows
# ===========================================================================
def _trident(c, scale=1):
    # shaft (glowing hot steel)
    c.vline(7, 5, 14, FIRE[3])
    c.vline(8, 5, 14, FIRE[4])
    c.vline(9, 5, 14, FIRE[2])
    # prongs
    c.vline(8, 2, 5, FIRE[5]); c.set(8, 1, FIRE[6])
    c.vline(5, 4, 6, FIRE[4]); c.line(5, 4, 8, 4, FIRE[4]); c.set(5, 3, FIRE[5])
    c.vline(11, 4, 6, FIRE[4]); c.line(8, 4, 11, 4, FIRE[4]); c.set(11, 3, FIRE[5])
    c.hline(5, 11, 5, FIRE[2]); c.hline(5, 11, 6, FIRE[3])
    c.set(5, 2, STEEL[5]); c.set(11, 2, STEEL[5]); c.set(8, 0, STEEL[5])
    c.set(7, 8, FIRE[5]); c.set(9, 10, FIRE[5]); c.set(8, 12, FIRE[4])
    for y in (11, 13):
        c.hline(7, 9, y, FIRE[1])
    return c


def t_trident_item():
    return _trident(Canvas(16, 16))


def t_trident_entity():
    c = Canvas(32, 32)
    c.vline(15, 4, 30, FIRE[3])
    c.vline(16, 4, 30, FIRE[4])
    c.vline(17, 4, 30, FIRE[2])
    c.vline(16, 1, 4, FIRE[5]); c.set(16, 0, FIRE[6])
    c.vline(10, 3, 6, FIRE[4]); c.line(10, 3, 16, 3, FIRE[4]); c.set(10, 2, FIRE[5])
    c.vline(22, 3, 6, FIRE[4]); c.line(16, 3, 22, 3, FIRE[4]); c.set(22, 2, FIRE[5])
    c.hline(10, 22, 4, FIRE[2]); c.hline(10, 22, 5, FIRE[3])
    c.set(10, 1, STEEL[5]); c.set(22, 1, STEEL[5]); c.set(16, 0, STEEL[6])
    c.set(15, 10, FIRE[5]); c.set(17, 14, FIRE[5]); c.set(16, 20, FIRE[4])
    for y in (12, 18, 25):
        c.hline(15, 17, y, FIRE[1])
    c.set(15, 28, FIRE[5]); c.set(17, 28, FIRE[4])
    return c


def t_shield():
    c = Canvas(16, 16)
    d, dk, m, b, l, h, co = STEEL
    pts = [(3, 3), (13, 3), (13, 8), (8, 14), (3, 8)]
    c.poly(pts, b)
    c.line(4, 4, 8, 4, h); c.line(3, 4, 3, 8, l)
    c.outline(pts, d)
    c.rect(7, 5, 10, 9, l)
    c.rect(8, 6, 9, 8, h)
    c.outline([(7, 5), (10, 5), (10, 9), (7, 9)], d)
    c.set(8, 7, FIRE[5])
    # glowing cross emblem
    c.vline(8, 4, 12, FIRE[3]); c.hline(5, 11, 7, FIRE[3])
    c.vline(8, 4, 6, FIRE[4]); c.hline(5, 6, 7, FIRE[5])
    for (rx, ry) in [(4, 4), (12, 4), (4, 7), (12, 7)]:
        rivet(c, rx, ry, STEEL)
    return c


def t_hot_steel_arrow():
    c = Canvas(16, 16)
    c.line(3, 13, 10, 6, WOOD[3]); c.line(4, 13, 11, 6, WOOD[4])
    c.line(2, 14, 9, 7, WOOD[1])
    c.line(10, 6, 14, 2, STEEL[5])
    c.line(14, 2, 13, 7, FIRE[2])
    c.line(10, 6, 13, 7, FIRE[3])
    c.set(14, 2, FIRE[6]); c.set(14, 3, FIRE[5]); c.set(13, 3, FIRE[5])
    c.set(2, 13, STRING[3]); c.set(3, 14, STRING[2]); c.set(2, 14, STRING[2])
    c.set(1, 12, STEEL[3]); c.set(2, 12, STRING[3]); c.set(3, 12, STRING[2])
    c.set(3, 13, STRING[2]); c.set(4, 12, STRING[2])
    return c


def t_hot_steel_arrow_entity():
    c = Canvas(32, 32)
    c.line(6, 26, 20, 12, WOOD[3]); c.line(7, 26, 21, 12, WOOD[4])
    c.line(5, 27, 19, 13, WOOD[1])
    c.line(20, 12, 28, 4, STEEL[5])
    c.line(28, 4, 26, 14, FIRE[2])
    c.line(20, 12, 26, 14, FIRE[3])
    c.set(28, 4, FIRE[6]); c.set(28, 5, FIRE[5]); c.set(26, 5, FIRE[5])
    c.set(27, 6, FIRE[5]); c.set(24, 8, FIRE[3]); c.set(25, 9, FIRE[3])
    for (fx, fy) in [(3, 26), (5, 28), (2, 24), (4, 27)]:
        c.set(fx, fy, STRING[2])
    c.set(4, 26, STRING[3]); c.set(5, 27, STRING[3]); c.set(3, 25, STRING[3])
    c.set(6, 28, STRING[2]); c.set(7, 27, STRING[2])
    return c


# ===========================================================================
# Misc items
# ===========================================================================
def t_fishing_rod():
    c = Canvas(16, 16)
    d, dk, m, b, l, h, co = STEEL
    c.line(4, 15, 14, 5, b); c.line(5, 15, 15, 5, l)
    c.line(3, 15, 13, 5, dk)
    c.set(4, 14, FIRE[3]); c.set(5, 13, FIRE[4]); c.set(4, 13, FIRE[2])
    c.line(14, 6, 9, 11, STRING[3])
    c.rect(8, 10, 10, 12, FIRE[3])
    c.set(9, 10, FIRE[5]); c.hline(8, 10, 12, FIRE[1]); c.set(8, 11, FIRE[2])
    c.rect(2, 10, 4, 12, WOOD[1]); c.set(3, 11, WOOD[4])
    return c


def t_sickle():
    c = Canvas(16, 16)
    d, dk, m, b, l, h, co = STEEL
    draw_handle(c, 5, 10, 14)
    arc = [(11, 2), (13, 4), (14, 7), (14, 9), (13, 11), (11, 13), (9, 14),
           (8, 13), (10, 12), (12, 10), (13, 8), (13, 6), (12, 4), (10, 3)]
    c.poly(arc, b)
    c.outline(arc, d)
    inner = [(10, 3), (12, 4), (13, 6), (13, 8), (12, 10), (10, 12),
             (9, 12), (10, 10), (11, 8), (11, 6), (10, 4)]
    c.poly(inner, l)
    c.line(11, 4, 12, 8, FIRE[4]); c.line(12, 8, 11, 11, FIRE[3])
    c.set(11, 2, FIRE[5]); c.set(12, 3, FIRE[4])
    return c


def t_apple():
    c = Canvas(16, 16)
    body = [(5, 5), (6, 3), (8, 2), (11, 3), (12, 5), (13, 8),
            (12, 11), (10, 13), (6, 13), (4, 11), (3, 8)]
    c.poly(body, RED[3])
    c.poly([(6, 5), (8, 4), (11, 5), (12, 8), (11, 11), (8, 12), (5, 11), (4, 8)], FIRE[3])
    c.poly([(7, 7), (9, 6), (10, 8), (9, 10), (7, 10), (6, 8)], FIRE[4])
    c.poly([(8, 8), (9, 8), (9, 9), (8, 9)], FIRE[5])
    c.set(8, 8, FIRE[6])
    c.poly([(3, 9), (4, 12), (6, 13), (8, 13), (5, 12), (4, 10)], RED[1])
    c.outline(body, RED[0])
    c.line(8, 2, 8, 1, WOOD[1]); c.set(8, 1, WOOD[3]); c.set(7, 2, WOOD[3])
    c.poly([(9, 2), (12, 1), (13, 3), (11, 4)], GREEN[2])
    c.set(10, 2, GREEN[4])
    return c


def t_fireball():
    c = Canvas(16, 16)
    flame = [(8, 1), (12, 3), (14, 7), (13, 11), (9, 14), (5, 14), (2, 10), (3, 5)]
    c.poly(flame, FIRE[1])
    c.poly([(5, 4), (11, 4), (13, 8), (11, 12), (5, 12), (3, 8)], FIRE[3])
    c.poly([(7, 6), (10, 6), (11, 9), (9, 11), (6, 11), (5, 8)], FIRE[4])
    c.poly([(8, 7), (9, 7), (9, 9), (8, 10), (7, 9), (7, 8)], FIRE[5])
    c.set(8, 8, FIRE[6]); c.set(9, 8, FIRE[6])
    c.outline(flame, FIRE[0])
    c.set(5, 2, FIRE[4]); c.set(11, 2, FIRE[4])
    c.set(2, 8, FIRE[3]); c.set(14, 9, FIRE[3])
    c.set(8, 15, FIRE[4]); c.set(6, 15, FIRE[3])
    c.set(3, 3, FIRE[5]); c.set(13, 4, FIRE[5])
    return c


def t_lava_bottle():
    c = Canvas(16, 16)
    body = [(5, 5), (11, 5), (11, 13), (5, 13)]
    c.poly(body, BOTTLE_BODY)
    c.outline(body, BOTTLE_EDGE)
    c.rect(6, 2, 10, 4, BOTTLE_BODY)
    c.vline(6, 2, 4, BOTTLE_EDGE); c.vline(10, 2, 4, BOTTLE_EDGE)
    c.hline(6, 10, 2, BOTTLE_EDGE)
    c.rect(6, 1, 10, 2, WOOD[3]); c.hline(6, 10, 1, WOOD[4])
    c.rect(6, 6, 10, 12, FIRE[3])
    c.hline(6, 10, 6, FIRE[5]); c.hline(6, 10, 12, FIRE[1])
    c.set(7, 8, FIRE[6]); c.set(9, 10, FIRE[6]); c.set(8, 7, FIRE[5])
    c.vline(5, 6, 10, GLASS_SHINE)
    c.set(3, 6, FIRE[4]); c.set(12, 5, FIRE[5])
    return c


def t_effect_icon():
    c = Canvas(16, 16)
    flame = [(8, 1), (11, 4), (12, 8), (11, 11), (8, 14), (5, 11), (4, 8), (5, 4)]
    c.poly(flame, FIRE[3])
    c.poly([(8, 3), (10, 6), (10, 9), (8, 12), (6, 9), (6, 6)], FIRE[4])
    c.poly([(8, 5), (9, 7), (9, 9), (8, 11), (7, 9), (7, 7)], FIRE[5])
    c.set(8, 7, FIRE[6]); c.set(8, 8, FIRE[6])
    c.outline(flame, FIRE[1])
    drop = [(8, 7), (11, 10), (11, 12), (8, 14), (5, 12), (5, 10)]
    c.poly(drop, QUENCH[3])
    c.poly([(8, 9), (9, 11), (9, 12), (8, 13), (7, 12), (7, 11)], QUENCH[4])
    c.outline(drop, QUENCH[0])
    c.set(2, 3, FIRE[5]); c.set(13, 3, FIRE[5])
    c.set(2, 12, FIRE[5]); c.set(13, 12, FIRE[5])
    return c


def t_forge_blessing():
    """18x18 mob effect icon: a hammer & anvil silhouette wrapped in flame."""
    c = Canvas(18, 18)
    flame = [(9, 1), (13, 4), (16, 9), (15, 14), (9, 17), (3, 14), (2, 9), (5, 4)]
    c.poly(flame, FIRE[2])
    c.poly([(9, 3), (12, 6), (14, 10), (12, 14), (9, 16), (6, 14), (4, 10), (6, 6)], FIRE[3])
    c.poly([(9, 5), (11, 8), (12, 11), (10, 14), (9, 15), (8, 14), (6, 11), (7, 8)], FIRE[4])
    c.outline(flame, FIRE[0])
    anvil = [(5, 12), (13, 12), (13, 13), (12, 13), (12, 15), (6, 15), (6, 13), (5, 13)]
    c.poly(anvil, CHAR[1])
    c.hline(6, 12, 12, CHAR[3]); c.hline(6, 12, 15, CHAR[0])
    c.rect(8, 6, 9, 11, CHAR[1])
    c.rect(6, 5, 11, 7, CHAR[2])
    c.hline(6, 11, 5, CHAR[4]); c.hline(6, 11, 7, CHAR[0])
    c.set(1, 9, FIRE[5]); c.set(16, 8, FIRE[5])
    c.set(4, 2, FIRE[4]); c.set(14, 2, FIRE[4])
    return c


# ===========================================================================
# Blocks
# ===========================================================================
def t_block(fam):
    c = Canvas(16, 16)
    for y in range(16):
        for x in range(16):
            c.set(x, y, noise_shade(fam, x, y, seed=3, amp=0.22))
    c.hline(0, 15, 0, sh(fam, 0.6)); c.hline(0, 15, 1, sh(fam, 0.3))
    c.vline(0, 0, 15, sh(fam, 0.5)); c.vline(1, 1, 15, sh(fam, 0.25))
    c.hline(0, 15, 15, fam[0]); c.hline(0, 15, 14, sh(fam, -0.35))
    c.vline(15, 0, 15, fam[0]); c.vline(14, 1, 15, sh(fam, -0.3))
    for (ox, oy) in [(3, 3), (9, 3), (3, 9), (9, 9)]:
        c.rect(ox, oy, ox + 3, oy + 3, sh(fam, 0.25))
        c.hline(ox, ox + 3, oy, fam[5])
        c.hline(ox, ox + 3, oy + 3, fam[0])
        c.vline(ox, oy, oy + 3, fam[0])
        c.vline(ox + 3, oy, oy + 3, sh(fam, -0.5))
    c.set(7, 7, fam[5]); c.set(8, 7, fam[4]); c.set(7, 8, fam[4]); c.set(8, 8, fam[6])
    for (rx, ry) in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        c.set(rx, ry, fam[5]); c.set(rx, ry + 1, fam[2])
    return c


def t_molten_block(ingot_fam, seam_fam):
    c = Canvas(16, 16)
    for y in range(16):
        for x in range(16):
            c.set(x, y, noise_shade(CHAR, x, y, seed=5, amp=0.3))
    for x in range(1, 16, 4):
        c.vline(x, 1, 14, sh(seam_fam, 0.1))
    for y in range(1, 16, 4):
        c.hline(1, 14, y, sh(seam_fam, -0.1))
    for (ox, oy) in [(2, 2), (9, 2), (2, 9), (9, 9)]:
        c.rect(ox, oy, ox + 3, oy + 3, ingot_fam[4])
        c.hline(ox, ox + 3, oy, ingot_fam[6])
        c.hline(ox, ox + 3, oy + 3, ingot_fam[1])
        c.vline(ox, oy, oy + 3, ingot_fam[5])
        c.vline(ox + 3, oy, oy + 3, ingot_fam[1])
        c.set(ox + 1, oy + 1, ingot_fam[6]); c.set(ox + 2, oy + 2, ingot_fam[4])
    c.hline(0, 15, 0, FIRE[5]); c.vline(0, 0, 15, FIRE[5])
    c.hline(0, 15, 15, FIRE[1]); c.vline(15, 0, 15, FIRE[1])
    c.hline(0, 15, 1, FIRE[4]); c.vline(1, 1, 14, FIRE[4])
    return c


def t_hot_block():
    return t_molten_block(FIRE, FIRE)


def t_molten_steel_block():
    return t_molten_block(GOLD, FIRE)


def _furnace(bubbles=False):
    c = Canvas(16, 16)
    for y in range(16):
        for x in range(16):
            c.set(x, y, noise_shade(CHAR, x, y, seed=9, amp=0.28))
    c.hline(0, 15, 0, STEEL[5]); c.vline(0, 0, 15, STEEL[4])
    c.hline(0, 15, 15, CHAR[0]); c.vline(15, 0, 15, CHAR[0])
    c.hline(0, 15, 1, STEEL[3]); c.vline(1, 1, 14, STEEL[3])
    c.hline(0, 15, 14, CHAR[1]); c.vline(14, 1, 14, CHAR[1])
    for (rx, ry) in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        c.set(rx, ry, STEEL[6]); c.set(rx, ry + 1, STEEL[2])
    c.rect(3, 4, 12, 13, CHAR[1])
    c.hline(3, 12, 4, CHAR[0])
    c.rect(4, 6, 11, 12, FIRE[2])
    c.rect(4, 6, 11, 7, FIRE[4])
    c.rect(5, 8, 10, 11, FIRE[3])
    c.hline(4, 11, 6, FIRE[5])
    if bubbles:
        c.set(6, 8, FIRE[6]); c.set(9, 9, FIRE[6]); c.set(7, 11, FIRE[5]); c.set(10, 7, FIRE[5])
    else:
        c.set(5, 9, FIRE[5]); c.set(10, 10, FIRE[5]); c.set(7, 8, FIRE[6])
    c.set(6, 3, FIRE[4]); c.set(8, 3, FIRE[5]); c.set(10, 3, FIRE[4]); c.set(7, 2, FIRE[3])
    return c


def t_forge():
    return _furnace(False)


def t_smelter():
    return _furnace(True)


def t_bricks(fam):
    c = Canvas(16, 16)
    mortar = sh(fam, -0.9)
    c.rect(0, 0, 15, 15, mortar)
    for row in range(4):
        y0 = row * 4
        offset = 0 if row % 2 == 0 else 4
        x = -offset
        while x < 16:
            bx = x
            c.rect(bx, y0, bx + 7, y0 + 3, sh(fam, -0.15))
            c.hline(bx, bx + 7, y0, sh(fam, 0.25))
            c.hline(bx, bx + 7, y0 + 3, sh(fam, -0.6))
            c.vline(bx, y0, y0 + 3, sh(fam, -0.55))
            c.set(bx + 1, y0 + 1, sh(fam, 0.1))
            x += 8
    for (x, y) in [(2, 2), (10, 6), (2, 10), (10, 14), (6, 2), (14, 6)]:
        c.set(x, y, sh(fam, 0.35))
    return c


def t_hot_steel_bricks():
    return t_bricks(FIRE)


def t_charred_bricks():
    c = Canvas(16, 16)
    mortar = CHAR[0]
    c.rect(0, 0, 15, 15, mortar)
    for row in range(4):
        y0 = row * 4
        offset = 0 if row % 2 == 0 else 4
        x = -offset
        while x < 16:
            bx = x
            c.rect(bx, y0, bx + 7, y0 + 3, noise_shade(CHAR, bx, y0, seed=11, amp=0.2))
            c.hline(bx, bx + 7, y0, CHAR[4])
            c.hline(bx, bx + 7, y0 + 3, CHAR[0])
            c.vline(bx, y0, y0 + 3, CHAR[0])
            c.set(bx + 1, y0 + 1, CHAR[5])
            x += 8
    # faint ember seams
    c.set(3, 4, FIRE[2]); c.set(11, 4, FIRE[2])
    c.set(7, 8, FIRE[3]); c.set(15, 8, FIRE[2])
    c.set(3, 12, FIRE[2]); c.set(11, 12, FIRE[3])
    return c


def t_door_item():
    c = Canvas(16, 16)
    d, dk, m, b, l, h, co = STEEL
    panel(c, 2, 1, 13, 15, STEEL)
    for y in (5, 10):
        c.hline(4, 11, y, dk); c.hline(4, 11, y - 1, b)
    c.hline(4, 11, 7, FIRE[3]); c.hline(4, 11, 8, FIRE[4])
    for (rx, ry) in [(4, 3), (11, 3), (4, 13), (11, 13)]:
        c.set(rx, ry, FIRE[5]); c.set(rx, ry + 1, FIRE[2])
    c.rect(10, 7, 11, 9, FIRE[4]); c.set(10, 7, FIRE[5])
    return c


def t_door_top():
    c = Canvas(16, 16)
    panel(c, 2, 1, 13, 15, STEEL)
    c.hline(4, 11, 5, STEEL[1]); c.hline(4, 11, 9, STEEL[1])
    c.vline(7, 5, 9, STEEL[1])
    c.hline(4, 11, 10, FIRE[3]); c.hline(4, 11, 11, FIRE[4])
    c.set(4, 3, FIRE[5]); c.set(11, 3, FIRE[5])
    return c


def t_door_bottom():
    c = Canvas(16, 16)
    panel(c, 2, 1, 13, 15, STEEL)
    c.hline(4, 11, 4, STEEL[1])
    for y in (6, 10, 13):
        c.hline(4, 11, y, STEEL[3]); c.hline(4, 11, y + 1, STEEL[1])
    c.hline(4, 11, 5, FIRE[3]); c.hline(4, 11, 6, FIRE[4])
    for (rx, ry) in [(4, 3), (11, 3), (4, 13), (11, 13)]:
        c.set(rx, ry, FIRE[5]); c.set(rx, ry + 1, FIRE[2])
    return c


def t_trapdoor():
    c = Canvas(16, 16)
    for y in range(16):
        for x in range(16):
            c.set(x, y, noise_shade(STEEL, x, y, seed=13, amp=0.25))
    for x in (3, 7, 11):
        c.vline(x, 0, 15, STEEL[1]); c.vline(x + 1, 0, 15, STEEL[3])
    c.hline(1, 14, 6, FIRE[3]); c.hline(1, 14, 7, FIRE[4])
    c.hline(1, 14, 9, FIRE[3]); c.hline(1, 14, 10, FIRE[4])
    c.hline(0, 15, 0, STEEL[5]); c.hline(0, 15, 15, STEEL[0])
    c.vline(0, 0, 15, STEEL[4]); c.vline(15, 0, 15, STEEL[0])
    for (rx, ry) in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        c.set(rx, ry, FIRE[5]); c.set(rx, ry + 1, FIRE[2])
    return c


def t_lantern(fam):
    c = Canvas(16, 16)
    d, dk, m, b, l, h, co = fam
    c.vline(7, 0, 2, dk); c.vline(8, 0, 2, dk); c.set(7, 0, h)
    c.hline(5, 10, 3, dk); c.set(5, 3, h); c.set(10, 3, h)
    c.hline(5, 10, 12, d)
    c.rect(4, 4, 11, 12, CHAR[1])
    c.vline(4, 4, 11, d); c.vline(11, 4, 11, d)
    c.hline(4, 11, 4, m); c.hline(4, 11, 11, d)
    c.rect(6, 6, 9, 9, FIRE[5])
    c.rect(7, 7, 8, 8, FIRE[6])
    c.hline(6, 9, 6, FIRE[4]); c.hline(6, 9, 9, FIRE[3])
    c.set(5, 5, FIRE[4]); c.set(10, 5, FIRE[4])
    c.set(5, 10, FIRE[3]); c.set(10, 10, FIRE[3])
    c.line(5, 5, 10, 10, CHAR[1]); c.line(10, 5, 5, 10, CHAR[1])
    c.vline(7, 4, 11, dk); c.vline(8, 4, 11, dk)
    return c


def t_chain(fam):
    c = Canvas(16, 16)
    d, dk, m, b, l, h, co = fam
    for cy in (-4, 4, 12):
        # vertical link
        c.outline([(5, cy + 1), (10, cy + 1), (11, cy + 4), (10, cy + 7),
                   (5, cy + 7), (4, cy + 4)], d)
        c.outline([(6, cy + 2), (9, cy + 2), (10, cy + 4), (9, cy + 6),
                   (6, cy + 6), (5, cy + 4)], b)
        c.set(7, cy + 2, h); c.set(7, cy + 3, l)
        c.set(8, cy + 6, dk)
        # horizontal link crossing
        c.outline([(1, cy + 4), (4, cy + 2), (7, cy + 4), (4, cy + 6)], d)
        c.outline([(12, cy + 4), (15, cy + 2)], d)
    # recompose crossing links cleanly
    for cy in (-4, 4, 12):
        c.outline([(4, cy + 4), (7, cy + 2), (10, cy + 4), (7, cy + 6)], b)
        c.set(7, cy + 3, l)
    return c


def t_ladder():
    c = Canvas(16, 16)
    d, dk, m, b, l, h, co = STEEL
    for y in range(16):
        c.set(2, y, dk); c.set(3, y, b)
        c.set(12, y, b); c.set(13, y, dk)
    for y in (0, 3, 6, 9, 12, 15):
        for x in range(3, 13):
            c.set(x, y, b); c.set(x, y + 1, l)
        c.set(3, y, FIRE[3]); c.set(12, y, FIRE[3])
        c.set(3, y + 1, FIRE[4]); c.set(12, y + 1, FIRE[4])
    for y in range(0, 16):
        if y % 3 == 1:
            c.set(4, y, FIRE[4]); c.set(11, y, FIRE[4])
    return c


# ===========================================================================
# Molten-era blocks
# ===========================================================================
def t_molten_altar_side():
    c = Canvas(16, 16)
    for y in range(16):
        for x in range(16):
            c.set(x, y, noise_shade(CHAR, x, y, seed=21, amp=0.3))
    c.hline(0, 15, 0, CHAR[5]); c.vline(0, 0, 15, CHAR[4])
    c.hline(0, 15, 15, CHAR[0]); c.vline(15, 0, 15, CHAR[0])
    c.rect(3, 3, 12, 12, CHAR[1])
    c.hline(3, 12, 3, CHAR[4]); c.vline(3, 3, 12, CHAR[3])
    c.hline(3, 12, 12, CHAR[0]); c.vline(12, 3, 12, CHAR[0])
    # glowing rune inset
    c.rect(6, 5, 9, 10, FIRE[2])
    c.rect(7, 6, 8, 9, FIRE[4])
    c.hline(6, 9, 5, FIRE[5]); c.hline(6, 9, 10, FIRE[1])
    c.set(8, 7, FIRE[6]); c.set(8, 8, FIRE[5])
    c.hline(7, 8, 14, FIRE[3])
    for (rx, ry) in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        c.set(rx, ry, GOLD[5]); c.set(rx, ry + 1, GOLD[2])
    c.set(4, 5, FIRE[3]); c.set(11, 5, FIRE[3])
    return c


def t_molten_altar_top():
    c = Canvas(16, 16)
    for y in range(16):
        for x in range(16):
            c.set(x, y, noise_shade(CHAR, x, y, seed=23, amp=0.3))
    c.hline(0, 15, 0, CHAR[5]); c.vline(0, 0, 15, CHAR[4])
    c.hline(0, 15, 15, CHAR[0]); c.vline(15, 0, 15, CHAR[0])
    for (r, col) in [(7, FIRE[2]), (6, FIRE[3]), (5, CHAR[1]), (4, FIRE[3]),
                     (3, CHAR[2])]:
        c.ring(8, 8, r, col)
    c.disc(8, 8, 2, FIRE[4])
    c.set(8, 8, FIRE[6]); c.set(7, 8, FIRE[5]); c.set(8, 7, FIRE[5])
    c.set(4, 4, GOLD[4]); c.set(11, 4, GOLD[4])
    c.set(4, 11, GOLD[4]); c.set(11, 11, GOLD[4])
    return c


def t_molten_altar_core():
    c = Canvas(16, 16)
    c.ring(8, 8, 7, CHAR[2])
    c.ring(8, 8, 6, FIRE[1])
    c.disc(8, 8, 5, FIRE[2])
    c.disc(8, 8, 4, FIRE[3])
    c.disc(8, 8, 3, FIRE[4])
    c.disc(8, 8, 2, FIRE[5])
    c.set(8, 8, FIRE[6]); c.set(7, 8, FIRE[6]); c.set(8, 7, FIRE[6])
    # orbiting ember sparks
    c.set(8, 0, FIRE[4]); c.set(15, 8, FIRE[4])
    c.set(8, 15, FIRE[3]); c.set(0, 8, FIRE[3])
    c.set(12, 3, FIRE[5]); c.set(3, 12, FIRE[5])
    return c


def t_molten_glass():
    c = Canvas(16, 16)
    for y in range(16):
        for x in range(16):
            c.set(x, y, GLASS_BASE)
    # frame
    for x in range(16):
        c.set(x, 0, GLASS_EDGE); c.set(x, 15, GLASS_EDGE)
    for y in range(16):
        c.set(0, y, GLASS_EDGE); c.set(15, y, GLASS_EDGE)
    # orange veined border
    c.hline(1, 14, 1, FIRE[2]); c.hline(1, 14, 14, FIRE[2])
    c.vline(1, 1, 14, FIRE[2]); c.vline(14, 1, 14, FIRE[2])
    c.hline(0, 15, 0, FIRE[3]); c.vline(0, 0, 15, FIRE[3])
    c.hline(0, 15, 15, FIRE[1]); c.vline(15, 0, 15, FIRE[1])
    for (x, y) in [(1, 1), (14, 1), (1, 14), (14, 14)]:
        c.set(x, y, FIRE[5])
    c.set(7, 1, FIRE[4]); c.set(8, 14, FIRE[4])
    c.set(1, 7, FIRE[4]); c.set(14, 8, FIRE[4])
    # fixed-alpha diagonal shine
    c.line(3, 3, 8, 3, GLASS_SHINE)
    c.line(3, 4, 4, 4, GLASS_SHINE)
    return c


def t_molten_lantern():
    return t_lantern(GOLD)


# ===========================================================================
# Entities
# ===========================================================================
def t_lava_golem():
    """128x128 molten golem entity atlas (procedural, deterministic)."""
    c = Canvas(128, 128)
    d, dk, m, b, l, h, co = CHAR
    # legs
    for (x0, x1) in [(42, 60), (68, 86)]:
        c.rect(x0, 96, x1, 116, CHAR[2])
        c.hline(x0 + 1, x1 - 1, 96, CHAR[4])
        c.vline(x0, 97, 115, CHAR[3])
        c.vline(x1, 97, 116, CHAR[0])
        c.hline(x0, x1, 116, CHAR[0])
    # torso
    c.rect(40, 40, 88, 96, CHAR[2])
    c.hline(41, 87, 40, CHAR[5])
    c.vline(40, 41, 95, CHAR[4])
    c.hline(41, 87, 96, CHAR[0])
    c.vline(88, 41, 96, CHAR[0])
    c.outline([(40, 40), (88, 40), (88, 96), (40, 96)], CHAR[0])
    # arms
    for (x0, x1) in [(26, 40), (88, 102)]:
        c.rect(x0, 42, x1, 92, CHAR[2])
        c.hline(x0 + 1, x1 - 1, 42, CHAR[4])
        c.vline(x0, 43, 91, CHAR[3])
        c.vline(x1, 43, 92, CHAR[0])
        c.hline(x0, x1, 92, CHAR[0])
    # head
    c.rect(52, 22, 76, 40, CHAR[2])
    c.hline(53, 75, 22, CHAR[5]); c.vline(52, 23, 39, CHAR[4])
    c.hline(53, 75, 40, CHAR[0]); c.vline(76, 23, 40, CHAR[0])
    c.outline([(52, 22), (76, 22), (76, 40), (52, 40)], CHAR[0])
    # molten cores & cracks (emissive layers)
    c.disc(64, 54, 12, FIRE[2]); c.disc(64, 54, 9, FIRE[3])
    c.disc(64, 54, 6, FIRE[4]); c.disc(64, 54, 3, FIRE[5])
    c.set(64, 54, FIRE[6]); c.set(63, 54, FIRE[6]); c.set(64, 53, FIRE[6])
    # face
    c.rect(57, 28, 61, 34, FIRE[4]); c.rect(67, 28, 71, 34, FIRE[4])
    c.rect(58, 30, 60, 32, FIRE[6]); c.rect(68, 30, 70, 32, FIRE[6])
    c.hline(56, 72, 37, FIRE[2]); c.set(60, 38, FIRE[3]); c.set(67, 38, FIRE[3])
    # crack veins
    for (x0, y0, x1, y1) in [(46, 48, 54, 60), (82, 46, 74, 58),
                             (48, 80, 58, 90), (80, 78, 70, 90),
                             (44, 60, 44, 76), (84, 58, 84, 74),
                             (56, 44, 60, 36), (72, 44, 68, 36)]:
        c.line(x0, y0, x1, y1, FIRE[3])
        c.line(x0, y0, x1, y1, FIRE[4]) if (x0 + y0) % 2 == 0 else None
    # arm cores
    c.disc(33, 84, 5, FIRE[3]); c.disc(33, 84, 3, FIRE[4]); c.set(33, 84, FIRE[5])
    c.disc(95, 84, 5, FIRE[3]); c.disc(95, 84, 3, FIRE[4]); c.set(95, 84, FIRE[5])
    return c


def t_fire_wraith():
    """64x32 blazing wraith entity atlas (procedural, deterministic)."""
    c = Canvas(64, 32)
    # body
    c.poly([(26, 8), (38, 8), (40, 24), (32, 31), (24, 24)], FIRE[2])
    c.poly([(28, 11), (36, 11), (37, 22), (32, 28), (27, 22)], FIRE[3])
    c.poly([(30, 14), (34, 14), (34, 20), (32, 23), (30, 20)], FIRE[4])
    c.poly([(31, 16), (33, 16), (33, 19), (32, 21), (31, 19)], FIRE[5])
    c.set(32, 18, FIRE[6])
    # eyes
    c.rect(27, 14, 29, 17, STEEL[6]); c.rect(35, 14, 37, 17, STEEL[6])
    c.set(28, 15, FIRE[6]); c.set(36, 15, FIRE[6])
    # flame arms
    for side in (-1, 1):
        bx = 32 + side * 12
        for i in range(8):
            c.set(bx + side * i, 16 + i, FIRE[3])
            c.set(bx + side * i, 17 + i, FIRE[4])
        c.set(bx + side * 3, 14, FIRE[5]); c.set(bx + side * 5, 15, FIRE[4])
    # top wisp
    c.poly([(29, 7), (35, 7), (32, 0)], FIRE[4])
    c.poly([(31, 6), (33, 6), (32, 1)], FIRE[5])
    c.set(32, 2, FIRE[6])
    # trailing flames
    c.poly([(28, 27), (36, 27), (32, 31)], FIRE[3])
    c.set(30, 30, FIRE[4]); c.set(34, 30, FIRE[4])
    c.set(22, 20, FIRE[3]); c.set(42, 20, FIRE[3])
    c.set(20, 22, FIRE[4]); c.set(44, 22, FIRE[4])
    return c


def t_slag_crawler():
    """64x64 slag crawler entity atlas.

    Every rectangle below is the exact UV bound sampled by SlagCrawlerModel: a
    10x6x6 body segment occupies 2*(10+6) x (6+6) = 32x12 px, so the painted
    atlas lines up 1:1 with the cube faces.
    """
    c = Canvas(64, 64)

    def plate(u, v, w, h, dd, fam, seed, glow=True):
        """Fill one part's UV rectangle (w/h/d given in model units)."""
        W = 2 * (w + dd)
        H = h + dd
        for y in range(v, v + H):
            for x in range(u, u + W):
                c.set(x, y, noise_shade(fam, x, y, seed=seed, amp=0.26))
        c.hline(u + 1, u + W - 2, v, fam[4])
        c.vline(u, v + 1, v + H - 2, fam[3])
        c.outline([(u, v), (u + W - 1, v), (u + W - 1, v + H - 1), (u, v + H - 1)], fam[0])
        if glow:
            gy = v + H // 2
            c.hline(u + 2, u + W - 3, gy, FIRE[2])
            c.hline(u + 2, u + W - 3, gy + 1, FIRE[3])
            for gx in range(u + 3, u + W - 3, 4):
                c.set(gx, gy, FIRE[4]); c.set(gx + 1, gy + 1, FIRE[5])

    plate(0, 0, 10, 6, 6, CHAR, 31, True)      # carapace segment (x3)
    plate(32, 0, 8, 6, 6, CHAR, 33, True)      # head
    plate(0, 12, 2, 2, 5, FIRE, 35, True)      # mandible (x2)
    plate(14, 12, 2, 6, 2, CHAR, 37, True)     # leg (x6)
    plate(0, 22, 4, 4, 10, CHAR, 39, True)     # tail
    plate(30, 22, 2, 2, 3, FIRE, 41, False)    # tail tip
    # bright maw pips on the head front
    c.set(33, 2, FIRE[5]); c.set(34, 2, FIRE[6])
    c.set(45, 2, FIRE[6]); c.set(46, 2, FIRE[5])
    return c


def t_ember_wisp():
    """32x32 ember wisp entity atlas.

    Rectangles match EmberWispModel exactly: the 8x8x8 orb samples 32x16 px, each
    6x2x4 wing 20x6 px, the 2x6x2 tail ribbon 8x8 px.
    """
    c = Canvas(32, 32)

    def blob(u, v, w, h, dd, seed, hot=True):
        W = 2 * (w + dd)
        H = h + dd
        for y in range(v, v + H):
            for x in range(u, u + W):
                c.set(x, y, noise_shade(FIRE, x, y, seed=seed, amp=0.24))
        c.outline([(u, v), (u + W - 1, v), (u + W - 1, v + H - 1), (u, v + H - 1)], FIRE[0])
        if hot:
            c.rect(u + 2, v + 2, u + W - 3, v + H - 3, FIRE[3])
            c.rect(u + 3, v + 3, u + W - 4, v + H - 4, FIRE[4])
            c.hline(u + 4, u + W - 5, v + (H - 1) // 2, FIRE[5])
            c.set(u + W // 2, v + H // 2, FIRE[6])

    blob(0, 0, 8, 8, 8, 41, True)      # orb core
    blob(0, 16, 6, 2, 4, 43, True)     # wing (x2)
    blob(20, 16, 2, 6, 2, 45, False)   # tail ribbon
    # eye pips on the orb front
    c.rect(14, 24, 19, 26, FIRE[2])
    c.set(15, 25, STEEL[6]); c.set(18, 25, STEEL[6])
    c.set(15, 26, FIRE[6]); c.set(18, 26, FIRE[6])
    return c


def t_ancient_forgeborn():
    """128x128 ancient forgeborn boss atlas.

    Every rectangle is the exact UV bound sampled by AncientForgebornModel, e.g.
    the 20x26x12 torso occupies 2*(20+12) x (26+12) = 64x38 px.
    """
    c = Canvas(128, 128)

    def plate(u, v, w, h, dd, seed, glow=True):
        W = 2 * (w + dd)
        H = h + dd
        for y in range(v, v + H):
            for x in range(u, u + W):
                c.set(x, y, noise_shade(GOLD, x, y, seed=seed, amp=0.24))
        c.hline(u + 1, u + W - 2, v, GOLD[4])
        c.vline(u, v + 1, v + H - 2, GOLD[3])
        c.outline([(u, v), (u + W - 1, v), (u + W - 1, v + H - 1), (u, v + H - 1)], GOLD[0])
        for rx in range(u + 4, u + W - 3, 8):
            for ry in range(v + 4, v + H - 3, 8):
                c.set(rx, ry, GOLD[5]); c.set(rx, ry + 1, GOLD[2])
        if glow:
            gy = v + H // 2
            c.hline(u + 3, u + W - 4, gy, FIRE[2])
            c.hline(u + 3, u + W - 4, gy + 1, FIRE[3])
            for gx in range(u + 5, u + W - 5, 6):
                c.set(gx, gy, FIRE[4]); c.set(gx + 1, gy + 1, FIRE[5])
            c.set(u + W // 2, v + 2, FIRE[5])

    plate(0, 0, 7, 20, 7, 51, True)        # leg (x2)
    plate(0, 28, 20, 26, 12, 53, True)     # torso
    plate(64, 28, 10, 10, 2, 55, True)     # chest forge-core
    plate(0, 67, 14, 8, 14, 57, False)     # head
    plate(64, 41, 6, 14, 6, 59, True)      # upper arm (x2)
    plate(64, 63, 6, 12, 6, 61, True)      # forearm (x2)
    plate(92, 41, 6, 7, 6, 63, False)      # fist (x2)
    plate(56, 67, 3, 4, 3, 65, False)      # crown horn (x2)
    plate(88, 67, 6, 6, 6, 67, False)      # pauldron (x2)
    # blazing eye slits on the head front
    c.rect(56, 75, 63, 78, GOLD[0])
    c.rect(57, 76, 62, 77, FIRE[4])
    c.set(58, 76, FIRE[6]); c.set(61, 76, FIRE[6])
    return c


# ===========================================================================
# Armour layers (64x32)
# ===========================================================================
def t_armor_layer(w, h, fam, glow=False):
    c = Canvas(w, h)
    for y in range(h):
        for x in range(w):
            c.set(x, y, noise_shade(fam, x, y, seed=7, amp=0.28))
    for y in range(7, h, 8):
        for x in range(w):
            c.set(x, y, sh(fam, -0.7))
            if y - 1 >= 0:
                c.set(x, y - 1, sh(fam, 0.4))
    for x in range(0, w, 16):
        for y in range(h):
            c.set(x, y, sh(fam, 0.45))
    for x in range(w):
        c.set(x, 0, fam[0]); c.set(x, h - 1, fam[0])
    for y in range(h):
        c.set(0, y, fam[0]); c.set(w - 1, y, fam[0])
    for y in range(4, h - 3, 8):
        for x in range(4, w - 3, 8):
            c.set(x, y, fam[5]); c.set(x + 1, y, fam[4])
            c.set(x, y + 1, fam[2]); c.set(x + 1, y + 1, fam[0])
    if glow:
        for y in range(3, h, 8):
            for x in range(2, w - 1, 4):
                if (x + y) % 8 == 0:
                    c.set(x, y, FIRE[5]); c.set(x, y + 1, FIRE[4])
    return c


# ===========================================================================
# Dispatch
# ===========================================================================
TEXTURES = [
    # ---- existing blocks ---------------------------------------------------
    ("block/crude_steel_block.png", lambda: t_block(CRUDE)),
    ("block/steel_block.png",       lambda: t_block(QUENCH)),
    ("block/hot_steel_block.png",   t_hot_block),
    ("block/hot_steel_forge.png",   t_forge),
    ("block/hot_steel_smelter.png", t_smelter),
    ("block/hot_steel_bricks.png",  t_hot_steel_bricks),
    ("block/hot_steel_door_top.png",    t_door_top),
    ("block/hot_steel_door_bottom.png", t_door_bottom),
    ("block/hot_steel_trapdoor.png",    t_trapdoor),
    ("block/hot_steel_lantern.png",     lambda: t_lantern(FIRE)),
    ("block/hot_steel_chain.png",       lambda: t_chain(STEEL)),
    ("block/hot_steel_ladder.png",      t_ladder),

    # ---- existing items ----------------------------------------------------
    ("item/crude_steel.png",     lambda: t_ingot(CRUDE)),
    ("item/steel_ingot.png",     lambda: t_ingot(QUENCH)),
    ("item/hot_steel_ingot.png", lambda: t_ingot(FIRE, glow=True)),
    ("item/hot_steel_nugget.png", lambda: t_nugget(FIRE, glow=True)),
    ("item/molten_core.png",     t_molten_core),
    ("item/hot_steel_sword.png",   lambda: t_tool("sword", STEEL)),
    ("item/hot_steel_pickaxe.png", lambda: t_tool("pickaxe", STEEL)),
    ("item/hot_steel_axe.png",     lambda: t_tool("axe", STEEL)),
    ("item/hot_steel_shovel.png",  lambda: t_tool("shovel", STEEL)),
    ("item/hot_steel_hoe.png",     lambda: t_tool("hoe", STEEL)),
    ("item/hot_steel_knife.png",   lambda: t_tool("knife", STEEL)),
    ("item/hot_steel_mace.png",    lambda: t_tool("mace", STEEL)),
    ("item/hot_steel_sickle.png",  t_sickle),
    ("item/hot_steel_paxel.png",   t_paxel),
    ("item/hot_steel_arrow.png",   t_hot_steel_arrow),
    ("item/hot_steel_helmet.png",    lambda: t_armor("helmet", STEEL)),
    ("item/hot_steel_chestplate.png", lambda: t_armor("chestplate", STEEL)),
    ("item/hot_steel_leggings.png",  lambda: t_armor("leggings", STEEL)),
    ("item/hot_steel_boots.png",     lambda: t_armor("boots", STEEL)),
    ("item/hot_steel_bow.png",              lambda: t_bow(0)),
    ("item/hot_steel_bow_pulling_0.png",    lambda: t_bow(1)),
    ("item/hot_steel_bow_pulling_1.png",    lambda: t_bow(2)),
    ("item/hot_steel_bow_pulling_2.png",    lambda: t_bow(3)),
    ("item/hot_steel_crossbow.png",              lambda: t_crossbow(None)),
    ("item/hot_steel_crossbow_pulling_0.png",    lambda: t_crossbow(0)),
    ("item/hot_steel_crossbow_pulling_1.png",    lambda: t_crossbow(1)),
    ("item/hot_steel_crossbow_pulling_2.png",    lambda: t_crossbow(2)),
    ("item/hot_steel_crossbow_arrow.png",        t_crossbow_arrow),
    ("item/hot_steel_crossbow_firework.png",     t_crossbow_firework),
    ("item/hot_steel_trident.png", t_trident_item),
    ("item/hot_steel_shield.png",  t_shield),
    ("item/hot_steel_fishing_rod.png", t_fishing_rod),
    ("item/hot_steel_door.png",    t_door_item),
    ("item/hot_steel_apple.png",   t_apple),
    ("item/hot_steel_fireball.png", t_fireball),
    ("item/lava_bottle.png",       t_lava_bottle),

    # ---- existing entities -------------------------------------------------
    ("entity/hot_steel_trident.png", t_trident_entity),
    ("entity/hot_steel_arrow.png",   t_hot_steel_arrow_entity),
    ("entity/lava_golem.png",        t_lava_golem),
    ("entity/fire_wraith.png",       t_fire_wraith),

    # ---- existing mob effect + armour layers -------------------------------
    ("mob_effect/super_fire_resistance.png", t_effect_icon),
    ("models/armor/hot_steel_layer_1.png", lambda: t_armor_layer(64, 32, STEEL)),
    ("models/armor/hot_steel_layer_2.png", lambda: t_armor_layer(64, 32, STEEL)),

    # ==== NEW Forge-Era content ============================================
    # item/
    ("item/molten_shard.png",        t_molten_shard),
    ("item/molten_steel_ingot.png",  lambda: t_ingot(GOLD, glow=True)),
    ("item/molten_steel_nugget.png", lambda: t_nugget(GOLD, glow=True)),
    ("item/forge_heart.png",         t_forge_heart),
    ("item/hot_steel_codex.png",     t_hot_steel_codex),
    ("item/scorched_page.png",       t_scorched_page),
    ("item/molten_steel_sword.png",   lambda: t_tool("sword", GOLD, glow=True)),
    ("item/molten_steel_pickaxe.png", lambda: t_tool("pickaxe", GOLD, glow=True)),
    ("item/molten_steel_axe.png",     lambda: t_tool("axe", GOLD, glow=True)),
    ("item/molten_steel_shovel.png",  lambda: t_tool("shovel", GOLD, glow=True)),
    ("item/molten_steel_hoe.png",     lambda: t_tool("hoe", GOLD, glow=True)),
    ("item/molten_steel_scythe.png",  lambda: t_tool("scythe", GOLD, glow=True)),
    ("item/molten_steel_helmet.png",     lambda: t_armor("helmet", GOLD, glow=True)),
    ("item/molten_steel_chestplate.png", lambda: t_armor("chestplate", GOLD, glow=True)),
    ("item/molten_steel_leggings.png",   lambda: t_armor("leggings", GOLD, glow=True)),
    ("item/molten_steel_boots.png",      lambda: t_armor("boots", GOLD, glow=True)),
    # block/
    ("block/molten_steel_block.png",  t_molten_steel_block),
    ("block/molten_altar_side.png",   t_molten_altar_side),
    ("block/molten_altar_top.png",    t_molten_altar_top),
    ("block/molten_altar_core.png",   t_molten_altar_core),
    ("block/molten_glass.png",        t_molten_glass),
    ("block/charred_bricks.png",      t_charred_bricks),
    ("block/molten_steel_chain.png",  lambda: t_chain(GOLD)),
    ("block/molten_lantern.png",      t_molten_lantern),
    # entity/
    ("entity/slag_crawler.png",       t_slag_crawler),
    ("entity/ember_wisp.png",         t_ember_wisp),
    ("entity/ancient_forgeborn.png",  t_ancient_forgeborn),
    # models/armor/
    ("models/armor/molten_steel_layer_1.png", lambda: t_armor_layer(64, 32, GOLD, glow=True)),
    ("models/armor/molten_steel_layer_2.png", lambda: t_armor_layer(64, 32, GOLD, glow=True)),
    # mob_effect/
    ("mob_effect/forge_blessing.png", t_forge_blessing),
]


def main():
    root = "/workspace/src/main/resources/assets/hotsteel/textures"
    total = 0
    for rel, fn in TEXTURES:
        path = os.path.join(root, rel)
        canvas = fn()
        if isinstance(canvas, Image.Image):
            os.makedirs(os.path.dirname(path), exist_ok=True)
            canvas.save(path, "PNG", optimize=True)
            w, h = canvas.size
        else:
            canvas.save(path)
            w, h = canvas.w, canvas.h
        total += 1
        print(f"  wrote {path}  ({w}x{h})")
    print(f"\nRegenerated {total} textures.")


if __name__ == "__main__":
    main()
