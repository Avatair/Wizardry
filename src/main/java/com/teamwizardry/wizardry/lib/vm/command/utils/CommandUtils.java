package com.teamwizardry.wizardry.lib.vm.command.utils;

import com.teamwizardry.wizardry.lib.vm.command.CommandException;
import com.teamwizardry.wizardry.lib.vm.command.CommandState;
import com.teamwizardry.wizardry.lib.vm.command.ICommand;
import com.teamwizardry.wizardry.lib.vm.command.operable.ICommandOperable;

public class CommandUtils {
	private CommandUtils() {}
	
	public static void expectString(ICommand command, CommandState state, ICommandOperable operable, Object data) throws CommandException {
		if( data == null )
			throw new CommandException(command, state, operable, "Expected string, but got null.");
		if( !(data instanceof String) )
			throw new CommandException(command, state, operable, "Expected string, but got '" + data.toString() + "'.");
	}
	
	public static void expectInteger(ICommand command, CommandState state, ICommandOperable operable, Object data) throws CommandException {
		if( data == null )
			throw new CommandException(command, state, operable, "Expected integer, but got null.");
		if( !(data instanceof Integer) )
			throw new CommandException(command, state, operable, "Expected integer, but got '" + data.toString() + "'.");
	}
}
