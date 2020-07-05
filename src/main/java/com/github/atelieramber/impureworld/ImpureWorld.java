package com.github.atelieramber.impureworld;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.atelieramber.impureworld.init.IWBlockRegistry;
import com.github.atelieramber.impureworld.init.IWItemRegistry;
import com.github.atelieramber.impureworld.lists.BlockList;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
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

	public ImpureWorld() {
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::setup);
		modEventBus.addListener(this::enqueueIMC);
		modEventBus.addListener(this::processIMC);
		modEventBus.addListener(this::clientSetup);

		IWBlockRegistry.registerBlockTileEntities(modEventBus);

		MinecraftForge.EVENT_BUS.register(this);
	}

	private void setup(final FMLCommonSetupEvent event) {

	}

	private void enqueueIMC(final InterModEnqueueEvent event) {

	}

	private void processIMC(final InterModProcessEvent event) {

	}

	public static class CustomRenderType extends RenderType {

		public static final RenderType BOUNDLESS_TRANSLUCENT = makeType("boundless_translucent", DefaultVertexFormats.BLOCK, 7, 262144,
				true, true, getBoundlessTranslucentState());

		private static RenderType.State getBoundlessTranslucentState() {
			return RenderType.State.getBuilder().shadeModel(SHADE_ENABLED).lightmap(LIGHTMAP_ENABLED)
					.texture(BLOCK_SHEET_MIPPED).transparency(TRANSLUCENT_TRANSPARENCY).alpha(DEFAULT_ALPHA).build(true);
		}

		public CustomRenderType(String nameIn, VertexFormat formatIn, int drawModeIn, int bufferSizeIn, boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
			super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
		}

	}

	private void clientSetup(final FMLClientSetupEvent event) {
		RenderTypeLookup.setRenderLayer(BlockList.polluted_air, RenderType.getTranslucent());
	}

	@SubscribeEvent
	public void onServerStarting(FMLServerStartingEvent event) {

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
