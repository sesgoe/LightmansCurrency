package io.github.lightman314.lightmanscurrency.client.gui.overlay;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenCorner;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class WalletDisplayOverlay {

    private WalletDisplayOverlay() {}

    public enum DisplayType { ITEMS_WIDE, ITEMS_NARROW, TEXT }


    @SubscribeEvent
    public static void onOverlayRendered(RenderGameOverlayEvent.Post event) {
        if(event.getType() == RenderGameOverlayEvent.ElementType.ALL)
        {
            render(Minecraft.getInstance().gui, event.getMatrixStack(), event.getPartialTicks(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight());
        }
    }

    private static void render(IngameGui gui, MatrixStack pose, float partialTick, int screenWidth, int screenHeight) {
        if(!Config.CLIENT.walletOverlayEnabled.get())
            return;

        ScreenCorner corner = Config.CLIENT.walletOverlayCorner.get();
        ScreenPosition offset = Config.CLIENT.walletOverlayPosition.get();

        ScreenPosition currentPosition = corner.getCorner(screenWidth, screenHeight).offset(offset);
        if(corner.isRightSide)
            currentPosition = currentPosition.offset(ScreenPosition.of(-16,0));
        if(corner.isBottomSide)
            currentPosition = currentPosition.offset(ScreenPosition.of(0, -16));

        //Draw the wallet
        ItemStack wallet = LightmansCurrency.getWalletStack(Minecraft.getInstance().player);
        if(!wallet.isEmpty())
        {
            //Draw the wallet
            ItemRenderUtil.drawItemStack(gui, gui.getFont(), wallet, currentPosition.x, currentPosition.y);
            if(corner.isRightSide)
                currentPosition = currentPosition.offset(ScreenPosition.of(-17,0));
            else
                currentPosition = currentPosition.offset(ScreenPosition.of(17,0));

            CoinValue walletValue = MoneyUtil.getCoinValue(WalletItem.getWalletInventory(wallet));

            //Draw the stored money
            switch(Config.CLIENT.walletOverlayType.get())
            {
                case ITEMS_NARROW:
                case ITEMS_WIDE:
                    int offsetAmount = Config.CLIENT.walletOverlayType.get() == DisplayType.ITEMS_WIDE ? 17 : 9;
                    List<ItemStack> contents = walletValue.getAsItemList();
                    for(ItemStack coin : contents)
                    {
                        ItemRenderUtil.drawItemStack(gui, gui.getFont(), coin, currentPosition.x, currentPosition.y);
                        if(corner.isRightSide)
                            currentPosition = currentPosition.offset(ScreenPosition.of(-offsetAmount,0));
                        else
                            currentPosition = currentPosition.offset(ScreenPosition.of(offsetAmount,0));
                    }
                    return;
                case TEXT:
                    String valueString = walletValue.getString();
                    if(corner.isRightSide)
                        gui.getFont().draw(pose, valueString, currentPosition.x - gui.getFont().width(valueString), currentPosition.y + 3, 0xFFFFFF);
                    else
                        gui.getFont().draw(pose, valueString, currentPosition.x, currentPosition.y + 3, 0xFFFFFF);
            }

        }

    }

}