package com.example.middleworld.reputation;

import com.example.middleworld.MiddleWorld;
import java.util.function.Supplier;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MiddleWorld.MODID);

    /** Saved with the player, and kept when the player dies. */
    public static final Supplier<AttachmentType<ReputationData>> REPUTATION = ATTACHMENTS.register(
            "reputation",
            () -> AttachmentType.builder(() -> new ReputationData())
                    .serialize(ReputationData.CODEC)
                    .copyOnDeath()
                    .build());
}
