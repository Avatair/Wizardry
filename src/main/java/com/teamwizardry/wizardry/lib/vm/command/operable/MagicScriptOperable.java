package com.teamwizardry.wizardry.lib.vm.command.operable;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public abstract class MagicScriptOperable<T extends MagicScriptOperable<T>> implements IMagicCommandOperable {
	final Map<String, Object> values;
	final LinkedList<Object> stack;
	final Class<T> clazz;
	
	public MagicScriptOperable(Class<T> clazz) {
		this.clazz = clazz;
		this.values = new HashMap<>();
		this.stack = new LinkedList<>();
	}
	
	public MagicScriptOperable(Class<T> clazz, T prev, boolean isForked) {
		this.clazz = clazz;
		this.values = prev.values;	// Mutable!
		
		if( isForked ) {
			// NOTE: Is an optimization. Don't copy stack for each new command, if there are no forks.
			this.stack = new LinkedList<>();
			this.stack.addAll( prev.stack );
		}
		else {
			this.stack = prev.stack;
		}
	}
	
	@Override
	public void setData(String key, Object obj) {
		values.put(key, obj);
	}
	
	@Override
	public Object getValue(String key) {
		return values.get(key);
	}
	
	@Override
	public boolean hasData(String key) {
		return values.containsKey(key);
	}
	
	public final String getValue_String(String key) {
		Object obj = getValue(key);
		if( obj == null )
			return "*null*";
		return obj.toString();
	}
	
	@Override
	public void pushData(Object obj) {
		if( obj == null )
			throw new IllegalArgumentException("Object may not be null.");
		stack.addLast(obj);
	}
	
	@Override
	public Object popData() {
		if( stack.isEmpty() )
			return null;
		Object obj = stack.removeLast();
		if( obj == null )
			throw new IllegalStateException("A null object was existing in stack.");
		return obj;
	}
	
	@Override
	public ICommandOperable makeCopy(boolean isForked) {
		try {
			Constructor<T> constructMethod = clazz.getDeclaredConstructor(clazz, boolean.class);
			return constructMethod.newInstance(this, isForked);
		}
		catch(Exception e) {
			throw new IllegalStateException("Exception caught when constructing new operator target.", e);
		}
	}
}
