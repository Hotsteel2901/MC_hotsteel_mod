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


def t_block_hot():
    """16x16 hot steel block: dark charcoal base with glowing orange veins and
    bright molten-hot ingots (emissive look)."""
    c = Canvas(16, 16)
    DARK = (0x3d, 0x17, 0x07)
    c.rect(0, 0, 15, 15, DARK)
    # glowing cracks / veins
    for x in (1, 5, 9, 13):
        c.vline(x, 1, 14, shade(HOT, HOT_D, HOT_L, 0.1)[:3])
    for y in (3, 7, 11):
        c.hline(2, 14, y, shade(HOT, HOT_D, HOT_L, -0.2)[:3])
    # four molten ingots in a 2x2 grid
    for (ox, oy) in [(2, 2), (8, 2), (2, 8), (8, 8)]:
        c.rect(ox, oy, ox + 3, oy + 3, HOT)
        c.hline(ox, ox + 3, oy, HOT_L)
        c.hline(ox, ox + 3, oy + 3, HOT_D)
        c.vline(ox, oy, oy + 3, HOT_H)
        c.vline(ox + 3, oy, oy + 3, HOT_D)
        c.set(ox + 1, oy + 1, HOT_H)
        c.set(ox + 2, oy + 2, HOT_L)
    # bright outer glow rim
    c.hline(0, 15, 0, HOT_L)
    c.vline(0, 0, 15, HOT_L)
    c.hline(0, 15, 15, HOT_D)
    c.vline(15, 0, 15, HOT_D)
    # center spark
    c.set(7, 7, HOT_H); c.set(8, 7, HOT_L); c.set(7, 8, HOT_L); c.set(8, 8, HOT_H)
    return c


def t_mace():
    """16x16 heavy Hot Steel mace: spiked hexagonal head on a wood handle with
    a glowing hot band."""
    c = Canvas(16, 16)
    # head
    head = [(5, 2), (11, 2), (12, 4), (11, 6), (5, 6), (4, 4)]
    c.poly(head, GRAY)
    c.hline(5, 11, 2, GRAY_L)
    c.outline(head, GRAY_D)
    # spikes on top
    c.set(5, 1, GRAY_D); c.set(8, 1, GRAY_D); c.set(11, 1, GRAY_D)
    c.set(6, 1, GRAY_L); c.set(10, 1, GRAY_L)
    # glowing hot band around the head
    c.hline(4, 12, 5, HOT)
    c.hline(4, 12, 4, HOT_D)
    c.set(4, 4, HOT_L); c.set(12, 4, HOT_L)
    # face rivets
    c.set(7, 3, GRAY_H); c.set(8, 4, GRAY_H); c.set(7, 5, GRAY_D)
    # handle
    draw_handle(c, x=7, y0=7, y1=14)
    # pommel cap
    c.set(7, 15, WOOD_D); c.set(8, 15, WOOD_D)
    return c


def t_hot_steel_arrow():
    """16x16 Hot Steel arrow item: diagonal arrow with a molten-hot head."""
    c = Canvas(16, 16)
    # shaft (bottom-left -> upper-right)
    c.line(3, 13, 10, 6, WOOD)
    c.line(4, 13, 11, 6, WOOD_L)
    # head (hot steel triangle pointing up-right)
    c.line(10, 6, 14, 2, GRAY_H)
    c.line(14, 2, 13, 7, HOT_D)
    c.line(10, 6, 13, 7, HOT)
    c.set(14, 2, HOT_H); c.set(14, 3, HOT_L); c.set(13, 3, HOT_L)
    # fletching (three feathers)
    c.set(2, 13, STR); c.set(3, 14, STR_D); c.set(2, 14, STR_D)
    c.set(1, 12, STR_D); c.set(2, 12, STR); c.set(3, 12, STR_D)
    c.set(3, 13, STR_D); c.set(4, 12, STR_D)
    return c


def t_hot_steel_arrow_entity():
    """32x32 Hot Steel arrow entity atlas (projectile model texture)."""
    c = Canvas(32, 32)
    # shaft
    c.line(6, 26, 20, 12, WOOD)
    c.line(7, 26, 21, 12, WOOD_L)
    # head (hot steel)
    c.line(20, 12, 28, 4, GRAY_H)
    c.line(28, 4, 26, 14, HOT_D)
    c.line(20, 12, 26, 14, HOT)
    c.set(28, 4, HOT_H); c.set(28, 5, HOT_L); c.set(26, 5, HOT_L)
    c.set(27, 6, HOT_L); c.set(24, 8, HOT); c.set(25, 9, HOT)
    # fletching
    for (fx, fy) in [(3, 26), (5, 28), (2, 24), (4, 27)]:
        c.set(fx, fy, STR_D)
    c.set(4, 26, STR); c.set(5, 27, STR); c.set(3, 25, STR)
    c.set(6, 28, STR_D); c.set(7, 27, STR_D)
    return c


