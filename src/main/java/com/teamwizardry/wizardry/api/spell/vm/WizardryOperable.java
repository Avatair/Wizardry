package com.teamwizardry.wizardry.api.spell.vm;

import com.teamwizardry.wizardry.api.spell.SpellData;
import com.teamwizardry.wizardry.lib.vm.command.CommandException;
import com.teamwizardry.wizardry.lib.vm.command.operable.MagicScriptOperable;
import com.teamwizardry.wizardry.lib.vm.command.operable.OperableException;

import kotlin.Pair;

public class WizardryOperable extends MagicScriptOperable<WizardryOperable> {

	private SpellData spellData;
	
	public WizardryOperable() {
		super(WizardryOperable.class);
	}

	public WizardryOperable(WizardryOperable prev, boolean isForked) {
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
		
		if( isSpellDataKey(key) ) {
			if( spellData == null )
				throw new OperableException("No spellData is associated in actual context.");
			String spellDataKey = getSpellDataKey(key);
			SpellDataConverter.setValue(spellData, spellDataKey, obj);
		}
		super.setData(key, obj);
	}
	
	@Override
	public Object getValue(String key) {
		// TODO: Handle errors!
		
		if( isSpellDataKey(key) ) {
			if( spellData == null )
				return null;
			String spellDataKey = getSpellDataKey(key);
			return SpellDataConverter.getValue(spellData, spellDataKey);
		}
		
		return super.getValue(key);
	}
	
	@Override
	public boolean hasData(String key) {
		// TODO: Handle errors!
		
		if( isSpellDataKey(key) ) {
			if( spellData == null )
				return false;
			String spellDataKey = getSpellDataKey(key);
			return SpellDataConverter.hasData(spellData, spellDataKey);
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
