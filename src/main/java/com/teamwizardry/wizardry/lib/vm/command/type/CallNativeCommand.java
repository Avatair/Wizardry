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

public class CallNativeCommand implements ICommand {
	
	private final String cmdName;
	
	public CallNativeCommand(String cmdName) {
		this.cmdName = cmdName;
	}
	
	@Override
	public void performOperation(ActionProcessor actionProcessor, CommandState cmdState, ICommandOperable cmdOperable)
			throws CommandException {
		if( !(cmdOperable instanceof IMagicCommandOperable) )
			throw new IllegalArgumentException("Incompatible type. Should be IMagicCommandOperable");
		IMagicCommandOperable stateData = (IMagicCommandOperable)cmdOperable;

		try {
			DebugUtils.printDebug("MAGICSCRIPT_BUILDER", "Calling native command " + cmdName);
			stateData.callNative(cmdName);
		}
		catch(OperableException exc) {
			throw new CommandException(this, cmdState, cmdOperable, "Failed to execute an operation. See cause.", exc);
		}
	}

	@Override
	public CommandState getDefaultState(CommandInstance target) {
		return new CommandState(target, 1);
	}
}