def t_forge():
    """16x16 Hot Steel forge block: dark furnace face with a glowing molten mouth."""
    c = Canvas(16, 16)
    DARK = (0x2b, 0x12, 0x06)
    c.rect(0, 0, 15, 15, DARK)
    # hot steel frame border
    c.hline(0, 15, 0, HOT)
    c.hline(0, 15, 15, HOT_D)
    c.vline(0, 0, 15, HOT)
    c.vline(15, 0, 15, HOT_D)
    c.hline(0, 15, 1, HOT_L); c.hline(0, 15, 14, HOT)
    # rivets at corners
    for (rx, ry) in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        c.set(rx, ry, HOT_H)
    # glowing mouth (molten opening)
    mouth = [(3, 5), (12, 5), (12, 12), (3, 12)]
    c.poly(mouth, (0x1a, 0x08, 0x02))
    # molten lava inside the mouth
    c.rect(4, 7, 11, 10, HOT)
    c.hline(4, 11, 7, HOT_L)
    c.hline(4, 11, 10, HOT_D)
    c.set(5, 8, HOT_H); c.set(9, 9, HOT_H); c.set(7, 8, HOT_L)
    # radiating glow above the mouth
    c.set(5, 3, HOT_L); c.set(6, 3, HOT_L); c.set(7, 2, HOT_L)
    c.set(8, 2, HOT_L); c.set(9, 3, HOT_L); c.set(10, 3, HOT_L)
    # chimney glow dots
    c.set(7, 4, HOT); c.set(8, 4, HOT)
    return c


def t_lava_bottle():
    """16x16 Lava Bottle item: glass bottle filled with glowing lava."""
    c = Canvas(16, 16)
    # bottle body
    body = [(5, 5), (11, 5), (11, 13), (5, 13)]
    c.poly(body, (0xb8, 0xd0, 0xd8, 180))   # translucent glass
    c.outline(body, (0x8a, 0x9f, 0xa8))
    # neck
    c.rect(6, 2, 10, 4, (0xb8, 0xd0, 0xd8, 180))
    c.vline(6, 2, 4, (0x8a, 0x9f, 0xa8))
    c.vline(10, 2, 4, (0x8a, 0x9f, 0xa8))
    c.hline(6, 10, 2, (0x8a, 0x9f, 0xa8))
    # cork
    c.rect(6, 1, 10, 2, WOOD)
    c.hline(6, 10, 1, WOOD_L)
    # lava inside
    c.rect(6, 6, 10, 12, HOT)
    c.hline(6, 10, 6, HOT_L)
    c.hline(6, 10, 12, HOT_D)
    # glowing bubbles
    c.set(7, 8, HOT_H); c.set(9, 10, HOT_H); c.set(8, 7, HOT_L)
    # glass shine highlight
    c.vline(5, 6, 10, (0xff, 0xff, 0xff, 120))
    # drips / sparks
    c.set(3, 6, HOT_L); c.set(12, 5, HOT_H)
    return c


def _molten(r, g, b, crack):
    """Map an iron-golem pixel to the molten palette. crack=glowing crack overlay brightness."""
    lum = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
    if crack > 0.6:
        # glowing crack -> near-white yellow
        return (0xff, 0xe7, 0x9c, 255)
    if lum > 0.85:
        return (0xff, 0xe7, 0x9c, 255)
    if lum > 0.62:
        return (0xff, 0xc2, 0x4a, 255)
    if lum > 0.42:
        return (0xe0, 0x61, 0x1f, 255)
    if lum > 0.22:
        return (0x9a, 0x3a, 0x10, 255)
    return (0x3d, 0x17, 0x07, 255)


