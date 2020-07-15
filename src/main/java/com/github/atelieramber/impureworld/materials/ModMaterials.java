package com.github.atelieramber.impureworld.materials;

import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;

public class ModMaterials{
	
	public static final Material POLLUTED_AIR = new Material(MaterialColor.AIR, false, false, false, false, false, true, PushReaction.DESTROY);
	
}