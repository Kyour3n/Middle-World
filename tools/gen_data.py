#!/usr/bin/env python3
"""Generates lang entries, loot tables, biome spawn modifiers and spawn egg item models for the 30 mobs.
Run:  python3 tools/gen_data.py
Keep RACES / ROLES in sync with Race.java / Role.java."""
import json
import os

ROOT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources")
ASSETS = os.path.join(ROOT, "assets", "middleworld")
DATA = os.path.join(ROOT, "data", "middleworld")

RACES = ["goblin", "elf", "dwarf"]
# role: (spawn weight, group min, group max) -- from Role.java
ROLES = {
    "warrior": (10, 1, 3), "archer": (7, 1, 3), "knight": (3, 1, 2), "berserker": (4, 1, 2), "scout": (6, 1, 3),
    "mage": (3, 1, 2), "healer": (3, 1, 2), "assassin": (3, 1, 1), "captain": (1, 1, 1), "brute": (2, 1, 1),
}
RACE_NAME = {"goblin": "Goblin", "elf": "Elf", "dwarf": "Dwarf"}
ROLE_NAME = {
    ("goblin", "mage"): "Shaman", ("elf", "mage"): "Mage", ("dwarf", "mage"): "Runesmith",
    ("goblin", "healer"): "Witch Doctor", ("elf", "healer"): "Healer", ("dwarf", "healer"): "Cleric",
    ("goblin", "captain"): "Chieftain", ("elf", "captain"): "Captain", ("dwarf", "captain"): "Thane",
    ("goblin", "brute"): "Brute", ("elf", "brute"): "Warden", ("dwarf", "brute"): "Hammerer",
}
BIOMES = {"goblin": "#minecraft:is_overworld", "elf": "#minecraft:is_forest", "dwarf": "#minecraft:is_mountain"}


def role_name(race, role):
    return ROLE_NAME.get((race, role), role.capitalize())


def write(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


# ---------------------------------------------------------------- lang
lang_path = os.path.join(ASSETS, "lang", "en_us.json")
with open(lang_path) as f:
    lang = json.load(f)
for k in ["entity.middleworld.goblin", "item.middleworld.goblin_spawn_egg"]:
    lang.pop(k, None)
for race in RACES:
    for role in ROLES:
        name = f"{RACE_NAME[race]} {role_name(race, role)}"
        lang[f"entity.middleworld.{race}_{role}"] = name
        lang[f"item.middleworld.{race}_{role}_spawn_egg"] = f"{name} Spawn Egg"
lang["reason.middleworld.kill_elf"] = "Killed an elf"
lang["reason.middleworld.kill_dwarf"] = "Killed a dwarf"
lang.update({
    "effect.middleworld.elves.hostile": "Elves attack you on sight and hunt you through the forest.",
    "effect.middleworld.elves.unfriendly": "Elves attack you on sight.",
    "effect.middleworld.elves.neutral": "Elves watch you warily but leave you alone unless provoked.",
    "effect.middleworld.dwarves.hostile": "Dwarves attack you on sight and hunt you through the mountains.",
    "effect.middleworld.dwarves.unfriendly": "Dwarves attack you on sight.",
    "effect.middleworld.dwarves.neutral": "Dwarves watch you warily but leave you alone unless provoked.",
    "effect.middleworld.elves.friendly": "Elves welcome you. Elf trading and safe village entry arrive in a later update.",
    "effect.middleworld.dwarves.friendly": "Dwarves welcome you. Dwarf trading and hall access arrive in a later update.",
    "effect.middleworld.elves.allied": "Elves treat you as kin. Fighting beside you arrives in a later update.",
    "effect.middleworld.dwarves.allied": "Dwarves treat you as kin. Fighting beside you arrives in a later update.",
})
write(lang_path, lang)

# ---------------------------------------------------------------- item models
for race in RACES:
    for role in ROLES:
        write(os.path.join(ASSETS, "models", "item", f"{race}_{role}_spawn_egg.json"),
              {"parent": "minecraft:item/template_spawn_egg"})
old = os.path.join(ASSETS, "models", "item", "goblin_spawn_egg.json")
if os.path.exists(old):
    os.remove(old)


# ---------------------------------------------------------------- loot tables
def pool(item, lo=None, hi=None, chance=None, player=False, looting=False):
    entry = {"type": "minecraft:item", "name": f"minecraft:{item}"}
    funcs = []
    if lo is not None:
        funcs.append({"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}})
    if looting:
        funcs.append({"function": "minecraft:enchanted_count_increase",
                      "count": {"type": "minecraft:uniform", "min": 0, "max": 1}, "enchantment": "minecraft:looting"})
    if funcs:
        entry["functions"] = funcs
    p = {"rolls": 1, "entries": [entry]}
    conds = []
    if player:
        conds.append({"condition": "minecraft:killed_by_player"})
    if chance is not None:
        conds.append({"condition": "minecraft:random_chance", "chance": chance})
    if conds:
        p["conditions"] = conds
    return p


RACE_DROPS = {
    "goblin": [pool("gold_nugget", 0, 3, looting=True)],
    "elf": [pool("arrow", 0, 2, looting=True), pool("feather", 0, 1)],
    "dwarf": [pool("iron_nugget", 0, 4, looting=True), pool("coal", 0, 2)],
}
ROLE_DROPS = {
    "warrior": [], "archer": [pool("arrow", 0, 2)], "knight": [pool("iron_ingot", chance=0.12, player=True)],
    "berserker": [pool("bone", 0, 2)], "scout": [pool("leather", 0, 1)], "mage": [pool("lapis_lazuli", 0, 2)],
    "healer": [pool("glistering_melon_slice", 0, 1)], "assassin": [pool("ender_pearl", chance=0.04, player=True)],
    "captain": [pool("emerald", 1, 2), pool("iron_ingot", 1, 2)],
    "brute": [pool("iron_ingot", 0, 2), pool("emerald", chance=0.15, player=True)],
}
for race in RACES:
    for role in ROLES:
        pools = list(RACE_DROPS[race]) + list(ROLE_DROPS[role])
        if role not in ("captain", "brute"):
            pools.append(pool("emerald", chance=0.10, player=True))
        write(os.path.join(DATA, "loot_table", "entities", f"{race}_{role}.json"),
              {"type": "minecraft:entity", "pools": pools})
old = os.path.join(DATA, "loot_table", "entities", "goblin.json")
if os.path.exists(old):
    os.remove(old)

# ---------------------------------------------------------------- spawns
for race in RACES:
    spawners = []
    for role, (weight, lo, hi) in ROLES.items():
        spawners.append({"type": f"middleworld:{race}_{role}", "weight": max(1, round(weight / 2)),
                         "minCount": lo, "maxCount": hi})
    write(os.path.join(DATA, "neoforge", "biome_modifier", f"{race}_spawns.json"),
          {"type": "neoforge:add_spawns", "biomes": BIOMES[race], "spawners": spawners})
print("done")
