#!/usr/bin/env python3
import os, shutil

OUT = "/home/hotsteel/mcmods/textures_svg_staging"
DEST = "/storage/emulated/0/TIE_TU"
os.makedirs(OUT, exist_ok=True)

# palette
GRAY   = "#6f7c8a"; GRAY_D = "#3e4750"; GRAY_L = "#a9b7c4"
CRUDE  = "#5a5f66"; CRUDE_D= "#33373c"; CRUDE_L= "#828a93"
BLUE   = "#5d7d99"; BLUE_D = "#324556"; BLUE_L = "#9fc0d6"
HOT    = "#e0611f"; HOT_D  = "#7a2c08"; HOT_L  = "#ffc24a"
WOOD   = "#6b4a2b"; WOOD_D = "#3f2b18"
STR    = "#d9d2c0"

def svg(w, h, body):
    return (f'<svg xmlns="http://www.w3.org/2000/svg" width="{w}" height="{h}" '
            f'viewBox="0 0 {w} {h}" shape-rendering="crispEdges">{body}</svg>')

def bg(w, h, c="#00000000"):
    return f'<rect width="{w}" height="{h}" fill="{c}"/>'

def ingot(fill, edge, light):
    return (f'<polygon points="2,10 4,7 12,7 14,10 14,13 2,13" fill="{fill}" '
            f'stroke="{edge}" stroke-width="0.5"/>'
            f'<polygon points="4,7 12,7 11,9 5,9" fill="{light}"/>')

def handle():
    return f'<rect x="8" y="7" width="2" height="8" fill="{WOOD}" stroke="{WOOD_D}" stroke-width="0.3"/>'

def sword(head, edge):
    return (handle() +
            f'<polygon points="9,1 11,1 11,9 9,9" fill="{head}" stroke="{edge}" stroke-width="0.4"/>'
            f'<rect x="6" y="9" width="8" height="1.5" fill="{WOOD_D}"/>')

def knife(head, edge):
    return (f'<rect x="7" y="10" width="2" height="5" fill="{WOOD}"/>'
            f'<polygon points="8,2 11,2 11,10 8,10" fill="{head}" stroke="{edge}" stroke-width="0.4"/>')

def pickaxe(head, edge):
    return (handle() +
            f'<path d="M2 5 Q8 2 14 5 L13 7 Q8 4.5 3 7 Z" fill="{head}" stroke="{edge}" stroke-width="0.4"/>')

def axe(head, edge):
    return (handle() +
            f'<path d="M6 3 L12 3 Q14 6 12 9 L8 8 L8 4 Z" fill="{head}" stroke="{edge}" stroke-width="0.4"/>')

def shovel(head, edge):
    return (handle() +
            f'<rect x="6.5" y="2" width="5" height="5" rx="1" fill="{head}" stroke="{edge}" stroke-width="0.4"/>')

def hoe(head, edge):
    return (handle() +
            f'<path d="M4 3 L12 3 L12 5 L6 5 L6 7 L4 7 Z" fill="{head}" stroke="{edge}" stroke-width="0.4"/>')

def helmet(fill, edge, light):
    return (f'<path d="M3 6 Q8 1 13 6 L13 11 L11 11 L11 9 L5 9 L5 11 L3 11 Z" '
            f'fill="{fill}" stroke="{edge}" stroke-width="0.5"/>'
            f'<path d="M4 6 Q8 3 12 6" fill="none" stroke="{light}" stroke-width="0.6"/>')

def chestplate(fill, edge, light):
    return (f'<path d="M3 3 L5 3 L8 5 L11 3 L13 3 L13 14 L3 14 Z" fill="{fill}" '
            f'stroke="{edge}" stroke-width="0.5"/>'
            f'<line x1="8" y1="6" x2="8" y2="13" stroke="{light}" stroke-width="0.5"/>')

def leggings(fill, edge, light):
    return (f'<path d="M3 2 L13 2 L13 14 L9 14 L8 8 L7 14 L3 14 Z" fill="{fill}" '
            f'stroke="{edge}" stroke-width="0.5"/>')

