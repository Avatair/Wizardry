package com.teamwizardry.wizardry.api.spell.module.vm;

import javax.annotation.Nonnull;

import com.teamwizardry.wizardry.api.spell.SpellRing;
import com.teamwizardry.wizardry.api.spell.module.IModule;
import com.teamwizardry.wizardry.api.spell.module.IModuleShape;

public abstract class AbstractModuleShapeVM extends AbstractModuleVM implements IModuleShape {
	
	public final ScriptKey scanConfiguration(@Nonnull SpellRing spellRing) {
		ScriptKey key = new ScriptKey();
		SpellRing cur = spellRing;
		while( cur != null ) {
			IModule moduleClass = cur.getModule().getModuleClass(); 
			if( !(moduleClass instanceof AbstractModuleVM) )
				break;
			AbstractModuleVM subModuleVM = (AbstractModuleVM)moduleClass;
			
			// Do append
			subModuleVM.appendConfiguration(key, cur);
			
			cur = cur.getChildRing();
		}
		
		return key;
	}
	
	public final SpellProgram getProgram(@Nonnull SpellRing spellRing) {
		ScriptKey key = scanConfiguration(spellRing);
		return ProgramCache.INSTANCE.getProgram(key);
	}
}
