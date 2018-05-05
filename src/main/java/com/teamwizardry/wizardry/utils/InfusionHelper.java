package com.teamwizardry.wizardry.utils;

import com.teamwizardry.wizardry.api.item.EnumPearlType;
import com.teamwizardry.wizardry.api.item.IInfusable;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class InfusionHelper {
	
	public static IInfusable getInfusable(final Item item) {
		if( item instanceof IInfusable )
			return (IInfusable)item;
		
		if( item == Items.BOOK || item == Items.ENCHANTED_BOOK ) {
			return new IInfusable() {
				@Override
				public EnumPearlType getType(ItemStack stack) {
					return ( item == Items.ENCHANTED_BOOK ) ? EnumPearlType.INFUSED : EnumPearlType.MUNDANE;
				}
			};
		}
		
		return null;
	}
	

}
