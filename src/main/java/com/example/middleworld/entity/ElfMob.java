package com.example.middleworld.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** Separate class per race so "alert nearby allies" only reaches the same race. */
public class ElfMob extends FactionMob {
    public ElfMob(EntityType<? extends FactionMob> type, Level level, Role role) {
        super(type, level, Race.ELF, role);
    }
}
