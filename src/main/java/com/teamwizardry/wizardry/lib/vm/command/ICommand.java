package com.teamwizardry.wizardry.lib.vm.command;

import com.teamwizardry.wizardry.lib.vm.ActionProcessor;
import com.teamwizardry.wizardry.lib.vm.command.operable.ICommandOperable;

public interface ICommand {
	void performOperation(ActionProcessor actionProcessor, ICommandOperable cmdOperable) throws CommandException;
	CommandState getDefaultState(CommandInstance target);	
}
