package io.github.lightman314.lightmanscurrency.containers.slots;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TicketSlot extends Slot{
	
	public static final ResourceLocation EMPTY_TICKET_SLOT = new ResourceLocation(LightmansCurrency.MODID, "items/empty_ticket_slot");
	
	public TicketSlot(Container inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}
	
	@Override
	public boolean mayPlace(ItemStack stack) {
		return stack.getItem().getTags().contains(new ResourceLocation(LightmansCurrency.MODID, "ticket" ));
	}
	
	@Override
	public Pair<ResourceLocation,ResourceLocation> getNoItemIcon() {
		return Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_TICKET_SLOT);
	}
	

}
