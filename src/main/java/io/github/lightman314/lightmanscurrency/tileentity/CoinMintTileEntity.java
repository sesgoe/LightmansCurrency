package io.github.lightman314.lightmanscurrency.tileentity;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.core.ModTileEntities;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.MintRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

@Mod.EventBusSubscriber
public class CoinMintTileEntity extends BlockEntity{

	Container storage = new SimpleContainer(2);
	public Container getStorage() { return this.storage; }
	
	private final LazyOptional<IItemHandler> inventoryHandlerLazyOptional = LazyOptional.of(() -> new MintItemCapability(this));
	
	private static boolean canMint() { return Config.canMint(); }
	private static boolean canMelt() { return Config.canMelt(); }
	
	public CoinMintTileEntity(BlockPos pos, BlockState state) {
		super(ModTileEntities.COIN_MINT, pos, state);
	}
	
	protected CoinMintTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}
	
	@Override
	public CompoundTag save(CompoundTag compound)
	{
		InventoryUtil.saveAllItems("Storage", compound, this.storage);
		return super.save(compound);
	}
	
	@Override
	public void load(CompoundTag compound)
	{
		super.load(compound);
		
		this.storage = InventoryUtil.loadAllItems("Storage", compound, 2);
		
	}
	
	public void dumpContents(Level world, BlockPos pos)
	{
		InventoryUtil.dumpContents(world, pos, this.storage);
	}
	
	//Coin Minting Functions
	public boolean validMintInput()
	{
		return !getMintOutput().isEmpty();
	}
	
	public boolean validMintInput(ItemStack item)
	{
		if(canMint())
		{
			for(MintRecipe recipe : MoneyUtil.getMintRecipes())
			{
				if(recipe.validInput(item.getItem()))
				{
					return true;
				}
			}
		}
		if(canMelt())
		{
			for(MintRecipe recipe : MoneyUtil.getMeltRecipes())
			{
				if(recipe.validInput(item.getItem()))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	public int validMintOutput()
	{
		//Determine how many more coins can fit in the output slot based on the input item
		ItemStack mintOutput = getMintOutput();
		ItemStack currentOutputSlot = this.getStorage().getItem(1);
		if(currentOutputSlot.isEmpty())
			return 64;
		else if(currentOutputSlot.getItem() != mintOutput.getItem())
			return 0;
		return 64 - currentOutputSlot.getCount();	
	}
	
	public ItemStack getMintOutput()
	{
		ItemStack mintInput = this.getStorage().getItem(0);
		if(mintInput.isEmpty())
			return ItemStack.EMPTY;
		
		if(canMint())
		{
			for(MintRecipe recipe : MoneyUtil.getMintRecipes())
			{
				if(recipe.validInput(mintInput.getItem()))
				{
					return recipe.getOutput();
				}
			}
		}
		if(canMelt())
		{
			for(MintRecipe recipe : MoneyUtil.getMeltRecipes())
			{
				if(recipe.validInput(mintInput.getItem()))
				{
					return recipe.getOutput();
				}
			}
		}
		
		return ItemStack.EMPTY;
	}
	
	public void mintCoins(int mintCount)
	{
		//Ignore if no valid input is present
		if(!validMintInput())
			return;
		
		//Determine how many to mint based on the input count & whether a fullStack input was given.
		if(mintCount > this.getStorage().getItem(0).getCount())
		{
			mintCount = this.getStorage().getItem(0).getCount();
		}
		
		//Confirm that the output slot has enough room for the expected outputs
		if(mintCount > validMintOutput())
			mintCount = validMintOutput();
		if(mintCount <= 0)
			return;
		
		//Get the output items
		ItemStack mintOutput = getMintOutput();
		mintOutput.setCount(mintCount);
		
		//Place the output item(s)
		if(this.getStorage().getItem(1).isEmpty())
		{
			this.getStorage().setItem(1, mintOutput);
		}
		else
		{
			this.getStorage().getItem(1).setCount(this.getStorage().getItem(1).getCount() + mintOutput.getCount());
		}
		
		//Remove the input item(s)
		this.getStorage().getItem(0).setCount(this.getStorage().getItem(0).getCount() - mintCount);
		
		//Job is done!
		this.setChanged();
		
	}
	
	//Client Synchronization
	@Override
	public void onLoad() {
		if(this.level.isClientSide)
			TileEntityUtil.requestUpdatePacket(this);
	}
	
	@Override
	public CompoundTag getUpdateTag() { return this.save(new CompoundTag()); }
	
	//Item capability for hopper and item automation
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			return inventoryHandlerLazyOptional.cast();
		}
		return super.getCapability(cap, side);
	}
	
	@Override
	public void invalidateCaps()
	{
		super.invalidateCaps();
		inventoryHandlerLazyOptional.invalidate();
	}
	
	public static class MintItemCapability implements IItemHandler
	{

		final CoinMintTileEntity tileEntity;
		public MintItemCapability(CoinMintTileEntity tileEntity) { this.tileEntity = tileEntity; }
		
		@Override
		public int getSlots() {
			return this.tileEntity.getStorage().getContainerSize();
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			return this.tileEntity.getStorage().getItem(slot);
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			//Can only insert into slot 0
			if(slot != 0)
				return stack.copy();
			if(!this.tileEntity.validMintInput(stack))
				return stack.copy();
			//Confirm that the input slot is valid
			ItemStack currentStack = this.tileEntity.getStorage().getItem(0);
			if(currentStack.isEmpty())
			{
				if(stack.getCount() > stack.getMaxStackSize())
				{
					//Move the item into storage, and return the leftovers
					if(!simulate)
					{
						ItemStack placeStack = stack.copy();
						placeStack.setCount(stack.getMaxStackSize());
						this.tileEntity.getStorage().setItem(0, placeStack);
					}
					ItemStack leftoverStack = stack.copy();
					leftoverStack .setCount(stack.getCount() - stack.getMaxStackSize());
					return leftoverStack;
				}
				else
				{
					//Move the item into storage and return an empty stack
					if(!simulate)
						this.tileEntity.getStorage().setItem(0, stack.copy());
					return ItemStack.EMPTY;
				}
				
			}
			else if(InventoryUtil.ItemMatches(currentStack, stack))
			{
				int newAmount = MathUtil.clamp(currentStack.getCount() + stack.getCount(), 0, currentStack.getMaxStackSize());
				if(!simulate)
				{
					ItemStack newStack = currentStack.copy();
					newStack.setCount(newAmount);
					this.tileEntity.getStorage().setItem(0, newStack);
				}
				ItemStack leftoverStack = stack.copy();
				leftoverStack.setCount(stack.getCount() + currentStack.getCount() - newAmount);
				return leftoverStack;
			}
			//Slot is already full of another item type
			return stack.copy();
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			//Can only extract from slot 1
			if(slot != 1)
				return ItemStack.EMPTY;
			//Limit request amount to 1 stack
			amount = MathUtil.clamp(amount, 0, 64);
			//Copy so that the simulation doesn't cause problems
			ItemStack currentStack = this.tileEntity.getStorage().getItem(1).copy();
			if(currentStack.isEmpty() || currentStack.getCount() < amount)
			{
				//Attempt to mint coins to fill the extra pull requests
				int mintAmount = MathUtil.clamp(amount - currentStack.getCount(), 0, this.tileEntity.validMintOutput());
				if(!simulate)
				{
					if(mintAmount > 0) //Mint the coins
					{
						//Mint the coins
						this.tileEntity.mintCoins(mintAmount);
						//Update the output stack now that the coins have been minted
						currentStack = this.tileEntity.getStorage().getItem(1).copy();
					}
				}
				else if(mintAmount > 0)
				{
					//Emulate the minting
					if(currentStack.isEmpty())
					{
						currentStack = this.tileEntity.getMintOutput();
						currentStack.setCount(mintAmount);
					}
					else
						currentStack.grow(mintAmount);
				}
			}
			
			//No items to output even after attempting to mint
			if(currentStack.isEmpty())
				return ItemStack.EMPTY;
			
			
			ItemStack outputStack = currentStack.copy();
			//Get the output stack
			outputStack.setCount(MathUtil.clamp(amount, 0, currentStack.getCount()));
			//Remove the output coins from the current stack
			currentStack.setCount(currentStack.getCount() - outputStack.getCount());
			if(currentStack.getCount() <= 0)
				currentStack = ItemStack.EMPTY;
			
			//Set the new current stack count
			if(!simulate)
				this.tileEntity.getStorage().setItem(1, currentStack);
			
			//Return the output stack
			return outputStack;
		}

		@Override
		public int getSlotLimit(int slot) {
			return 64;
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return slot == 0 ? this.tileEntity.validMintInput(stack) : false;
		}
		
	}
	
}
