package com.teamwizardry.wizardry.lib.vm.command.program;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import com.teamwizardry.wizardry.lib.vm.command.CommandInstance;
import com.teamwizardry.wizardry.lib.vm.command.ICommand;
import com.teamwizardry.wizardry.lib.vm.command.ICommandGenerator;
import com.teamwizardry.wizardry.lib.vm.command.program.CommandBuildResult.CommandGenTargetBuildResult;
import com.teamwizardry.wizardry.lib.vm.command.program.CommandBuildResult.CommandTargetBuildResult;
import com.teamwizardry.wizardry.lib.vm.command.program.CommandStream.CommandStreamEntry;
import com.teamwizardry.wizardry.lib.vm.command.type.NullCommand;

public class CommandStreamBuilder<T> {
	private ArrayList<InstructionEntry<T>> entries = new ArrayList<InstructionEntry<T>>();
	int curCommandIdx = 0;
	
	public CommandStreamBuilder() {
		entries.add(new InstructionEntry<T>(null, null, 0));
	}
	
	public CommandStreamBuilder<T> putCommand(ICommand cmdData, T context, int amountFollowers) {
		if( curCommandIdx >= entries.size() )
			throw new IllegalStateException("Program is terminated.");
		if( cmdData == null )
			throw new IllegalArgumentException("Command is null.");
		
		// special case for non null entry point. It needs to be ensured, that first command is always a Null command
		if( curCommandIdx == 0 && !cmdData.equals(NullCommand.SINGLETON) ) {
			putCommand(NullCommand.SINGLETON, null, 1);
		}
		
		InstructionEntry<T> curEntry = entries.get(curCommandIdx);
		incrementCurCommandIdx();
		curEntry.setInstContainer( new CommandInstructionType( cmdData ) );
		for( int i = 0; i < amountFollowers; i ++ ) {
			InstructionEntry<T> subEntry = new InstructionEntry<T>(cmdData, context, i);
			curEntry.addFollower(subEntry);
			entries.add(subEntry);
		}
		
		return this;
	}
	
	public CommandStreamBuilder<T> putEnter(ICommandGenerator enterGen) {
		if( curCommandIdx >= entries.size() )
			throw new IllegalStateException("Program is terminated.");
		if( enterGen == null )
			throw new IllegalArgumentException("Command generator is null.");
		
		CommandInstance[] subCommands = enterGen.getNextInstances(null);
		if( subCommands != null && subCommands.length > 0 ) {
			InstructionEntry<T> curEntry = entries.get(curCommandIdx);
			incrementCurCommandIdx();
			if( subCommands.length == 1 ) {
				curEntry.setInstContainer( new EnterInstructionType(enterGen, subCommands[0]) );
			}
			else {
				curEntry.setInstContainer( new CommandInstructionType( NullCommand.SINGLETON ) );
	
				for( int i = 0; i < subCommands.length; i ++ ) {
					InstructionEntry<T> subEntry = new InstructionEntry<T>(NullCommand.SINGLETON, null, i);
					subEntry.setInstContainer(new EnterInstructionType(enterGen, subCommands[i]) );
					curEntry.addFollower(subEntry);
					entries.add(subEntry);
				}
			}
		}
		
		return this;
	}
	
	private void incrementCurCommandIdx() {
		while(true) {
			if( curCommandIdx >= entries.size() )
				break;
			
			InstructionEntry<T> curEntry = entries.get(curCommandIdx ++);
			if( curEntry.getInstContainer() == null )
				break;
		}
	}

	public CommandBuildContext<T> getNextOpenCommand() {
		if( curCommandIdx >= entries.size() )
			return null;
		InstructionEntry<T> curEntry = entries.get(curCommandIdx);
		return new CommandBuildContext<T>( curEntry.getContext(), curEntry.getParentFollowerIdx(), curEntry.getFollowers().size() );
	}
	
	@SuppressWarnings("unchecked")
	public CommandStreamBuilder<T> processOpen(Function<CommandBuildContext<T>, CommandBuildResult> fct) {
		CommandBuildContext<T> ctx;
		while( (ctx = getNextOpenCommand()) != null ) {
			CommandBuildResult newCtx = fct.apply(ctx);
			if( newCtx != null ) {
				if( newCtx instanceof CommandTargetBuildResult ) {
					CommandTargetBuildResult<T> newCmdCtx = (CommandTargetBuildResult<T>)newCtx;
					putCommand(newCmdCtx.getCommandData(), newCmdCtx.getNextContext(), newCmdCtx.getNextAmountFollowers());
				}
				else if( newCtx instanceof CommandGenTargetBuildResult ) {
					CommandGenTargetBuildResult newGenCtx = (CommandGenTargetBuildResult)newCtx;
					putEnter(newGenCtx.getCommandGenerator());
				}
				else
					throw new IllegalStateException("Unknown build target type");
				
			}
			else
				putCommand(NullCommand.SINGLETON, null, 0);
		}
		
		return this;
	}
	
	private List<InstructionEntry<T>> getAggregatedFollowers(InstructionEntry<T> root) {
		LinkedList<InstructionEntry<T>> followers = new LinkedList<InstructionEntry<T>>();
		if( root != null ) {
			Collection<InstructionEntry<T>> origFollowers = root.getFollowers();
			for(InstructionEntry<T> entry : origFollowers ) {
				AbstractInstructionType container = entry.getInstContainer();
				if( container != null && (container instanceof CommandInstructionType) ) {
					ICommand cmd = ((CommandInstructionType)container).getCommand();
					if( cmd.equals(NullCommand.SINGLETON) )
						followers.addAll(getAggregatedFollowers(entry));
					else
						followers.add(entry);
				}
				else
					followers.add(entry);
			}
		}

		return followers;
	}

