package com.example.middleworld.client;

import com.example.middleworld.MiddleWorld;
import com.example.middleworld.ModEntities;
import com.example.middleworld.entity.Race;
import com.example.middleworld.entity.Role;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = MiddleWorld.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (Race race : Race.values()) {
            event.registerLayerDefinition(RaceModel.layer(race), () -> RaceModel.createBodyLayer(race));
        }
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (Race race : Race.values()) {
            for (Role role : Role.values()) {
                event.registerEntityRenderer(ModEntities.get(race, role).get(),
                        context -> new FactionMobRenderer(context, race, role));
            }
        }
    }
}
