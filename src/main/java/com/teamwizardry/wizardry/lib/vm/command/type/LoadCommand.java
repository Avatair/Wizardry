package com.teamwizardry.wizardry.lib.vm.command.type;

import com.teamwizardry.wizardry.lib.vm.ActionProcessor;
import com.teamwizardry.wizardry.lib.vm.command.CommandException;
import com.teamwizardry.wizardry.lib.vm.command.CommandInstance;
import com.teamwizardry.wizardry.lib.vm.command.CommandState;
import com.teamwizardry.wizardry.lib.vm.command.ICommand;
import com.teamwizardry.wizardry.lib.vm.command.operable.ICommandOperable;
import com.teamwizardry.wizardry.lib.vm.command.operable.IMagicCommandOperable;
import com.teamwizardry.wizardry.lib.vm.command.utils.DebugUtils;

public class LoadCommand implements ICommand {
	
	private final String fromPointer;
	private final boolean isPointerVariable;
	private final String toVariable;
	
	public LoadCommand(String fromPointer, boolean isPointerVariable, String toVariable) {
		this.fromPointer = fromPointer;
		this.isPointerVariable = isPointerVariable;
		this.toVariable = toVariable;
	}

	@Override
	public void performOperation(ActionProcessor actionProcessor, ICommandOperable cmdOperable)
			throws CommandException {
		if( !(cmdOperable instanceof IMagicCommandOperable) )
			throw new IllegalArgumentException("Incompatible type. Should be IMagicCommandOperable");
		IMagicCommandOperable stateData = (IMagicCommandOperable)cmdOperable;
		
		// Indirect addressing via a "load" command which takes a string argument from stack
		// and resolves a variable value from it.
		// SECURITY NOTE: In a productive system, such commands are potential security leaks due to their universality.

		Object data;
		if( fromPointer != null ) {
			if( isPointerVariable ) {
				data = stateData.getValue(fromPointer);
				if( data == null ) {
					DebugUtils.printDebug("MAGICSCRIPT_BUILDER",
							"Failed to execute 'load' command due to missing pointer variable '" + fromPointer + "'.");
					throw new CommandException("Pointer variable '" + fromPointer + "' is missing while reading arguments for 'load'.");
				}
			}
			else {
				data = fromPointer;
			}
		}
		else {
			data = stateData.popData();
			if( data == null ) {
				DebugUtils.printDebug("MAGICSCRIPT_BUILDER",
						"Failed to execute 'load' command due to empty stack.");
				throw new CommandException("Stack is empty while reading arguments for 'load'.");
			}
		}
		
		String variableName = data.toString();
		Object value = stateData.getValue(variableName);
		if( value == null ) {
			DebugUtils.printDebug("MAGICSCRIPT_BUILDER",
					"Failed to execute 'load' command due to unknown variable '" + variableName + "'.");
			throw new CommandException("Unknown variable '" + variableName + "'.");
		}

		if( toVariable != null ) {
			DebugUtils.printDebug("MAGICSCRIPT_BUILDER", "Loading value from '" + variableName + "' to variable '" + toVariable + "'.");
			stateData.setData(toVariable, value);
		}
		else {
			DebugUtils.printDebug("MAGICSCRIPT_BUILDER", "Loading value from '" + variableName + "' to stack.");
			stateData.pushData(value);
		}
	}

	@Override
	public CommandState getDefaultState(CommandInstance target) {
		return new CommandState(target, 1);
	}

}
