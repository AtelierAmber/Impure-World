package com.github.atelieramber.impureworld.init;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.atelieramber.impureworld.ImpureWorld;
import com.github.atelieramber.impureworld.util.Registration;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.event.RegistryEvent;

public class IWBlockRegistry {
	private static ArrayList<Block> blocks = new ArrayList<Block>();
	private static ArrayList<BlockItem> blockItems = new ArrayList<BlockItem>();
	public static final Logger LOGGER = LogManager.getLogger(ImpureWorld.MODID);

	private static void registerBlock(Block block, BlockItem item, String registry, Material material, float hardness,
			float resistance, SoundType sound, ItemGroup group) {
		blocks.add(block = new Block(
				Block.Properties.create(material).hardnessAndResistance(hardness, resistance).sound(sound))
						.setRegistryName(Registration.location(registry)));

		blockItems.add(item = (BlockItem) (new BlockItem(block, new BlockItem.Properties().group(group))
				.setRegistryName(block.getRegistryName())));
	}

	private static void registerBlock(Block block, BlockItem item, Block constructedBlock, String registry,
			ItemGroup group) {
		block = constructedBlock.setRegistryName(Registration.location(registry));
		blocks.add(block);

		blockItems.add(item = (BlockItem) (new BlockItem(block, new BlockItem.Properties().group(group))
				.setRegistryName(Registration.location(new StringBuilder("blockitem/").append(registry).toString()))));
	}

	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		for (final Block block : blocks) {
			event.getRegistry().register(block);
		}

		LOGGER.info("Blocks registered");
	}

	public static void registerBlockItems(RegistryEvent.Register<Item> event) {
		for (final BlockItem item : blockItems) {
			event.getRegistry().register(item);
		}
	}
}
