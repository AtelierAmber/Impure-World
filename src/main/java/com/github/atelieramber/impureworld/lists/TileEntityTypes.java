package com.github.atelieramber.impureworld.lists;

import com.github.atelieramber.impureworld.ImpureWorld;
import com.github.atelieramber.impureworld.blocks.tileentities.TileEntityPollutedAir;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TileEntityTypes {
	public static final DeferredRegister<TileEntityType<?>> TILE_ENTITY_TYPES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, ImpureWorld.MODID);
	
	public static final RegistryObject<TileEntityType<TileEntityPollutedAir>> POLLUTED_AIR = TILE_ENTITY_TYPES.register("polluted_air", 
			() -> TileEntityType.Builder.create(TileEntityPollutedAir::new, BlockList.polluted_air).build(null));
}
