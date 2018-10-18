package com.teamwizardry.wizardry.lib.vm.command.type;

import com.teamwizardry.wizardry.lib.vm.ActionProcessor;
import com.teamwizardry.wizardry.lib.vm.command.CommandException;
import com.teamwizardry.wizardry.lib.vm.command.CommandInstance;
import com.teamwizardry.wizardry.lib.vm.command.CommandState;
import com.teamwizardry.wizardry.lib.vm.command.ICommand;
import com.teamwizardry.wizardry.lib.vm.command.operable.ICommandOperable;
import com.teamwizardry.wizardry.lib.vm.command.operable.IMagicCommandOperable;
import com.teamwizardry.wizardry.lib.vm.testutils.DebugUtils;

public class PopValueCommand implements ICommand {
	
	private final String popToVariable;
	
	public PopValueCommand(String popToVariable) {
		this.popToVariable = popToVariable;
	}

	@Override
	public void performOperation(ActionProcessor actionProcessor, ICommandOperable cmdOperable)
			throws CommandException {
		if( !(cmdOperable instanceof IMagicCommandOperable) )
			throw new IllegalArgumentException("Incompatible type. Should be IMagicCommandOperable");
		IMagicCommandOperable stateData = (IMagicCommandOperable)cmdOperable;

		Object value = stateData.popData();
		if( popToVariable != null ) {
			if( value == null ) {
				DebugUtils.printDebug("MAGICSCRIPT_BUILDER", "Failed to pop a value from stack to variable '" + popToVariable + "'. Stack is empty.");
				throw new CommandException("Stack is empty.");
			}
			else {
				DebugUtils.printDebug("MAGICSCRIPT_BUILDER", "Pop value " + value.toString() + " to variable " + popToVariable);
				stateData.setData(popToVariable, value);			
			}
		}
		else {
			if( value == null ) {
				DebugUtils.printDebug("MAGICSCRIPT_BUILDER", "Failed to pop a value from stack to variable.");
				throw new CommandException("Stack is empty.");
			}
		}
	}

	@Override
	public CommandState getDefaultState(CommandInstance target) {
		return new CommandState(target, 1);
	}
}
