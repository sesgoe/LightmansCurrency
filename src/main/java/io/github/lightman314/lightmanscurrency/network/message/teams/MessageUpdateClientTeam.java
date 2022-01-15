package io.github.lightman314.lightmanscurrency.network.message.teams;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageUpdateClientTeam {
	
	CompoundNBT traderData;
	
	public MessageUpdateClientTeam(CompoundNBT traderData)
	{
		this.traderData = traderData;
	}
	
	public static void encode(MessageUpdateClientTeam message, PacketBuffer buffer) {
		buffer.writeCompoundTag(message.traderData);
	}

	public static MessageUpdateClientTeam decode(PacketBuffer buffer) {
		return new MessageUpdateClientTeam(buffer.readCompoundTag());
	}

	public static void handle(MessageUpdateClientTeam message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.updateTeam(message.traderData));
		supplier.get().setPacketHandled(true);
	}

}