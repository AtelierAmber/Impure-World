package com.github.atelieramber.impureworld.items;

import java.util.UUID;

import com.github.atelieramber.impureworld.blocks.tileentities.TileEntityPollutedAir;
import com.github.atelieramber.impureworld.materials.ModMaterials;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class AirQualityMeter extends Item {

	public AirQualityMeter(Item.Properties properties) {
		super(properties);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		BlockState state = worldIn.getBlockState(playerIn.func_233580_cy_().up());
		if (state.getMaterial() == ModMaterials.POLLUTED_AIR) {
			TileEntityPollutedAir TE = (TileEntityPollutedAir) worldIn.getTileEntity(playerIn.func_233580_cy_().up());
			if (TE != null) {
				if(!playerIn.isSneaking()) {
					ITextComponent text = new StringTextComponent(
							((worldIn.isRemote) ? "--Remote--\n" : "--Client--\n") + TE.toString());
					playerIn.sendMessage(text, playerIn.getUniqueID());
				}else {
					TE.setComposition(1.0/3.0, 1.0/3.0, 1.0/3.0);
				}
			}
		}
		return new ActionResult<ItemStack>(ActionResultType.SUCCESS, playerIn.getHeldItem(handIn));
	}
	
	public static Item.Properties properties(ItemGroup group) {
		return new Item.Properties().group(group).maxStackSize(1);
	}

}