def t_lava_golem():
    """128x128 Lava Golem entity texture: molten recolor of the vanilla iron golem
    with glowing cracks. Falls back to a procedural golem if the vanilla texture
    cannot be found."""
    import os
    ref = "/tmp/vanillatex/assets/minecraft/textures/entity/iron_golem"
    base_path = os.path.join(ref, "iron_golem.png")
    crack_path = os.path.join(ref, "iron_golem_crackiness_high.png")
    if os.path.exists(base_path):
        base = Image.open(base_path).convert("RGBA")
        crack = Image.open(crack_path).convert("L") if os.path.exists(crack_path) else None
        px = base.load()
        cp = crack.load() if crack else None
        for y in range(128):
            for x in range(128):
                r, g, b, a = px[x, y]
                if a == 0:
                    continue
                cv = 0.0
                if cp is not None:
                    cv = cp[x, y] / 255.0
                px[x, y] = _molten(r, g, b, cv)
        return base
    # Procedural fallback: a simple molten golem silhouette.
    c = Canvas(128, 128)
    c.rect(0, 0, 127, 127, (0, 0, 0, 0))
    # big body block
    c.rect(40, 40, 88, 96, HOT)
    c.rect(42, 42, 86, 94, HOT_L)
    # head
    c.rect(52, 24, 76, 40, HOT)
    c.rect(54, 26, 74, 38, HOT_L)
    # arms
    c.rect(28, 40, 40, 90, HOT)
    c.rect(88, 40, 100, 90, HOT)
    # legs
    c.rect(42, 96, 60, 116, HOT)
    c.rect(68, 96, 86, 116, HOT)
    # face
    c.rect(56, 28, 60, 34, (0xff, 0xe7, 0x9c))
    c.rect(68, 28, 72, 34, (0xff, 0xe7, 0x9c))
    return c


def t_hot_steel_nugget():
    """16x16 Hot Steel nugget: a small molten glob with a bright specular."""
    c = Canvas(16, 16)
    pts = [(5, 8), (6, 6), (9, 5), (12, 6), (13, 8), (12, 11), (9, 12), (6, 11)]
    c.poly(pts, HOT)
    c.poly([(7, 7), (9, 6), (11, 7), (9, 8)], HOT_L)
    c.poly([(8, 8), (9, 7), (10, 8), (9, 9)], HOT_H)
    c.outline(pts, HOT_D)
    # molten drips
    c.set(5, 12, HOT); c.set(12, 12, HOT)
    c.set(4, 11, HOT_D); c.set(13, 11, HOT_D)
    return c


def t_molten_core():
    """16x16 Molten Core: a glowing fiery heart/crystal."""
    c = Canvas(16, 16)
    # dark rocky shell
    shell = [(4, 6), (6, 3), (10, 3), (12, 6), (12, 10), (10, 13), (6, 13), (4, 10)]
    c.poly(shell, (0x2a, 0x12, 0x06))
    c.outline(shell, HOT_D)
    # molten heart
    heart = [(6, 6), (8, 5), (10, 6), (10, 9), (8, 11), (6, 9)]
    c.poly(heart, HOT)
    c.poly([(7, 7), (8, 6), (9, 7), (8, 9)], HOT_L)
    c.poly([(8, 8), (9, 8), (9, 9), (8, 9)], HOT_H)
    # crack glow
    c.line(6, 4, 4, 6, HOT_L)
    c.line(10, 4, 12, 6, HOT_L)
    # floating sparks
    c.set(3, 3, HOT_L); c.set(13, 3, HOT_L)
    c.set(2, 8, HOT); c.set(14, 9, HOT)
    c.set(5, 14, HOT_L); c.set(11, 14, HOT_L)
    return c


def t_fishing_rod():
    """16x16 Hot Steel fishing rod: hot-steel rod with a taut string and bobber."""
    c = Canvas(16, 16)
    # rod shaft (diagonal from bottom-left up to top-right)
    pts = [(4, 15), (5, 14), (14, 5), (14, 7), (5, 15)]
    c.poly(pts, GRAY)
    c.hline(5, 13, 15, GRAY_L)   # top edge highlight
    # hot steel band at the grip end
    c.poly([(4, 13), (6, 13), (6, 15), (4, 15)], HOT)
    c.set(5, 13, HOT_L)
    # line running from the tip down
    c.line(14, 6, 9, 11, STR)
    # bobber at the end
    c.poly([(8, 10), (10, 10), (10, 12), (8, 12)], HOT)
    c.set(9, 10, HOT_L)
    c.set(8, 12, HOT_D); c.set(10, 12, HOT_D)
    # reel
    c.rect(3, 10, 5, 12, WOOD_D)
    c.set(4, 11, WOOD_L)
    return c


