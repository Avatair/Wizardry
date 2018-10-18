package com.teamwizardry.wizardry.lib.vm.command.type;

import com.teamwizardry.wizardry.lib.vm.ActionProcessor;
import com.teamwizardry.wizardry.lib.vm.command.CommandException;
import com.teamwizardry.wizardry.lib.vm.command.CommandInstance;
import com.teamwizardry.wizardry.lib.vm.command.CommandState;
import com.teamwizardry.wizardry.lib.vm.command.ICommand;
import com.teamwizardry.wizardry.lib.vm.command.operable.ICommandOperable;
import com.teamwizardry.wizardry.lib.vm.command.operable.IMagicCommandOperable;
import com.teamwizardry.wizardry.lib.vm.command.utils.DebugUtils;

public class PushValueCommand implements ICommand {
	private final Object pushWhat;
	private final boolean isVariable;
	
	public PushValueCommand( Object pushWhat, boolean isVariable ) {
		this.pushWhat = pushWhat;
		this.isVariable = isVariable;
	}

	@Override
	public void performOperation(ActionProcessor actionProcessor, ICommandOperable cmdOperable)
			throws CommandException {
		if( !(cmdOperable instanceof IMagicCommandOperable) )
			throw new IllegalArgumentException("Incompatible type. Should be IMagicCommandOperable");
		IMagicCommandOperable stateData = (IMagicCommandOperable)cmdOperable;
		
		if( isVariable ) {
			Object value = stateData.getValue(pushWhat.toString());
			if( value == null ) {
				DebugUtils.printDebug("MAGICSCRIPT_BUILDER", "Failed to push from variable " + pushWhat);
				throw new CommandException("Variable " + pushWhat + " has no assigned value.");
			}
			DebugUtils.printDebug("MAGICSCRIPT_BUILDER", "Pushing value " + value.toString() + " from variable " + pushWhat);
			stateData.pushData(value);
		}
		else {
			DebugUtils.printDebug("MAGICSCRIPT_BUILDER", "Pushing string value '" + pushWhat + "'");
			stateData.pushData(pushWhat);
		}
	}

	@Override
	public CommandState getDefaultState(CommandInstance target) {
		return new CommandState(target, 1);
	}
}
