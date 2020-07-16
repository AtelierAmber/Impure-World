package com.github.atelieramber.impureworld.util;

import java.util.Map;
import java.util.Map.Entry;

import com.github.atelieramber.impureworld.config.ImpureWorldPolluter;
import com.github.atelieramber.impureworld.config.PolluterEntry;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

public class JsonDataManager extends JsonReloadListener {
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
	
	private Map<ResourceLocation, ImpureWorldPolluter> polluterData = ImmutableMap.of();
	
	public Map<ResourceLocation, PolluterEntry> polluters = ImmutableMap.of();

	public JsonDataManager() {
		super(GSON, "polluters");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> map, IResourceManager resourceManager, IProfiler profiler) {
		Map<ResourceLocation, ImpureWorldPolluter> polluterMap = Maps.newHashMap();
		for(Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
	         ResourceLocation loc = entry.getKey();
	         JsonElement elem = entry.getValue();
	         ImpureWorldPolluter polluter = GSON.fromJson(elem, ImpureWorldPolluter.class);
	         polluterMap.put(loc, polluter);
		}
		
		polluterData = polluterMap.entrySet().stream().collect(ImmutableMap.toImmutableMap(Entry::getKey, (v) -> {
	         return v.getValue();
	      }));

		polluters = polluterData.entrySet().stream().collect(ImmutableMap.toImmutableMap(Entry::getKey, (v) -> {
	         return PolluterEntry.fromData(v.getValue());
	      }));
	}
	
	public final Map<ResourceLocation, PolluterEntry> getPolluters(){ 
		return polluters;
	}

}
