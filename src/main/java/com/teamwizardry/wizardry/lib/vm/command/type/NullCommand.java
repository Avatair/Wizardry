package com.teamwizardry.wizardry.lib.vm.command.type;

import com.teamwizardry.wizardry.lib.vm.ActionProcessor;
import com.teamwizardry.wizardry.lib.vm.command.CommandInstance;
import com.teamwizardry.wizardry.lib.vm.command.CommandState;
import com.teamwizardry.wizardry.lib.vm.command.ICommand;
import com.teamwizardry.wizardry.lib.vm.command.operable.ICommandOperable;

public class NullCommand implements ICommand {
	
	public static final NullCommand SINGLETON = new NullCommand();
	
	private NullCommand() {
	}

	@Override
	public void performOperation(ActionProcessor actionProcessor, CommandState cmdState, ICommandOperable cmdOperable) {
		// Do nothing
	}

	@Override
	public CommandState getDefaultState(CommandInstance target) {
		return new CommandState(target, 0);
	}
}
