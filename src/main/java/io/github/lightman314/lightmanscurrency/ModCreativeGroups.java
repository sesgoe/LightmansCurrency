package io.github.lightman314.lightmanscurrency;

import com.google.common.base.Suppliers;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.ModRegistries;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBiBundle;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBundle;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = LightmansCurrency.MODID)
public class ModCreativeGroups {

    public static final ResourceLocation COIN_GROUP_ID = new ResourceLocation(LightmansCurrency.MODID,"coins");
    public static final ResourceLocation MACHINE_GROUP_ID = new ResourceLocation(LightmansCurrency.MODID,"machines");
    public static final ResourceLocation TRADER_GROUP_ID = new ResourceLocation(LightmansCurrency.MODID,"traders");
    public static final ResourceLocation UPGRADE_GROUP_ID = new ResourceLocation(LightmansCurrency.MODID,"upgrades");

    /**
     * Placeholder function to force the static class loading
     */
    public static void init() { }

    static {

        COIN_GROUP = ModRegistries.CREATIVE_TABS.register("coins", () -> CreativeModeTab.builder()
                .title(EasyText.translatable("itemGroup.lightmanscurrency.coins"))
                .icon(ezIcon(ModBlocks.COINPILE_GOLD))
                .displayItems((parameters,p) -> {
                    //Coin -> Coin Pile -> Coin Block by type
                    ezPop(p, ModItems.COIN_COPPER);
                    ezPop(p, ModBlocks.COINPILE_COPPER);
                    ezPop(p, ModBlocks.COINBLOCK_COPPER);
                    ezPop(p, ModItems.COIN_IRON);
                    ezPop(p, ModBlocks.COINPILE_IRON);
                    ezPop(p, ModBlocks.COINBLOCK_IRON);
                    ezPop(p, ModItems.COIN_GOLD);
                    ezPop(p, ModBlocks.COINPILE_GOLD);
                    ezPop(p, ModBlocks.COINBLOCK_GOLD);
                    ezPop(p, ModItems.COIN_EMERALD);
                    ezPop(p, ModBlocks.COINPILE_EMERALD);
                    ezPop(p, ModBlocks.COINBLOCK_EMERALD);
                    ezPop(p, ModItems.COIN_DIAMOND);
                    ezPop(p, ModBlocks.COINPILE_DIAMOND);
                    ezPop(p, ModBlocks.COINBLOCK_DIAMOND);
                    ezPop(p, ModItems.COIN_NETHERITE);
                    ezPop(p, ModBlocks.COINPILE_NETHERITE);
                    ezPop(p, ModBlocks.COINBLOCK_NETHERITE);
                    //Wallets
                    ezPop(p, ModItems.WALLET_COPPER);
                    ezPop(p, ModItems.WALLET_IRON);
                    ezPop(p, ModItems.WALLET_GOLD);
                    ezPop(p, ModItems.WALLET_EMERALD);
                    ezPop(p, ModItems.WALLET_DIAMOND);
                    ezPop(p, ModItems.WALLET_NETHERITE);
                    //Trading Core
                    ezPop(p, ModItems.TRADING_CORE);
                }).build()
        );

        MACHINE_GROUP = ModRegistries.CREATIVE_TABS.register("machines", () -> CreativeModeTab.builder()
                .withTabsBefore(COIN_GROUP_ID)
                .title(EasyText.translatable("itemGroup.lightmanscurrency.machines"))
                .icon(ezIcon(ModBlocks.MACHINE_MINT))
                .displayItems((parameters, p) -> {
                    //Coin Mint
                    ezPop(p, ModBlocks.MACHINE_MINT);
                    //ATM
                    ezPop(p, ModBlocks.MACHINE_ATM);
                    ezPop(p, ModItems.PORTABLE_ATM);
                    //Cash Register
                    ezPop(p, ModBlocks.CASH_REGISTER);
                    //Terminal
                    ezPop(p, ModBlocks.TERMINAL);
                    ezPop(p, ModBlocks.GEM_TERMINAL);
                    ezPop(p, ModItems.PORTABLE_TERMINAL);
                    ezPop(p, ModItems.PORTABLE_GEM_TERMINAL);
                    //Trader Interface
                    ezPop(p, ModBlocks.ITEM_TRADER_INTERFACE);
                    //Auction Stands
                    ezPop(p, ModBlocks.AUCTION_STAND);
                    //Ticket Machine
                    ezPop(p, ModBlocks.TICKET_STATION);
                    //Tickets (with a creative default UUID)
                    p.accept(TicketItem.CreateMasterTicket(TicketItem.CREATIVE_TICKET_ID, TicketItem.CREATIVE_TICKET_COLOR));
                    p.accept(TicketItem.CreatePass(TicketItem.CREATIVE_TICKET_ID, TicketItem.CREATIVE_TICKET_COLOR));
                    p.accept(TicketItem.CreateTicket(TicketItem.CREATIVE_TICKET_ID, TicketItem.CREATIVE_TICKET_COLOR));
                    //Ticket Stub
                    ezPop(p, ModItems.TICKET_STUB);
                    //Coin Chest
                    ezPop(p, ModBlocks.COIN_CHEST);
                    //Coin Jars
                    ezPop(p, ModBlocks.PIGGY_BANK);
                    ezPop(p, ModBlocks.COINJAR_BLUE);
                }).build()
        );

        TRADER_GROUP = ModRegistries.CREATIVE_TABS.register("traders", () -> CreativeModeTab.builder()
                .withTabsBefore(MACHINE_GROUP_ID)
                .title(EasyText.translatable("itemGroup.lightmanscurrency.trading"))
                .icon(ezIcon(ModBlocks.DISPLAY_CASE))
                .displayItems((parameters, p) -> {
                    //Item Traders (normal)
                    ezPop(p, ModBlocks.SHELF);
                    ezPop(p, ModBlocks.DISPLAY_CASE);
                    ezPop(p, ModBlocks.CARD_DISPLAY);
                    ezPop(p, ModBlocks.VENDING_MACHINE);
                    debugItems(ModBlocks.VENDING_MACHINE);
                    LightmansCurrency.LogDebug("Light Blue VM ID is " + ForgeRegistries.BLOCKS.getKey(ModBlocks.VENDING_MACHINE.get(Color.LIGHT_BLUE)));
                    ezPop(p, ModBlocks.FREEZER);
                    ezPop(p, ModBlocks.VENDING_MACHINE_LARGE);
                    debugItems(ModBlocks.VENDING_MACHINE_LARGE);
                    //Item Traders (specialty)
                    ezPop(p, ModBlocks.ARMOR_DISPLAY);
                    ezPop(p, ModBlocks.TICKET_KIOSK);
                    ezPop(p, ModBlocks.BOOKSHELF_TRADER);
                    //Slot Machine Trader
                    ezPop(p, ModBlocks.SLOT_MACHINE);
                    //Item Traders (network)
                    ezPop(p, ModBlocks.ITEM_NETWORK_TRADER_1);
                    ezPop(p, ModBlocks.ITEM_NETWORK_TRADER_2);
                    ezPop(p, ModBlocks.ITEM_NETWORK_TRADER_3);
                    ezPop(p, ModBlocks.ITEM_NETWORK_TRADER_4);
                    //Paygate
                    ezPop(p, ModBlocks.PAYGATE);
                }).build()
        );

        UPGRADE_GROUP = ModRegistries.CREATIVE_TABS.register("upgrades", () -> CreativeModeTab.builder()
                .withTabsBefore(TRADER_GROUP_ID)
                .title(EasyText.translatable("itemGroup.lightmanscurrency.upgrades"))
                .icon(ezIcon(ModItems.ITEM_CAPACITY_UPGRADE_1))
                .displayItems((parameters, p) -> {
                    ezPop(p, ModItems.UPGRADE_SMITHING_TEMPLATE);
                    ezPop(p, ModItems.ITEM_CAPACITY_UPGRADE_1);
                    ezPop(p, ModItems.ITEM_CAPACITY_UPGRADE_2);
                    ezPop(p, ModItems.ITEM_CAPACITY_UPGRADE_3);
                    ezPop(p, ModItems.SPEED_UPGRADE_1);
                    ezPop(p, ModItems.SPEED_UPGRADE_2);
                    ezPop(p, ModItems.SPEED_UPGRADE_3);
                    ezPop(p, ModItems.SPEED_UPGRADE_4);
                    ezPop(p, ModItems.SPEED_UPGRADE_5);
                    ezPop(p, ModItems.NETWORK_UPGRADE);
                    ezPop(p, ModItems.HOPPER_UPGRADE);
                    ezPop(p, ModItems.COIN_CHEST_EXCHANGE_UPGRADE);
                    //ezPop(p, ModItems.COIN_CHEST_BANK_UPGRADE);
                    ezPop(p, ModItems.COIN_CHEST_MAGNET_UPGRADE_1);
                    ezPop(p, ModItems.COIN_CHEST_MAGNET_UPGRADE_2);
                    ezPop(p, ModItems.COIN_CHEST_MAGNET_UPGRADE_3);
                    ezPop(p, ModItems.COIN_CHEST_MAGNET_UPGRADE_4);
                    ezPop(p, ModItems.COIN_CHEST_SECURITY_UPGRADE);
                }).build()
        );

    }

