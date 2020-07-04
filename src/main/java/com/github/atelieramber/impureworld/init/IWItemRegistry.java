package com.github.atelieramber.impureworld.init;

import java.util.ArrayList;

import com.github.atelieramber.impureworld.lists.ItemList;
import com.github.atelieramber.impureworld.util.Registration;

import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.event.RegistryEvent;

public class IWItemRegistry {
	private static ArrayList<Item> items = new ArrayList<Item>();

	private static void registerItem(Item item, String registry, ItemGroup group) {
		items.add(item = new Item(new Item.Properties().group(group)).setRegistryName(Registration.location(registry)));
	}

	private static void registerItem(Item item, Item constructedItem, String registry) {
		item = constructedItem.setRegistryName(Registration.location(registry));
		items.add(item);
	}

	public static void register(RegistryEvent.Register<Item> event) {
				
		for (Item item : items) {
			event.getRegistry().register(item);
		}
	}

	public static void initItemColors(final ColorHandlerEvent.Item event) {
		ItemColors itemColors = event.getItemColors();

//	    itemColors.register((stack, tintIndex) -> {
//	      if(tintIndex == 1 && stack.hasTag() && stack.getTag().contains("color")) {
//	        return DyeColor.byId(stack.getTag().getByte("color")).getColorValue(); 
//	      }
//	      return -1;
//	    }, ItemList.tablet);
//	    itemColors.register((stack, tintIndex) -> {
//	      if(tintIndex == 1 && stack.hasTag() && stack.getTag().contains("color")) {
//	        return DyeColor.byId(stack.getTag().getByte("color")).getColorValue(); 
//	      }
//	      return -1;
//	    }, ItemList.tablet_case);
	}

}