def t_sickle():
    """16x16 Hot Steel sickle: curved blade on a short handle."""
    c = Canvas(16, 16)
    draw_handle(c, x=6, y0=10, y1=14)
    # curved crescent blade
    arc = [(11, 2), (13, 4), (14, 7), (14, 9), (13, 11), (11, 13), (9, 14), (8, 13), (10, 12), (12, 10), (13, 8), (13, 6), (12, 4), (10, 3)]
    c.outline(arc, GRAY)
    c.poly(arc, GRAY)
    # inner edge highlight (the cutting edge)
    inner = [(10, 3), (12, 4), (13, 6), (13, 8), (12, 10), (10, 12), (9, 12), (10, 10), (11, 8), (11, 6), (10, 4)]
    c.poly(inner, GRAY_L)
    # hot steel glow on the blade
    c.line(11, 4, 12, 8, HOT_L)
    c.line(12, 8, 11, 11, HOT)
    # blade tip sparkle
    c.set(11, 2, HOT_H); c.set(12, 3, HOT_L)
    return c


def t_smelter():
    """16x16 Hot Steel smelter block: a molten-hot metal plate face."""
    c = Canvas(16, 16)
    DARK = (0x2b, 0x12, 0x06)
    c.rect(0, 0, 15, 15, DARK)
    # hot steel frame
    c.hline(0, 15, 0, HOT); c.hline(0, 15, 15, HOT_D)
    c.vline(0, 0, 15, HOT); c.vline(15, 0, 15, HOT_D)
    c.hline(0, 15, 1, HOT_L); c.hline(0, 15, 14, HOT)
    # rivets at corners
    for (rx, ry) in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        c.set(rx, ry, HOT_H)
    # molten pool in the middle
    pool = [(3, 5), (12, 5), (12, 11), (3, 11)]
    c.poly(pool, (0x1a, 0x08, 0x02))
    c.rect(4, 6, 11, 10, HOT)
    c.hline(4, 11, 6, HOT_L)
    c.hline(4, 11, 10, HOT_D)
    # glowing bubbles
    c.set(5, 7, HOT_H); c.set(9, 8, HOT_H); c.set(7, 9, HOT_L); c.set(10, 7, HOT_L)
    # heat shimmer
    c.set(6, 3, HOT_L); c.set(9, 3, HOT_L); c.set(7, 4, HOT)
    c.set(5, 12, HOT); c.set(10, 12, HOT)
    return c


def t_door():
    """16x16 Hot Steel door item: a metal door panel with hot glow rivets."""
    c = Canvas(16, 16)
    c.rect(2, 1, 13, 15, GRAY)
    # frame
    c.hline(2, 13, 1, GRAY_L); c.hline(2, 13, 15, GRAY_D)
    c.vline(2, 1, 15, GRAY_L); c.vline(13, 1, 15, GRAY_D)
    # horizontal panels
    for y in (4, 9, 13):
        c.hline(4, 11, y, GRAY_D)
        c.hline(4, 11, y - 1, GRAY)
    # hot steel glow strip
    c.hline(4, 11, 6, HOT)
    c.hline(4, 11, 7, HOT_L)
    # rivets
    for (rx, ry) in [(4, 2), (11, 2), (4, 12), (11, 12)]:
        c.set(rx, ry, HOT_H)
    # handle
    c.rect(10, 7, 11, 9, HOT_L)
    c.set(10, 7, HOT_H)
    return c


def t_door_top():
    """16x16 upper half of the hot steel door."""
    c = Canvas(16, 16)
    c.rect(2, 1, 13, 15, GRAY)
    c.hline(2, 13, 1, GRAY_L); c.hline(2, 13, 15, GRAY_D)
    c.vline(2, 1, 15, GRAY_L); c.vline(13, 1, 15, GRAY_D)
    # window / lattice
    c.hline(4, 11, 4, GRAY_D); c.hline(4, 11, 9, GRAY_D)
    c.vline(7, 4, 9, GRAY_D)
    # glow trim
    c.hline(4, 11, 10, HOT)
    c.hline(4, 11, 11, HOT_L)
    # rivets
    for (rx, ry) in [(4, 3), (11, 3)]:
        c.set(rx, ry, HOT_H)
    return c


