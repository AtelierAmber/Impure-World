package com.github.atelieramber.impureworld.items;

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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class AirQualityMeter extends Item {

	public AirQualityMeter(Item.Properties properties) {
		super(properties);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		BlockState state = worldIn.getBlockState(playerIn.getPosition().up());
		if (state.getMaterial() == ModMaterials.POLLUTED_AIR) {
			TileEntityPollutedAir TE = (TileEntityPollutedAir) worldIn.getTileEntity(playerIn.getPosition().up());
			if (TE != null) {
				ITextComponent text = new StringTextComponent(
						((worldIn.isRemote) ? "--Remote--\n" : "--Client--\n") + TE.toString());
				playerIn.sendMessage(text);
			}
		}
		return new ActionResult<ItemStack>(ActionResultType.SUCCESS, playerIn.getHeldItem(handIn));
	}

	public static Item.Properties properties(ItemGroup group) {
		return new Item.Properties().group(group).maxStackSize(1);
	}

}