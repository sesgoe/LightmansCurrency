package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageTab;
import org.anti_ad.mc.ipn.api.IPNIgnore;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TabButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.IScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.LazyWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu.IClientMessage;
import io.github.lightman314.lightmanscurrency.common.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageOpenTrades;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageStoreCoins;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@IPNIgnore
public class TraderStorageScreen extends AbstractContainerScreen<TraderStorageMenu> implements IClientMessage, IScreen {

	Map<Integer,TraderStorageClientTab<?>> availableTabs = new HashMap<>();
	public TraderStorageClientTab<?> currentTab() { return this.availableTabs.get(this.menu.getCurrentTabIndex()); }

	Map<Integer,TabButton> tabButtons = new HashMap<>();

	Button buttonShowTrades;
	Button buttonCollectMoney;

	Button buttonStoreMoney;

	Button buttonTradeRules;

	List<AbstractWidget> tabRenderables = new ArrayList<>();
	List<GuiEventListener> tabListeners = new ArrayList<>();

	private final List<Runnable> tickListeners = new ArrayList<>();

	public final LazyWidgetPositioner leftEdgePositioner = LazyWidgetPositioner.create(this, LazyWidgetPositioner.MODE_BOTTOMUP, -20, TraderScreen.HEIGHT - 20, 20);

	public TraderStorageScreen(TraderStorageMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.menu.getAllTabs().forEach((key,tab) -> {
			try{
				TraderStorageClientTab<?> ct = tab.createClientTab(this);
				if(ct != null)
					this.availableTabs.put(key, ct);
			} catch (Throwable t) { LightmansCurrency.LogError("Error initializing the Trader Storage Client Tabs!", t); }
		});
		this.imageWidth = TraderScreen.WIDTH;
		this.imageHeight = TraderScreen.HEIGHT;
		menu.addMessageListener(this::serverMessage);
	}

	@Override
	public void init() {

		super.init();

		this.tabRenderables.clear();
		this.tabListeners.clear();
		this.leftEdgePositioner.clear();

		//Create the tab buttons
		this.tabButtons.clear();
		this.availableTabs.forEach((key,tab) ->{
			TabButton newButton = this.addRenderableWidget(new TabButton(button -> this.changeTab(key), this.font, tab));
			if(key == this.menu.getCurrentTabIndex())
				newButton.active = false;
			this.tabButtons.put(key, newButton);
		});
		this.tickTabButtons();
		//Position the tab buttons
		int xPos = this.leftPos - TabButton.SIZE;
		AtomicInteger index = new AtomicInteger(0);
		this.tabButtons.forEach((key,button) -> {
			int yPos = this.topPos + TabButton.SIZE * index.get();
			button.reposition(xPos, yPos, 3);
			index.set(index.get() + 1);
		});

		//Other buttons
		this.buttonShowTrades = this.addRenderableWidget(IconAndButtonUtil.traderButton(0, 0, this::PressTradesButton));

		this.buttonCollectMoney = this.addRenderableWidget(IconAndButtonUtil.collectCoinButton(0,0, this::PressCollectionButton, this.menu.player, this.menu::getTrader));
		this.buttonCollectMoney.visible = false;

		this.buttonStoreMoney = this.addRenderableWidget(IconAndButtonUtil.storeCoinButton(this.leftPos + 71, this.topPos + 120, this::PressStoreCoinsButton));
		this.buttonStoreMoney.visible = false;

		this.buttonTradeRules = this.addRenderableWidget(IconAndButtonUtil.tradeRuleButton(this.leftPos + this.imageWidth, this.topPos, this::PressTradeRulesButton));
		this.buttonTradeRules.visible = false;


		//Left side auto-position
		this.leftEdgePositioner.addWidgets(this.buttonShowTrades, this.buttonCollectMoney);

		TraderData trader = this.menu.getTrader();
		if(trader != null)
			trader.onStorageScreenInit(this, this::addRenderableWidget);

		//Initialize the current tab
		this.currentTab().onOpen();

		this.containerTick();

	}