def t_door_bottom():
    """16x16 lower half of the hot steel door."""
    c = Canvas(16, 16)
    c.rect(2, 1, 13, 15, GRAY)
    c.hline(2, 13, 1, GRAY_L); c.hline(2, 13, 15, GRAY_D)
    c.vline(2, 1, 15, GRAY_L); c.vline(13, 1, 15, GRAY_D)
    # solid lower panel with glow rivets
    c.hline(4, 11, 4, GRAY_D)
    for y in (6, 10, 13):
        c.hline(4, 11, y, GRAY)
        c.hline(4, 11, y + 1, GRAY_D)
    c.hline(4, 11, 5, HOT)
    c.hline(4, 11, 6, HOT_L)
    # rivets
    for (rx, ry) in [(4, 3), (11, 3), (4, 12), (11, 12)]:
        c.set(rx, ry, HOT_H)
    return c


def t_trapdoor():
    """16x16 Hot Steel trapdoor block face."""
    c = Canvas(16, 16)
    c.rect(0, 0, 15, 15, GRAY)
    # plank / metal plate structure
    for x in (3, 7, 11):
        c.vline(x, 0, 15, GRAY_D)
        c.vline(x + 1, 0, 15, GRAY)
    # hot steel cross brace
    c.hline(1, 14, 6, HOT); c.hline(1, 14, 7, HOT_L)
    c.hline(1, 14, 9, HOT); c.hline(1, 14, 10, HOT_L)
    # edge bevels
    c.hline(0, 15, 0, GRAY_L); c.hline(0, 15, 15, GRAY_D)
    c.vline(0, 0, 15, GRAY_L); c.vline(15, 0, 15, GRAY_D)
    # rivets
    for (rx, ry) in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        c.set(rx, ry, HOT_H)
    return c


def t_fence():
    """16x16 Hot Steel fence item: vertical bars with a hot glow."""
    c = Canvas(16, 16)
    # three vertical posts
    for x in (3, 7, 11):
        c.vline(x, 1, 14, GRAY)
        c.set(x, 1, GRAY_L); c.set(x, 14, GRAY_D)
    # horizontal rails
    c.hline(3, 11, 5, GRAY)
    c.hline(3, 11, 10, GRAY)
    # hot glow highlight on center post
    c.vline(7, 3, 12, HOT)
    c.set(7, 6, HOT_L); c.set(7, 9, HOT_L)
    # base
    c.hline(2, 12, 15, GRAY_D)
    c.hline(2, 12, 14, GRAY)
    return c


def t_pressure_plate():
    """16x16 Hot Steel pressure plate block face."""
    c = Canvas(16, 16)
    c.rect(1, 1, 14, 14, GRAY)
    # plate bevel
    c.hline(1, 14, 1, GRAY_L); c.vline(1, 1, 14, GRAY_L)
    c.hline(1, 14, 14, GRAY_D); c.vline(14, 1, 14, GRAY_D)
    # inner plate with hot steel inlay
    c.rect(3, 3, 12, 12, shade(GRAY, GRAY_D, GRAY_L, 0.15))
    c.hline(3, 12, 3, GRAY); c.hline(3, 12, 12, GRAY_D)
    c.vline(3, 3, 12, GRAY); c.vline(12, 3, 12, GRAY_D)
    # hot steel hot-spot in the middle
    c.rect(6, 6, 9, 9, HOT)
    c.set(7, 7, HOT_L); c.set(8, 7, HOT_L); c.set(7, 8, HOT_L); c.set(8, 8, HOT_H)
    # corner rivets
    for (rx, ry) in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        c.set(rx, ry, HOT_H)
    return c


def t_bricks():
    """16x16 Hot Steel bricks: offset brick pattern with hot mortar."""
    c = Canvas(16, 16)
    c.rect(0, 0, 15, 15, (0x3d, 0x17, 0x07))
    # brick rows (each 8 wide, offset each row)
    rows = [(0, 0, 7), (8, 0, 15), (0, 4, 7), (8, 4, 15),
            (0, 8, 7), (8, 8, 15), (0, 12, 7), (8, 12, 15)]
    for (x0, y0, x1) in rows:
        c.hline(x0, x1, y0, shade(HOT, HOT_D, HOT_L, 0.3)[:3])
        c.hline(x0, x1, y0 + 3, HOT_D)
    # vertical mortar joints
    for x in (3, 7, 11, 15):
        for y in (0, 4, 8, 12):
            c.set(x, y, shade(HOT, HOT_D, HOT_L, 0.3)[:3])
    # bright speculars
    c.set(1, 1, HOT_L); c.set(9, 5, HOT_L); c.set(2, 13, HOT_L)
    return c


