package io.github.lightman314.lightmanscurrency.tradedata.rules;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

public class PlayerTradeLimit extends TradeRule{
	
	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "tradelimit");
	public static final ITradeRuleDeserializer<PlayerTradeLimit> DESERIALIZER = new Deserializer();
	
	private int limit = 1;
	public int getLimit() { return this.limit; }
	public void setLimit(int newLimit) { this.limit = newLimit; }
	
	Map<UUID,Integer> tradeHistory = new HashMap<>();
	public void resetMemory() { this.tradeHistory.clear(); }
	
	public PlayerTradeLimit() { super(TYPE); }
	
	@Override
	public void beforeTrade(PreTradeEvent event) {
		
		if(getTradeCount(event.getPlayer().getUniqueID()) >= this.limit)
			event.setCanceled(true);
		
	}

	@Override
	public void afterTrade(PostTradeEvent event) {
		
		addTrade(event.getPlayer().getUniqueID());
		event.markDirty();
		
	}

	private int getTradeCount(UUID playerID)
	{
		if(tradeHistory.containsKey(playerID))
			return tradeHistory.get(playerID);
		return 0;
	}
	
	private void addTrade(UUID playerID)
	{
		tradeHistory.put(playerID, getTradeCount(playerID) + 1);
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		
		compound.putInt("Limit", this.limit);
		ListNBT memoryList = new ListNBT();
		this.tradeHistory.forEach((id,count) ->{
			CompoundNBT thisMemory = new CompoundNBT();
			thisMemory.putUniqueId("id", id);
			thisMemory.putInt("count", count);
			memoryList.add(thisMemory);
		});
		compound.put("Memory", memoryList);
		return compound;
	}

	@Override
	public void readNBT(CompoundNBT compound) {
		
		if(compound.contains("Limit", Constants.NBT.TAG_INT))
			this.limit = compound.getInt("Limit");
		if(compound.contains("Memory", Constants.NBT.TAG_LIST))
		{
			this.tradeHistory.clear();
			ListNBT memoryList = compound.getList("Memory", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < memoryList.size(); i++)
			{
				CompoundNBT thisMemory = memoryList.getCompound(i);
				UUID id = null;
				int count = 0;
				if(thisMemory.contains("id"))
					id = thisMemory.getUniqueId("id");
				if(thisMemory.contains("count", Constants.NBT.TAG_INT))
					count = thisMemory.getInt("count");
				if(id != null)
					this.tradeHistory.put(id, count);
			}
		}
		
	}
	
	@Override
	public ITextComponent getButtonText() { return new TranslationTextComponent("gui.button.lightmanscurrency.playerlimit"); }
	
	private static class Deserializer implements ITradeRuleDeserializer<PlayerTradeLimit>
	{
		@Override
		public PlayerTradeLimit deserialize(CompoundNBT compound) {
			PlayerTradeLimit rule = new PlayerTradeLimit();
			rule.readNBT(compound);
			return rule;
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRule.GUIHandler createHandler(TradeRuleScreen screen, Supplier<TradeRule> rule)
	{
		return new GUIHandler(screen, rule);
	}
	
	@OnlyIn(Dist.CLIENT)
	private static class GUIHandler extends TradeRule.GUIHandler
	{
		
		private final PlayerTradeLimit getTradeLimitRule()
		{
			if(getRule() instanceof PlayerTradeLimit)
				return (PlayerTradeLimit)getRule();
			return null;
		}
		
		GUIHandler(TradeRuleScreen screen, Supplier<TradeRule> rule)
		{
			super(screen, rule);
		}

		TextFieldWidget limitInput;
		Button buttonSetLimit;
		Button buttonClearMemory;
		
		private final String allowedChars = "0123456789";
		
		@Override
		public void initTab() {
			
			this.limitInput = new TextFieldWidget(screen.getFont(), screen.guiLeft() + 10, screen.guiTop() + 19, 30, 20, new StringTextComponent(""));
			this.limitInput.setMaxStringLength(3);
			this.limitInput.setText(Integer.toString(this.getTradeLimitRule().limit));
			screen.addCustomListener(this.limitInput);
			
			this.buttonSetLimit = screen.addCustomButton(new Button(screen.guiLeft() + 41, screen.guiTop() + 19, 40, 20, new TranslationTextComponent("gui.button.lightmanscurrency.playerlimit.setlimit"), this::PressSetLimitButton));
			this.buttonClearMemory = screen.addCustomButton(new Button(screen.guiLeft() + 10, screen.guiTop() + 50, screen.xSize - 20, 20, new TranslationTextComponent("gui.button.lightmanscurrency.playerlimit.clearmemory"), this::PressClearMemoryButton));
			
		}

		@Override
		public void renderTab(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
			
			this.limitInput.render(matrixStack, mouseX, mouseY, partialTicks);
			
			screen.getFont().drawString(matrixStack, new TranslationTextComponent("gui.button.lightmanscurrency.playerlimit.info", this.getTradeLimitRule().limit).getString(), screen.guiLeft() + 10, screen.guiTop() + 9, 0xFFFFFF);
			
			if(this.buttonClearMemory.isMouseOver(mouseX, mouseY))
				screen.renderTooltip(matrixStack, new TranslationTextComponent("gui.button.lightmanscurrency.playerlimit.clearmemory.tooltip"), mouseX, mouseY);
			
		}

		@Override
		public void onTabClose() {
			
			screen.removeListener(this.limitInput);
			screen.removeButton(this.buttonSetLimit);
			screen.removeButton(this.buttonClearMemory);
			
		}
		
		@Override
		public void onScreenTick() {
			
			StringBuilder limitText = new StringBuilder(this.limitInput.getText());
			for(int i = 0; i < limitText.length(); i++)
			{
				boolean allowed = false;
				for(int x = 0; x < allowedChars.length() && !allowed; x++)
				{
					if(allowedChars.charAt(x) == limitText.charAt(i))
						allowed = true;
				}
				if(!allowed)
				{
					limitText.deleteCharAt(i);
				}
			}
			this.limitInput.setText(limitText.toString());
			
			if(inputValue() <= 0)
				this.limitInput.setText("1");
			else if(inputValue() > 100)
				this.limitInput.setText("100");
		}
		
		private int inputValue()
		{
			if(isNumeric(this.limitInput.getText()))
				return Integer.parseInt(this.limitInput.getText());
			return 1;
		}
		
		private static boolean isNumeric(String string)
		{
			if(string == null)
				return false;
			try
			{
				@SuppressWarnings("unused")
				int i = Integer.parseInt(string);
			} 
			catch(NumberFormatException nfe)
			{
				return false;
			}
			return true;
		}
		
		void PressSetLimitButton(Button button)
		{
			this.getTradeLimitRule().limit = MathUtil.clamp(inputValue(), 1, 100);
			this.screen.markRulesDirty();
		}
		
		void PressClearMemoryButton(Button button)
		{
			this.getTradeLimitRule().tradeHistory.clear();
			this.screen.markRulesDirty();
		}
		
	}

}