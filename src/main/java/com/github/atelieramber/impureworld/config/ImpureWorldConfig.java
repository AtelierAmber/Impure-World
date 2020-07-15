package com.github.atelieramber.impureworld.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.atelieramber.impureworld.ImpureWorld;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.Property;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;

@EventBusSubscriber(modid = ImpureWorld.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ImpureWorldConfig {

	public static final CommonConfig COMMON;
	public static final ForgeConfigSpec SPEC;
	
	public static HashMap<ResourceLocation, PolluterEntry> polluters = new HashMap<ResourceLocation, PolluterEntry>();
	public static List<? extends Object> li;
	
	protected static final Logger LOGGER = LogManager.getLogger(ImpureWorld.MODID);
	
	static {
		final Pair<CommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
		SPEC = specPair.getRight();
		COMMON = specPair.getLeft();
		
		//polluters.put(new ResourceLocation("minecraft:cauldron"), new PolluterEntry("minecraft:cauldron", "", .33f, .33f, .33f, 1.0f, null, 
		//		Arrays.asList(new PolluterEntry.PolluterProperty("level", "1", PolluterEntry.PropertyHandling.ACCEPT)), PolluterEntry.PolluterType.BLOCK));
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
		public List<PolluterProperty> properties;
		public PolluterType type;
		
		private IForgeRegistryEntry<?> registry = null;
		private List<BlockState> states = null;
		
		public static class PolluterExtras{
			public PolluterExtras(String registryName, String target, float carbon, float sulfur, float particulate) {
				this.registryName = registryName;
				this.target = target;
				this.carbon = carbon;
				this.sulfur = sulfur;
				this.particulate = particulate;
			}
			public String registryName;
			public String target; // fuel, consumedItem, result
			public float carbon;
			public float sulfur;
			public float particulate;
		}
		
		public static class PolluterProperty{
			public PolluterProperty(String name, String value, PropertyHandling handling) {
				this.name = name;
				this.value = value;
				this.handling = handling;
			}

			public String name;
			public String value;
			public PropertyHandling handling;
			
			protected Property<?> raw;
		}

		public enum PolluterType{
			BLOCK,
			ENTITY,
			ITEM
		}

		public enum PropertyHandling{
			ACCEPT,
			DENY
		}
		
		public PolluterEntry(String registryName, float carbon, float sulfur, float particulate,
				float polluteFrequency, PolluterType type) {
			this(registryName, "", carbon, sulfur, particulate, polluteFrequency, null, null, type);
			
		}
		
		public PolluterEntry(String registryName, String metaData, float carbon, float sulfur, float particulate,
				float polluteFrequency, List<PolluterExtras> polluterExtras, List<PolluterProperty> properties, PolluterType type) {
			super();
			this.registryName = registryName;
			this.metaData = metaData;
			this.carbon = carbon;
			this.sulfur = sulfur;
			this.particulate = particulate;
			this.polluteFrequency = polluteFrequency;
			this.polluterExtras = polluterExtras;
			this.properties = properties; // Infers ACCEPT, and IGNOREs all not specified
			this.type = type;
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

		public List<BlockState> getBlockStates() {
			if(type != PolluterType.BLOCK) {
				return null;
			}
			if(states == null) {
				states = new ArrayList<BlockState>();
				Block block = (Block)getRegistry();
				validateProperties(block);
				List<BlockState> possibleStates = block.getStateContainer().getValidStates();
				for(BlockState state : possibleStates) {
					for(PolluterProperty handler : properties) {
						boolean valueMatch = state.get(handler.raw).equals(handler.raw.parseValue(handler.value).get());
						switch(handler.handling) {
						case ACCEPT:
							if(valueMatch) states.add(state);
							break;
						case DENY:
							if(!valueMatch) states.add(state);
							break;
						default:
							break;
						}
					}
				}
			}
			return states;
		}
		
		private void validateProperties(Block block) {
			for(PolluterProperty handler : properties) {
				Property<?> p = block.getStateContainer().getProperty(handler.name);
				if(p != null) {
					handler.raw = p;
				}else {
					LOGGER.warn("Invalid property, " + handler.name + " for " + registryName + "! Will be skipped but should be fixed or removed!");
				}
			}
		}

		@Override
		public int hashCode() {
			return registryName.hashCode();
		}
		
		public List<? extends Object> toConfigList(){
			return Arrays.asList(registryName, direction, carbon, sulfur, particulate, (polluterExtras == null) ? new ArrayList<Object>() : polluterExtras, (properties == null) ? new ArrayList<Object>() : properties, type.toString().toLowerCase());
		}
		public static List<? extends Object> defaultConfigList(){
			return Arrays.asList("minecraft:air", "up", 0.0f, 0.0f, 0.0f, 1.0f, Arrays.asList("minecraft:air", "result", 0.0f, 0.0f, 0.0f), Arrays.asList("level", "1", "accept"), "block");
		}
	}

	
	public static class CommonConfig {
		
		ConfigValue<List<? extends Object>> li;
		
		public CommonConfig(ForgeConfigSpec.Builder builder) {
			li = builder.defineList("polluters", PolluterEntry.defaultConfigList(), V -> true);
		}
		
		
	}
	
	public static class SpecificConfig{
		
	}
	
	public static void bakeConfig() {
		li = COMMON.li.get();
	}
	
	@SubscribeEvent
	public static void onModConfigEvent(final ModConfig.ModConfigEvent configEvent) {
		if (configEvent.getConfig().getSpec() == SPEC) {
			bakeConfig();
		}
	}
}
