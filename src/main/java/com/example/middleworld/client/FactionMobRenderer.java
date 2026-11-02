package com.example.middleworld.client;

import com.example.middleworld.MiddleWorld;
import com.example.middleworld.entity.FactionMob;
import com.example.middleworld.entity.Race;
import com.example.middleworld.entity.Role;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

/** Renders every race/role. The texture is textures/entity/{race}_{role}.png. */
public class FactionMobRenderer extends MobRenderer<FactionMob, RaceModel> {
    private final ResourceLocation texture;
    private final Race race;
    private final Role role;

    public FactionMobRenderer(EntityRendererProvider.Context context, Race race, Role role) {
        super(context, new RaceModel(context.bakeLayer(RaceModel.layer(race)), race), 0.35F * race.width / 0.5F);
        this.race = race;
        this.role = role;
        this.texture = ResourceLocation.fromNamespaceAndPath(MiddleWorld.MODID,
                "textures/entity/" + race.id + "_" + role.id + ".png");
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    protected void scale(FactionMob entity, PoseStack poseStack, float partialTick) {
        float s = this.role.scale;
        poseStack.scale(this.race.scaleX * s, this.race.scaleY * s, this.race.scaleZ * s);
    }

    @Override
    public ResourceLocation getTextureLocation(FactionMob entity) {
        return this.texture;
    }
}
