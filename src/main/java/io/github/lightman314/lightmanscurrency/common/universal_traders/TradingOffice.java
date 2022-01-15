package io.github.lightman314.lightmanscurrency.common.universal_traders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Supplier;
import com.google.common.collect.Maps;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.traderSearching.TraderSearchFilter;
import io.github.lightman314.lightmanscurrency.events.UniversalTraderEvent.*;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.command.MessageSyncAdminList;
import io.github.lightman314.lightmanscurrency.network.message.teams.MessageInitializeClientTeams;
import io.github.lightman314.lightmanscurrency.network.message.teams.MessageRemoveClientTeam;
import io.github.lightman314.lightmanscurrency.network.message.teams.MessageUpdateClientTeam;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageInitializeClientTraders;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageRemoveClientTrader;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageUpdateClientData;
import io.github.lightman314.lightmanscurrency.tileentity.UniversalTraderTileEntity;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID)
public class TradingOffice extends WorldSavedData{
	
	private static final Map<ResourceLocation,Supplier<? extends UniversalTraderData>> registeredDeserializers = Maps.newHashMap();
	
	public static final void RegisterDataType(ResourceLocation key, Supplier<? extends UniversalTraderData> source)
	{
		if(registeredDeserializers.containsKey(key))
		{
			LightmansCurrency.LogError("A universal trader type of key " + key + " has already been registered.");
			return;
		}
		else
			registeredDeserializers.put(key, source);
	}
	
	private static final String DATA_NAME = LightmansCurrency.MODID + "_trading_office";
	
	private static List<UUID> adminPlayers = new ArrayList<>();
	
	private Map<UUID, UniversalTraderData> universalTraderMap = new HashMap<>();
	private Map<UUID, Team> playerTeams = new HashMap<>();
	
	public TradingOffice()
	{
		super(DATA_NAME);
	}
	
	public TradingOffice(String name)
	{
		super(name);
	}

	@SuppressWarnings("deprecation")
	public static UniversalTraderData Deserialize(CompoundNBT compound)
	{
		ResourceLocation thisType = new ResourceLocation(compound.getString("type"));
		//New method
		if(registeredDeserializers.containsKey(thisType))
		{
			UniversalTraderData data = registeredDeserializers.get(thisType).get();
			data.read(compound);
			return data;
		}
		//Fall back onto the old method to allow older addon mods
		return IUniversalDataDeserializer.ClassicDeserialize(compound);
	}
	
	@Override
	public void read(CompoundNBT compound) {
		
		universalTraderMap.clear();
		if(compound.contains("UniversalTraders", Constants.NBT.TAG_LIST))
		{
			ListNBT universalTraderDataList = compound.getList("UniversalTraders", Constants.NBT.TAG_COMPOUND);
			universalTraderDataList.forEach(nbt ->{
				CompoundNBT traderNBT = (CompoundNBT)nbt;
				//UUID traderID = traderNBT.getUniqueId("ID");
				//UniversalTraderData data = IUniversalDataDeserializer.Deserialize(traderNBT);
				UniversalTraderData data = Deserialize(traderNBT);
				if(data != null)
					universalTraderMap.put(data.getTraderID(), data);
			});
		}
		if(compound.contains("Teams", Constants.NBT.TAG_LIST))
		{
			ListNBT teamList = compound.getList("Teams", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < teamList.size(); ++i)
			{
				Team team = Team.load(teamList.getCompound(i));
				if(team != null)
					this.playerTeams.put(team.getID(), team);
			}
		}
		
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		ListNBT universalTraderDataList = new ListNBT();
		this.universalTraderMap.forEach((traderID, traderData) ->
		{
			if(traderData != null)
			{
				CompoundNBT traderNBT = traderData.write(new CompoundNBT());
				traderNBT.putUniqueId("ID", traderID);
				universalTraderDataList.add(traderNBT);
			}
		});
		compound.put("UniversalTraders", universalTraderDataList);
		
		ListNBT teamList = new ListNBT();
		this.playerTeams.forEach((teamID, team) ->{
			if(team != null)
				teamList.add(team.save());
		});
		compound.put("Teams", teamList);
		
		return compound;
	}
	
