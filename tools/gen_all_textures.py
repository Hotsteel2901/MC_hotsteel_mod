#!/usr/bin/env python3
"""Redraw every Hot Steel mod texture as proper pixel-art PNGs.

Covers 31 textures:
  - 24 item textures (16x16)        -> textures/item/
  - 1  block texture (16x16)        -> textures/block/
  - 1  item-form trident texture (16x16) -> textures/item/  (same as item)
  - 1  entity trident atlas (32x32) -> textures/entity/
  - 2  armor layers (64x32)         -> textures/models/armor/
  - 1  mob effect icon (16x16)      -> textures/mob_effect/

All textures are drawn directly with Pillow (no SVG intermediate), so the
output is crisp 1:1 pixel art at the right dimensions.
"""
from PIL import Image
import os

# ---------------------------------------------------------------------------
# Palette
# ---------------------------------------------------------------------------
GRAY   = (0x6f, 0x7c, 0x8a)   # main steel-plate gray
GRAY_D = (0x3e, 0x47, 0x50)   # shadow / edge
GRAY_L = (0xa9, 0xb7, 0xc4)   # highlight
GRAY_H = (0xcf, 0xda, 0xe4)   # bright specular

CRUDE  = (0x5a, 0x5f, 0x66)   # crude steel (dull)
CRUDE_D= (0x33, 0x37, 0x3c)
CRUDE_L= (0x82, 0x8a, 0x93)

BLUE   = (0x5d, 0x7d, 0x99)   # refined steel ingot / block
BLUE_D = (0x32, 0x45, 0x56)
BLUE_L = (0x9f, 0xc0, 0xd6)

HOT    = (0xe0, 0x61, 0x1f)   # hot steel (orange glow)
HOT_D  = (0x7a, 0x2c, 0x08)
HOT_L  = (0xff, 0xc2, 0x4a)
HOT_H  = (0xff, 0xe7, 0x9c)

WOOD   = (0x6b, 0x4a, 0x2b)   # handle
WOOD_D = (0x3f, 0x2b, 0x18)
WOOD_L = (0x8b, 0x63, 0x3c)

STR    = (0xd9, 0xd2, 0xc0)   # bowstring
STR_D  = (0x9a, 0x94, 0x84)

TRANSPARENT = (0, 0, 0, 0)

# ---------------------------------------------------------------------------
# Tiny pixel-art canvas helper
# ---------------------------------------------------------------------------
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

    def fill(self, color):
        for y in range(self.h):
            for x in range(self.w):
                self.set(x, y, color)

    def rect(self, x0, y0, x1, y1, color):  # inclusive
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                self.set(x, y, color)

    def hline(self, x0, x1, y, color):
        self.rect(x0, y, x1, y, color)

    def vline(self, x, y0, y1, color):
        self.rect(x, y0, x, y1, color)

    def poly(self, pts, color):  # scanline-fill polygon
        ys = [p[1] for p in pts]
        y0, y1 = max(0, min(ys)), min(self.h - 1, max(ys))
        for y in range(y0, y1 + 1):
            xs = []
            n = len(pts)
            for i in range(n):
                xa, ya = pts[i]
                xb, yb = pts[(i + 1) % n]
                if (ya <= y < yb) or (yb <= y < xa and yb <= y < ya) or (yb <= y < ya):
                    pass
            # Simpler: compute edge crossings
            xs = []
            for i in range(n):
                xa, ya = pts[i]
                xb, yb = pts[(i + 1) % n]
                if (ya <= y < yb) or (yb <= y < ya):
                    t = (y - ya) / (yb - ya)
                    xs.append(xa + t * (xb - xa))
            xs.sort()
            for i in range(0, len(xs), 2):
                if i + 1 < len(xs):
                    for x in range(int(round(xs[i])), int(round(xs[i + 1])) + 1):
                        self.set(x, y, color)

    def line(self, x0, y0, x1, y1, color):  # Bresenham
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

    def save(self, path):
        os.makedirs(os.path.dirname(path), exist_ok=True)
        self.im.save(path, "PNG", optimize=True)


