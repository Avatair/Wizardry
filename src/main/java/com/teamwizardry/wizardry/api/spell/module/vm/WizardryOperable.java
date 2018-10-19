package com.teamwizardry.wizardry.api.spell.module.vm;

import com.teamwizardry.wizardry.api.spell.SpellData;
import com.teamwizardry.wizardry.lib.vm.command.CommandException;
import com.teamwizardry.wizardry.lib.vm.command.operable.MagicScriptOperable;

import kotlin.Pair;

public class WizardryOperable extends MagicScriptOperable<WizardryOperable> {

	private SpellData spellData;
	
	public WizardryOperable() {
		super(WizardryOperable.class);
	}

	public WizardryOperable(Class<WizardryOperable> clazz, WizardryOperable prev, boolean isForked) {
		super(WizardryOperable.class, prev, isForked);
	}

	@Override
	public void callNative(String cmdName) throws CommandException {
		// TODO Auto-generated method stub
		
	}
	
	public void setSpellData(SpellData spellData) {
		this.spellData = spellData;
	}
	
	public SpellData getSpellData() {
		return this.spellData;
	}
	
	@Override
	public void setData(String key, Object obj) {
		// TODO: Handle errors!
		
		if( spellData != null && isSpellDataKey(key) ) {
			String spellDataKey = getSpellDataKey(key);
/*			Pair<String, Class<Object>> serializableKey = KeySupport.getKeyFor(key);
			if( serializableKey == null )
				return;	// TODO: Throw exception on null
			if( !serializableKey.component2().isInstance(obj) )
				return;	// TODO: Throw exception
			spellData.<Object>addData(serializableKey, obj); */
		}
		super.setData(key, obj);
	}
	
	@Override
	public Object getValue(String key) {
		// TODO: Handle errors!
		
		if( spellData != null && isSpellDataKey(key) ) {
			String spellDataKey = getSpellDataKey(key);
			// ...
		}
		
		return super.getValue(key);
	}
	
	@Override
	public boolean hasData(String key) {
		// TODO: Handle errors!
		
		if( spellData != null && isSpellDataKey(key) ) {
			String spellDataKey = getSpellDataKey(key);
			// ...
		}
		
		return super.hasData(key);
	}
	
	public boolean isSpellDataKey(String key) {
		return key.startsWith("spell.");
	}
	
	public String getSpellDataKey(String key) {
		return key.substring(6);
	}
}
