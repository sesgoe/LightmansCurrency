package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class UniversalTraderButton extends Button{
	
	public static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/universaltraderbuttons.png");
	
	public static final int WIDTH = 146;
	public static final int HEIGHT = 30;
	
	UniversalTraderData data;
	
	Font font;
	
	public UniversalTraderButton(int x, int y, OnPress pressable, Font font)
	{
		super(x, y, WIDTH, HEIGHT, new TextComponent(""), pressable);
		this.font = font;
	}
	
	/**
	 * Updates the trader data for this buttons trade.
	 */
	public void SetData(UniversalTraderData data)
	{
		this.data = data;
	}
	
	@Override
	public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
	{
		//Set active status
		this.active = this.data != null;
		//Render nothing if there is no data
		if(this.data == null)
			return;
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, BUTTON_TEXTURES);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		
		int offset = 0;
		if(this.isHovered)
			offset = HEIGHT;
		//Draw Button BG
		this.blit(poseStack, this.x, this.y, 0, offset, WIDTH, HEIGHT);
		
		//Draw the icon
		ResourceLocation iconResource = this.data.IconLocation();
		if(iconResource != null)
		{
			RenderSystem.setShaderTexture(0, iconResource);
			this.blit(poseStack, this.x + 4, this.y + 7, this.data.IconPositionX(), this.data.IconPositionY(), 16, 16);
		}
		
		//Draw the name & owner of the trader
		this.font.draw(poseStack, this.data.getName().getString(), this.x + 24f, this.y + 6f, 0x404040);
		this.font.draw(poseStack, this.data.getOwnerName(), this.x + 24f, this.y + 16f, 0x404040);
		
	}

}