def t_lantern():
    """16x16 Hot Steel lantern block texture: hot-steel cage with a glowing core."""
    c = Canvas(16, 16)
    # outer cage (dark metal)
    c.rect(4, 4, 11, 11, (0x3d, 0x17, 0x07))
    # frame corners
    c.set(4, 4, GRAY); c.set(11, 4, GRAY)
    c.set(4, 11, GRAY_D); c.set(11, 11, GRAY_D)
    # hot steel cage bars
    c.vline(5, 4, 11, HOT)
    c.vline(10, 4, 11, HOT)
    c.hline(4, 11, 5, HOT)
    c.hline(4, 11, 10, HOT)
    # glowing core
    c.rect(6, 6, 9, 9, HOT_H)
    c.rect(7, 7, 8, 8, (0xff, 0xff, 0xff))
    c.set(6, 6, HOT_L); c.set(9, 6, HOT_L)
    c.set(6, 9, HOT); c.set(9, 9, HOT)
    # hanging top
    c.hline(5, 10, 3, GRAY)
    c.set(5, 3, GRAY_L); c.set(10, 3, GRAY_L)
    c.vline(7, 1, 2, GRAY_L); c.vline(8, 1, 2, GRAY_L)
    # bottom detail
    c.hline(5, 10, 12, GRAY_D)
    return c


def t_fire_wraith():
    """64x32 Fire Wraith entity texture: a blazing recolor of the vanilla Blaze
    texture with hot-orange flames. Falls back to a procedural wraith if the
    vanilla texture cannot be found."""
    import os
    ref = "/tmp/vanillatex/assets/minecraft/textures/entity/blaze.png"
    if os.path.exists(ref):
        base = Image.open(ref).convert("RGBA")
        px = base.load()
        w, h = base.size
        for y in range(h):
            for x in range(w):
                r, g, b, a = px[x, y]
                if a == 0:
                    continue
                lum = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
                if lum > 0.8:
                    px[x, y] = (0xff, 0xe7, 0x9c, 255)
                elif lum > 0.55:
                    px[x, y] = (0xff, 0xc2, 0x4a, 255)
                elif lum > 0.3:
                    px[x, y] = (0xe0, 0x61, 0x1f, 255)
                else:
                    px[x, y] = (0x9a, 0x3a, 0x10, 255)
        return base
    # Procedural fallback: a simple blazing wraith (Blaze model uses a 64x32 atlas).
    c = Canvas(64, 32)
    # core body (head region centered)
    body = [(26, 10), (38, 10), (40, 26), (32, 32), (24, 26)]
    c.poly(body, HOT)
    c.poly([(28, 13), (36, 13), (37, 22), (32, 27), (27, 22)], HOT_L)
    c.poly([(30, 15), (34, 15), (34, 20), (32, 22), (30, 20)], HOT_H)
    # eyes
    c.poly([(27, 15), (30, 15), (30, 18), (27, 18)], (0xff, 0xff, 0xff))
    c.poly([(34, 15), (37, 15), (37, 18), (34, 18)], (0xff, 0xff, 0xff))
    # flame arms
    for (sx, sy) in [(20, 17), (44, 17)]:
        c.line(sx, sy, sx + (2 if sx < 32 else -2), sy + 8, HOT)
        c.line(sx, sy, sx + (4 if sx < 32 else -4), sy + 6, HOT_L)
    # flame wisp on top
    c.poly([(30, 8), (34, 8), (32, 1)], HOT_L)
    c.poly([(31, 7), (33, 7), (32, 2)], HOT_H)
    # trail below
    c.poly([(28, 28), (36, 28), (32, 31)], HOT)
    return c


def t_paxel():
    """16x16 Hot Steel paxel: pickaxe head merged with an axe blade and a
    shovel scoop — a glowing three-in-one multitool."""
    c = Canvas(16, 16)
    draw_handle(c, x=7, y0=6, y1=14)
    # pickaxe head band
    pts = [(2, 6), (4, 4), (12, 4), (14, 6), (13, 7), (8, 5), (3, 7)]
    c.poly(pts, GRAY)
    c.hline(4, 12, 4, GRAY_L)
    c.set(3, 5, GRAY_D); c.set(13, 5, GRAY_D)
    c.set(2, 6, GRAY_D); c.set(14, 6, GRAY_D)
    # axe blade on the right
    axe = [(7, 4), (13, 4), (14, 7), (13, 9), (8, 9), (7, 8)]
    c.poly(axe, HOT)
    c.hline(8, 13, 4, HOT_L)
    c.vline(13, 5, 8, HOT_H)
    # shovel scoop on the left
    c.poly([(2, 6), (6, 6), (6, 9), (5, 10), (3, 10), (2, 9)], GRAY)
    c.hline(3, 5, 6, GRAY_L)
    c.set(6, 7, GRAY_D); c.set(6, 8, GRAY_D)
    # glowing core rivet
    c.set(8, 5, HOT_H)
    return c


