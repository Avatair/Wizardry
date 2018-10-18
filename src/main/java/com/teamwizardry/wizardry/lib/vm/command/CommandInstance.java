package com.teamwizardry.wizardry.lib.vm.command;

public class CommandInstance {
	private final ICommandGenerator delegated;
	private final ICommand command;
	private final int commandIdx;
	
	public CommandInstance( int commandIdx, ICommand command ) {
		this.delegated = null;
		this.command = command;
		this.commandIdx = commandIdx;
	}
	
	public CommandInstance( int commandIdx, ICommandGenerator delegated, ICommand command ) {
		this.delegated = delegated;
		this.command = command;
		this.commandIdx = commandIdx;
	}

	public ICommand getCommand() {
		return command;
	}
	
	public CommandState getDefaultState() {
		return command.getDefaultState(this);
	}

	public int getCommandIdx() {
		return commandIdx;
	}

	public ICommandGenerator getDelegated() {
		return delegated;
	}
}
