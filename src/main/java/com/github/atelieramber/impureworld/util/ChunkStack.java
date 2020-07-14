package com.github.atelieramber.impureworld.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.WorldSavedData;

public class ChunkStack {
	
	public ChunkStack(WorldSavedData data) {
		savedData = data;
	}

	private static final int TIMER_TICKS = 40;
	
	private Random rand = new Random();
	
	final WorldSavedData savedData;
	
	private class ChunkWithTimer implements Comparable<ChunkWithTimer>{
		public ChunkPos pos;
		public int timer;
		
		public ChunkWithTimer(ChunkPos pos, int timer) {
			this.pos = pos;
			this.timer = timer;
		}
		
		@Override
		public int hashCode() {
			return pos.hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			if(obj.getClass() == ChunkPos.class) {
				return pos.equals(obj);
			}else if(obj.getClass() == this.getClass()){
				return pos.equals(((ChunkWithTimer)obj).pos);
			}
			return false;
		}
		
		@Override
		public int compareTo(ChunkWithTimer o) {
			return timer - o.timer;
		}
	}

	private Map<ChunkPos, Integer> cachedChunks = new HashMap<ChunkPos, Integer>();
	private ArrayList<ChunkWithTimer> stack = new ArrayList<ChunkWithTimer>();

	boolean resort = false;

	public int size() {
		return stack.size();
	}
	
	public void track(ChunkPos pos) {
		int timer = 0;
		Integer iTemp = cachedChunks.get(pos);
		if(iTemp == null) {
			timer = rand.nextInt(TIMER_TICKS);
		}else {
			timer = iTemp;
		}
		
		
		//System.out.println("Tracking " + pos);
		
		stack.add(new ChunkWithTimer(pos, timer));
		resort = true;
	}
	
	/* Returns the index if it is tracked. -1 if it is not tracked */
	@SuppressWarnings("unlikely-arg-type")
	public int trackedIndex(ChunkPos pos) {
		if (pos == null) {
            for (int i = 0; i < size(); i++)
                if (stack.get(i)==null)
                    return i;
        } else {
            for (int i = 0; i < size(); i++)
                if (stack.get(i).equals(pos))
                    return i;
        }
		return -1;
	}
	
	public void untrack(ChunkPos pos) {
		int index = trackedIndex(pos);
		if(index >= 0) {
			ChunkWithTimer chunk = stack.get(index);
			cachedChunks.put(pos, chunk.timer);
			stack.remove(index);
			//System.out.println("Untracked " + pos);
		}else {
			System.out.println("Attempted to untrack untracked chunk! " + pos);
		}		
	}

	public int getChunkTimer(ChunkPos pos) {
		return stack.get(trackedIndex(pos)).timer;
	}
	
	public void cacheChunkChanges() {
		for(ChunkWithTimer chunk : stack) {
			if(cachedChunks.containsKey(chunk.pos)) {
				cachedChunks.remove(chunk.pos);
			}
			cachedChunks.put(chunk.pos, chunk.timer);
		}
	}
	
	/**/
	public void tick() { tick(1); }
	public void tick(int ticks) {
		for(ChunkWithTimer chunk : stack) {
			chunk.timer -= ticks;
		}
		
		if(resort) {
			Collections.sort(stack);
			resort = false;
		}
		
		savedData.markDirty();
	}
	
	public ChunkPos next() {
		if(stack.isEmpty()) return null;
		ChunkWithTimer chunk = stack.get(0);
		if(chunk.timer <= 0) {
			chunk = stack.remove(0);
			chunk.timer = TIMER_TICKS;
			stack.add(chunk);
			return chunk.pos;
		}
		return null;
	}

	public CompoundNBT write(CompoundNBT data)  {
		cacheChunkChanges();
		
		ListNBT keyList = new ListNBT();
		for(ChunkPos chunk : cachedChunks.keySet()) {
			keyList.add(new IntArrayNBT(new int[] {chunk.x, chunk.z}) );
		}
		data.put("keys", keyList);
		
		ListNBT valueList = new ListNBT();
		for(int timer : cachedChunks.values()) {
			valueList.add(IntNBT.valueOf(timer));
		}
		data.put("values", valueList);
		return data;
	}

	public void loadCache(CompoundNBT data) {
		ListNBT keyList   = (ListNBT)data.get("keys");
		ListNBT valueList = (ListNBT)data.get("values");
		
		for(int i = 0; i < keyList.size(); ++i) {
			int[] chunkPos = keyList.getIntArray(i);
			int timer = valueList.getInt(i);
			cachedChunks.put(new ChunkPos(chunkPos[0], chunkPos[1]), timer);
		}
	}
}
