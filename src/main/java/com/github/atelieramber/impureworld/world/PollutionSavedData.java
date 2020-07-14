package com.github.atelieramber.impureworld.world;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.atelieramber.impureworld.ImpureWorld;
import com.github.atelieramber.impureworld.util.ChunkStack;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

public class PollutionSavedData extends WorldSavedData {

	private static final String  DATA_NAME = "impureworld:pollution_data";
	
	protected static final Logger LOGGER = LogManager.getLogger(ImpureWorld.MODID);
	
	public final ChunkStack chunkTracker;

	public PollutionSavedData() {
		this(DATA_NAME);
	}

	public PollutionSavedData(String name) {
		super(name);
		chunkTracker = new ChunkStack(this);
	}

	@Override
	public void read(CompoundNBT nbt) {
		CompoundNBT chunkData = nbt.getCompound("chunk_data");
		chunkTracker.loadCache(chunkData);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		CompoundNBT chunkDataNBT = chunkTracker.write(new CompoundNBT());
		compound.put("chunk_data", chunkDataNBT);
		return compound;
	}

	public static PollutionSavedData get(World world) {
		if(world.isRemote() || !(world instanceof ServerWorld)) {
			LOGGER.warn("Trying to retrieve WorldSavedData from client! World class: " + world.getClass());
			return null;
		}
		ServerWorld server = (ServerWorld)world;
		DimensionSavedDataManager storage = server.getSavedData();
		PollutionSavedData instance = (PollutionSavedData) storage.get(PollutionSavedData::new, DATA_NAME);

		if (instance == null) {
			instance = new PollutionSavedData();
			storage.set(instance);
		}
		
		return instance;
	}
}
