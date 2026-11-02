package com.example.middleworld.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class DwarfMob extends FactionMob {
    public DwarfMob(EntityType<? extends FactionMob> type, Level level, Role role) {
        super(type, level, Race.DWARF, role);
    }
}
