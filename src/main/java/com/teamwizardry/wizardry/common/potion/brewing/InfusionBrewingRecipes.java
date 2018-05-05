package com.teamwizardry.wizardry.common.potion.brewing;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;

import com.teamwizardry.wizardry.Wizardry;
import com.teamwizardry.wizardry.api.Constants;
import com.teamwizardry.wizardry.init.ModItems;
import com.teamwizardry.wizardry.init.ModPotionTypes;
import com.teamwizardry.wizardry.init.ModPotions;
import com.teamwizardry.wizardry.utils.InfusedItemStackNBTData;

import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.common.brewing.IBrewingRecipe;

public class InfusionBrewingRecipes implements IBrewingRecipe {

	@Override
	public boolean isInput(ItemStack input) {
        Item item = input.getItem();
        return item == Items.POTIONITEM || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION;
	}

	@Override
	public boolean isIngredient(ItemStack ingredient) {
		Item item = ingredient.getItem();
		return item == ModItems.PEARL_NACRE;
	}

	@Override
	public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
        if (!input.isEmpty() && !ingredient.isEmpty() && isIngredient(ingredient))
        {
            ItemStack result = doReaction(ingredient, input);
            if (result != input)
            {
                return result;
            }
            return ItemStack.EMPTY;
        }

        return ItemStack.EMPTY;
	}

	private ItemStack doReaction(ItemStack reagent, ItemStack potionIn) {
        if (!potionIn.isEmpty())
        {
            PotionType potiontype = PotionUtils.getPotionFromItem(potionIn);
            Item item = potionIn.getItem();

            if( potiontype == PotionTypes.AWKWARD ) {
            	if( reagent.getItem() == ModItems.PEARL_NACRE ) {
            		InfusedItemStackNBTData data = new InfusedItemStackNBTData("").initByStack(reagent);
            		if( data.isComplete() ) {
            			InfusedItemStackNBTData targetData = new InfusedItemStackNBTData(data, Constants.NBT.VANILLA_PREFIX);
            			
            			ItemStack potionOut = PotionUtils.addPotionToItemStack(
            					new ItemStack(item),
            					ModPotionTypes.BOTTLED_MAGIC_TYPES[ModPotionTypes.SubType.NORMAL.ordinal()]);
            			targetData.assignToStack(potionOut);
            			return potionOut;
            		}
            	}
            }
            else {
            	List<PotionEffect> effects = potiontype.getEffects();
            	List<PotionEffect> newEffects = new ArrayList<PotionEffect>(effects.size());
            	PotionEffect infusedPotionEffect = null;
            	
            	for( PotionEffect effect : effects ) {
            		if( effect.getPotion() == ModPotions.BOTTLED_MAGIC ) {
            			if( infusedPotionEffect != null ) {
            				Wizardry.logger.log(Level.WARN, "Found multiple BOTTLED_MAGIC effects at potion. Ignoring others.");
            				continue;
            			}
            			infusedPotionEffect = effect;
            		}
            		else
            			newEffects.add(effect);
            	}
            	
            	if( infusedPotionEffect != null ) {
            		// TODO: potent, extended, lingering and splash potions!
            	}
            }
        }
        
        return potionIn;
	}

}