	private void tickTabButtons()
	{
		//Position the tab buttons
		int xPos = this.leftPos - TabButton.SIZE;
		AtomicInteger index = new AtomicInteger(0);
		this.tabButtons.forEach((key,button) -> {
			TraderStorageClientTab<?> tab = this.availableTabs.get(key);
			button.visible = tab != null && tab.tabButtonVisible() && tab.commonTab.canOpen(this.menu.player);
			if(button.visible)
			{
				int yPos = this.topPos + TabButton.SIZE * index.get();
				button.reposition(xPos, yPos, 3);
				index.set(index.get() + 1);
			}
		});
	}

	@Override
	protected void renderBg(@NotNull PoseStack pose, float partialTicks, int mouseX, int mouseY) {

		this.tickTabButtons();

		RenderSystem.setShaderTexture(0, TraderScreen.GUI_TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

		//Main BG
		this.blit(pose, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

		//Coin Slots
		for(CoinSlot slot : this.menu.getCoinSlots())
		{
			if(slot.isActive())
				this.blit(pose, this.leftPos + slot.x - 1, this.topPos + slot.y - 1, this.imageWidth, 0, 18, 18);
		}

		//Current tab
		try {
			this.currentTab().renderBG(pose, mouseX, mouseY, partialTicks);
			this.tabRenderables.forEach(widget -> widget.render(pose, mouseX, mouseY, partialTicks));
		} catch(Exception e) { LightmansCurrency.LogError("Error rendering trader storage tab " + this.currentTab().getClass().getName(), e); }


	}

	@Override
	protected void renderLabels(@NotNull PoseStack pose, int mouseX, int mouseY) {

		if(this.currentTab().shouldRenderInventoryText())
			this.font.draw(pose, this.playerInventoryTitle, TraderStorageMenu.SLOT_OFFSET + 8, this.imageHeight - 94, 0x404040);

	}

	@Override
	public void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		if(this.menu.getTrader() == null)
		{
			this.menu.player.closeContainer();
			return;
		}

		this.renderBackground(pose);
		super.render(pose, mouseX, mouseY, partialTicks);
		this.renderTooltip(pose, mouseX, mouseY);

		try { this.currentTab().renderTooltips(pose, mouseX, mouseY);
		} catch(Exception e) { LightmansCurrency.LogError("Error rendering trader storage tab tooltips " + this.currentTab().getClass().getName(), e); }

		IconAndButtonUtil.renderButtonTooltips(pose, mouseX, mouseY, this.renderables);

		this.tabButtons.forEach((key, button) ->{
			if(button.isMouseOver(mouseX, mouseY))
				this.renderTooltip(pose, button.tab.getTooltip(), mouseX, mouseY);
		});

	}

	@Override
	public void containerTick()
	{
		if(this.menu.getTrader() == null)
		{
			this.menu.player.closeContainer();
			return;
		}

		this.menu.validateCoinSlots();

		if(!this.menu.hasPermission(Permissions.OPEN_STORAGE))
		{
			this.menu.player.closeContainer();
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenTrades(this.menu.getTrader().getID()));
			return;
		}

		this.buttonTradeRules.visible = this.menu.hasPermission(Permissions.EDIT_TRADE_RULES) && this.currentTab().getTradeRuleTradeIndex() >= 0;

		this.buttonStoreMoney.visible = this.menu.HasCoinsToAdd() && this.menu.hasPermission(Permissions.STORE_COINS) && this.menu.areCoinSlotsVisible();

		//Reset to the default tab if the currently selected tab doesn't have access permissions
		if(!this.currentTab().commonTab.canOpen(this.menu.player))
			this.changeTab(TraderStorageTab.TAB_TRADE_BASIC);

		this.currentTab().tick();

		for(Runnable r : this.tickListeners) r.run();

	}

	@Override
	public boolean keyPressed(int p_97765_, int p_97766_, int p_97767_) {
		InputConstants.Key mouseKey = InputConstants.getKey(p_97765_, p_97766_);
		assert this.minecraft != null;
		//Manually block closing by inventory key, to allow usage of all letters while typing player names, etc.
		if (this.currentTab().blockInventoryClosing() && this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) { return true; }
		return super.keyPressed(p_97765_, p_97766_, p_97767_);
	}

