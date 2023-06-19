package io.github.lightman314.lightmanscurrency.common.menus;

import java.util.List;

import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionSaveData;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.menus.containers.SuppliedContainer;
import io.github.lightman314.lightmanscurrency.common.menus.providers.NamelessMenuProvider;
import io.github.lightman314.lightmanscurrency.common.menus.slots.OutputSlot;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.emergencyejection.SPacketChangeSelectedData;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class TraderRecoveryMenu extends Container {

	public static final INamedContainerProvider PROVIDER = new Provider();
	
	public TraderRecoveryMenu(int menuID, PlayerInventory inventory) { this(ModMenus.TRADER_RECOVERY.get(), menuID, inventory); }
	
	private final PlayerEntity player;
	
	public boolean isClient() { return this.player.level.isClientSide; }
	
	public List<EjectionData> getValidEjectionData() {
		return EjectionSaveData.GetValidEjectionData(this.isClient(), this.player);
	}
	
	private int selectedIndex = 0;
	public int getSelectedIndex() { return this.selectedIndex; }
	public EjectionData getSelectedData() { 
		List<EjectionData> data = this.getValidEjectionData();
		if(data.size() > 0 && this.selectedIndex >= 0 && this.selectedIndex < data.size())
			return data.get(this.selectedIndex);
		return null;
	}
	
	private final SuppliedContainer ejectionContainer;
	private final IInventory dummyContainer = new Inventory(54);
	
	private IInventory getSelectedContainer() {
		//Get valid data
		List<EjectionData> data = this.getValidEjectionData();
		//Refresh selection, just in case it's no longer valid.
		this.changeSelection(this.selectedIndex, data.size());
		if(data.size() > 0 && this.selectedIndex >= 0 && this.selectedIndex < data.size())
			return data.get(this.selectedIndex);
		return this.dummyContainer;
	}
	
	protected TraderRecoveryMenu(ContainerType<?> type, int menuID, PlayerInventory inventory) {
		super(type, menuID);
		this.player = inventory.player;
		
		this.ejectionContainer = new SuppliedContainer(this::getSelectedContainer);
		
		//Menu slots
		for(int y = 0; y < 6; ++y)
		{
			for(int x = 0; x < 9; ++x)
			{
				 this.addSlot(new OutputSlot(this.ejectionContainer, x + y * 9, 8 + x * 18, 18 + y * 18));
			}
		}
		
		//Player's Inventory
		for(int y = 0; y < 3; ++y) {
			for(int x = 0; x < 9; ++x) {
				this.addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 140 + y * 18));
			}
		}

		//Player's hotbar
		for(int x = 0; x < 9; ++x) {
			this.addSlot(new Slot(inventory, x, 8 + x * 18, 198));
		}
		
	}

	@Nonnull
	public ItemStack quickMoveStack(@Nonnull PlayerEntity player, int slotIndex) {
	      ItemStack itemstack = ItemStack.EMPTY;
	      Slot slot = this.slots.get(slotIndex);
	      if (slot != null && slot.hasItem()) {
	         ItemStack itemstack1 = slot.getItem();
	         itemstack = itemstack1.copy();
	         if (slotIndex < 54) {
	            if (!this.moveItemStackTo(itemstack1, 54, this.slots.size(), true)) {
	               return ItemStack.EMPTY;
	            }
	         } else if (!this.moveItemStackTo(itemstack1, 0, 54, false)) {
	            return ItemStack.EMPTY;
	         }

	         if (itemstack1.isEmpty()) {
	            slot.set(ItemStack.EMPTY);
	         } else {
	            slot.setChanged();
	         }
	      }

	      return itemstack;
	   }

	@Override
	public boolean stillValid(@Nonnull PlayerEntity player) { return true; }
	
	@Override
	public void removed(@Nonnull PlayerEntity player) {
		super.removed(player);
		//Clear the dummy container for safety.
		this.clearContainer(player, player.level, this.dummyContainer);
	}

	public void changeSelection(int newSelection) {
		this.changeSelection(newSelection, this.getValidEjectionData().size());
	}
	
	private void changeSelection(int newSelection, int dataSize) {
		int oldSelection = this.selectedIndex;
		this.selectedIndex = MathUtil.clamp(newSelection, 0, dataSize - 1);
		if(this.selectedIndex != oldSelection && !this.isClient())
		{
			//Inform the client of the change
			LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(this.player), new SPacketChangeSelectedData(this.selectedIndex));
		}
	}
	
	private static class Provider extends NamelessMenuProvider {

		@Override
		public Container createMenu(int id, @Nonnull PlayerInventory inventory, @Nonnull PlayerEntity player) { return new TraderRecoveryMenu(id, inventory); }
		
	}
	
}