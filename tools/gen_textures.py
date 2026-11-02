#!/usr/bin/env python3
"""Generates the 30 mob skins (3 races x 10 roles) into src/main/resources/assets/middleworld/textures/entity.

UV layout (64x64, identical for every race) -- KEEP IN SYNC with RaceModel.java:
  head 0,0 | body 0,16 | right_arm 20,16 | left_arm 32,16 | right_leg 44,16 | left_leg 0,32
  right_ear 32,0 | left_ear 32,6 | nose 44,0 | beard 44,8 | loincloth 0,48 | hat 24,48 | hat_tip 0,54
Run:  python3 tools/gen_textures.py   (needs Pillow)
"""
import os
import random
from PIL import Image

OUT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "assets", "middleworld", "textures", "entity")

RACES = {
    #          body arm leg  ear(w,h,d)  nose(w,h,d)  beard
    "goblin": dict(body=7, arm=7, leg=6, ear=(4, 3, 1), nose=(2, 2, 2), beard=False),
    "elf":    dict(body=9, arm=10, leg=10, ear=(4, 3, 1), nose=(1, 2, 1), beard=False),
    "dwarf":  dict(body=8, arm=7, leg=5, ear=(2, 2, 1), nose=(3, 3, 2), beard=True),
}
ROLES = ["warrior", "archer", "knight", "berserker", "scout", "mage", "healer", "assassin", "captain", "brute"]

# role -> (tunic, accent/belt, trousers)
CLOTHES = {
    "warrior": ((122, 92, 56), (90, 60, 30), (70, 55, 40)),
    "archer": ((62, 110, 62), (40, 80, 40), (70, 60, 40)),
    "knight": ((150, 158, 168), (110, 118, 128), (90, 95, 105)),
    "berserker": ((140, 40, 34), (100, 25, 20), (80, 55, 40)),
    "scout": ((160, 140, 95), (120, 100, 60), (90, 75, 50)),
    "mage": ((96, 62, 150), (70, 40, 120), (60, 45, 90)),
    "healer": ((235, 235, 235), (90, 170, 110), (150, 140, 120)),
    "assassin": ((40, 40, 48), (25, 25, 30), (30, 30, 36)),
    "captain": ((150, 30, 40), (225, 180, 40), (60, 45, 40)),
    "brute": ((70, 70, 76), (40, 40, 44), (60, 50, 45)),
}
CLOTH_MOD = {"goblin": 0.85, "elf": 1.10, "dwarf": 0.95}

SKIN = {"goblin": (98, 152, 72), "elf": (238, 216, 188), "dwarf": (214, 160, 120)}
EYES = {"goblin": (245, 215, 40), "elf": (80, 185, 115), "dwarf": (40, 35, 35)}

HAIR_ELF = {"warrior": (225, 205, 120), "archer": (150, 105, 55), "knight": (225, 205, 120), "berserker": (190, 70, 40),
            "scout": (205, 170, 90), "mage": (205, 210, 225), "healer": (240, 235, 200), "assassin": (50, 40, 62),
            "captain": (235, 225, 150), "brute": (120, 90, 60)}
HAIR_DWARF = {"warrior": (110, 70, 40), "archer": (150, 90, 40), "knight": (155, 155, 160), "berserker": (195, 60, 30),
              "scout": (200, 170, 90), "mage": (228, 228, 228), "healer": (212, 192, 160), "assassin": (40, 35, 35),
              "captain": (212, 170, 60), "brute": (70, 50, 40)}

HAT = {  # role -> color of the helmet/hood (None = no hat)
    "warrior": (140, 145, 155), "archer": (45, 85, 45), "knight": (172, 177, 188), "assassin": (25, 25, 30),
    "mage": (80, 50, 130), "captain": (222, 182, 52), "brute": (82, 82, 92),
}
TIP = {"mage": (60, 36, 105), "captain": (192, 36, 42)}

rng = random.Random(1234)


def clamp(v):
    return max(0, min(255, int(v)))


def mul(c, f):
    return tuple(clamp(x * f) for x in c)


def jitter(c, amt=7):
    d = rng.randint(-amt, amt)
    return tuple(clamp(x + d) for x in c)


def faces(u, v, w, h, d):
    return {
        "top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d),
        "right": (u, v + d, d, h), "front": (u + d, v + d, w, h),
        "left": (u + d + w, v + d, d, h), "back": (u + 2 * d + w, v + d, w, h),
    }


def bounds(u, v, w, h, d):
    return (u, v, 2 * d + 2 * w, d + h)


def paint(img, rect, fn):
    x0, y0, w, h = rect
    for ly in range(h):
        for lx in range(w):
            c = fn(lx, ly)
            if c is not None:
                img.putpixel((x0 + lx, y0 + ly), c + (255,))


def solid(color, amt=6):
    return lambda x, y: jitter(color, amt)


