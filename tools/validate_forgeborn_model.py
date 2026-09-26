#!/usr/bin/env python3
"""Validate the composite boss model without launching the game.

Checks two classes of mistake that would only blow up at runtime:

1. Every `getChild("x")` / `getChild("x").addOrReplaceChild(...)` must refer to a
   part that some `addOrReplaceChild("x"` actually creates. `ModelPart.getChild`
   throws IllegalArgumentException on an unknown name, which would crash the
   renderer on the first frame the boss appears.

2. Every `addBox(..., texOffsU, texOffsV)` (the 9-arg form, whose u/v are
   ABSOLUTE) must fit inside the atlas rectangle the generator reserved, at that
   u/v. A box that overflows samples whatever art is next door — the classic
   "the model wears somebody else's texture" bug.
"""
import os
import re
import sys

JAVA = "/workspace/src/main/java/com/hotsteel/client/model/AncientForgebornModel.java"

# --- rectangles reserved by tools/gen_forgeborn_forms.py -------------------
# (name, w, h, d, u, v) copied from the generator's printed UV tables.
ASCENDANT = [
    ("skirt", 18, 12, 14, 0, 37), ("waist", 12, 6, 10, 0, 112),
    ("torso", 26, 22, 14, 0, 0), ("core", 12, 12, 3, 82, 112),
    ("head", 14, 9, 14, 0, 88), ("crown", 4, 7, 4, 25, 129),
    ("eye", 5, 3, 1, 42, 129), ("pauldron", 9, 7, 9, 45, 112),
    ("upper_arm", 6, 14, 6, 57, 88), ("forearm", 6, 13, 6, 82, 88),
    ("fist", 6, 7, 6, 0, 129), ("lower_arm", 5, 12, 5, 107, 88),
    ("wing", 3, 18, 14, 81, 0), ("halo", 22, 1, 22, 0, 64),
]
HORROR = [
    ("leg", 9, 13, 9, 29, 39), ("foot", 10, 5, 10, 0, 84),
    ("torso", 28, 22, 16, 0, 0), ("back_plate", 16, 12, 4, 72, 64),
    ("skull", 13, 8, 13, 66, 39), ("eye", 4, 2, 1, 58, 84),
    ("heart", 15, 15, 4, 0, 64), ("upper_arm", 7, 19, 7, 89, 0),
    ("forearm", 7, 17, 7, 0, 39), ("claw", 8, 10, 8, 39, 64),
    ("spike", 4, 10, 4, 41, 84),
]
# tools/gen_all_textures.py :: t_ancient_forgeborn
COLOSSUS = [
    ("leg", 7, 20, 7, 0, 0), ("torso", 20, 26, 12, 0, 28),
    ("chest_core", 10, 10, 2, 64, 28), ("head", 14, 8, 14, 0, 67),
    ("horn", 3, 4, 3, 56, 67), ("eye", 3, 3, 1, 56, 75),
    ("pauldron", 6, 6, 6, 88, 67), ("upper_arm", 6, 14, 6, 64, 41),
    ("forearm", 6, 12, 6, 64, 63), ("fist", 6, 7, 6, 92, 41),
]


def rect(w, h, d):
    return 2 * (w + d), h + d


def check_boxes(src, table, label):
    # addBox("name", x, y, z, w, h, d, DEF, U, V)
    pat = re.compile(
        r'addBox\(\s*"([^"]+)"\s*,\s*([-\d.]+)f\s*,\s*([-\d.]+)f\s*,\s*([-\d.]+)f\s*,'
        r'\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*,\s*CubeDeformation\.\w+\s*,\s*(\d+)\s*,\s*(\d+)\s*\)')
    problems = []
    seen = 0
    for m in pat.finditer(src):
        name, w, h, d, u, v = m.group(1), int(m.group(5)), int(m.group(6)), int(m.group(7)), int(m.group(8)), int(m.group(9))
        seen += 1
        need_w, need_h = rect(w, h, d)
        match = None
        for (tname, tw, th, td, tu, tv) in table:
            if tu == u and tv == v:
                match = (tname, tw, th, td)
                break
        if match is None:
            problems.append(f'{label}: "{name}" texOffs({u},{v}) is not a reserved rectangle')
            continue
        tname, tw, th, td = match
        got_w, got_h = rect(tw, th, td)
        if need_w > got_w or need_h > got_h:
            problems.append(
                f'{label}: "{name}" needs {need_w}x{need_h} at ({u},{v}) but only '
                f'{got_w}x{got_h} is reserved (for "{tname}")')
    return seen, problems


def check_children(src):
    defined = set(re.findall(r'addOrReplaceChild\(\s*"([^"]+)"', src))
    used = set(re.findall(r'getChild\(\s*"([^"]+)"', src))
    # getChild is also called on the result of another getChild, so all are covered.
    return defined, used, sorted(used - defined)


def main():
    src = open(JAVA).read()
    ok = True

    # Split the source into the three per-form builders; the tables are per-form,
    # so checking the whole file against one table produces false positives.
    def section(start_marker, end_marker):
        i = src.index(start_marker)
        j = src.index(end_marker, i)
        return src[i:j]

    colossus_src = section("private static void buildColossus", "private static void buildAscendant")
    ascendant_src = section("private static void buildAscendant", "private static void buildHorror")
    horror_src = section("private static void buildHorror", "public ModelPart root()")

    total = 0
    for label, chunk, table in (("phase1", colossus_src, COLOSSUS),
                                ("phase2", ascendant_src, ASCENDANT),
                                ("phase3", horror_src, HORROR)):
        n, probs = check_boxes(chunk, table, label)
        total += n
        print(f"{label}: {n} named boxes checked")
        for p in probs:
            print("  FAIL", p)
            ok = False
    print(f"total named boxes: {total}")

    defined, used, missing = check_children(src)
    print(f"parts defined: {len(defined)}, part lookups: {len(used)}")
    if missing:
        for m in missing:
            print(f"  FAIL getChild(\"{m}\") has no addOrReplaceChild")
        ok = False

    print("\nMODEL VALIDATION:", "PASS" if ok else "FAIL")
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())