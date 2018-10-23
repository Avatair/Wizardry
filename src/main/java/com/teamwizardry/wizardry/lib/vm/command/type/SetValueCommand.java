package com.teamwizardry.wizardry.lib.vm.command.type;

import com.teamwizardry.wizardry.lib.vm.ActionProcessor;
import com.teamwizardry.wizardry.lib.vm.command.CommandException;
import com.teamwizardry.wizardry.lib.vm.command.CommandInstance;
import com.teamwizardry.wizardry.lib.vm.command.CommandState;
import com.teamwizardry.wizardry.lib.vm.command.ICommand;
import com.teamwizardry.wizardry.lib.vm.command.operable.ICommandOperable;
import com.teamwizardry.wizardry.lib.vm.command.operable.IMagicCommandOperable;
import com.teamwizardry.wizardry.lib.vm.command.operable.OperableException;
import com.teamwizardry.wizardry.lib.vm.command.utils.DebugUtils;

public class SetValueCommand implements ICommand {
	
	private final String key;
	private final Object value;
	private final boolean valueIsVariable;
	
	public SetValueCommand(String key, Object value, boolean valueIsVariable) {
		this.key = key;
		this.value = value;
		this.valueIsVariable = valueIsVariable;
	}

	@Override
	public void performOperation(ActionProcessor actionProcessor, CommandState cmdState, ICommandOperable cmdOperable) throws CommandException {
		if( !(cmdOperable instanceof IMagicCommandOperable) )
			throw new IllegalArgumentException("Incompatible type. Should be IMagicCommandOperable");
		IMagicCommandOperable stateData = (IMagicCommandOperable)cmdOperable;

		try {
			DebugUtils.printDebug("MAGICSCRIPT_BUILDER", "Setting " + key + " to " + value);
			if( valueIsVariable ) {
				Object storedValue = stateData.getValue(value.toString());
				if( storedValue == null )
					throw new CommandException("Variable " + value + " has no value.");
				stateData.setData(key, storedValue);
			}
			else
				stateData.setData(key, value);
		}
		catch(OperableException exc) {
			throw new CommandException("Failed to execute an operation. See cause.", exc);
		}
	}

	@Override
	public CommandState getDefaultState(CommandInstance target) {
		return new CommandState(target, 1);
	}
	
}
