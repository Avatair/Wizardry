package com.teamwizardry.wizardry.common.potion;

import java.util.List;

import javax.annotation.Nullable;

import com.teamwizardry.wizardry.api.Constants;
import com.teamwizardry.wizardry.api.spell.SpellData;
import com.teamwizardry.wizardry.api.spell.SpellRing;
import com.teamwizardry.wizardry.api.spell.SpellUtils;
import com.teamwizardry.wizardry.common.module.shapes.ModuleShapeSelf;
import com.teamwizardry.wizardry.init.ModPotions;
import com.teamwizardry.wizardry.utils.InfusedItemStackNBTData;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PotionBottledMagic extends PotionBase {

	public PotionBottledMagic() {
		super("infusion", false, 0);
	}

	@Override
    public boolean isInstant()
    {
        return true;
    }
    
    @Override
    public boolean isReady(int duration, int amplifier)
    {
        return duration >= 1;
    }
    
    @Override
    public int getLiquidColor()
    {
    	return 0;
    }
	
    @Override
	public void affectEntity(@Nullable Entity source, @Nullable Entity indirectSource, EntityLivingBase entityLivingBaseIn, int amplifier, double health) {
		// Do nothing here, because item stack is not known
	}
    
    @SubscribeEvent
    public void onItemUseFinish(LivingEntityUseItemEvent.Finish event ) {
    	EntityLivingBase entityLiving = event.getEntityLiving();
    	ItemStack potion = event.getItem();
    	
    	if( potion.getItem() != Items.POTIONITEM )
    		return;
    	
    	List<PotionEffect> effects = PotionUtils.getEffectsFromStack(potion);
    	PotionEffect infusionEffect = null;
    	for( PotionEffect effect : effects ) {
    		if( effect.getPotion() == ModPotions.BOTTLED_MAGIC ) {
    			infusionEffect = effect;
    		}
    	}
    	if( infusionEffect == null )
    		return;
    	
    	InfusedItemStackNBTData data = new InfusedItemStackNBTData(Constants.NBT.VANILLA_PREFIX).initByStack(potion);
    	if( !data.isComplete() )
    		return;
    	List<SpellRing> spellList = SpellUtils.getSpellChains(data.getSpellList());
    	spellList = SpellUtils.filterByShape(spellList, ModuleShapeSelf.class);

		SpellData spell = new SpellData(entityLiving.getEntityWorld());
		spell.processEntity(entityLiving, true);
		SpellUtils.runSpell(spellList, spell);
    }
}
