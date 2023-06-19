package io.github.lightman314.lightmanscurrency.common.menus.wallet;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.common.menus.slots.DisplaySlot;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;

public abstract class WalletMenuBase extends Container {

	private static int maxWalletSlots = 0;
	public static int getMaxWalletSlots() { return maxWalletSlots; }
	public static void updateMaxWalletSlots(int slotCount) { maxWalletSlots = Math.max(maxWalletSlots, slotCount); }
	
	protected final Inventory dummyInventory = new Inventory(1);
	
	protected final int walletStackIndex;
	public final boolean isEquippedWallet() { return this.walletStackIndex < 0; }
	public final int getWalletStackIndex() { return this.walletStackIndex; }
	
	protected final PlayerInventory inventory;
	public final boolean hasWallet() { ItemStack wallet = this.getWallet(); return !wallet.isEmpty() && wallet.getItem() instanceof WalletItem; }
	public final ItemStack getWallet()
	{
		if(this.isEquippedWallet())
			return LightmansCurrency.getWalletStack(this.inventory.player);
		return this.inventory.getItem(this.walletStackIndex);
	}
	
	private boolean autoConvert;
	public boolean canConvert() { return WalletItem.CanConvert(this.walletItem); }
	public boolean canPickup() { return WalletItem.CanPickup(this.walletItem); }
	public boolean hasBankAccess() { return WalletItem.HasBankAccess(this.walletItem); }
	public boolean getAutoConvert() { return this.autoConvert; }
	public void ToggleAutoConvert() { this.autoConvert = !this.autoConvert; this.saveWalletContents(); }
	
	protected final IInventory coinInput;
	
	protected final WalletItem walletItem;
	
	public final PlayerEntity player;
	public PlayerEntity getPlayer() { return this.player; }
	
	protected WalletMenuBase(ContainerType<?> type, int windowID, PlayerInventory inventory, int walletStackIndex) {
		super(type, windowID);
		
		this.inventory = inventory;
		this.player = this.inventory.player;
		
		this.walletStackIndex = walletStackIndex;
		
		Item item = this.getWallet().getItem();
		if(item instanceof WalletItem)
			this.walletItem = (WalletItem)item;
		else
			this.walletItem = null;
		
		this.coinInput = new Inventory(WalletItem.InventorySize(this.walletItem));
		this.reloadWalletContents();
		
		this.autoConvert = WalletItem.getAutoConvert(this.getWallet());
		
	}
	
	protected final void addCoinSlots(int yPosition) {
		for(int y = 0; (y * 9) < this.coinInput.getContainerSize(); y++)
		{
			for(int x = 0; x < 9 && (x + y * 9) < this.coinInput.getContainerSize(); x++)
			{
				this.addSlot(new CoinSlot(this.coinInput, x + y * 9, 8 + x * 18, yPosition + y * 18).addListener(this::saveWalletContents));
			}
		}
	}
	
	protected final void addDummySlots(int slotLimit) {
		while(this.slots.size() < slotLimit) {
			this.addSlot(new DisplaySlot(this.dummyInventory, 0, Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2));
		}
	}
	
	public final void reloadWalletContents() {
		NonNullList<ItemStack> walletInventory = WalletItem.getWalletInventory(getWallet());
		for(int i = 0; i < this.coinInput.getContainerSize() && i < walletInventory.size(); i++)
		{
			this.coinInput.setItem(i, walletInventory.get(i));
		}
	}
	
	public final int getRowCount() { return 1 + ((this.coinInput.getContainerSize() - 1)/9); }
	
	public final int getSlotCount() { return this.coinInput.getContainerSize(); }
	
	@Override
	public boolean stillValid(@Nonnull PlayerEntity playerIn) { return this.hasWallet(); }
	
	public final void saveWalletContents()
	{
		if(!this.hasWallet())
			return;
		//Write the bag contents back into the item stack
		NonNullList<ItemStack> walletInventory = NonNullList.withSize(WalletItem.InventorySize(this.walletItem), ItemStack.EMPTY);
		for(int i = 0; i < walletInventory.size() && i < this.coinInput.getContainerSize(); i++)
		{
			walletInventory.set(i, this.coinInput.getItem(i));
		}
		WalletItem.putWalletInventory(this.getWallet(), walletInventory);
		
		if(this.autoConvert != WalletItem.getAutoConvert(this.getWallet()))
			WalletItem.toggleAutoConvert(this.getWallet());
		
	}
	
	public final void ConvertCoins()
	{
		MoneyUtil.ConvertAllCoinsUp(this.coinInput);
		MoneyUtil.SortCoins(this.coinInput);
		this.saveWalletContents();
	}
	
	public final ItemStack PickupCoins(ItemStack stack)
	{
		
		ItemStack returnValue = stack.copy();
		
		for(int i = 0; i < this.coinInput.getContainerSize() && !returnValue.isEmpty(); i++)
		{
			ItemStack thisStack = this.coinInput.getItem(i);
			if(thisStack.isEmpty())
			{
				this.coinInput.setItem(i, returnValue.copy());
				returnValue = ItemStack.EMPTY;
			}
			else if(InventoryUtil.ItemMatches(thisStack, returnValue))
			{
				int amountToAdd = MathUtil.clamp(returnValue.getCount(), 0, thisStack.getMaxStackSize() - thisStack.getCount());
				thisStack.setCount(thisStack.getCount() + amountToAdd);
				returnValue.setCount(returnValue.getCount() - amountToAdd);
			}
		}
		
		if(this.autoConvert)
			this.ConvertCoins();
		else
			this.saveWalletContents();
		
		return returnValue;
	}

	public static void OnWalletUpdated(Entity entity) {
		if(entity instanceof PlayerEntity)
		{
			PlayerEntity player = (PlayerEntity)entity;
			if(player.containerMenu instanceof WalletMenuBase)
			{
				WalletMenuBase menu = (WalletMenuBase)player.containerMenu;
				menu.reloadWalletContents();
			}
		}
	}
	
}