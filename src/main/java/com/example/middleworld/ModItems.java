package com.example.middleworld;

import com.example.middleworld.entity.FactionMob;
import com.example.middleworld.entity.Race;
import com.example.middleworld.entity.Role;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MiddleWorld.MODID);

    /** Every spawn egg, ordered by race then role, for the creative tab. */
    public static final List<DeferredItem<DeferredSpawnEggItem>> SPAWN_EGGS = new ArrayList<>();

    private static final Map<Race, Integer> PRIMARY = new EnumMap<>(Race.class);
    private static final Map<Role, Integer> SECONDARY = new EnumMap<>(Role.class);

    static {
        PRIMARY.put(Race.GOBLIN, 0x4C8C3A);
        PRIMARY.put(Race.ELF, 0xD9C27A);
        PRIMARY.put(Race.DWARF, 0xA8622E);

        SECONDARY.put(Role.WARRIOR, 0x7A5A33);
        SECONDARY.put(Role.ARCHER, 0x2F6B2F);
        SECONDARY.put(Role.KNIGHT, 0x9AA3AD);
        SECONDARY.put(Role.BERSERKER, 0xB02A20);
        SECONDARY.put(Role.SCOUT, 0xC9B27A);
        SECONDARY.put(Role.MAGE, 0x6A3FB0);
        SECONDARY.put(Role.HEALER, 0xF2F2F2);
        SECONDARY.put(Role.ASSASSIN, 0x202020);
        SECONDARY.put(Role.CAPTAIN, 0xE0B020);
        SECONDARY.put(Role.BRUTE, 0x454545);

        for (Race race : Race.values()) {
            for (Role role : Role.values()) {
                SPAWN_EGGS.add(ITEMS.register(ModEntities.id(race, role) + "_spawn_egg",
                        () -> new DeferredSpawnEggItem(ModEntities.get(race, role), PRIMARY.get(race),
                                SECONDARY.get(role), new Item.Properties())));
            }
        }
    }
}
