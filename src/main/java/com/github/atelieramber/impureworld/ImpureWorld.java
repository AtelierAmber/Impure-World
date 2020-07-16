package com.github.atelieramber.impureworld;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.atelieramber.impureworld.config.ImpureWorldConfig;
import com.github.atelieramber.impureworld.config.PolluterEntry;
import com.github.atelieramber.impureworld.events.HandleChunkEvents;
import com.github.atelieramber.impureworld.init.IWBlockRegistry;
import com.github.atelieramber.impureworld.init.IWItemRegistry;
import com.github.atelieramber.impureworld.lists.BlockList;
import com.github.atelieramber.impureworld.util.JsonDataManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ImpureWorld.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ImpureWorld {
	public static final String MODID = "impureworld";
	public static final String NAME = "Impure World";
	public static final String VERSION = "1.0";

	protected static final Logger LOGGER = LogManager.getLogger(MODID);

	JsonDataManager dataManager;
	
	private static ImpureWorld instance;
	
	public ImpureWorld() {
		instance = this;
		
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::setup);
		modEventBus.addListener(this::enqueueIMC);
		modEventBus.addListener(this::processIMC);
		modEventBus.addListener(this::clientSetup);
		modEventBus.addListener(this::onServerStarting);
		
		final IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

		forgeEventBus.addListener(HandleChunkEvents::load);
		forgeEventBus.addListener(HandleChunkEvents::unload);
		forgeEventBus.addListener(HandleChunkEvents::worldTick);
		forgeEventBus.addListener(this::onDataPackReload);
		
		forgeEventBus.addListener(this::removeFog);

		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ImpureWorldConfig.SPEC);
		
		IWBlockRegistry.registerBlockTileEntities(modEventBus);

		MinecraftForge.EVENT_BUS.register(this);
	}

	public static ImpureWorld Instance() {
		return instance;
	}
	
	public static final Map<ResourceLocation, PolluterEntry> polluters(){
		return instance.dataManager.getPolluters();
	}
	
	private void setup(final FMLCommonSetupEvent event) {
	}

	private void enqueueIMC(final InterModEnqueueEvent event) {

	}

	private void processIMC(final InterModProcessEvent event) {

	}

	private void clientSetup(final FMLClientSetupEvent event) {
		RenderTypeLookup.setRenderLayer(BlockList.polluted_air, RenderType.getTranslucent());
	}

	public void onServerStarting(FMLServerStartingEvent event) {
		
	}
	
	public void onDataPackReload(final AddReloadListenerEvent event) {
		dataManager = new JsonDataManager();
		event.addListener(dataManager);
		System.out.println("Registered Data loader.");
	}

    public void removeFog(final EntityViewRenderEvent.FogDensity event) {
		Minecraft mc = Minecraft.getInstance();

        final float farPlane = mc.gameSettings.renderDistanceChunks * 16.0f;
        RenderSystem.fogStart(farPlane * 32.0f);
        RenderSystem.fogEnd(farPlane * 32.0f);
        event.setDensity(0.0f);
        event.setCanceled(true);
    }
	
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		IWBlockRegistry.registerBlocks(event);
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		IWItemRegistry.register(event);
		IWBlockRegistry.registerBlockItems(event);
	}
}
