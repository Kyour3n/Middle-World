package com.example.middleworld;

import com.example.middleworld.entity.DwarfMob;
import com.example.middleworld.entity.ElfMob;
import com.example.middleworld.entity.FactionMob;
import com.example.middleworld.entity.GoblinMob;
import com.example.middleworld.entity.Race;
import com.example.middleworld.entity.Role;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registers one entity type for every race/role pair, e.g. middleworld:goblin_archer. */
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, MiddleWorld.MODID);

    private static final Map<Race, Map<Role, DeferredHolder<EntityType<?>, EntityType<FactionMob>>>> MOBS =
            new EnumMap<>(Race.class);

    static {
        for (Race race : Race.values()) {
            Map<Role, DeferredHolder<EntityType<?>, EntityType<FactionMob>>> byRole = new EnumMap<>(Role.class);
            for (Role role : Role.values()) {
                String id = id(race, role);
                byRole.put(role, ENTITY_TYPES.register(id, () -> EntityType.Builder
                        .<FactionMob>of((type, level) -> create(type, level, race, role), race.category)
                        .sized(race.width * role.scale, race.height * role.scale)
                        .eyeHeight(race.height * role.scale * 0.85F)
                        .clientTrackingRange(8)
                        .build(id)));
            }
            MOBS.put(race, byRole);
        }
    }

    private static FactionMob create(EntityType<? extends FactionMob> type, net.minecraft.world.level.Level level,
                                     Race race, Role role) {
        return switch (race) {
            case GOBLIN -> new GoblinMob(type, level, role);
            case ELF -> new ElfMob(type, level, role);
            case DWARF -> new DwarfMob(type, level, role);
        };
    }

    public static String id(Race race, Role role) {
        return race.id + "_" + role.id;
    }

    public static DeferredHolder<EntityType<?>, EntityType<FactionMob>> get(Race race, Role role) {
        return MOBS.get(race).get(role);
    }
}
