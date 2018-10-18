package com.teamwizardry.wizardry.lib.vm.command.type;

import com.teamwizardry.wizardry.lib.vm.ActionProcessor;
import com.teamwizardry.wizardry.lib.vm.command.CommandException;
import com.teamwizardry.wizardry.lib.vm.command.CommandInstance;
import com.teamwizardry.wizardry.lib.vm.command.CommandState;
import com.teamwizardry.wizardry.lib.vm.command.ICommand;
import com.teamwizardry.wizardry.lib.vm.command.operable.ICommandOperable;
import com.teamwizardry.wizardry.lib.vm.command.operable.IMagicCommandOperable;
import com.teamwizardry.wizardry.lib.vm.command.utils.DebugUtils;

public class Int3Command implements ICommand {

	public static final Int3Command SINGLETON = new Int3Command();
	
	private Int3Command() {}
	
	@Override
	public void performOperation(ActionProcessor actionProcessor, ICommandOperable cmdOperable)
			throws CommandException {
		throw new CommandException("Int 3 reached!");
	}

	@Override
	public CommandState getDefaultState(CommandInstance target) {
		return new CommandState(target, 1);
	}

}