def t_apple():
    """16x16 Hot Steel apple: a molten-hot red-orange apple with a glowing core."""
    c = Canvas(16, 16)
    # apple body
    body = [(5, 5), (6, 3), (8, 2), (11, 3), (12, 5), (13, 8), (12, 11), (10, 13), (6, 13), (4, 11), (3, 8)]
    c.poly(body, (0xd8, 0x32, 0x1e))
    c.poly([(6, 5), (8, 4), (11, 5), (12, 8), (11, 11), (8, 12), (5, 11), (4, 8)], HOT)
    c.poly([(7, 7), (9, 6), (10, 8), (9, 10), (7, 10), (6, 8)], HOT_H)
    c.poly([(8, 8), (9, 8), (9, 9), (8, 9)], (0xff, 0xff, 0xff))
    # hot steel rim band
    c.poly([(3, 9), (4, 12), (6, 13), (8, 13), (5, 12), (4, 10)], HOT_D)
    c.outline(body, (0x7a, 0x14, 0x08))
    # stem
    c.line(8, 2, 8, 1, WOOD_D)
    c.set(8, 1, WOOD)
    c.set(7, 2, WOOD)
    # leaf
    c.poly([(9, 2), (12, 1), (13, 3), (11, 4)], (0x4f, 0x8f, 0x2f))
    c.set(10, 2, (0x6f, 0xb5, 0x45))
    # sparkles
    c.set(2, 6, HOT_L); c.set(14, 7, HOT_L)
    c.set(5, 4, HOT_H); c.set(11, 12, HOT_L)
    return c


def t_chain():
    """16x16 Hot Steel chain block texture: overlapping molten metal links."""
    c = Canvas(16, 16)
    c.rect(0, 0, 15, 15, (0, 0, 0, 0))
    # diagonal links (two interlocking ovals)
    for oy in range(-16, 16, 8):
        for ox in range(-16, 16, 8):
            c.outline([(ox + 2, oy + 1), (ox + 6, oy + 1), (ox + 7, oy + 4), (ox + 6, oy + 7),
                       (ox + 2, oy + 7), (ox + 1, oy + 4)], HOT_D)
            c.outline([(ox + 3, oy + 2), (ox + 5, oy + 2), (ox + 6, oy + 4), (ox + 5, oy + 6),
                       (ox + 3, oy + 6), (ox + 2, oy + 4)], HOT)
            c.set(ox + 4, oy + 3, HOT_L)
            c.set(ox + 4, oy + 5, HOT)
    # bright highlights
    for x in range(16):
        for y in range(16):
            if x % 2 == 0 and y % 4 == 2:
                c.set(x, y, HOT_H)
    return c


def t_ladder():
    """16x16 Hot Steel ladder block texture: two glowing rails with rungs."""
    c = Canvas(16, 16)
    # rails (left and right)
    for y in range(0, 16):
        c.set(2, y, GRAY_D)
        c.set(3, y, GRAY)
        c.set(12, y, GRAY)
        c.set(13, y, GRAY_D)
    # rungs every 3px
    for y in (0, 3, 6, 9, 12, 15):
        for x in range(3, 13):
            c.set(x, y, GRAY)
            c.set(x, y + 1, GRAY_L)
        c.set(3, y, HOT); c.set(12, y, HOT)   # hot steel rivets where rung meets rail
        c.set(3, y + 1, HOT_L); c.set(12, y + 1, HOT_L)
    # hot glow down the rails
    for y in range(0, 16):
        if y % 3 == 1:
            c.set(4, y, HOT_L); c.set(11, y, HOT_L)
    return c


