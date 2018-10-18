package com.teamwizardry.wizardry.lib.vm.command;

import com.teamwizardry.wizardry.lib.vm.Action;
import com.teamwizardry.wizardry.lib.vm.ActionEventType;
import com.teamwizardry.wizardry.lib.vm.command.operable.ICommandOperable;
import com.teamwizardry.wizardry.lib.vm.testutils.DebugUtils;

public class CommandDispatcherAction extends Action {
	
	private final ICommandOperable cmdOperable;
	private final CommandState cmdState;
	private final ICommandGenerator generator;
	private final int generation;
	
	public CommandDispatcherAction(ICommandOperable cmdOperable, CommandInstance turtleCommand, ICommandGenerator generator, int generation) {
		this.cmdOperable = cmdOperable;
		this.cmdState = turtleCommand.getDefaultState();
		this.generation = generation;
		this.generator = generator;
	}
	
	@Override
	public boolean handleEvent(ActionEventType type) {
		if( type == ActionEventType.STARTED ) {
			DebugUtils.printDebug("EVENTS", "\tLaunched dispatcher action with generation " + generation);
		}
		else if( type == ActionEventType.UPDATE_TICK ) {
			DebugUtils.printDebug("EVENTS", "\tAction ticked with TTL " + cmdState.getTimeToLive() );
			if( cmdState.getTimeToLive() > 0 ) {
				try {
					cmdState.getInstance().getCommand().performOperation(getRegisteredAt(), cmdOperable);
					cmdState.setTimeToLive(cmdState.getTimeToLive() - 1);
				}
				catch(CommandException exc) {
					dieByException(exc);
				}
			}
			else
				stopThisAction();
		}
		else if( type == ActionEventType.STOPPED ) {
			CommandInstance[] childrenCommands = generator.getNextInstances(cmdState.getInstance());
			if( childrenCommands != null && childrenCommands.length > 0 ) {
				DebugUtils.printDebug("EVENTS", "\tAction died. Launching new generation.");
				for( CommandInstance cmdInst : childrenCommands ) {
					startAction( new CommandDispatcherAction(cmdOperable.makeCopy(childrenCommands.length > 1), cmdInst, generator, generation + 1) );
				}
			}
			else
				DebugUtils.printDebug("EVENTS", "\tAction died. Last generation died.");
		}
		
		return true;
	}
	
}

