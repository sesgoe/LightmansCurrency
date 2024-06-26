package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import javax.annotation.Nonnull;

import com.google.common.base.Supplier;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.easy.WidgetAddon;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class TeamButton extends EasyButton {

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/teambutton.png");

	public enum Size { WIDE(180, 0), NORMAL(156, 1), NARROW(90, 2);
		public final int width;
		public final int guiPos;
		Size(int width, int guiPos) {
			this.width = width;
			this.guiPos = guiPos * HEIGHT * 2;
		}
	}
	
	public static final int HEIGHT = 20;
	public static final int TEXT_COLOR = 0xFFFFFF;

	private final Size size;
	private final Supplier<Team> teamSource;
	public Team getTeam() { return this.teamSource.get(); }
	private final Supplier<Boolean> selectedSource;
	
	public TeamButton(ScreenPosition pos, Size size, Consumer<EasyButton> press, @Nonnull Supplier<Team> teamSource, @Nonnull Supplier<Boolean> selectedSource)
	{
		super(pos, size.width, HEIGHT, press);
		this.size = size;
		this.teamSource = teamSource;
		this.selectedSource = selectedSource;
	}

	@Override
	public TeamButton withAddons(WidgetAddon... addons) { this.withAddonsInternal(addons); return this; }

	@Override
	public void renderWidget(@Nonnull EasyGuiGraphics gui)
	{
		if(!this.visible || this.getTeam() == null)
			return;
		
		//Render Background
		gui.resetColor();
		gui.blit(GUI_TEXTURE, 0, 0, 0, (selectedSource.get() ? HEIGHT : 0) + this.size.guiPos, this.size.width, HEIGHT);
		
		//Render Team Name
		gui.drawString(TextRenderUtil.fitString(this.getTeam().getName(), this.width - 4), 2, 2, TEXT_COLOR);
		//Render Owner Name
		gui.drawString(TextRenderUtil.fitString(LCText.GUI_OWNER_CURRENT.get(this.getTeam().getOwner().getName(true)), this.width - 4), 2, 10, TEXT_COLOR);
		
	}
	
	@Override
	public void playDownSound(@NotNull SoundManager soundManager) {
		if(!this.visible || this.getTeam() == null)
			return;
		super.playDownSound(soundManager);
	}
	
}
