package com.teamwizardry.wizardry.lib.vm.command.utils;

import com.teamwizardry.wizardry.lib.vm.command.CommandException;

public class CommandUtils {
	private CommandUtils() {}
	
	public static void expectString(Object data) throws CommandException {
		if( data == null )
			throw new CommandException("Expected string, but got null.");
		if( !(data instanceof String) )
			throw new CommandException("Expected string, but got '" + data.toString() + "'.");
	}
	
	public static void expectInteger(Object data) throws CommandException {
		if( data == null )
			throw new CommandException("Expected integer, but got null.");
		if( !(data instanceof Integer) )
			throw new CommandException("Expected integer, but got '" + data.toString() + "'.");
	}
}