	public CommandStream build() {
		LinkedList<LinkerEntry<T>> linkedProgram = new LinkedList<LinkerEntry<T>>();

		if( !entries.isEmpty() ) {
			LinkedList<LinkerEntry<T>> pending = new LinkedList<LinkerEntry<T>>();
			pending.add(new LinkerEntry<T>(null, 0, entries.get(0)));
			
			int curPos = 0;		
			while(!pending.isEmpty()) {
				LinkerEntry<T> entry = pending.pollFirst();
				
				// Add entry to program with new index
				entry.setPos(curPos ++);
				linkedProgram.add(entry);
				
				// Link to parent as follower, if available
				LinkerEntry<T> parent = entry.getParent();
				if( parent != null ) {
					parent.getChildren()[entry.getFollowerIdx()] = entry;
				}
				
				// Retrieve followers 
				List<InstructionEntry<T>> followers = getAggregatedFollowers(entry.getEntry());
				int followerIdx = 0;
				for( InstructionEntry<T> follower : followers ) {
					pending.addLast(new LinkerEntry<T>(entry, followerIdx ++, follower) );
				}

				// Allocate follower array (will be populated later)
				entry.allocateFollowers(followers.size());
			}
		}

		// Convert to program data
		CommandStreamEntry[] programBuffer = new CommandStreamEntry[linkedProgram.size()];
		int i = 0;
		for( LinkerEntry<T> entry : linkedProgram ) {
			programBuffer[i ++] = entry.toProgramEntry();
		}
		
		return new CommandStream( programBuffer );
	}
	
	private static class LinkerEntry<T> {
		private final InstructionEntry<T> entry;
		private int pos;

		private final int followerIdx;
		private final LinkerEntry<T> parent;
		private LinkerEntry<T>[] children;
		
		LinkerEntry(LinkerEntry<T> parent, int followerIdx, InstructionEntry<T> entry) {
			this.entry = entry;

			this.parent = parent;
			this.followerIdx = followerIdx;
		}

		InstructionEntry<T> getEntry() {
			return entry;
		}

		void setPos(int pos) {
			this.pos = pos;
		}
		
		int getPos() {
			return pos;
		}

		int getFollowerIdx() {
			return followerIdx;
		}

		LinkerEntry<T> getParent() {
			return parent;
		}
		
		@SuppressWarnings("unchecked")
		void allocateFollowers(int numFollowers) {
			this.children = new LinkerEntry[numFollowers];	
		}

		LinkerEntry<T>[] getChildren() {
			return children;
		}
		
		CommandStreamEntry toProgramEntry() {
			AbstractInstructionType container = getEntry().getInstContainer();
			if( container == null )
				throw new IllegalStateException("Program has open nodes!");
			
			CommandInstance cmd;
			if( container instanceof CommandInstructionType ) {
				ICommand cmdData = ((CommandInstructionType)container).getCommand();
				cmd = new CommandInstance(getPos(), cmdData);
			}
			else if( container instanceof EnterInstructionType ) {
				EnterInstructionType enterType = (EnterInstructionType)container;
				cmd = new CommandInstance(getPos(), enterType.getDelegateTo(), enterType.getSubCommandInstance().getCommand());
			}
			else
				throw new IllegalStateException("Unknown instruction type.");
			
			int[] childrenPos = new int[getChildren().length];
			for( int i = 0; i < childrenPos.length; i ++ ) {
				LinkerEntry<T> child = getChildren()[i];
				childrenPos[i] = child.getPos();
			}
			
			return new CommandStreamEntry(cmd, childrenPos);
		}
	}
	
	private static class InstructionEntry<T> {
		private final List<InstructionEntry<T>> followers = new LinkedList<InstructionEntry<T>>();
		private final ICommand prevCmdData;
		private final T context;
		private final int contextFollowerIdx;
		private AbstractInstructionType instContainer;

		InstructionEntry(ICommand prevCmdData, T context, int followerIdx) {
			this.prevCmdData = prevCmdData;
			this.context = context;
			this.contextFollowerIdx = followerIdx;
			this.setInstContainer(null);
		}

		ICommand getPrevCommand() {
			return this.prevCmdData;
		}

		Collection<InstructionEntry<T>> getFollowers() {
			return followers;
		}

		T getContext() {
			return this.context;
		}

		public int getParentFollowerIdx() {
			return this.contextFollowerIdx;
		}

		void addFollower(InstructionEntry<T> entry) {
			followers.add(entry);
		}

		public AbstractInstructionType getInstContainer() {
			return instContainer;
		}

		public void setInstContainer(AbstractInstructionType instContainer) {
			this.instContainer = instContainer;
		}
	}
	
	private static abstract class AbstractInstructionType {
	}
	
	private static class CommandInstructionType extends AbstractInstructionType {
		private final ICommand cmdData;

		CommandInstructionType(ICommand cmdData) {
			super();
			this.cmdData = cmdData;
		}
		
		ICommand getCommand() {
			return this.cmdData;
		}
	}
	
	private static class EnterInstructionType extends AbstractInstructionType {
		private final ICommandGenerator delegateTo;
		private final CommandInstance subCommand;
		
		EnterInstructionType(ICommandGenerator delegateTo, CommandInstance subCommand) {
			super();
			this.delegateTo = delegateTo;
			this.subCommand = subCommand;
		}

		public ICommandGenerator getDelegateTo() {
			return delegateTo;
		}

		public CommandInstance getSubCommandInstance() {
			return subCommand;
		}
	}
}