    @SubscribeEvent
    public static void buildVanillaTabContents(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS)
        {
            event.accept(ModBlocks.PIGGY_BANK);
            event.accept(ModBlocks.COINJAR_BLUE);
        }
        if(event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS)
        {
            event.accept(ModBlocks.PAYGATE);
        }
        if(event.getTabKey() == CreativeModeTabs.COLORED_BLOCKS)
        {
            event.acceptAll(convertToStack(ModBlocks.VENDING_MACHINE.getAllSorted()));
            event.acceptAll(convertToStack(ModBlocks.VENDING_MACHINE_LARGE.getAllSorted()));
            event.acceptAll(convertToStack(ModBlocks.FREEZER.getAllSorted()));
        }
    }

    private static Supplier<ItemStack> ezIcon(RegistryObject<? extends ItemLike> item) { return Suppliers.memoize(() -> new ItemStack(item.get())); }

    public static void ezPop(CreativeModeTab.Output populator, RegistryObject<? extends ItemLike> item)  { populator.accept(item.get()); }
    public static void ezPop(CreativeModeTab.Output populator, RegistryObjectBundle<? extends ItemLike,?> bundle) { bundle.getAllSorted().forEach(populator::accept); }
    public static void ezPop(CreativeModeTab.Output populator, RegistryObjectBiBundle<? extends ItemLike,?,?> bundle) { bundle.getAllSorted().forEach(populator::accept); }

    private static Collection<ItemStack> convertToStack(Collection<? extends ItemLike> list) {
        List<ItemStack> result = new ArrayList<>();
        for (ItemLike item : list) result.add(new ItemStack(item));
        return result;
    }


    public static final RegistryObject<CreativeModeTab> COIN_GROUP;
    public static final RegistryObject<CreativeModeTab> MACHINE_GROUP;
    public static final RegistryObject<CreativeModeTab> TRADER_GROUP;
    public static final RegistryObject<CreativeModeTab> UPGRADE_GROUP;

    private static void debugItems(RegistryObjectBundle<?,?> bundle)
    {
        StringBuilder builder = new StringBuilder("Bundle Contains:");
        for(ResourceLocation id : bundle.getAllKeys())
            builder.append('\n').append(id.toString());
        LightmansCurrency.LogDebug(builder.toString());
    }

}