def boots(fill, edge, light):
    return (f'<path d="M4 5 L8 5 L8 11 L13 11 L13 14 L4 14 Z" fill="{fill}" '
            f'stroke="{edge}" stroke-width="0.5"/>')

def bow(pull=0):
    dx = 4 - pull  # string pulled back
    return (f'<path d="M12 2 Q4 8 12 14" fill="none" stroke="{WOOD}" stroke-width="1.4"/>'
            f'<line x1="12" y1="2" x2="{12-dx}" y2="8" stroke="{STR}" stroke-width="0.4"/>'
            f'<line x1="12" y1="14" x2="{12-dx}" y2="8" stroke="{STR}" stroke-width="0.4"/>')

def crossbow(pull=None):
    body = (f'<rect x="3" y="7" width="10" height="2" fill="{WOOD}" stroke="{WOOD_D}" stroke-width="0.3"/>'
            f'<path d="M3 4 Q8 3 13 4" fill="none" stroke="{GRAY_D}" stroke-width="1"/>')
    if pull is None:
        return body + f'<line x1="3" y1="4" x2="13" y2="4" stroke="{STR}" stroke-width="0.4"/>'
    return body + f'<line x1="3" y1="4" x2="8" y2="{5+pull}" stroke="{STR}" stroke-width="0.4"/><line x1="13" y1="4" x2="8" y2="{5+pull}" stroke="{STR}" stroke-width="0.4"/>'

def crossbow_arrow():
    return crossbow() + f'<line x1="4" y1="8" x2="14" y2="8" stroke="{GRAY_D}" stroke-width="0.6"/><polygon points="14,8 12,7 12,9" fill="{GRAY_D}"/>'

def crossbow_firework():
    return crossbow() + f'<circle cx="12" cy="8" r="1.6" fill="{HOT}"/><line x1="4" y1="8" x2="10" y2="8" stroke="{GRAY_D}" stroke-width="0.6"/>'

def trident_item():
    return (f'<rect x="7" y="6" width="1.5" height="9" fill="{HOT}" stroke="{HOT_D}" stroke-width="0.3"/>'
            f'<path d="M4 6 L4 2 M7.7 6 L7.7 1 M11.5 6 L11.5 2" stroke="{HOT}" stroke-width="1.2" fill="none"/>'
            f'<path d="M4 3 L7.7 3 L11.5 3" stroke="{HOT}" stroke-width="1.2" fill="none"/>')

def trident_entity(w, h):
    # 32x32 model atlas placeholder
    return (bg(w, h) +
            f'<rect x="2" y="2" width="28" height="28" fill="none" stroke="{HOT_D}" stroke-width="0.5"/>'
            f'<rect x="14" y="4" width="4" height="24" fill="{HOT}"/>'
            f'<rect x="6" y="4" width="4" height="8" fill="{HOT_L}"/>'
            f'<rect x="14" y="2" width="4" height="6" fill="{HOT_L}"/>'
            f'<rect x="22" y="4" width="4" height="8" fill="{HOT_L}"/>')

def shield():
    return (f'<path d="M4 2 L12 2 L12 9 Q8 14 4 9 Z" fill="{GRAY}" stroke="{GRAY_D}" stroke-width="0.6"/>'
            f'<path d="M8 3 L8 12" stroke="{HOT}" stroke-width="0.8"/>'
            f'<path d="M5 6 L11 6" stroke="{HOT}" stroke-width="0.8"/>')

def block(fill, edge, light):
    return (f'<rect x="1" y="1" width="14" height="14" fill="{fill}" stroke="{edge}" stroke-width="1"/>'
            f'<polygon points="1,1 15,1 13,3 3,3" fill="{light}"/>'
            f'<polygon points="1,1 3,3 3,13 1,15" fill="{light}"/>')

