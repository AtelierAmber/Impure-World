package com.github.atelieramber.impureworld.events;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.atelieramber.impureworld.ImpureWorld;
import com.github.atelieramber.impureworld.blocks.tileentities.TileEntityPollutedAir;
import com.github.atelieramber.impureworld.config.ImpureWorldConfig;
import com.github.atelieramber.impureworld.config.ImpureWorldConfig.PolluterEntry;
import com.github.atelieramber.impureworld.lists.BlockList;
import com.github.atelieramber.impureworld.world.PollutionSavedData;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.palette.ArrayPalette;
import net.minecraft.util.palette.HashMapPalette;
import net.minecraft.util.palette.IPalette;
import net.minecraft.util.palette.PalettedContainer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.LogicalSide;

public class HandleChunkEvents {	
	protected static final Logger LOGGER = LogManager.getLogger(ImpureWorld.MODID);
	
	public static void load(ChunkEvent.Load event) {
		if(event.getWorld().isRemote()) return;
		PollutionSavedData savedData;
		if(event.getWorld() instanceof WorldGenRegion) {
			savedData = PollutionSavedData.get(event.getWorld().getWorld());
		}else {
			savedData = PollutionSavedData.get((World)event.getWorld());
		}
		savedData.chunkTracker.track(event.getChunk().getPos());
	}

	public static void unload(ChunkEvent.Unload event) {
		if(event.getWorld().isRemote()) return;
		PollutionSavedData savedData;
		if(event.getWorld() instanceof WorldGenRegion) {
			savedData = PollutionSavedData.get(event.getWorld().getWorld());
		}else {
			savedData = PollutionSavedData.get((World)event.getWorld());
		}
		savedData.chunkTracker.untrack(event.getChunk().getPos());
	}
	
	public static void worldTick(WorldTickEvent event) {
		if(event.side == LogicalSide.CLIENT || event.phase == Phase.START || event.world.isRemote()) return;
		
		World world = event.world;
		
		IProfiler profiler = world.getProfiler();
		profiler.startSection("pollutionUpdates");
		
		PollutionSavedData savedData = PollutionSavedData.get(world);
		
		tickChunks(savedData, world, profiler);
		
		profiler.endSection();
	}

	@SuppressWarnings("deprecation")
	private static void tickChunks(PollutionSavedData savedData, World world, IProfiler profiler) {
		profiler.startSection("pollutionUpdates:tick");
		savedData.chunkTracker.tick();
		profiler.endSection();
		profiler.startSection("pollutionUpdates:loop");
		ChunkPos pos;
		while((pos = savedData.chunkTracker.next()) != null) {
			profiler.startSection("pollutionUpdates:loop.getChunk");
			IChunk chunk = world.getChunk(pos.x, pos.z);
			if(!chunk.isModified()) {
				profiler.endSection();
				continue;
			}
			ChunkSection[] sections = chunk.getSections();
			profiler.endSection();
			profiler.startSection("pollutionUpdates:loop.sections");
			for(int i = 0; i < sections.length; ++i) {
				ChunkSection section = sections[i];
				if(section != null && !section.isEmpty() && sectionHasPolluter(section, ImpureWorldConfig.polluters)) {
					for(int x = 0; x < 16; ++x) {
						for(int y = 0; y < 16; ++y) {
							for(int z = 0; z < 16; ++z) {
								BlockState state = section.getBlockState(x, y, z);
								Block block = state.getBlock();
								ResourceLocation registryName = block.getRegistryName();
								ImpureWorldConfig.PolluterEntry polluter = ImpureWorldConfig.polluters.get(registryName);
								if(polluter != null) {
									if(y < 15 ) {
										if(section.getBlockState(x, y+1, z).isAir()) {
											BlockPos blockPos = new BlockPos(x, y + 1 + section.getYLocation(), z);
											blockPos = pos.asBlockPos().add(blockPos);
											System.out.println("Polluting at " + blockPos);
											world.setBlockState(blockPos, BlockList.polluted_air.getDefaultState());
											TileEntityPollutedAir te = (TileEntityPollutedAir) world.getTileEntity(blockPos);
											te.setComposition(polluter.carbon, polluter.sulfur, polluter.particulate);
										}
									}
								}
							}
						}
					}
				}
			}
			profiler.endSection();
		}
		profiler.endSection();
	}
	
	private static boolean sectionHasPolluter(ChunkSection section, HashMap<ResourceLocation, PolluterEntry> polluters) {
		PalettedContainer<BlockState> data = section.getData();
		IPalette<BlockState> palette = data.palette;
		for(PolluterEntry p : polluters.values()){
			switch(p.type) {
			case BLOCK:
				List<BlockState> states = p.getBlockStates();
				for(BlockState state : states) {
					if(paletteContains(palette, state)) {
						return true;
					}
				}
			case ENTITY:
			case ITEM:
			default:
				break;
			}
		}
		return false;
	}
	
	private static <T> boolean paletteContains(IPalette<T> palette, T value) {
		if(palette instanceof ArrayPalette) {
			ArrayPalette<T> newPalette = (ArrayPalette<T>) palette;
			return ArrayUtils.contains(newPalette.states, value);
		} else if(palette instanceof HashMapPalette) {
			HashMapPalette<T> newPalette = (HashMapPalette<T>) palette;
		      return newPalette.statePaletteMap.getId(value) != -1;
		}
		return true;
	}
	
}
