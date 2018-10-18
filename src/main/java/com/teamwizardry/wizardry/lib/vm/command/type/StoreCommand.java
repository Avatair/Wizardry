package com.teamwizardry.wizardry.lib.vm.command.type;

import com.teamwizardry.wizardry.lib.vm.ActionProcessor;
import com.teamwizardry.wizardry.lib.vm.command.CommandException;
import com.teamwizardry.wizardry.lib.vm.command.CommandInstance;
import com.teamwizardry.wizardry.lib.vm.command.CommandState;
import com.teamwizardry.wizardry.lib.vm.command.ICommand;
import com.teamwizardry.wizardry.lib.vm.command.operable.ICommandOperable;
import com.teamwizardry.wizardry.lib.vm.command.operable.IMagicCommandOperable;
import com.teamwizardry.wizardry.lib.vm.command.utils.DebugUtils;

public class StoreCommand implements ICommand {
	
	private final String toPointer;
	private final boolean isPointerVariable;
	private final Object value;
	private final boolean isValueVariable;
	
	public StoreCommand(String toPointer, boolean isPointerVariable, Object value, boolean isValueVariable) {
		this.toPointer = toPointer;
		this.isPointerVariable = isPointerVariable;
		this.value = value;
		this.isValueVariable = isValueVariable;
	}

	@Override
	public void performOperation(ActionProcessor actionProcessor, ICommandOperable cmdOperable)
			throws CommandException {
		if( !(cmdOperable instanceof IMagicCommandOperable) )
			throw new IllegalArgumentException("Incompatible type. Should be IMagicCommandOperable");
		IMagicCommandOperable stateData = (IMagicCommandOperable)cmdOperable;
		
		// Indirect addressing via a "store" command which takes an object and a string argument from stack
		// and resolves a variable from string to store the passed object in it.
		// SECURITY NOTE: In a productive system, such commands are potential security leaks due to their universality.
		Object data1;
		if( value != null ) {
			if( isValueVariable ) {
				data1 = stateData.getValue(value.toString());
				if( data1 == null ) {
					DebugUtils.printDebug("MAGICSCRIPT_BUILDER", "Failed to store value. Couldn't resolve variable '" + value.toString() + "'");
					throw new CommandException("Couldn't retrieve value from variable '" + value.toString() + "'.");
				}
			}
			else {
				data1 = value;
			}
		}
		else {
			data1 = stateData.popData();
			if( data1 == null ) {
				DebugUtils.printDebug("MAGICSCRIPT_BUILDER", "Failed to store value. Couldn't retrieve value from stack.");
				throw new CommandException("Couldn't retrieve value from stack. Stack is empty.");
			}
		}
		
		Object data2;
		if( toPointer != null ) { 		// TODO: Argument fetching pattern is used multiple times. Make a method. 
			if( isPointerVariable ) {
				data2 = stateData.getValue(toPointer);
				if( data2 == null ) {
					DebugUtils.printDebug("MAGICSCRIPT_BUILDER", "Failed to store value. Couldn't resolve variable '" + toPointer + "'");
					throw new CommandException("Couldn't retrieve pointer value from variable '" + toPointer + "'.");
				}
			}
			else {
				data2 = toPointer;
			}
		}
		else {
			data2 = stateData.popData();
			if( data2 == null ) {
				DebugUtils.printDebug("MAGICSCRIPT_BUILDER", "Failed to store value. Couldn't retrieve pointer from stack.");
				throw new CommandException("Couldn't retrieve pointer from stack. Stack is empty.");
			}
		}

//		if( data1 == null || data2 == null ) {
//			DebugUtils.printDebug("MAGICSCRIPT_BUILDER", "Failed to store values. Couldn't process all arguments.");
//			throw new CommandException("Couldn't process all arguments.");
//		}
		
		String variableName = data2.toString();
		DebugUtils.printDebug("MAGICSCRIPT_BUILDER", "Store value '" + data1 + "' from stack to variable '" + variableName + "'.");
		stateData.setData(variableName, data1);
	}

	@Override
	public CommandState getDefaultState(CommandInstance target) {
		return new CommandState(target, 1);
	}
	
}
