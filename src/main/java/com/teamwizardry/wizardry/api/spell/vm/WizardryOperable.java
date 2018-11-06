package com.teamwizardry.wizardry.api.spell.vm;

import java.util.Map;

import com.teamwizardry.wizardry.api.spell.SpellData;
import com.teamwizardry.wizardry.api.spell.vm.SpellProgramHandler.BuiltinPointer;
import com.teamwizardry.wizardry.lib.vm.command.operable.MagicScriptOperable;
import com.teamwizardry.wizardry.lib.vm.command.operable.OperableException;

public class WizardryOperable extends MagicScriptOperable<WizardryOperable> {

	private SpellData spellData;
	private final Map<String, BuiltinPointer> builtins;
	
	public WizardryOperable(Map<String, BuiltinPointer> builtins) {
		super(WizardryOperable.class);
		this.builtins = builtins;
	}

	public WizardryOperable(WizardryOperable prev, boolean isForked) {
		super(WizardryOperable.class, prev, isForked);
		this.builtins = prev.builtins;
	}

	@Override
	public void callNative(String cmdName) throws OperableException {
		BuiltinPointer builtin = builtins.get(cmdName);
		if( builtin == null )
			throw new OperableException("Builtin '" + cmdName + "' is not existing.");
		
		// Get arguments 
		int countArgs = builtin.getArgumentCount();
		Object args[] = new Object[countArgs];
		for( int i = 0; i < countArgs; i ++ ) {
			Object data = popData();
			if( data == null )
				throw new OperableException("Not enough arguments in stack to call builtin '" + cmdName + "'. Expected " + countArgs + ", but found only " + i + ".");
			args[i] = data;
		}
		
		// invoke builtin
		try {
			Object object = builtin.invoke(this, args);
			if( object != null ) {
				// TODO: Translate primitive types like "int", "float" etc. into their container class types
				pushData(object);
			}
		}
		catch(Throwable exc) {
			// TODO: Improve exceptions after refactoring invokes.
			if( exc instanceof Error )
				throw (Error)exc;	// Simply rethrow errors
			throw new OperableException("Exception occurred when callin a builtin '" + cmdName + "'. See cause.", exc);
		}
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
