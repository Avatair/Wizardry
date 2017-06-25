package com.teamwizardry.wizardry.common.item;

import com.teamwizardry.librarianlib.core.LibrarianLib;
import com.teamwizardry.librarianlib.features.base.item.ItemMod;
import com.teamwizardry.wizardry.api.item.IExplodable;
import com.teamwizardry.wizardry.api.item.IInfusable;
import com.teamwizardry.wizardry.api.item.INacreColorable;
import com.teamwizardry.wizardry.api.spell.SpellUtils;
import com.teamwizardry.wizardry.api.spell.module.Module;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Saad on 6/28/2016.
 */
public class ItemNacrePearl extends ItemMod implements IInfusable, IExplodable, INacreColorable {

	public ItemNacrePearl() {
		super("nacre_pearl");
		setMaxStackSize(1);
	}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		colorableOnUpdate(stack, worldIn);
	}

	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {
		colorableOnEntityItemUpdate(entityItem);

		return super.onEntityItemUpdate(entityItem);
	}

	@Nonnull
	@Override
	public String getItemStackDisplayName(@Nonnull ItemStack stack) {
		StringBuilder finalName = null;
		ArrayList<Module> modules = SpellUtils.getModules(stack);
		Module lastModule = null;
		for (Module module : modules) {
			if (lastModule == null) lastModule = module;
			if (module != null) {
				Module tempModule = module;
				while (tempModule != null) {

					boolean next = false;
					if (lastModule != module) {
						lastModule = module;
						finalName.append(" || ");
						next = true;
					}

					if (finalName == null) finalName = new StringBuilder(tempModule.getReadableName());
					else {
						if (!next) finalName.append(" -> ");
						finalName.append(tempModule.getReadableName());
					}

					tempModule = tempModule.nextModule;
				}
			}
		}

		if (finalName == null)
			return LibrarianLib.PROXY.translate(this.getUnlocalizedNameInefficiently(stack) + ".name").trim();
		else return finalName.toString();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
		ArrayList<Module> modules = SpellUtils.getModules(stack);
		Module lastModule = null;
		for (Module module : modules) {
			if (lastModule == null) lastModule = module;

			if (module != null) {
				if (module != lastModule) tooltip.add("");
				//tooltip.add("Final " + TextFormatting.BLUE + "Mana" + TextFormatting.GRAY + "/" + TextFormatting.RED + "Burnout" + TextFormatting.GRAY + " Cost: " + TextFormatting.BLUE + module.finalManaDrain + TextFormatting.GRAY + "/" + TextFormatting.RED + module.finalBurnoutFill);
				Module tempModule = module;
				int i = 0;
				while (tempModule != null) {
					tooltip.add(new String(new char[i]).replace("\0", "-") + "> " + TextFormatting.GRAY + tempModule.getReadableName() + " - " + TextFormatting.BLUE + (int) Math.round(tempModule.getManaDrain()) + TextFormatting.GRAY + "/" + TextFormatting.RED + (int) Math.round(tempModule.getBurnoutFill()));
					if (GuiScreen.isShiftKeyDown()) {
						for (String key : tempModule.attributes.getKeySet())
							tooltip.add(new String(new char[i]).replace("\0", " ") + " ^ " + TextFormatting.DARK_GRAY + key + " * " + (int) Math.round(tempModule.attributes.getDouble(key)));
					}
					tempModule = tempModule.nextModule;
					i++;
				}
			}
		}

		if (!GuiScreen.isShiftKeyDown()) {
			tooltip.add(TextFormatting.GRAY + "<- " + TextFormatting.DARK_GRAY + "Shift for more info" + TextFormatting.GRAY + " ->");
		}
	}
}
