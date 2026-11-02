# Middle World (NeoForge 1.21.1), Stages 1-2: Reputation + 30 mobs

Six factions (Elves, Goblins, Dwarves, Villagers, Pillagers, Piglins) each track your reputation
independently from -1000 to +1000. Your actions change how mobs and villagers treat you.
Stage 2 adds 30 animated mobs: Goblins, Elves and Dwarves, 10 roles each.

## Build
Push this folder to GitHub (the `.github/workflows/build.yml` builds it for you), then download
the `middleworld-jar` artifact from the Actions tab. Or locally: add the Gradle wrapper and run
`./gradlew build`, jar in `build/libs/`.

## Try it
Open your inventory: a **Reputation** button sits under it. Click a faction on the left to see its
bar, rank, current effects, and recent changes.

Operator test commands (works in creative worlds with cheats):

    /middleworld rep villagers set -500     -> Iron Golems attack you, villagers refuse trades
    /middleworld rep goblins add 150        -> goblins stop attacking you
    /middleworld rep pillagers set 300      -> pillagers leave you alone

## Mobs (Stage 2)
Three races x ten roles = 30 mobs, all sharing one class (`FactionMob`). IDs are `middleworld:<race>_<role>`,
for example `/summon middleworld:elf_archer`. Spawn eggs for all 30 are in the Spawn Eggs creative tab.

| Role | Weapon | Behavior |
|---|---|---|
| Warrior | sword / axe / pickaxe | Plain melee fighter |
| Archer | bow | Keeps distance and shoots arrows |
| Knight | sword + shield | Heavy armor; blocks about a third of frontal attacks |
| Berserker | axe | Hits hard; enrages below half health (+50% damage, +30% speed) |
| Scout | light weapon | Very fast hit-and-run: strikes, then backs off briefly |
| Mage | staff | Ranged magic bolt. Goblin: weakness, Elf: poison, Dwarf: sets you on fire |
| Healer | sapling / stick / shovel | Runs to wounded allies and heals them; weak in melee |
| Assassin | sword | Double damage on the first strike against a new target |
| Captain | sword / axe | Strength aura for allies nearby when in combat; high health |
| Brute | mace | Slow, 50 HP, ground slam hurts and knocks back everyone near the victim |

Race traits: **Goblins** are small, fast, low health, spawn in dark places in the whole Overworld and are
hostile unless you are Friendly or better. **Elves** are tall and quick, spawn in forests and are peaceful
unless you are Unfriendly or worse. **Dwarves** are stocky, tough and armored, spawn in mountains, same attitude as elves.
Goblins fight elves and dwarves, and dwarves also fight piglins. Killing a faction member changes reputation
(goblin -10, elf/dwarf -15, captains x3); rivals gain a little. Hitting a calm elf or dwarf makes it fight back
and call nearby members of its race.

Textures: run `python3 tools/gen_textures.py` to regenerate them. `python3 tools/gen_data.py` regenerates
lang, loot, spawn rules and egg models (spawn weights come from the table at its top).

## Ranks
Hostile (<= -400), Unfriendly (<= -100), Neutral, Friendly (>= 100), Honored (>= 400), Allied (>= 700).

## What changes reputation (Stage 1)
| Action | Effect |
|---|---|
| Kill a villager / wandering trader / Iron Golem | Villagers -50 / -40 / -60 (a quarter of that if it was attacking you) |
| Kill a pillager (outside a raid) | Pillagers -12, captain -35, rivals gain a little |
| Kill raiders during a village raid | Villagers +12 (captain +30), Pillagers -15 |
| Kill a villager/golem during a raid | Pillagers +20 (helping the raiders), Villagers -50 as usual |
| Earn Hero of the Village | Villagers +40, Pillagers -25 |
| Trade with villagers | Villagers +1 per trade, capped per day |
| Open an unlooted loot chest with villagers watching | Villagers -15 |
| Kill a goblin / elf / dwarf | -10 / -15 / -15 (captains x3) |
| Kill a piglin / piglin brute | Piglins -20 / -30 |
| Hit a peaceful faction member | -2 per hit (config) |

Helping a faction lowers its rivals, hurting it slightly raises them:
Elves vs Goblins, Dwarves vs Goblins and Piglins, Villagers vs Pillagers.

## What reputation does (Stage 1)
- Villagers Hostile: Iron Golems attack you, villagers refuse to trade.
- Villagers Unfriendly: higher trade prices. Friendly / Honored / Allied: growing discounts.
- Friendly or better with Goblins, Pillagers, Piglins: their mobs stop targeting you unless you hurt them first.
- Negative reputation heals a little every in-game day, so nothing is permanent.
- Feedback: action-bar message, sound and particles on every change, plus a chat message when your rank changes.

## Config
`config/middleworld-common.toml`: gain/loss multipliers, rival spillover, hit penalty,
daily trade cap, daily recovery, and switches for golem hostility, trade refusal and calm friendly mobs.

## Known limits of Stage 1
- Elves and Dwarves have reputation but no mobs or villages yet (later stages).
- Piglins use a different AI system, so "stay calm when friendly" may not always apply to them.
- Custom raids, faction villages, faction trading and quests are later stages.