# ---------------------------------------------------------------------------
# Palette helpers for shading steel plates
# ---------------------------------------------------------------------------
def shade(base, dark, light, frac):
    """frac in [-1,1]; -1=dark, +1=light, 0=base."""
    if frac <= 0:
        t = -frac
        return tuple(int(base[i] * (1 - t) + dark[i] * t) for i in range(3)) + (255,)
    else:
        t = frac
        return tuple(int(base[i] * (1 - t) + light[i] * t) for i in range(3)) + (255,)


def noise_shade(base, dark, light, x, y, seed=0):
    """Deterministic per-pixel shading for a metal-plate look."""
    h = ((x * 73856093) ^ (y * 19349663) ^ (seed * 83492791)) & 0x7fffffff
    r = (h % 1000) / 1000.0 - 0.5
    if r > 0.25:
        return shade(base, dark, light, 0.5)
    elif r < -0.25:
        return shade(base, dark, light, -0.5)
    return base + (255,)


# ---------------------------------------------------------------------------
# Item primitives (all 16x16 unless noted)
# ---------------------------------------------------------------------------
def draw_handle(c, x=7, y0=7, y1=14, wood=WOOD, dark=WOOD_D, light=WOOD_L):
    """Vertical wooden handle with a slight highlight on the left edge."""
    for y in range(y0, y1 + 1):
        c.set(x, y, wood)
        c.set(x + 1, y, wood)
    # highlight
    for y in range(y0, y1):
        c.set(x, y, light)
    # shadow / wrap
    c.set(x + 2, y0, dark)
    c.set(x + 2, y1, dark)
    c.vline(x + 2, y0 + 1, y1 - 1, shade(wood, dark, light, -0.4)[:3])
    # grip notches
    for y in (y0 + 2, y0 + 5, y0 + 8, y0 + 11):
        if y <= y1:
            c.set(x, y, dark)
            c.set(x + 1, y, dark)


def draw_ingot(c, base, dark, light):
    """Beveled ingot sitting horizontally, occupying the lower-center."""
    # main trapezoid body
    pts = [(2, 11), (3, 8), (13, 8), (14, 11), (14, 13), (2, 13)]
    c.poly(pts, base)
    # top face (lighter)
    top = [(3, 8), (13, 8), (12, 9), (4, 9)]
    c.poly(top, light)
    # specular highlight strip on top
    c.hline(5, 11, 8, shade(base, dark, light, 0.8)[:3])
    # left side face (darker)
    left = [(2, 11), (3, 8), (4, 9), (4, 12), (3, 13), (2, 13)]
    c.poly(left, dark)
    # outline
    c.outline(pts, dark)
    # tiny rivets
    c.set(6, 11, dark); c.set(10, 11, dark)


# ---------------------------------------------------------------------------
# Individual texture functions
# ---------------------------------------------------------------------------
def t_ingot(base, dark, light, with_glow=False):
    c = Canvas(16, 16)
    draw_ingot(c, base, dark, light)
    if with_glow:
        # emissive cracks along the top
        for x in range(4, 12):
            if x % 2 == 0:
                c.set(x, 9, HOT_L)
        c.set(6, 10, HOT); c.set(9, 10, HOT)
        # tiny sparks
        c.set(3, 6, HOT_L); c.set(12, 5, HOT_H); c.set(8, 4, HOT_L)
    return c


def t_sword():
    c = Canvas(16, 16)
    # blade: pointing up
    blade = [(7, 2), (9, 2), (9, 10), (7, 10)]
    c.poly(blade, GRAY)
    # central fuller (highlight)
    c.vline(8, 3, 9, GRAY_L)
    # edge highlights
    c.vline(7, 3, 9, GRAY_H)
    # edge shadows
    c.vline(9, 3, 9, GRAY_D)
    # tip
    c.set(8, 1, GRAY_L)
    # crossguard
    c.rect(5, 10, 11, 11, WOOD_D)
    c.hline(5, 11, 10, GRAY_D)
    c.hline(5, 11, 11, GRAY)
    c.set(5, 10, GRAY_L); c.set(11, 10, GRAY_L)
    # handle
    draw_handle(c, x=7, y0=12, y1=14)
    # pommel
    c.set(7, 15, GRAY_L); c.set(8, 15, GRAY_L)
    c.set(7, 14, GRAY); c.set(8, 14, GRAY)
    return c


