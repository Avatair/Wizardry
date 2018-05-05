package com.teamwizardry.wizardry.init;

import com.teamwizardry.wizardry.Wizardry;

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class ModPotionTypes {
	public enum SubType {
		NORMAL //,
//		EXTENDED,
//		POTENT		
	}
	
	public static PotionType[] BOTTLED_MAGIC_TYPES;
	
	public static void init() {
		BOTTLED_MAGIC_TYPES = initType(ModPotions.BOTTLED_MAGIC);
	}
	
	@SubscribeEvent
	public void onRegisterPotionTypes(RegistryEvent.Register<PotionType> event) {
		IForgeRegistry<PotionType> registry = event.getRegistry();
		
		registerType(registry, BOTTLED_MAGIC_TYPES);
	}
	
	private static PotionType[] initType(Potion potion) {
		PotionType[] types = new PotionType[SubType.values().length];
		
		types[SubType.NORMAL.ordinal()] = new PotionType(new PotionEffect(potion));
	
		return types;
	}
	
	private static PotionType[] registerType(IForgeRegistry<PotionType> registry, PotionType[] types) {
		for( SubType subType : SubType.values() ) {
			PotionType type = types[subType.ordinal()];
			Potion potion = type.getEffects().get(0).getPotion();	// NOTE: Assuming this potion has exactly one effect.
			
			type.setRegistryName(new ResourceLocation(Wizardry.MODID,
					potion.getName().toLowerCase() + "_" + subType.toString().toLowerCase()));
			registry.register(type);
		}
		
		return types;
	}
}