	private TabButton getTabButton(int key) {
		if(this.tabButtons.containsKey(key))
			return this.tabButtons.get(key);
		return null;
	}

	public void changeTab(int newTab) { this.changeTab(newTab, true, null); }

	public void changeTab(int newTab, boolean sendMessage, @Nullable CompoundTag selfMessage) {

		if(newTab == this.menu.getCurrentTabIndex())
			return;

		//Close the old tab
		int oldTab = this.menu.getCurrentTabIndex();
		this.currentTab().onClose();

		//Make the old tabs button active again
		TabButton button = this.getTabButton(this.menu.getCurrentTabIndex());
		if(button != null)
			button.active = true;

		//Clear the renderables & listeners
		this.tabRenderables.clear();
		this.tabListeners.clear();

		//Change the tab officially
		this.menu.changeTab(newTab);

		//Make the tab button for the current tab inactive
		button = this.getTabButton(this.menu.getCurrentTabIndex());
		if(button != null)
			button.active = false;

		//Open the new tab
		if(selfMessage != null)
			this.currentTab().receiveSelfMessage(selfMessage);
		this.currentTab().onOpen();

		//Inform the server that the tab has been changed
		if(oldTab != this.menu.getCurrentTabIndex() && sendMessage)
			this.menu.sendMessage(this.menu.createTabChangeMessage(newTab, selfMessage));

	}

	@Override
	public void selfMessage(CompoundTag message) {
		//LightmansCurrency.LogInfo("Received self-message:\n" + message.getAsString());
		if(message.contains("ChangeTab",Tag.TAG_INT))
			this.changeTab(message.getInt("ChangeTab"), false, message);
		this.currentTab().receiveSelfMessage(message);
	}

	public void serverMessage(CompoundTag message) {
		this.currentTab().receiveServerMessage(message);
	}

	public <T extends AbstractWidget> T addRenderableTabWidget(T widget) {
		this.tabRenderables.add(widget);
		return widget;
	}

	public <T extends AbstractWidget> void removeRenderableTabWidget(T widget) {
		this.tabRenderables.remove(widget);
	}

	public <T extends GuiEventListener> T addTabListener(T listener) {
		this.tabListeners.add(listener);
		return listener;
	}

	public <T extends GuiEventListener> void removeTabListener(T listener) {
		this.tabListeners.remove(listener);
	}

	@Override
	public @NotNull List<? extends GuiEventListener> children()
	{
		List<? extends GuiEventListener> coreListeners = super.children();
		List<GuiEventListener> listeners = Lists.newArrayList();
		listeners.addAll(coreListeners);
		listeners.addAll(this.tabRenderables);
		listeners.addAll(this.tabListeners);
		return listeners;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		try {
			if(this.currentTab().mouseClicked(mouseX, mouseY, button))
				return true;
		} catch(Throwable ignored) {}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		try {
			if(this.currentTab().mouseReleased(mouseX, mouseY, button))
				return true;
		} catch(Throwable ignored) {}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	private void PressTradesButton(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenTrades(this.menu.getTrader().getID()));
	}

	private void PressCollectionButton(Button button)
	{
		//Open the container screen
		if(this.menu.hasPermission(Permissions.COLLECT_COINS))
		{
			//CurrencyMod.LOGGER.info("Owner attempted to collect the stored money.");
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCollectCoins());
		}
		else
			Permissions.PermissionWarning(this.menu.player, "collect stored coins", Permissions.COLLECT_COINS);
	}

	private void PressStoreCoinsButton(Button button)
	{
		if(this.menu.hasPermission(Permissions.STORE_COINS))
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageStoreCoins());
		}
		else
			Permissions.PermissionWarning(this.menu.player, "store coins", Permissions.STORE_COINS);
	}

	private void PressTradeRulesButton(Button button)
	{
		if(this.currentTab().getTradeRuleTradeIndex() < 0)
			return;
		CompoundTag message = new CompoundTag();
		message.putInt("TradeIndex", this.currentTab().getTradeRuleTradeIndex());
		this.changeTab(TraderStorageTab.TAB_RULES_TRADE, true, message);
	}

	@Override
	public void addTickListener(Runnable r) {
		this.tickListeners.add(r);
	}

}