def t_knife():
    c = Canvas(16, 16)
    # short blade pointing up-right
    blade = [(8, 3), (12, 3), (12, 10), (8, 10)]
    c.poly(blade, GRAY)
    c.vline(8, 3, 10, GRAY_L)
    c.vline(12, 3, 10, GRAY_D)
    c.hline(8, 12, 3, GRAY_H)
    c.set(12, 3, GRAY_L)
    # tip
    c.set(13, 3, GRAY_L); c.set(13, 4, GRAY_L)
    # bolster
    c.rect(7, 10, 12, 11, WOOD_D)
    # handle
    draw_handle(c, x=7, y0=11, y1=15)
    return c


def t_pickaxe():
    c = Canvas(16, 16)
    draw_handle(c, x=7, y0=6, y1=14)
    # diagonal head band
    pts = [(2, 6), (4, 4), (12, 4), (14, 6), (13, 7), (8, 5), (3, 7)]
    c.poly(pts, GRAY)
    # highlights / shadows
    c.hline(4, 12, 4, GRAY_L)
    c.set(3, 5, GRAY_D); c.set(13, 5, GRAY_D)
    c.set(2, 6, GRAY_D); c.set(14, 6, GRAY_D)
    c.set(8, 5, GRAY_H)
    # pick tips
    c.set(2, 7, GRAY_D); c.set(14, 7, GRAY_D)
    return c


def t_axe():
    c = Canvas(16, 16)
    draw_handle(c, x=7, y0=6, y1=14)
    # axe head: chunky trapezoid on the right
    head = [(7, 4), (13, 4), (14, 7), (13, 9), (8, 9), (7, 8)]
    c.poly(head, GRAY)
    # bevel highlight
    c.hline(7, 13, 4, GRAY_L)
    c.set(14, 5, GRAY_D); c.set(14, 6, GRAY_D); c.set(14, 7, GRAY_D)
    # cutting edge highlight
    c.vline(13, 5, 8, GRAY_H)
    # butt (back)
    c.vline(7, 5, 7, GRAY_D)
    c.outline(head, GRAY_D)
    return c


def t_shovel():
    c = Canvas(16, 16)
    draw_handle(c, x=7, y0=6, y1=14)
    # square shovel head
    head = [(6, 2), (10, 2), (10, 6), (9, 7), (7, 7), (6, 6)]
    c.poly(head, GRAY)
    c.hline(6, 10, 2, GRAY_L)
    c.set(6, 2, GRAY_D); c.set(10, 2, GRAY_D)
    c.vline(10, 3, 6, GRAY_D)
    c.vline(6, 3, 5, GRAY_H)
    # tip bevel
    c.set(7, 7, GRAY_D); c.set(9, 7, GRAY_D)
    c.set(8, 7, GRAY_L)
    return c


def t_hoe():
    c = Canvas(16, 16)
    draw_handle(c, x=7, y0=6, y1=14)
    # L-shaped hoe head
    head = [(4, 4), (12, 4), (12, 6), (7, 6), (7, 7), (4, 7)]
    c.poly(head, GRAY)
    c.hline(4, 12, 4, GRAY_L)
    c.vline(12, 5, 6, GRAY_D)
    c.vline(4, 5, 6, GRAY_D)
    c.hline(5, 11, 5, GRAY_H)
    c.set(7, 7, GRAY_D)
    return c


