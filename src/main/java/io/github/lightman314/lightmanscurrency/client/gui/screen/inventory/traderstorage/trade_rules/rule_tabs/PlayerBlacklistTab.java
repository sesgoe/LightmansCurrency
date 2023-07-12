package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRuleSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.PlayerBlacklist;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.List;

public class PlayerBlacklistTab extends TradeRuleSubTab<PlayerBlacklist> {

    public PlayerBlacklistTab(@Nonnull TradeRulesClientTab<?> parent) { super(parent, PlayerBlacklist.TYPE); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconAndButtonUtil.ICON_BLACKLIST; }

    EditBox nameInput;

    EasyButton buttonAddPlayer;
    EasyButton buttonRemovePlayer;

    ScrollTextDisplay playerDisplay;

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.nameInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 10, screenArea.y + 9, screenArea.width - 20, 20, EasyText.empty()));

        this.buttonAddPlayer = this.addChild(new EasyTextButton(screenArea.pos.offset(10, 30), 78, 20, EasyText.translatable("gui.button.lightmanscurrency.blacklist.add"), this::PressBlacklistButton));
        this.buttonRemovePlayer = this.addChild(new EasyTextButton(screenArea.pos.offset(screenArea.width - 88, 30), 78, 20, EasyText.translatable("gui.button.lightmanscurrency.blacklist.remove"), this::PressForgiveButton));

        this.playerDisplay = this.addChild(new ScrollTextDisplay(screenArea.pos.offset(7, 55), screenArea.width - 14, 84, this::getBlacklistedPlayers));
        this.playerDisplay.setColumnCount(2);

    }

    private List<Component> getBlacklistedPlayers()
    {
        List<Component> playerList = Lists.newArrayList();
        PlayerBlacklist rule = this.getRule();
        if(rule == null)
            return playerList;
        for(PlayerReference player : rule.getBannedPlayers())
            playerList.add(player.getNameComponent(true));
        return playerList;
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) { }

    void PressBlacklistButton(EasyButton button)
    {
        String name = nameInput.getValue();
        if(!name.isBlank())
        {
            nameInput.setValue("");
            CompoundTag updateInfo = new CompoundTag();
            updateInfo.putBoolean("Add", true);
            updateInfo.putString("Name", name);
            this.sendUpdateMessage(updateInfo);
        }
    }

    void PressForgiveButton(EasyButton button)
    {
        String name = nameInput.getValue();
        if(!name.isBlank())
        {
            nameInput.setValue("");
            CompoundTag updateInfo = new CompoundTag();
            updateInfo.putBoolean("Add", false);
            updateInfo.putString("Name", name);
            this.sendUpdateMessage(updateInfo);
        }
    }

}