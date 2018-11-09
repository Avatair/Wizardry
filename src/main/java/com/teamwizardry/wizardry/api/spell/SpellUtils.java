package com.teamwizardry.wizardry.api.spell;

import com.teamwizardry.librarianlib.features.helpers.ItemNBTHelper;
import com.teamwizardry.wizardry.api.Constants;
import com.teamwizardry.wizardry.api.item.BaublesSupport;
import com.teamwizardry.wizardry.api.spell.module.IModuleShape;
import com.teamwizardry.wizardry.api.spell.module.ModuleInstance;
import com.teamwizardry.wizardry.api.spell.module.ModuleInstanceModifier;
import com.teamwizardry.wizardry.api.util.ColorUtils;
import com.teamwizardry.wizardry.init.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class SpellUtils {

	// TODO: config this
	public final static Item CODE_LINE_BREAK = ModItems.DEVIL_DUST;
	
	public static Color getAverageSpellColor(List<SpellRing> spellChains) {
		List<Color> colorSet = new ArrayList<>();

		NBTTagList list = new NBTTagList();
		for (SpellRing spellRing : spellChains) {
			colorSet.add(spellRing.getPrimaryColor());
			//colorSet.add(spellRing.getSecondaryColor());
			list.appendTag(spellRing.serializeNBT());
		}

		if (colorSet.size() == 1) return colorSet.get(0);

		Color lastColor = ColorUtils.mixColors(colorSet);

		boolean r = lastColor.getRed() == 0;
		boolean g = lastColor.getGreen() == 0;
		boolean b = lastColor.getBlue() == 0;

		if (g ? !(b && r) : b) return lastColor;
		if (lastColor.getRed() / lastColor.getBlue() < 0.8 || lastColor.getRed() / lastColor.getGreen() < 0.8) {
			// todo: rebalance the color so its not white-ish
		}

		return lastColor;
	}

	public static void runSpell(@Nonnull ItemStack spellHolder, @Nonnull SpellData data) {
		runSpell(spellHolder, null, data);
	}
	
	public static void runSpell(@Nonnull ItemStack spellHolder, String prefix, @Nonnull SpellData data) {
		if( prefix == null )
			prefix = "";
		
		NBTTagList list = ItemNBTHelper.getList(spellHolder, prefix + Constants.NBT.SPELL, net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND);
		if( list == null )
			return;
		
		runSpell(list, data);
	}
	
	public static void runSpell(@Nonnull NBTTagList spellList, @Nonnull SpellData data) {
		runSpell(getSpellChains(spellList), data);
	}
	
	public static void runSpell(@Nonnull List<SpellRing> spellList, @Nonnull SpellData data) {
		if (data.world.isRemote) return;
		if( spellList == null || spellList.isEmpty() ) return;

		Entity caster = data.getData(SpellData.DefaultKeys.CASTER);
		if (caster instanceof EntityLivingBase && BaublesSupport.getItem((EntityLivingBase) caster, ModItems.CREATIVE_HALO, ModItems.FAKE_HALO, ModItems.REAL_HALO).isEmpty())
			return;

		for (SpellRing spellRing : spellList) {
			spellRing.runSpellRing(data);
		}
	}
	
	public static List<SpellRing> filterByShape(List<SpellRing> list, Class<? extends IModuleShape> shapeClass) {
		ArrayList<SpellRing> newList = new ArrayList<SpellRing>(list.size());
		if( list == null || list.isEmpty() )
			return newList;
		
		for( SpellRing spell : list ) {
			if( shapeClass.isInstance(spell.getModule().getModuleClass()) )
				newList.add(spell);
		}
		
		return newList;
	}

	/**
	 * Gets all SpellRings that exist in an ItemStack with the children of each ring inside
	 * of them, compressed essentially.
	 * This basically returns the head of each spellData chain only.
	 *
	 * @param spellHolder The ItemStack containing the spells.
	 * @return List with all spellData ring heads in the stack.
	 */
	public static List<SpellRing> getSpellChains(@Nonnull ItemStack spellHolder) {
		List<SpellRing> rings = new ArrayList<>();

		NBTTagList list = ItemNBTHelper.getList(spellHolder, Constants.NBT.SPELL, net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND);
		if (list == null) return rings;

		return getSpellChains(list);
	}

	/**
	 * Gets all spellData ring heads (containing children inside of each).
	 *
	 * @param list NBTTagList where each tag contains a whole SpellRing chain.
	 * @return List with the spellData ring heads.
	 */
	public static List<SpellRing> getSpellChains(@Nonnull NBTTagList list) {
		ArrayList<SpellRing> rings = new ArrayList<>();
		if( list == null || list.isEmpty() )
			return rings;
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound compound = list.getCompoundTagAt(i);
			SpellRing ring = SpellRingCache.INSTANCE.getSpellRingByNBT(compound);
			if (ring == null) continue;
			rings.add(ring);
		}
		return rings;
	}
	
	public static List<List<ModuleInstance>> deserializeModuleList(@Nonnull NBTTagList list)
	{
		List<List<ModuleInstance>> modules = new ArrayList<>();
		List<ModuleInstance> moduleList = new ArrayList<>();
		for (int i = 0; i < list.tagCount(); i++)
		{
			NBTBase base = list.get(i);
			if (!(base instanceof NBTTagString)) continue;
			NBTTagString string = (NBTTagString) base;
			if (string.isEmpty())
			{
				if (!moduleList.isEmpty())
					modules.add(moduleList);
				moduleList = new ArrayList<>();
			}
			ModuleInstance module = ModuleInstance.deserialize(string);
			if (module == null) continue;
			moduleList.add(module);
		}
		if (!moduleList.isEmpty())
			modules.add(moduleList);
		return modules;
	}
	
	/**
	 * Gets a list of items required to create the given list of spell module chains.
	 * @param modules The list of spell module chains.
	 * @return A list of required items.
	 */
	public static List<ItemStack> getSpellItems(List<List<ModuleInstance>> modules)
	{
		Deque<ItemStack> items = new ArrayDeque<>();
		for (List<ModuleInstance> moduleList : modules)
		{
			for (ModuleInstance module : moduleList)
			{
				ItemStack stack = module.getItemStack();
				ItemStack last = items.peekLast();
				if (last == null)
					items.add(stack.copy());
				else if (items.peekLast().isItemEqual(stack))
					last.grow(stack.getCount());
				else
					items.add(stack.copy());
			}
			items.add(new ItemStack(CODE_LINE_BREAK));
		}
		items.add(new ItemStack(ModItems.PEARL_NACRE));
		return new ArrayList<>(items);
	}

	/**
	 * Gives a list of all non-modifier modules in a spell.
	 * @param modules The list of spell chains in the spell.
	 * @return The list of spell chains containing only essential modules.
	 */
	public static List<List<ModuleInstance>> getEssentialModules(List<List<ModuleInstance>> modules)
	{
		List<List<ModuleInstance>> essentialModules = new ArrayList<>();
		modules.forEach(moduleList -> {
			List<ModuleInstance> essentialList = new ArrayList<>();
			moduleList.forEach(module -> {
				if (!(module instanceof ModuleInstanceModifier))
					essentialList.add(module);
			});
			essentialModules.add(essentialList);
		});
		return essentialModules;
	}
	
	/**
	 * Gets all SpellRings children from the passed spellRing object with itself included.
	 *
	 * @param spellRing The SpellRing to uncompress.
	 * @return List with all spellData rings that exist in the spellRing including itself.
	 */
	public static List<SpellRing> getAllSpellRings(@Nonnull SpellRing spellRing) {
		List<SpellRing> rings = new ArrayList<>();
		SpellRing tempSpellRing = spellRing;
		while (tempSpellRing != null) {
			rings.add(tempSpellRing);
			tempSpellRing = tempSpellRing.getChildRing();
		}
		return rings;
	}

	/**
	 * Gets all SpellRings that exist in an ItemStack with children of each ring included
	 * in the list returned.
	 *
	 * @param spellHolder The ItemStack containing the spellData.
	 * @return List with all spellData rings that exist in the stack.
	 */
	public static List<SpellRing> getAllSpellRings(@Nonnull ItemStack spellHolder) {
		List<SpellRing> rings = new ArrayList<>();
		List<SpellRing> heads = getSpellChains(spellHolder);
		for (SpellRing spellRing : heads) {
			SpellRing tempSpellRing = spellRing;
			while (tempSpellRing != null) {
				rings.add(tempSpellRing);
				tempSpellRing = tempSpellRing.getChildRing();
			}
		}
		return rings;
	}
}