	public static UniversalTraderData getData(UUID traderID)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			TradingOffice office = get(server);
			if(office.universalTraderMap.containsKey(traderID))
			{
				return office.universalTraderMap.get(traderID);
			}
		}
		return null;
	}
	
	public static List<UniversalTraderData> getTraders()
	{
		TradingOffice office = get(ServerLifecycleHooks.getCurrentServer());
		return office.universalTraderMap.values().stream().collect(Collectors.toList());
	}
	
	public static List<UniversalTraderData> filterTraders(String searchFilter, List<UniversalTraderData> traders)
	{
		if(searchFilter.isEmpty())
			return traders;
		Stream<UniversalTraderData> stream = traders.stream().filter(entry ->{
			String searchText = searchFilter.toLowerCase().trim();
			//Search the display name of the traders
			if(entry.getName().getString().toLowerCase().contains(searchText))
				return true;
			//Search the owner name of the traders
			if(entry.getCoreSettings().getOwner().lastKnownName().toLowerCase().contains(searchText))
				return true;
			//Search any custom filters
			return TraderSearchFilter.checkFilters(entry, searchText);
		});
		return stream.collect(Collectors.toList());
	}
	
	public static List<UniversalTraderData> getTraders(String searchFilter)
	{
		return filterTraders(searchFilter, getTraders());
	}
	
	public static void MarkDirty(UUID traderID)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			get(server).markDirty();
			//Send update packet to all connected clients
			UniversalTraderData data = getData(traderID);
			if(data != null)
			{
				CompoundNBT compound = data.write(new CompoundNBT());
				LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new MessageUpdateClientData(compound));
			}
		}
	}
	
	public static void MarkDirty(UUID traderID, CompoundNBT updateMessage)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			get(server).markDirty();
			//Send update packet to all connected clients
			UniversalTraderData data = getData(traderID);
			if(data != null)
				LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new MessageUpdateClientData(updateMessage));
		}
	}
	
	public static List<Team> getTeams()
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
			return get(server).playerTeams.values().stream().collect(Collectors.toList());
		return new ArrayList<>();
	}
	
	public static Team getTeam(UUID teamID)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			TradingOffice office = get(server);
			if(office.playerTeams.containsKey(teamID))
				return office.playerTeams.get(teamID);
		}
		return null;
	}
	
	public static void MarkTeamDirty(UUID teamID)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			get(server).markDirty();
			//Send update packet to all connected clients
			Team team = getTeam(teamID);
			if(team != null)
			{
				CompoundNBT compound = team.save();
				LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new MessageUpdateClientTeam(compound));
			}
		}
	}
	
	public static UUID registerTrader(UniversalTraderData data, PlayerEntity owner)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			TradingOffice office = get(server);
			//Generate a trader ID
			UUID traderID = UUID.randomUUID();
			while(office.universalTraderMap.containsKey(traderID))
				traderID = UUID.randomUUID();
			
			//Apply it to the trader
			data.initTraderID(traderID);
			
			LightmansCurrency.LogInfo("Successfully registered the universal trader with id '" + traderID + "'!");
			office.universalTraderMap.put(traderID, data);
			office.markDirty();
			//Send update packet to the connected clients
			CompoundNBT compound = data.write(new CompoundNBT());
			LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new MessageUpdateClientData(compound));
			//Post Universal Trader Create Event
			MinecraftForge.EVENT_BUS.post(new UniversalTradeCreateEvent(traderID, owner));
			
			return traderID;
		}
		return null;
	}
	
	public static void removeTrader(UUID traderID)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		TradingOffice office = get(server);
		if(office.universalTraderMap.containsKey(traderID))
		{
			UniversalTraderData removedData = office.universalTraderMap.get(traderID);
			office.universalTraderMap.remove(traderID);
			office.markDirty();
			LightmansCurrency.LogInfo("Successfully removed the universal trader with id '" + traderID + "'!");
			//Send update packet to the connected clients
			LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new MessageRemoveClientTrader(traderID));
			MinecraftForge.EVENT_BUS.post(new UniversalTradeRemoveEvent(traderID, removedData));
		}
	}
	
	public static Team registerTeam(PlayerEntity owner, String teamName)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			TradingOffice office = get(server);
			UUID teamID = UUID.randomUUID();
			while(office.playerTeams.containsKey(teamID))
				teamID = UUID.randomUUID();
			
			Team newTeam = Team.of(teamID, PlayerReference.of(owner), teamName);
			office.playerTeams.put(teamID, newTeam);
			
			//Send update packet to the connected clients
			MarkTeamDirty(newTeam.getID());
			
			return newTeam;
		}
		return null;
	}
	
	public static void removeTeam(UUID teamID)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			TradingOffice office = get(server);
			if(office.playerTeams.containsKey(teamID))
			{
				office.playerTeams.remove(teamID);
				office.markDirty();
				//Send update packet to the connected clients
				LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new MessageRemoveClientTeam(teamID));
			}
		}
	}
	
	private static TradingOffice get(MinecraftServer server)
    {
        ServerWorld world = server.getWorld(World.OVERWORLD);
        return world.getSavedData().getOrCreate(TradingOffice::new, DATA_NAME);
    }
	
	/**
	 * Sync traders with new players on login
	 */
	@SubscribeEvent
	public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			PacketTarget target = LightmansCurrencyPacketHandler.getTarget(event.getPlayer());
			TradingOffice office = get(server);
			//Send update message to the connected clients
			CompoundNBT compound = new CompoundNBT();
			ListNBT traderList = new ListNBT();
			office.universalTraderMap.forEach((id, trader)-> traderList.add(trader.write(new CompoundNBT())) );
			compound.put("Traders", traderList);
			LightmansCurrencyPacketHandler.instance.send(target, new MessageInitializeClientTraders(compound));
			
			CompoundNBT compound2 = new CompoundNBT();
			ListNBT teamList = new ListNBT();
			office.playerTeams.forEach((id,team) -> teamList.add(team.save()));
			compound2.put("Teams", teamList);
			LightmansCurrencyPacketHandler.instance.send(target, new MessageInitializeClientTeams(compound2));
		}
	}
	
	/**
	 * Clean up invalid traders
	 */
	@SubscribeEvent
	public static void onTick(TickEvent.WorldTickEvent event)
	{
		if(event.phase != TickEvent.Phase.START)
			return;
		
		if(event.side != LogicalSide.SERVER)
			return;
		
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null && server.getTickCounter() % 1200 == 0)
		{
			TradingOffice office = get(server);
			office.universalTraderMap.values().removeIf(traderData ->{
				BlockPos pos = traderData.getPos();
				ServerWorld world = server.getWorld(traderData.getWorld());
				if(world.isAreaLoaded(pos, 0))
				{
					TileEntity tileEntity = world.getTileEntity(pos);
					if(tileEntity instanceof UniversalTraderTileEntity)
					{
						UniversalTraderTileEntity traderEntity = (UniversalTraderTileEntity)tileEntity;
						return traderEntity.getTraderID() == null || !traderEntity.getTraderID().equals(traderData.getTraderID());
					}
					return true;
				}
				return false;
			});
		}
	}
	
	public static boolean isAdminPlayer(PlayerEntity player)
	{
		return adminPlayers.contains(player.getUniqueID()) && player.hasPermissionLevel(2);
	}
	
	public static void toggleAdminPlayer(PlayerEntity player)
	{
		UUID playerID = player.getUniqueID();
		if(adminPlayers.contains(playerID))
		{
			adminPlayers.remove(playerID);
			if(!player.world.isRemote)
			{
				LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), getAdminSyncMessage());
			}
		}
		else
		{
			adminPlayers.add(playerID);
			if(!player.world.isRemote)
			{
				LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), getAdminSyncMessage());
			}
		}
	}
	
	public static MessageSyncAdminList getAdminSyncMessage()
	{
		return new MessageSyncAdminList(adminPlayers);
	}
	
	public static void loadAdminPlayers(List<UUID> serverAdminList)
	{
		adminPlayers = serverAdminList;
	}
	
}
