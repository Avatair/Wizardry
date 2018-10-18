package com.teamwizardry.wizardry.lib.vm.command.operable;

import com.teamwizardry.wizardry.lib.vm.command.CommandException;

public interface IMagicCommandOperable extends ICommandOperable {
	void callNative(String cmdName) throws CommandException;
	
	void pushData(Object obj);
	Object popData();
	
	void setData(String key, Object obj);
	Object getValue(String key);
	boolean hasData(String key);
}
