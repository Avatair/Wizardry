package com.teamwizardry.wizardry.lib.vm.command;

public class CommandException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 668798688110836328L;

	public CommandException() {
		super();
	}

	public CommandException(String message, Throwable cause) {
		super(message, cause);
	}

	public CommandException(String message) {
		super(message);
	}
	
}
