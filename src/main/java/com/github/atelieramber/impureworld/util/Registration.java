package com.github.atelieramber.impureworld.util;

import com.github.atelieramber.impureworld.ImpureWorld;

import net.minecraft.util.ResourceLocation;

public class Registration {
	public static ResourceLocation location(String registry) {
		return new ResourceLocation(ImpureWorld.MODID, registry);
	}
}
