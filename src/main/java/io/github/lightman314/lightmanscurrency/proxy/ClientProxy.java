package io.github.lightman314.lightmanscurrency.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.lightman314.lightmanscurrency.BlockItemSet;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.ClientEvents;
import io.github.lightman314.lightmanscurrency.client.gui.screen.UniversalTraderSelectionScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.*;
import io.github.lightman314.lightmanscurrency.client.model.ModelWallet;
import io.github.lightman314.lightmanscurrency.client.renderer.entity.layers.WalletLayer;
import io.github.lightman314.lightmanscurrency.client.renderer.tileentity.*;
import io.github.lightman314.lightmanscurrency.common.universal_traders.IUniversalDataDeserializer;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.core.ModContainers;
import io.github.lightman314.lightmanscurrency.core.ModTileEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

public class ClientProxy extends CommonProxy{
	
	@Override
	public void setupClient() {
		
		//Set Render Layers
    	RenderTypeLookup.setRenderLayer(ModBlocks.DISPLAY_CASE.block, RenderType.getCutout());
    	
    	setRenderLayerForSet(ModBlocks.VENDING_MACHINE1, RenderType.getCutout());
    	setRenderLayerForSet(ModBlocks.VENDING_MACHINE2, RenderType.getCutout());
    	
    	RenderTypeLookup.setRenderLayer(ModBlocks.ARMOR_DISPLAY.block, RenderType.getCutout());
    	
    	//Register Screens
    	ScreenManager.registerFactory(ModContainers.ATM, ATMScreen::new);
    	ScreenManager.registerFactory(ModContainers.MINT, MintScreen::new);
    	ScreenManager.registerFactory(ModContainers.ITEMTRADER, ItemTraderScreen::new);
    	ScreenManager.registerFactory(ModContainers.ITEMTRADERSTORAGE, ItemTraderStorageScreen::new);
    	ScreenManager.registerFactory(ModContainers.ITEMTRADERCR, ItemTraderScreenCR::new);
    	ScreenManager.registerFactory(ModContainers.ITEM_EDIT, ItemEditScreen::new);
    	ScreenManager.registerFactory(ModContainers.UNIVERSAL_ITEM_EDIT, ItemEditScreen::new);
    	ScreenManager.registerFactory(ModContainers.WALLET, WalletScreen::new);
    	ScreenManager.registerFactory(ModContainers.PAYGATE, PaygateScreen::new);
    	ScreenManager.registerFactory(ModContainers.TICKET_MACHINE, TicketMachineScreen::new);
    	ScreenManager.registerFactory(ModContainers.UNIVERSAL_ITEMTRADER, UniversalItemTraderScreen::new);
    	ScreenManager.registerFactory(ModContainers.UNIVERSAL_ITEMTRADERSTORAGE, UniversalItemTraderStorageScreen::new);
    	
    	//Register Tile Entity Renderers
    	ClientRegistry.bindTileEntityRenderer(ModTileEntities.ITEM_TRADER, ItemTraderTileEntityRenderer::new);
    	ClientRegistry.bindTileEntityRenderer(ModTileEntities.FREEZER_TRADER, FreezerTraderTileEntityRenderer::new);
    	
    	//Register ClientEvents
    	MinecraftForge.EVENT_BUS.register(new ClientEvents());
    	
    	//Register the key bind
    	ClientRegistry.registerKeyBinding(ClientEvents.KEY_WALLET);
    	
    	//Add wallet layer unless curios is loaded.
    	if(!LightmansCurrency.isCuriosLoaded())
    	{
	    	Map<String, PlayerRenderer> skinMap = Minecraft.getInstance().getRenderManager().getSkinMap();
	    	this.addWalletLayer(skinMap.get("default"));
	    	this.addWalletLayer(skinMap.get("slim"));
    	}
    	
    	
	}
	
	private static void setRenderLayerForSet(BlockItemSet<?> blockItemSet, RenderType type)
	{
		blockItemSet.getAll().forEach(blockItemPair -> RenderTypeLookup.setRenderLayer(blockItemPair.block, type));
	}
	
	private void addWalletLayer(PlayerRenderer renderer)
	{
		List<LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>>> layers = ObfuscationReflectionHelper.getPrivateValue(LivingRenderer.class, renderer, "field_177097_h");
		if(layers != null)
		{
			layers.add(new WalletLayer<>(renderer, new ModelWallet<>()));
		}
	}
	
	@Override
	public void updateTraders(CompoundNBT compound)
	{
		Minecraft minecraft = Minecraft.getInstance();
		if(minecraft.currentScreen instanceof UniversalTraderSelectionScreen)
		{
			if(compound.contains("Traders", Constants.NBT.TAG_LIST))
			{
				List<UniversalTraderData> traders = new ArrayList<>();
				ListNBT traderList = compound.getList("Traders", Constants.NBT.TAG_COMPOUND);
				traderList.forEach(nbt -> traders.add(IUniversalDataDeserializer.Deserialize((CompoundNBT)nbt)));
				((UniversalTraderSelectionScreen)minecraft.currentScreen).updateTraders(traders);
			}
		}
	}
	
	@Override
	public void openTerminalScreen(PlayerEntity player)
	{
		Minecraft.getInstance().displayGuiScreen(new UniversalTraderSelectionScreen(player));
	}
	
}