package com.example.middleworld.network;

import com.example.middleworld.MiddleWorld;
import com.example.middleworld.reputation.ClientReputation;
import com.example.middleworld.reputation.ReputationData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Server to client: the player's full reputation data (small enough to just resend). */
public record ReputationSyncPayload(ReputationData data) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ReputationSyncPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MiddleWorld.MODID, "reputation_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReputationSyncPayload> STREAM_CODEC =
            StreamCodec.ofMember(ReputationSyncPayload::write, ReputationSyncPayload::read);

    public void write(RegistryFriendlyByteBuf buf) {
        data.write(buf);
    }

    public static ReputationSyncPayload read(RegistryFriendlyByteBuf buf) {
        return new ReputationSyncPayload(ReputationData.read(buf));
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ReputationSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientReputation.set(payload.data()));
    }
}