def build(race, role):
    spec = RACES[race]
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    skin = SKIN[race]
    skin_dark = mul(skin, 0.82)
    tunic, accent, trousers = [mul(c, CLOTH_MOD[race]) for c in CLOTHES[role]]
    hair = None
    if race == "elf":
        hair = HAIR_ELF[role]
    elif race == "dwarf":
        hair = HAIR_DWARF[role]
    boxes = {}

    # ---- head ---------------------------------------------------------
    head = faces(0, 0, 8, 8, 8)
    boxes["head"] = bounds(0, 0, 8, 8, 8)
    hair_rows_side = {"goblin": 0, "elf": 3, "dwarf": 4}[race]
    for name, rect in head.items():
        def fn(x, y, name=name):
            if name == "top" or name == "bottom" and hair:
                return jitter(hair, 8) if hair else jitter(skin_dark, 6)
            if name == "back":
                return jitter(hair, 8) if hair else jitter(skin_dark, 6)
            if name in ("left", "right") and hair and y < hair_rows_side:
                return jitter(hair, 8)
            return jitter(skin, 5)
        paint(img, rect, fn)

    # face (front 8x8)
    fx, fy, _, _ = head["front"]

    def px(x, y, c):
        img.putpixel((fx + x, fy + y), c + (255,))

    if hair:
        fringe = {"elf": 2, "dwarf": 3}[race]
        for y in range(fringe):
            for x in range(8):
                px(x, y, jitter(hair, 8))
        if race == "elf":  # side locks
            for y in range(fringe, 5):
                px(0, y, jitter(hair, 8))
                px(7, y, jitter(hair, 8))
    eye = EYES[race]
    if role == "mage":
        eye = (170, 120, 255)
    if race == "goblin":
        for ex in (1, 5):          # big angry eyes
            px(ex, 3, eye)
            px(ex + 1, 3, eye)
            px(ex, 4, eye)
            px(ex + 1, 4, (30, 20, 10))
        for x in (1, 2):           # angry brows
            px(x, 2, skin_dark)
        for x in (5, 6):
            px(x, 2, skin_dark)
        for x in range(2, 6):      # wide mouth
            px(x, 6, (60, 25, 20))
        px(2, 7, (235, 235, 215))  # fangs
        px(5, 7, (235, 235, 215))
        for wx, wy in ((0, 5), (7, 4), (3, 1)):  # warts
            px(wx, wy, skin_dark)
    elif race == "elf":
        for ex in (2, 5):
            px(ex, 4, eye)
            px(ex, 3, mul(hair, 0.8))
        px(3, 6, (205, 120, 120))
        px(4, 6, (205, 120, 120))
    else:  # dwarf
        for x in (1, 2, 3):
            px(x, 3, mul(hair, 0.85))
        for x in (4, 5, 6):
            px(x, 3, mul(hair, 0.85))
        px(2, 4, eye)
        px(5, 4, eye)
        for y in range(5, 8):       # mouth area hidden by the beard
            for x in range(8):
                px(x, y, jitter(hair, 8))

    # role face details
    if role == "berserker":
        for x in (1, 2, 5, 6):
            px(x, 5, (175, 30, 30))
        px(3, 1, (175, 30, 30)); px(4, 1, (175, 30, 30)) if race == "goblin" else None
    if role == "assassin":
        for y in range(5, 8):
            for x in range(8):
                px(x, y, (22, 22, 28))
    if role == "healer":
        px(3, 2, (90, 200, 120)); px(4, 2, (90, 200, 120))
    if role == "captain":
        px(6, 3, mul(skin, 0.6)); px(6, 4, mul(skin, 0.6)); px(5, 5, mul(skin, 0.6))
    if role == "mage":
        pass  # glowing eyes handled above

    # ---- ears -----------------------------------------------------------
    ew, eh, ed = spec["ear"]
    for name, (u, v) in (("right_ear", (32, 0)), ("left_ear", (32, 6))):
        boxes[name] = bounds(u, v, ew, eh, ed)
        for fname, rect in faces(u, v, ew, eh, ed).items():
            inner = mul(skin, 0.9) if race == "goblin" else (mul((235, 160, 160), 1.0) if race == "elf" else skin_dark)

            def fn(x, y, fname=fname, inner=inner):
                if fname in ("front", "back") and 0 < x < rect[2] - 1 and 0 < y < rect[3] - 1:
                    return jitter(inner, 4)
                return jitter(skin, 5)
            paint(img, rect, fn)

    # ---- nose -----------------------------------------------------------
    nw, nh, nd = spec["nose"]
    boxes["nose"] = bounds(44, 0, nw, nh, nd)
    nose_col = mul(skin, 0.95) if race != "dwarf" else mul(skin, 1.05)
    for fname, rect in faces(44, 0, nw, nh, nd).items():
        paint(img, rect, solid(nose_col if fname != "bottom" else skin_dark, 5))

    # ---- beard ----------------------------------------------------------
    if spec["beard"]:
        boxes["beard"] = bounds(44, 8, 6, 5, 2)
        for fname, rect in faces(44, 8, 6, 5, 2).items():
            def fn(x, y, fname=fname):
                c = jitter(hair, 8)
                if fname == "front" and x % 2 == 1:
                    c = mul(c, 0.82)  # braided strands
                if fname == "front" and y == 3:
                    c = jitter(accent, 6)  # beard ring
                return c
            paint(img, rect, fn)

    # ---- body -------------------------------------------------------------
    bh = spec["body"]
    boxes["body"] = bounds(0, 16, 6, bh, 4)
    belt_y = int(bh * 0.65)
    for fname, rect in faces(0, 16, 6, bh, 4).items():
        def fn(x, y, fname=fname):
            if fname in ("top", "bottom"):
                return jitter(mul(tunic, 0.75), 5)
            if y == belt_y:
                return jitter(accent, 5)
            if y == 0:
                return jitter(mul(tunic, 1.12), 5)  # collar
            c = tunic
            if role == "knight" and fname == "front" and 1 <= y < belt_y and 2 <= x <= 3:
                c = mul(tunic, 1.18)  # chest plate highlight
            if role == "captain" and fname == "front" and x in (0, 5):
                c = accent  # gold trim
            return jitter(c, 6)
        paint(img, rect, fn)

    # ---- loincloth --------------------------------------------------------
    boxes["loincloth"] = bounds(0, 48, 6, 4, 1)
    for fname, rect in faces(0, 48, 6, 4, 1).items():
        paint(img, rect, lambda x, y: jitter(accent if y < 3 else mul(accent, 0.8), 5))

    # ---- arms -------------------------------------------------------------
    al = spec["arm"]
    for name, u in (("right_arm", 20), ("left_arm", 32)):
        boxes[name] = bounds(u, 16, 3, al, 3)
        for fname, rect in faces(u, 16, 3, al, 3).items():
            def fn(x, y, fname=fname):
                if fname == "top":
                    return jitter(tunic, 5)
                if fname == "bottom":
                    return jitter(skin, 5)
                if y >= al - 3:
                    return jitter(skin, 5)  # hands
                if role == "knight" and y == al - 4:
                    return jitter(mul(tunic, 0.8), 5)  # gauntlet band
                return jitter(tunic, 6)
            paint(img, rect, fn)

    # ---- legs -------------------------------------------------------------
    ll = spec["leg"]
    for name, u, v in (("right_leg", 44, 16), ("left_leg", 0, 32)):
        boxes[name] = bounds(u, v, 3, ll, 3)
        for fname, rect in faces(u, v, 3, ll, 3).items():
            def fn(x, y, fname=fname):
                if fname in ("top", "bottom"):
                    return jitter((50, 35, 25), 4)
                if y >= ll - 2:
                    return jitter((52, 36, 26), 5)  # boots
                return jitter(trousers, 6)
            paint(img, rect, fn)

    # ---- hat ------------------------------------------------------------
    boxes["hat"] = bounds(24, 48, 9, 3, 9)
    boxes["hat_tip"] = bounds(0, 54, 4, 4, 4)
    hat_col = HAT.get(role) or (120, 120, 120)
    if race == "dwarf" and role in ("warrior", "knight", "brute"):
        hat_col = mul((170, 130, 70), 1.0)  # bronze dwarven helmets
    for fname, rect in faces(24, 48, 9, 3, 9).items():
        def fn(x, y, fname=fname):
            c = hat_col
            if fname in ("front", "left", "right", "back") and y == 2:
                c = mul(hat_col, 0.78)  # rim shadow
            return jitter(c, 5)
        paint(img, rect, fn)
    tip_col = TIP.get(role, hat_col)
    for fname, rect in faces(0, 54, 4, 4, 4).items():
        paint(img, rect, solid(tip_col, 5))

    # ---- sanity: no overlapping UV regions --------------------------------
    names = list(boxes)
    for i in range(len(names)):
        for j in range(i + 1, len(names)):
            a, b = boxes[names[i]], boxes[names[j]]
            if a[0] < b[0] + b[2] and b[0] < a[0] + a[2] and a[1] < b[1] + b[3] and b[1] < a[1] + a[3]:
                raise SystemExit(f"UV overlap in {race}: {names[i]} vs {names[j]}")
    for n, (x, y, w, h) in boxes.items():
        if x + w > 64 or y + h > 64:
            raise SystemExit(f"{race}: {n} leaves the 64x64 texture")
    return img


def main():
    os.makedirs(OUT, exist_ok=True)
    for race in RACES:
        for role in ROLES:
            build(race, role).save(os.path.join(OUT, f"{race}_{role}.png"))
    print("wrote", len(RACES) * len(ROLES), "textures to", os.path.abspath(OUT))


if __name__ == "__main__":
    main()
