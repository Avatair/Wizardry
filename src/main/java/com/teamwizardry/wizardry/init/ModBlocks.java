package com.teamwizardry.wizardry.init;


import com.teamwizardry.wizardry.common.block.*;
import com.teamwizardry.wizardry.common.block.wisdomwood.*;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

/**
 * Created by Saad on 3/24/2016.
 */
public class ModBlocks {

	public static Material NACRE_MATERIAL = new MaterialNacre(MapColor.WATER);
	public static Material MANA_MATERIAL = new MaterialMana(MapColor.WATER);

	public static BlockCraftingPlate CRAFTING_PLATE;
	public static BlockMagiciansWorktable MAGICIANS_WORKTABLE;
	public static BlockManaBattery MANA_BATTERY;
	public static BlockStaff STAFF_BLOCK;
	public static BlockCloud CLOUD;
	public static BlockManaMagnet MANA_MAGNET;

	public static BlockNacre NACRE;
	public static BlockNacreBrick NACRE_BRICK;

	public static BlockWisdomWoodLog WISDOM_WOOD_LOG;
	public static BlockWisdomWoodPlanks WISDOM_WOOD_PLANKS;
	public static BlockWisdomWoodSlab WISDOM_WOOD_SLAB;
	public static BlockWisdomWoodStairs WISDOM_WOOD_STAIRS;
	public static BlockWisdomWoodTrapdoor WISDOM_WOOD_TRAPDOOR;
	public static BlockWisdomWoodDoor WISDOM_WOOD_DOOR;
	public static BlockWisdomWoodFence WISDOM_WOOD_FENCE;
	public static BlockWisdomWoodFenceGate WISDOM_WOOD_FENCE_GATE;
	public static BlockWisdomWoodPigmentedPlanks WISDOM_WOOD_PIGMENTED_PLANKS;

	public static void init() {
		CRAFTING_PLATE = new BlockCraftingPlate();
		MAGICIANS_WORKTABLE = new BlockMagiciansWorktable();
		MANA_BATTERY = new BlockManaBattery();
		STAFF_BLOCK = new BlockStaff();
		CLOUD = new BlockCloud();
		NACRE = new BlockNacre();
		NACRE_BRICK = new BlockNacreBrick();
		MANA_MAGNET = new BlockManaMagnet();

		WISDOM_WOOD_LOG = new BlockWisdomWoodLog();
		WISDOM_WOOD_PLANKS = new BlockWisdomWoodPlanks();
		WISDOM_WOOD_SLAB = new BlockWisdomWoodSlab();
		WISDOM_WOOD_STAIRS = new BlockWisdomWoodStairs();
		WISDOM_WOOD_TRAPDOOR = new BlockWisdomWoodTrapdoor();
		WISDOM_WOOD_DOOR = new BlockWisdomWoodDoor();
		WISDOM_WOOD_FENCE = new BlockWisdomWoodFence();
		WISDOM_WOOD_FENCE_GATE = new BlockWisdomWoodFenceGate();
		WISDOM_WOOD_PIGMENTED_PLANKS = new BlockWisdomWoodPigmentedPlanks();
	}

	public static void initModel() {
		CRAFTING_PLATE.initModel();
		MANA_BATTERY.initModel();
	}
}
