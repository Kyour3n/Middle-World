package com.example.middleworld;

import com.example.middleworld.entity.FactionMob;
import com.example.middleworld.entity.Race;
import com.example.middleworld.entity.Role;
import com.example.middleworld.reputation.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

@Mod(MiddleWorld.MODID)
public class MiddleWorld {
    public static final String MODID = "middleworld";

    public MiddleWorld(IEventBus modBus, ModContainer container) {
        ModEntities.ENTITY_TYPES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModAttachments.ATTACHMENTS.register(modBus);

        container.registerConfig(ModConfig.Type.COMMON, MWConfig.SPEC);

        modBus.addListener(this::registerAttributes);
        modBus.addListener(this::registerSpawnPlacements);
        modBus.addListener(this::addToCreativeTab);
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        for (Race race : Race.values()) {
            for (Role role : Role.values()) {
                event.put(ModEntities.get(race, role).get(), FactionMob.createAttributes(race, role).build());
            }
        }
    }

    private void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        for (Race race : Race.values()) {
            for (Role role : Role.values()) {
                event.register(
                        ModEntities.get(race, role).get(),
                        SpawnPlacementTypes.ON_GROUND,
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        (type, level, spawnType, pos, random) -> canSpawn(race, type, level, spawnType, pos, random),
                        RegisterSpawnPlacementsEvent.Operation.OR);
            }
        }
    }

    /** Goblins follow monster rules (dark places, not on Peaceful). Elves and dwarves just need solid ground in daylight or cave. */
    private static boolean canSpawn(Race race, EntityType<? extends Mob> type, ServerLevelAccessor level,
                                    MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (race == Race.GOBLIN) {
            return level.getDifficulty() != Difficulty.PEACEFUL
                    && Monster.isDarkEnoughToSpawn(level, pos, random)
                    && Mob.checkMobSpawnRules(type, level, spawnType, pos, random);
        }
        return Mob.checkMobSpawnRules(type, level, spawnType, pos, random);
    }

    private void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            for (var egg : ModItems.SPAWN_EGGS) {
                event.accept(egg.get());
            }
        }
    }
}
