package com.teamwizardry.wizardry.common.potion;

import javax.annotation.Nullable;

import org.apache.logging.log4j.Level;

import com.teamwizardry.wizardry.Wizardry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

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
	
	public void affectEntity(@Nullable Entity source, @Nullable Entity indirectSource, EntityLivingBase entityLivingBaseIn, int amplifier, double health) {
		// TODO: Perform Self spell!
		Wizardry.logger.log(Level.INFO, "Bang!");
	}	
}
