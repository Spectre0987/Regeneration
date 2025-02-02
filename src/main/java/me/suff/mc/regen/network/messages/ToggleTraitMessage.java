package me.suff.mc.regen.network.messages;

import me.suff.mc.regen.common.regen.RegenCap;
import me.suff.mc.regen.common.regen.state.RegenStates;
import me.suff.mc.regen.util.RegenSources;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ToggleTraitMessage {

    public ToggleTraitMessage() {
    }

    public ToggleTraitMessage(PacketBuffer buffer) {
    }

    public static void handle(ToggleTraitMessage message, Supplier< NetworkEvent.Context > ctx) {
        ctx.get().getSender().getServer().submitAsync(() -> RegenCap.get(ctx.get().getSender()).ifPresent((cap) -> {
           cap.toggleTrait();
           if(cap.traitActive()){
               cap.trait().apply(cap);
           } else {
               cap.trait().remove(cap);
           }
           cap.syncToClients(null);
        }));
        ctx.get().setPacketHandled(true);
    }

    public void toBytes(PacketBuffer buf) {

    }

}