def t_helmet():
    c = Canvas(16, 16)
    # dome
    dome = [(3, 8), (4, 5), (5, 4), (7, 3), (9, 3), (11, 4), (12, 5), (13, 8), (13, 11), (3, 11)]
    c.poly(dome, GRAY)
    # crown highlight
    c.hline(5, 11, 4, GRAY_L)
    c.set(8, 3, GRAY_H)
    # face opening
    c.rect(5, 8, 11, 10, None)  # nothing
    c.poly([(5, 8), (11, 8), (11, 10), (5, 10)], (0, 0, 0, 0))
    # nose guard
    c.vline(8, 8, 11, GRAY_D)
    # rim
    c.hline(3, 13, 11, GRAY_D)
    c.hline(3, 13, 10, GRAY)
    # side rivets
    c.set(4, 9, GRAY_D); c.set(11, 9, GRAY_D)
    # crest line
    c.hline(7, 9, 3, GRAY_H)
    return c


def t_chestplate():
    c = Canvas(16, 16)
    # body shape with shoulders
    body = [(3, 4), (5, 3), (7, 4), (9, 4), (11, 3), (13, 4), (13, 14), (3, 14)]
    c.poly(body, GRAY)
    # shoulder caps
    c.set(4, 4, GRAY_L); c.set(12, 4, GRAY_L)
    # collar
    c.poly([(7, 4), (9, 4), (9, 6), (7, 6)], GRAY_D)
    # chest center seam
    c.vline(8, 6, 13, GRAY_D)
    # chest highlight (left side)
    c.vline(5, 6, 13, GRAY_L)
    c.vline(6, 6, 13, GRAY_H)
    # right shadow
    c.vline(10, 6, 13, GRAY_D)
    c.vline(11, 6, 13, shade(GRAY, GRAY_D, GRAY_L, -0.4)[:3])
    # belt
    c.hline(3, 13, 13, GRAY_D)
    c.hline(3, 13, 12, GRAY)
    # rivets
    c.set(4, 7, GRAY_H); c.set(11, 7, GRAY_H)
    c.set(4, 10, GRAY_H); c.set(11, 10, GRAY_H)
    return c


def t_leggings():
    c = Canvas(16, 16)
    # waist + two legs
    pts = [(3, 3), (13, 3), (13, 14), (9, 14), (8, 8), (7, 14), (3, 14)]
    c.poly(pts, GRAY)
    # waistband
    c.hline(3, 13, 3, GRAY_L)
    c.hline(3, 13, 4, GRAY_D)
    # belt buckle
    c.set(7, 3, GRAY_H); c.set(8, 3, GRAY_H)
    c.set(7, 4, GRAY_D); c.set(8, 4, GRAY_D)
    # left leg shading
    c.vline(4, 5, 13, GRAY_H)
    c.vline(5, 5, 13, GRAY_L)
    # right leg shading
    c.vline(10, 5, 13, GRAY_D)
    c.vline(11, 5, 13, shade(GRAY, GRAY_D, GRAY_L, -0.4)[:3])
    # center seam
    c.vline(8, 8, 14, GRAY_D)
    # knee pads
    c.set(5, 10, GRAY_D); c.set(10, 10, GRAY_D)
    c.set(5, 11, GRAY_H); c.set(10, 11, GRAY_H)
    return c


def t_boots():
    c = Canvas(16, 16)
    # left boot
    left = [(3, 5), (7, 5), (7, 11), (13, 11), (13, 14), (3, 14)]
    c.poly(left, GRAY)
    # right boot (slightly offset, mirror)
    right = [(3, 5), (7, 5), (7, 11), (13, 11), (13, 14), (3, 14)]
    # The single boot design: shaft + foot
    # shaft highlight
    c.vline(4, 6, 10, GRAY_H)
    c.vline(5, 6, 10, GRAY_L)
    # top opening
    c.hline(3, 7, 5, GRAY_D)
    c.hline(3, 7, 6, GRAY)
    # ankle bend
    c.hline(7, 13, 11, GRAY_D)
    c.hline(7, 13, 12, GRAY_L)
    # sole
    c.hline(3, 13, 13, GRAY_D)
    c.hline(3, 13, 14, shade(GRAY, GRAY_D, GRAY_L, -0.6)[:3])
    # heel
    c.vline(3, 8, 13, GRAY_D)
    # toe cap highlight
    c.set(12, 11, GRAY_L); c.set(12, 12, GRAY_H)
    return c


