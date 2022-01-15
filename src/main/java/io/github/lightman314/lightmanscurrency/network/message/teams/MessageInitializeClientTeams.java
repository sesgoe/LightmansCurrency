package io.github.lightman314.lightmanscurrency.network.message.teams;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageInitializeClientTeams {
	
	CompoundNBT compound;
	
	public MessageInitializeClientTeams(CompoundNBT compound)
	{
		this.compound = compound;
	}
	
	public static void encode(MessageInitializeClientTeams message, PacketBuffer buffer) {
		buffer.writeCompoundTag(message.compound);
	}

	public static MessageInitializeClientTeams decode(PacketBuffer buffer) {
		return new MessageInitializeClientTeams(buffer.readCompoundTag());
	}

	public static void handle(MessageInitializeClientTeams message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.initializeTeams(message.compound));
		supplier.get().setPacketHandled(true);
	}

}