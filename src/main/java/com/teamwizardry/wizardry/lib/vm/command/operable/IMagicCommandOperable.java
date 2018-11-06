package com.teamwizardry.wizardry.lib.vm.command.operable;

public interface IMagicCommandOperable extends ICommandOperable {
	void callNative(String cmdName) throws OperableException;
	
	void pushData(Object obj);
	Object popData();
	
	void setData(String key, Object obj);
	Object getValue(String key);
	boolean hasData(String key);
}
