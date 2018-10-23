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

public class EchoCommand implements ICommand {
	private final boolean isStdErrTarget;
	private final Object outputValue;
	private final boolean isOutputVariable;
	
	public EchoCommand(boolean isStdErrTarget, Object outputValue, boolean isOutputVariable) {
		this.isStdErrTarget = isStdErrTarget;
		this.outputValue = outputValue;
		this.isOutputVariable = isOutputVariable;
	}

	@Override
	public void performOperation(ActionProcessor actionProcessor, CommandState cmdState, ICommandOperable cmdOperable)
			throws CommandException {
		if( !(cmdOperable instanceof IMagicCommandOperable) )
			throw new IllegalArgumentException("Incompatible type. Should be IMagicCommandOperable");
		IMagicCommandOperable stateData = (IMagicCommandOperable)cmdOperable;
		
		// SECURITY NOTE: If program output stream is redirected to a file then a leak is existing.
		//                Echo shouldn't be allowed to print to custom files.

		try {
			Object valueToEcho;
			if( outputValue != null ) {
				if( isOutputVariable ) {
					valueToEcho = stateData.getValue(outputValue.toString());
					if( valueToEcho == null ) {
						DebugUtils.printDebug("MAGICSCRIPT_BUILDER",
								"Failed to execute 'load' command due to missing variable '" + outputValue.toString() + "'.");
						throw new CommandException("Missing variable '" + outputValue.toString() + "'");
					}			                         
				}
				else {
					valueToEcho = outputValue;
				}
			}
			else {
				valueToEcho = stateData.popData();
				if( valueToEcho == null ) {
					DebugUtils.printDebug("MAGICSCRIPT_BUILDER",
							"Failed to execute 'load' command due to empty stack.");
					throw new CommandException("Stack is empty.");
				}
			}
			
			String outputString = valueToEcho.toString();
			DebugUtils.printDebug("MAGICSCRIPT_BUILDER",
					"Printing '" + outputString + "' command due to " + (isStdErrTarget ? "stderr":"stdout") + ".");
			if( isStdErrTarget ) {
				System.err.println(outputString);
			}
			else {
				System.out.println(outputString);
			}
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
