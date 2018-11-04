package com.teamwizardry.wizardry.api.spell.vm;

import com.teamwizardry.wizardry.api.spell.DataSerializationException;
import com.teamwizardry.wizardry.api.spell.SpellData;
import com.teamwizardry.wizardry.api.spell.SpellData.DataField;
import com.teamwizardry.wizardry.lib.vm.command.operable.OperableException;

public class SpellDataConverter {
	private SpellDataConverter() {}
	
	public static void setValue(SpellData data, String key, Object value) throws OperableException {
		try {
			DataField<?> field = SpellData.getFieldByName(key);
			if( field == null )
				throw new OperableException("SpellData field '" + key + "' is not existing.");
			if( value != null )
				data.addDataTypeless(field, value);
			else
				data.removeData(field);
		}
		catch( DataSerializationException exc ) {
			throw new OperableException("Failed to set field. See cause.", exc);
		}
	}
	
	public static Object getValue(SpellData data, String key) throws OperableException {
		try {
			DataField<?> field = SpellData.getFieldByName(key);
			if( field == null )
				return null;
			return data.getData(field);
		}
		catch( DataSerializationException exc ) {
			throw new OperableException("Failed to get field. See cause.", exc);
		}
	}
	
	public static boolean hasData(SpellData data, String key) throws OperableException {
		try {
			DataField<?> field = SpellData.getFieldByName(key);
			if( field == null )
				return false;
			return data.hasData(field);
		}
		catch( DataSerializationException exc ) {
			throw new OperableException("Failed to query if a spellData field '" + key + "' exists. See cause.", exc);
		}
	}
	
/*	public static Pair<String, Class<Object>> getKeyFor(String name) {
		if( name.equals("seed") ) {
			return DefaultKeys.SEED;
		}
	} */
}
