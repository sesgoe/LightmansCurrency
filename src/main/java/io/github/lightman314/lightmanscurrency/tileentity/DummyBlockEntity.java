package io.github.lightman314.lightmanscurrency.tileentity;

import io.github.lightman314.lightmanscurrency.core.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DummyBlockEntity extends BlockEntity{

	public DummyBlockEntity(BlockPos pos, BlockState state)
	{
		super(ModTileEntities.DUMMY, pos, state);
	}
	
}
