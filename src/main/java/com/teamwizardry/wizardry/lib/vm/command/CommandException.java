package com.teamwizardry.wizardry.lib.vm.command;

import com.teamwizardry.wizardry.lib.vm.command.operable.ICommandOperable;

public class CommandException extends Exception {

	private final ICommand command;
	private final CommandState state;
	private final ICommandOperable operable;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 668798688110836328L;

	public CommandException(ICommand command, CommandState state, ICommandOperable operable) {
		super();

		this.command = command;
		this.state = state;
		this.operable = operable;
	}

	public CommandException(ICommand command, CommandState state, ICommandOperable operable, String message, Throwable cause) {
		super(message, cause);

		this.command = command;
		this.state = state;
		this.operable = operable;
	}

	public CommandException(ICommand command, CommandState state, ICommandOperable operable, String message) {
		super(message);

		this.command = command;
		this.state = state;
		this.operable = operable;
	}
	
	// TODO: Format message
}