def armor_layer(w, h, fill, edge):
    return (bg(w, h) +
            f'<rect x="0" y="0" width="{w}" height="{h}" fill="{fill}" opacity="0.55"/>'
            f'<rect x="0.5" y="0.5" width="{w-1}" height="{h-1}" fill="none" stroke="{edge}" stroke-width="0.5"/>')

def effect_icon(w, h):
    return (bg(w, h) +
            f'<path d="M9 2 Q13 7 11 12 Q9 16 5 12 Q3 8 7 6 Q6 9 8 10 Q10 8 9 2 Z" fill="{HOT}" stroke="{HOT_D}" stroke-width="0.4"/>'
            f'<path d="M4 4 L14 4 L14 8 Q9 13 4 8 Z" fill="none" stroke="{BLUE_L}" stroke-width="0.8" opacity="0.8"/>')

# name -> (w, h, body)
items = {
    "crude_steel":       (16,16, ingot(CRUDE, CRUDE_D, CRUDE_L)),
    "steel_ingot":       (16,16, ingot(BLUE, BLUE_D, BLUE_L)),
    "hot_steel_ingot":   (16,16, ingot(HOT, HOT_D, HOT_L)),
    "hot_steel_sword":   (16,16, sword(HOT, HOT_D)),
    "hot_steel_pickaxe": (16,16, pickaxe(HOT, HOT_D)),
    "hot_steel_axe":     (16,16, axe(HOT, HOT_D)),
    "hot_steel_shovel":  (16,16, shovel(HOT, HOT_D)),
    "hot_steel_hoe":     (16,16, hoe(HOT, HOT_D)),
    "hot_steel_knife":   (16,16, knife(HOT, HOT_D)),
    "hot_steel_helmet":  (16,16, helmet(GRAY, GRAY_D, GRAY_L)),
    "hot_steel_chestplate": (16,16, chestplate(GRAY, GRAY_D, GRAY_L)),
    "hot_steel_leggings":(16,16, leggings(GRAY, GRAY_D, GRAY_L)),
    "hot_steel_boots":   (16,16, boots(GRAY, GRAY_D, GRAY_L)),
    "hot_steel_bow":     (16,16, bow(0)),
    "hot_steel_bow_pulling_0": (16,16, bow(1)),
    "hot_steel_bow_pulling_1": (16,16, bow(2)),
    "hot_steel_bow_pulling_2": (16,16, bow(3)),
    "hot_steel_crossbow":(16,16, crossbow(None)),
    "hot_steel_crossbow_pulling_0": (16,16, crossbow(0)),
    "hot_steel_crossbow_pulling_1": (16,16, crossbow(1)),
    "hot_steel_crossbow_pulling_2": (16,16, crossbow(2)),
    "hot_steel_crossbow_arrow": (16,16, crossbow_arrow()),
    "hot_steel_crossbow_firework": (16,16, crossbow_firework()),
    "hot_steel_trident_item": (16,16, trident_item()),
    "hot_steel_shield":  (16,16, shield()),
    "crude_steel_block": (16,16, block(BLUE, BLUE_D, BLUE_L)),
    "hot_steel_layer_1": (64,32, armor_layer(64,32, GRAY, GRAY_D)),
    "hot_steel_layer_2": (64,32, armor_layer(64,32, GRAY, GRAY_D)),
    "hot_steel_trident_entity": (32,32, trident_entity(32,32)),
    "super_fire_resistance": (18,18, effect_icon(18,18)),
}

count = 0
for name, (w, h, body) in items.items():
    content = svg(w, h, bg(w, h) + body if not body.startswith('<svg') else body)
    path = os.path.join(OUT, name + ".svg")
    with open(path, "w") as f:
        f.write(content)
    count += 1

# copy to android shared storage
copied = 0
for fn in os.listdir(OUT):
    if fn.endswith(".svg"):
        shutil.copy2(os.path.join(OUT, fn), os.path.join(DEST, fn))
        copied += 1

print(f"generated={count} copied_to_TIE_TU={copied}")
print("files:", sorted(os.listdir(OUT)))
