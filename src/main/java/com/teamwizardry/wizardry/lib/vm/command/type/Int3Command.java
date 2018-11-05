package com.teamwizardry.wizardry.lib.vm.command.type;

import com.teamwizardry.wizardry.lib.vm.ActionProcessor;
import com.teamwizardry.wizardry.lib.vm.command.CommandException;
import com.teamwizardry.wizardry.lib.vm.command.CommandInstance;
import com.teamwizardry.wizardry.lib.vm.command.CommandState;
import com.teamwizardry.wizardry.lib.vm.command.ICommand;
import com.teamwizardry.wizardry.lib.vm.command.operable.ICommandOperable;

public class Int3Command implements ICommand {

	public static final Int3Command SINGLETON = new Int3Command();
	
	private Int3Command() {}
	
	@Override
	public void performOperation(ActionProcessor actionProcessor, CommandState cmdState, ICommandOperable cmdOperable)
			throws CommandException {
		throw new CommandException(this, cmdState, cmdOperable, "Int 3 reached!");
	}

	@Override
	public CommandState getDefaultState(CommandInstance target) {
		return new CommandState(target, 1);
	}

}