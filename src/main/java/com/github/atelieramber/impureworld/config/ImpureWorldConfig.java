package com.github.atelieramber.impureworld.config;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryManager;

public class ImpureWorldConfig {

	public static final CommonConfig COMMON;
	public static final ForgeConfigSpec SPEC;
	public static HashMap<ResourceLocation, PolluterEntry> polluters = new HashMap<ResourceLocation, PolluterEntry>();
	
	static {
		final Pair<CommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
		SPEC = specPair.getRight();
		COMMON = specPair.getLeft();
		
		//polluters.put("minecraft:furnace", new PolluterEntry("minecraft:furnace", .33f, .33f, .33f, 1.0f));
		polluters.put(new ResourceLocation("minecraft", "campfire"), new PolluterEntry("minecraft:campfire", .33f, .33f, .33f, 1.0f, "block"));
	}
	
	public enum PolluterType{
		BLOCK,
		ENTITY,
		ITEM
	}
	
	public static class PolluterEntry{
		public String registryName;
		public String metaData;
		public String direction; // right, left, up, down, front, back, random || combine any to randomize between them all || 
								 //NOTE: If the desired direction is blocked it will push to the next viable spot, if no viable spot exists, it will start filling up inside the block
		public float carbon;
		public float sulfur;
		public float particulate;
		public float polluteFrequency;
		public List<PolluterExtras> polluterExtras;
		private IForgeRegistryEntry<?> registry = null;
		public PolluterType type;
		
		public static class PolluterExtras{
			public String registryName;
			public String target; // fuel, consumedItem, result
			public float carbon;
			public float sulfur;
			public float particulate;
		}
		
		public PolluterEntry(String registryName, float carbon, float sulfur, float particulate,
				float polluteFrequency, String type) {
			this(registryName, "", carbon, sulfur, particulate, polluteFrequency, null, type);
			
		}
		
		public PolluterEntry(String registryName, String metaData, float carbon, float sulfur, float particulate,
				float polluteFrequency, List<PolluterExtras> polluterExtras, String type) {
			super();
			this.registryName = registryName;
			this.metaData = metaData;
			this.carbon = carbon;
			this.sulfur = sulfur;
			this.particulate = particulate;
			this.polluteFrequency = polluteFrequency;
			this.polluterExtras = polluterExtras;
			type = type.toLowerCase();
			if(type == "block") {
				this.type = PolluterType.BLOCK;
			}else if(type == "entity") {
				this.type = PolluterType.ENTITY;
			}else if(type == "item") {
				this.type = PolluterType.ITEM;
			}
		}
		
		@SuppressWarnings("unchecked")
		public <V extends IForgeRegistryEntry<V>> V getRegistry(){
			if(registry == null) {
				ResourceLocation loc = new ResourceLocation(registryName);
				switch(type) {
				case BLOCK:
					registry = ForgeRegistries.BLOCKS.getValue(loc);
					break;
				case ENTITY:
					registry = ForgeRegistries.ENTITIES.getValue(loc);
					break;
				case ITEM:
					registry = ForgeRegistries.ITEMS.getValue(loc);
					break;
				default:
					break;
				}
			}
			return (V) registry;
		}

		@Override
		public int hashCode() {
			return registryName.hashCode();
		}
	}
	
	public static class CommonConfig {
		
		public CommonConfig(ForgeConfigSpec.Builder builder) {
			
		}
	}
	
	public static class SpecificConfig{
		
	}
	
}