def t_bow(pull=0):
    """pull: 0=relaxed, 1/2/3=increasing draw."""
    c = Canvas(16, 16)
    # bow arc: from top-right, curving left to bottom-right
    # Use Bresenham segments along an arc
    bow_pts = [(12, 2), (10, 3), (8, 5), (7, 8), (8, 11), (10, 13), (12, 14)]
    for i in range(len(bow_pts) - 1):
        c.line(*bow_pts[i], *bow_pts[i + 1], WOOD)
    # highlight side
    hl_pts = [(12, 3), (10, 4), (8, 6), (8, 10), (10, 12), (12, 13)]
    for i in range(len(hl_pts) - 1):
        c.line(*hl_pts[i], *hl_pts[i + 1], WOOD_L)
    # grip wrap
    c.set(7, 7, WOOD_D); c.set(7, 8, WOOD_D); c.set(7, 9, WOOD_D)
    # bowstring: V shape from top bow tip to bottom bow tip, drawn to pull point
    sx = 8 - pull  # pull point x: 8=relaxed, 7, 6, 5 for pulls 0..3
    sy = 8 + (pull // 2)  # slight downward
    c.line(12, 2, sx, sy, STR)
    c.line(12, 14, sx, sy, STR)
    c.line(12, 2, sx, sy, STR_D) if pull > 0 else None
    # arrow when fully drawn
    if pull >= 2:
        ax = sx - 1
        c.line(ax, 8, 14, 8, WOOD)
        c.set(13, 7, GRAY_L); c.set(14, 8, GRAY_L)  # tip
        c.set(12, 9, STR_D); c.set(13, 9, STR_D)    # fletching
    return c


def t_crossbow(pull=None):
    """pull: None=loaded-no-ammo, 0/1/2=increasing draw, 'arrow'/'firework' = special."""
    c = Canvas(16, 16)
    # main stock
    c.rect(3, 7, 13, 8, WOOD)
    c.hline(3, 13, 7, WOOD_L)
    c.hline(3, 13, 8, WOOD_D)
    # limbs
    c.line(3, 7, 1, 4, WOOD_D)
    c.line(1, 4, 3, 4, WOOD_D)
    c.line(3, 8, 1, 11, WOOD_D)
    c.line(1, 11, 3, 11, WOOD_D)
    # limb tips highlight
    c.set(2, 4, WOOD_L); c.set(2, 11, WOOD_L)
    # string
    if pull is None:
        # taut straight
        c.vline(2, 4, 11, STR)
        c.set(2, 5, STR_D); c.set(2, 10, STR_D)
    elif pull == 0:
        c.line(2, 4, 6, 7, STR); c.line(2, 11, 6, 8, STR)
    elif pull == 1:
        c.line(2, 4, 5, 7, STR); c.line(2, 11, 5, 8, STR)
    elif pull == 2:
        c.line(2, 4, 4, 7, STR); c.line(2, 11, 4, 8, STR)
        # arrow loaded
        c.line(4, 7, 14, 7, WOOD)
        c.set(13, 6, GRAY_L); c.set(14, 7, GRAY_H)
        c.set(5, 8, STR_D); c.set(6, 8, STR_D)
    # trigger
    c.set(11, 9, WOOD_D); c.set(11, 10, WOOD_D)
    # bolt in slot indicator
    if pull == 2:
        c.set(12, 6, GRAY_H)
    return c


def t_crossbow_arrow():
    c = Canvas(16, 16)
    # base crossbow
    base = t_crossbow(pull=None)
    for y in range(16):
        for x in range(16):
            r, g, b, a = base.im.getpixel((x, y))
            if a > 0:
                c.set(x, y, (r, g, b, a))
    # arrow loaded, pointing right
    c.line(4, 7, 14, 7, WOOD)
    c.line(4, 8, 14, 8, WOOD_L)
    # tip
    c.set(13, 6, GRAY_L); c.set(14, 7, GRAY_H); c.set(13, 8, GRAY_D)
    # fletching
    c.set(4, 6, STR_D); c.set(5, 6, STR_D)
    c.set(4, 9, STR_D); c.set(5, 9, STR_D)
    c.set(3, 7, STR); c.set(3, 8, STR)
    return c


def t_crossbow_firework():
    c = Canvas(16, 16)
    base = t_crossbow(pull=None)
    for y in range(16):
        for x in range(16):
            r, g, b, a = base.im.getpixel((x, y))
            if a > 0:
                c.set(x, y, (r, g, b, a))
    # firework rocket loaded
    c.rect(11, 5, 13, 10, HOT)
    c.hline(11, 13, 5, HOT_L)
    c.hline(11, 13, 10, HOT_D)
    # nose cone
    c.set(12, 4, HOT_L); c.set(13, 4, HOT_L)
    # fins
    c.set(10, 5, HOT_D); c.set(10, 6, HOT_D)
    c.set(10, 9, HOT_D); c.set(10, 10, HOT_D)
    # fuse spark
    c.set(12, 11, HOT_H)
    return c


def t_trident_item():
    c = Canvas(16, 16)
    # shaft (hot steel orange, glowing)
    c.vline(7, 5, 14, HOT)
    c.vline(8, 5, 14, HOT_L)
    c.vline(9, 5, 14, HOT_D)
    # three prongs at top
    # center prong
    c.vline(8, 2, 5, HOT_L)
    c.set(8, 1, HOT_H)
    # left prong
    c.vline(5, 4, 6, HOT)
    c.line(5, 4, 8, 4, HOT)
    c.set(5, 3, HOT_L)
    # right prong
    c.vline(11, 4, 6, HOT)
    c.line(8, 4, 11, 4, HOT)
    c.set(11, 3, HOT_L)
    # crossbar
    c.hline(5, 11, 5, HOT_D)
    c.hline(5, 11, 6, HOT)
    # glow highlights
    c.set(7, 8, HOT_L); c.set(8, 10, HOT_H); c.set(9, 12, HOT_L)
    # grip wrap on shaft
    c.set(7, 11, HOT_D); c.set(8, 11, HOT_D); c.set(9, 11, HOT_D)
    c.set(7, 13, HOT_D); c.set(8, 13, HOT_D); c.set(9, 13, HOT_D)
    return c


def t_shield():
    c = Canvas(16, 16)
    # shield body
    pts = [(3, 3), (13, 3), (13, 8), (8, 14), (3, 8)]
    c.poly(pts, GRAY)
    # border
    c.outline(pts, GRAY_D)
    # central boss
    c.rect(7, 6, 9, 8, GRAY_L)
    c.set(8, 7, GRAY_H)
    c.outline([(7, 6), (9, 6), (9, 8), (7, 8)], GRAY_D)
    # hot steel cross emblem
    c.vline(8, 4, 12, HOT)
    c.hline(5, 11, 7, HOT)
    c.vline(8, 4, 5, HOT_L)   # top highlight of vertical bar
    c.hline(5, 6, 7, HOT_L)  # left highlight of horizontal bar
    # corner rivets
    c.set(4, 4, GRAY_H); c.set(12, 4, GRAY_H)
    c.set(4, 7, GRAY_H); c.set(12, 7, GRAY_H)
    # diagonal highlight (top-left)
    c.line(3, 4, 7, 3, GRAY_L)
    c.line(3, 5, 6, 3, GRAY_L)
    return c


def t_block(base, dark, light):
    """16x16 block texture: cube face with bevel."""
    c = Canvas(16, 16)
    # base face
    c.rect(0, 0, 15, 15, base)
    # top bevel
    c.hline(0, 15, 0, light)
    c.hline(0, 15, 1, shade(base, dark, light, 0.3)[:3])
    # left bevel
    c.vline(0, 0, 15, light)
    c.vline(1, 1, 15, shade(base, dark, light, 0.2)[:3])
    # bottom shadow
    c.hline(0, 15, 15, dark)
    c.hline(0, 15, 14, shade(base, dark, light, -0.4)[:3])
    # right shadow
    c.vline(15, 0, 15, dark)
    c.vline(14, 1, 15, shade(base, dark, light, -0.3)[:3])
    # ore/ingot pattern: four ingots arranged 2x2
    for (ox, oy) in [(3, 3), (9, 3), (3, 9), (9, 9)]:
        # mini ingot at (ox,oy) size 4x4
        c.rect(ox, oy, ox + 3, oy + 3, light)
        c.hline(ox, ox + 3, oy, GRAY_H if base == BLUE else GRAY_L)
        c.hline(ox, ox + 3, oy + 3, dark)
        c.vline(ox, oy, oy + 3, shade(base, dark, light, 0.3)[:3])
        c.vline(ox + 3, oy, oy + 3, dark)
    # center cross dot
    c.set(7, 7, light); c.set(8, 7, light); c.set(7, 8, light); c.set(8, 8, light)
    return c


def t_armor_layer(w, h, base, dark, light):
    """64x32 armor texture covering full UV area for the player model.

    Vanilla 1.8+ skin layout (we just need a textured fill so every body
    part renders): we draw a subtle plate pattern with edges and rivets.
    """
    c = Canvas(w, h)
    # base fill across the whole sheet
    for y in range(h):
        for x in range(w):
            c.set(x, y, noise_shade(base, dark, light, x, y, seed=1))
    # horizontal banding every 8px (subtle)
    for y in range(0, h, 8):
        for x in range(w):
            cur = c.im.getpixel((x, y))
            c.set(x, y, shade(base, dark, light, -0.35)[:3] + (cur[3],))
    # vertical highlight every 16px
    for x in range(0, w, 16):
        for y in range(h):
            cur = c.im.getpixel((x, y))
            c.set(x, y, shade(base, dark, light, 0.25)[:3] + (cur[3],))
    # outer 1px border
    for x in range(w):
        c.set(x, 0, dark); c.set(x, h - 1, dark)
    for y in range(h):
        c.set(0, y, dark); c.set(w - 1, y, dark)
    # rivet pattern at 8px grid
    for y in range(4, h - 4, 8):
        for x in range(4, w - 4, 8):
            c.set(x, y, light)
            c.set(x + 1, y, shade(base, dark, light, 0.5)[:3] + (255,))
            c.set(x, y + 1, shade(base, dark, light, -0.3)[:3] + (255,))
            c.set(x + 1, y + 1, dark)
    return c


def t_effect_icon():
    """16x16 mob effect icon: stylized fire + water combo."""
    c = Canvas(16, 16)
    # outer flame (orange)
    flame = [(8, 2), (11, 5), (12, 8), (11, 11), (8, 13), (5, 11), (4, 8), (5, 5)]
    c.poly(flame, HOT)
    # inner flame (yellow)
    inner = [(8, 4), (10, 6), (10, 9), (8, 11), (6, 9), (6, 6)]
    c.poly(inner, HOT_L)
    # hottest core (white-yellow)
    core = [(8, 6), (9, 7), (9, 9), (8, 10), (7, 9), (7, 7)]
    c.poly(core, HOT_H)
    # blue water drop overlay (representing fire resistance)
    drop = [(8, 8), (10, 10), (10, 12), (8, 13), (6, 12), (6, 10)]
    c.poly(drop, BLUE)
    c.poly([(8, 10), (9, 11), (9, 12), (8, 12), (7, 11)], BLUE_L)
    c.outline(drop, BLUE_D)
    # sparkles around
    c.set(2, 3, HOT_L); c.set(13, 3, HOT_L)
    c.set(2, 12, HOT_L); c.set(13, 12, HOT_L)
    return c


def t_trident_entity():
    """32x32 trident entity atlas (held/thrown model texture)."""
    c = Canvas(32, 32)
    # transparent background already
    # shaft running vertically through the center
    c.vline(15, 2, 30, HOT)
    c.vline(16, 2, 30, HOT_L)
    c.vline(17, 2, 30, HOT_D)
    # three prongs at top
    # center prong up
    c.vline(16, 0, 4, HOT_L)
    c.set(16, 0, HOT_H)
    # left prong
    c.vline(10, 2, 6, HOT)
    c.line(10, 2, 16, 2, HOT)
    c.set(10, 1, HOT_L)
    # right prong
    c.vline(22, 2, 6, HOT)
    c.line(16, 2, 22, 2, HOT)
    c.set(22, 1, HOT_L)
    # crossbar connecting prongs
    c.hline(10, 22, 3, HOT_D)
    c.hline(10, 22, 4, HOT)
    # grip wrap on shaft
    for y in (10, 11, 18, 19, 26, 27):
        c.hline(15, 17, y, HOT_D)
    # glow highlights
    c.set(15, 8, HOT_L); c.set(17, 12, HOT_H); c.set(16, 16, HOT_L); c.set(15, 22, HOT_L)
    # tip speculars
    c.set(10, 0, HOT_H); c.set(22, 0, HOT_H)
    return c


# ---------------------------------------------------------------------------
# Dispatch
# ---------------------------------------------------------------------------
TEXTURES = [
    # (path_relative, function)
    ("block/crude_steel_block.png", lambda: t_block(BLUE, BLUE_D, BLUE_L)),
    ("item/crude_steel.png",         lambda: t_ingot(CRUDE, CRUDE_D, CRUDE_L)),
    ("item/steel_ingot.png",         lambda: t_ingot(BLUE, BLUE_D, BLUE_L)),
    ("item/hot_steel_ingot.png",     lambda: t_ingot(HOT, HOT_D, HOT_L, with_glow=True)),
    ("item/hot_steel_sword.png",     t_sword),
    ("item/hot_steel_pickaxe.png",   t_pickaxe),
    ("item/hot_steel_axe.png",       t_axe),
    ("item/hot_steel_shovel.png",    t_shovel),
    ("item/hot_steel_hoe.png",       t_hoe),
    ("item/hot_steel_knife.png",     t_knife),
    ("item/hot_steel_helmet.png",    t_helmet),
    ("item/hot_steel_chestplate.png",t_chestplate),
    ("item/hot_steel_leggings.png",  t_leggings),
    ("item/hot_steel_boots.png",     t_boots),
    ("item/hot_steel_bow.png",       lambda: t_bow(0)),
    ("item/hot_steel_bow_pulling_0.png", lambda: t_bow(1)),
    ("item/hot_steel_bow_pulling_1.png", lambda: t_bow(2)),
    ("item/hot_steel_bow_pulling_2.png", lambda: t_bow(3)),
    ("item/hot_steel_crossbow.png",                  lambda: t_crossbow(None)),
    ("item/hot_steel_crossbow_pulling_0.png",        lambda: t_crossbow(0)),
    ("item/hot_steel_crossbow_pulling_1.png",        lambda: t_crossbow(1)),
    ("item/hot_steel_crossbow_pulling_2.png",        lambda: t_crossbow(2)),
    ("item/hot_steel_crossbow_arrow.png",            t_crossbow_arrow),
    ("item/hot_steel_crossbow_firework.png",         t_crossbow_firework),
    ("item/hot_steel_trident.png",   t_trident_item),
    ("item/hot_steel_shield.png",    t_shield),
    ("entity/hot_steel_trident.png", t_trident_entity),
    ("mob_effect/super_fire_resistance.png", t_effect_icon),
    ("models/armor/hot_steel_layer_1.png", lambda: t_armor_layer(64, 32, GRAY, GRAY_D, GRAY_L)),
    ("models/armor/hot_steel_layer_2.png", lambda: t_armor_layer(64, 32, GRAY, GRAY_D, GRAY_L)),
]


def main():
    root = "/workspace/src/main/resources/assets/hotsteel/textures"
    for rel, fn in TEXTURES:
        path = os.path.join(root, rel)
        canvas = fn()
        canvas.save(path)
        print(f"  wrote {path}  ({canvas.w}x{canvas.h})")
    print(f"\nRegenerated {len(TEXTURES)} textures.")


if __name__ == "__main__":
    main()