def t_fireball():
    """16x16 Hot Steel fireball item: a molten-hot orb wrapped in flame."""
    c = Canvas(16, 16)
    # outer flame
    flame = [(8, 1), (12, 3), (14, 7), (13, 11), (9, 14), (5, 14), (2, 10), (3, 5)]
    c.poly(flame, HOT_D)
    # inner fiery orb
    orb = [(5, 4), (11, 4), (13, 8), (11, 12), (5, 12), (3, 8)]
    c.poly(orb, HOT)
    c.poly([(7, 6), (10, 6), (11, 9), (9, 11), (6, 11), (5, 8)], HOT_L)
    c.poly([(8, 7), (9, 7), (9, 9), (8, 10), (7, 9), (7, 8)], HOT_H)
    # white-hot core
    c.set(8, 8, (0xff, 0xff, 0xff))
    # flame tips
    c.set(5, 2, HOT_L); c.set(11, 2, HOT_L)
    c.set(2, 8, HOT); c.set(14, 9, HOT)
    c.set(6, 15, HOT_L); c.set(10, 15, HOT)
    c.set(8, 1, HOT_L)
    # sparks
    c.set(3, 3, HOT_H); c.set(13, 4, HOT_H)
    c.set(4, 13, HOT_L); c.set(12, 12, HOT_L)
    return c


# ---------------------------------------------------------------------------
# Dispatch
# ---------------------------------------------------------------------------
TEXTURES = [
    # (path_relative, function)
    ("block/crude_steel_block.png", lambda: t_block(BLUE, BLUE_D, BLUE_L)),
    ("block/steel_block.png",       lambda: t_block(BLUE, BLUE_D, BLUE_L)),
    ("block/hot_steel_block.png",   t_block_hot),
    ("item/crude_steel.png",         lambda: t_ingot(CRUDE, CRUDE_D, CRUDE_L)),
    ("item/steel_ingot.png",         lambda: t_ingot(BLUE, BLUE_D, BLUE_L)),
    ("item/hot_steel_ingot.png",     lambda: t_ingot(HOT, HOT_D, HOT_L, with_glow=True)),
    ("item/hot_steel_sword.png",     t_sword),
    ("item/hot_steel_pickaxe.png",   t_pickaxe),
    ("item/hot_steel_axe.png",       t_axe),
    ("item/hot_steel_shovel.png",    t_shovel),
    ("item/hot_steel_hoe.png",       t_hoe),
    ("item/hot_steel_knife.png",     t_knife),
    ("item/hot_steel_mace.png",      t_mace),
    ("item/hot_steel_arrow.png",     t_hot_steel_arrow),
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
    ("entity/hot_steel_arrow.png",   t_hot_steel_arrow_entity),
    ("entity/lava_golem.png",        t_lava_golem),
    ("entity/fire_wraith.png",       t_fire_wraith),
    ("block/hot_steel_forge.png",    t_forge),
    ("block/hot_steel_smelter.png",  t_smelter),
    ("block/hot_steel_bricks.png",   t_bricks),
    ("block/hot_steel_door_top.png", t_door_top),
    ("block/hot_steel_door_bottom.png", t_door_bottom),
    ("block/hot_steel_trapdoor.png", t_trapdoor),
    ("block/hot_steel_lantern.png",  t_lantern),
    ("item/lava_bottle.png",         t_lava_bottle),
    ("item/hot_steel_nugget.png",    t_hot_steel_nugget),
    ("item/molten_core.png",         t_molten_core),
    ("item/hot_steel_fishing_rod.png", t_fishing_rod),
    ("item/hot_steel_sickle.png",    t_sickle),
    ("item/hot_steel_door.png",      t_door),
    ("item/hot_steel_paxel.png",     t_paxel),
    ("item/hot_steel_apple.png",     t_apple),
    ("item/hot_steel_fireball.png",  t_fireball),
    ("block/hot_steel_chain.png",    t_chain),
    ("block/hot_steel_ladder.png",   t_ladder),
    ("mob_effect/super_fire_resistance.png", t_effect_icon),
    ("models/armor/hot_steel_layer_1.png", lambda: t_armor_layer(64, 32, GRAY, GRAY_D, GRAY_L)),
    ("models/armor/hot_steel_layer_2.png", lambda: t_armor_layer(64, 32, GRAY, GRAY_D, GRAY_L)),
]


def main():
    root = "/workspace/src/main/resources/assets/hotsteel/textures"
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
        print(f"  wrote {path}  ({w}x{h})")
    print(f"\nRegenerated {len(TEXTURES)} textures.")


if __name__ == "__main__":
    main()
