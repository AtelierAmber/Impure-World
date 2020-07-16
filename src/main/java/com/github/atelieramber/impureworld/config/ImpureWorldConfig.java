package com.github.atelieramber.impureworld.config;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.atelieramber.impureworld.ImpureWorld;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;

@EventBusSubscriber(modid = ImpureWorld.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ImpureWorldConfig {

	public static final CommonConfig COMMON;
	public static final ForgeConfigSpec SPEC;
	
	public static List<? extends Object> li;
	
	protected static final Logger LOGGER = LogManager.getLogger(ImpureWorld.MODID);
	
	static {
		final Pair<CommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
		SPEC = specPair.getRight();
		COMMON = specPair.getLeft();
		
		//polluters.put(new ResourceLocation("minecraft:cauldron"), new PolluterEntry("minecraft:cauldron", "", .33f, .33f, .33f, 1.0f, null, 
		//		Arrays.asList(new PolluterEntry.PolluterProperty("level", "1", PolluterEntry.PropertyHandling.ACCEPT)), PolluterEntry.PolluterType.BLOCK));
	}

	
	public static class CommonConfig {
				
		public CommonConfig(ForgeConfigSpec.Builder builder) {
			
		}
		
		
	}
	
	public static class SpecificConfig{
		
	}
	
	public static void bakeConfig() {
	}
	
	@SubscribeEvent
	public static void onModConfigEvent(final ModConfig.ModConfigEvent configEvent) {
		if (configEvent.getConfig().getSpec() == SPEC) {
			bakeConfig();
		}
	}
}
