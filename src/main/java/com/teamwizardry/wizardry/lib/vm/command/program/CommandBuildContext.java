package com.teamwizardry.wizardry.lib.vm.command.program;

import com.teamwizardry.wizardry.lib.vm.command.ICommand;
import com.teamwizardry.wizardry.lib.vm.command.ICommandGenerator;
import com.teamwizardry.wizardry.lib.vm.command.program.CommandBuildResult.CommandGenTargetBuildResult;
import com.teamwizardry.wizardry.lib.vm.command.program.CommandBuildResult.CommandTargetBuildResult;

public class CommandBuildContext<T> {
	private final T context;
	private final int parentFollowerIdx;
	private final int amountFollowers;
	
	CommandBuildContext(T context, int parentFollowerIdx, int amountFollowers) {
		this.context = context;
		this.parentFollowerIdx = parentFollowerIdx;
		this.amountFollowers = amountFollowers;
	}
	
	public CommandBuildResult make(ICommand commandData, T nextContext, int nextAmountFollowers) {
		return new CommandTargetBuildResult<T>(commandData, nextContext, nextAmountFollowers);
	}
	
	public CommandBuildResult make(ICommandGenerator commandGenerator) {
		return new CommandGenTargetBuildResult(commandGenerator);
	}
	
	public CommandBuildResult make(Exception exc) {
		return CommandBuildResult.makeExceptionResult(exc);
	}
	
	public T getContext() {
		return context;
	}
	
	public int getAmountFollowers() {
		return amountFollowers;
	}

	public int getParentFollowerIdx() {
		return parentFollowerIdx;
	}
}
