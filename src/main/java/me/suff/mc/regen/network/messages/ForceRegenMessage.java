package me.suff.mc.regen.network.messages;

import me.suff.mc.regen.common.regen.RegenCap;
import me.suff.mc.regen.common.regen.state.RegenStates;
import me.suff.mc.regen.util.RegenSources;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ForceRegenMessage {

    public ForceRegenMessage() {
    }

    public ForceRegenMessage(PacketBuffer buffer) {
    }

    public static void handle(ForceRegenMessage message, Supplier< NetworkEvent.Context > ctx) {
        ctx.get().getSender().getServer().submitAsync(() -> RegenCap.get(ctx.get().getSender()).ifPresent((cap) -> {
            if (cap.getCurrentState() == RegenStates.ALIVE || cap.getCurrentState().isGraceful()) {
                if (cap.canRegenerate()) {
                    cap.getLiving().hurt(RegenSources.REGEN_DMG_FORCED, Integer.MAX_VALUE);
                }
            }
        }));
        ctx.get().setPacketHandled(true);
    }

    public void toBytes(PacketBuffer buf) {

    }

}
