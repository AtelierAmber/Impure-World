package com.github.atelieramber.impureworld.lists;

import com.github.atelieramber.impureworld.ImpureWorld;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(ImpureWorld.MODID)
public class BlockList {
	public static final Block polluted_air = null;
	
	
	public static final class BlockItems {
		public static final BlockItem polluted_air = null;
	}
	//public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<Block>(ForgeRegistries.BLOCKS, ImpureWorld.MODID);
	
	//public static final RegistryObject<Block> POLLUTED_AIR = BLOCKS.register("polluted_air", () -> new PollutedAir(PollutedAir.properties));
}
