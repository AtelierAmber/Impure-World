package com.github.atelieramber.impureworld.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.atelieramber.impureworld.ImpureWorld;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.Property;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class PolluterEntry{
	protected static final Logger LOGGER = LogManager.getLogger(ImpureWorld.MODID);
	
	public ResourceLocation registryName;
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
		this(registryName, carbon, sulfur, particulate, polluteFrequency, null, null, type);
		
	}
	
	public PolluterEntry(String registryName, float carbon, float sulfur, float particulate,
			float polluteFrequency, List<PolluterExtras> polluterExtras, List<PolluterProperty> properties, PolluterType type) {
		super();
		this.registryName = new ResourceLocation(registryName);
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
			switch(type) {
			case BLOCK:
				registry = ForgeRegistries.BLOCKS.getValue(registryName);
				break;
			case ENTITY:
				registry = ForgeRegistries.ENTITIES.getValue(registryName);
				break;
			case ITEM:
				registry = ForgeRegistries.ITEMS.getValue(registryName);
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
				if(properties != null && !properties.isEmpty()) {
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
				}else {
					states.add(state);
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
	
	public static PolluterEntry fromData(ImpureWorldPolluter data) {
		PolluterType type = PolluterType.BLOCK;
		if(data.type.toLowerCase() == "block") {
			type = PolluterType.BLOCK;
		}else if(data.type.toLowerCase() == "entity") {
			type = PolluterType.ENTITY;
		}else if(data.type.toLowerCase() == "item") {
			type = PolluterType.ITEM;
		}
		List<PolluterExtras> extras = new ArrayList<PolluterExtras>();
		if(data.extras != null) {
			for(ImpureWorldPolluter.PolluterExtras extra : data.extras) {
				extras.add(new PolluterExtras(extra.name, extra.target, extra.emissions.carbon, extra.emissions.sulfur, extra.emissions.particulate));
			}
		}
		List<PolluterProperty> properties = new ArrayList<PolluterProperty>();
		if(data.properties != null) {
			for(ImpureWorldPolluter.PolluterProperties property : data.properties) {
				PropertyHandling handling = PropertyHandling.ACCEPT;
				if(property.handling.toLowerCase() == "accept") {
					handling = PropertyHandling.ACCEPT;
				}else if(property.handling.toLowerCase() == "deny") {
					handling = PropertyHandling.DENY;
				}
				properties.add(new PolluterProperty(property.name, property.value, handling));
			}
		}
		return new PolluterEntry(data.registryName, data.emissions.carbon, data.emissions.sulfur, data.emissions.particulate, data.frequency, extras, properties, type);
	}
}
