package com.teamwizardry.wizardry.lib.vm.command.type;

import com.teamwizardry.wizardry.lib.vm.ActionProcessor;
import com.teamwizardry.wizardry.lib.vm.command.CommandException;
import com.teamwizardry.wizardry.lib.vm.command.CommandInstance;
import com.teamwizardry.wizardry.lib.vm.command.CommandState;
import com.teamwizardry.wizardry.lib.vm.command.ICommand;
import com.teamwizardry.wizardry.lib.vm.command.operable.ICommandOperable;

public class NopCommand implements ICommand {
	
	// NOTE: The difference between nop and null command is that null commands are usually stripped of from the sequence when optimizing
	//       while a nop command is forced to be executed for an amount of ticks.
	
	public final int countTicks;
	
	public NopCommand(int countTicks) {
		if( countTicks < 1 )
			throw new IllegalArgumentException("Expected a positive number for tick count.");
		this.countTicks = countTicks;
	}

	@Override
	public void performOperation(ActionProcessor actionProcessor, ICommandOperable cmdOperable)
			throws CommandException {
		// Do nothing.		
	}

	@Override
	public CommandState getDefaultState(CommandInstance target) {
		return new CommandState(target, this.countTicks);
	}

}
