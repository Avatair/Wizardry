package com.teamwizardry.wizardry.lib.vm.command.program;

import com.teamwizardry.wizardry.lib.vm.command.CommandInstance;
import com.teamwizardry.wizardry.lib.vm.command.ICommandGenerator;

public class CommandStream implements ICommandGenerator {
	private final CommandStreamEntry[] streamBuffer;

	CommandStream(CommandStreamEntry[] programBuffer) {
		this.streamBuffer = programBuffer;
	}
	
	@Override
	public CommandInstance[] getNextInstances(CommandInstance curNode) {
		int curIdx = 0;
		if( curNode != null ) {
			ICommandGenerator delegated = curNode.getDelegated();
			if( delegated != null && delegated != this )
				return enscopeDelegation( delegated.getNextInstances(curNode), delegated );
			curIdx = curNode.getCommandIdx();
		}
		
		int[] followers = streamBuffer[curIdx].getBranchTo();
		CommandInstance[] branchTo = new CommandInstance[followers.length];
		for( int i = 0; i < followers.length; i ++ )
			branchTo[i] = streamBuffer[followers[i]].getCommand();
		return branchTo;
	}
	
	private CommandInstance[] enscopeDelegation(CommandInstance[] children, ICommandGenerator delegated) {
		if( children == null )
			return null;
		CommandInstance[] newChildren = new CommandInstance[children.length];
		for( int i = 0; i < children.length; i ++ ) {
			newChildren[i] = new CommandInstance(children[i].getCommandIdx(), delegated, children[i].getCommand());
		}
		
		return newChildren;
	}
	
	/////////////////
	
	static class CommandStreamEntry {
		private final CommandInstance command;
		private final int[] branchTo;
		
		public CommandStreamEntry(CommandInstance command, int[] branchTo) {
			this.command = command;
			this.branchTo = branchTo;
		}

		public CommandInstance getCommand() {
			return command;
		}

		public int[] getBranchTo() {
			return branchTo;
		}
	}
}
