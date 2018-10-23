package com.teamwizardry.wizardry.lib.vm.command.program.factory;

import java.util.List;

import com.teamwizardry.wizardry.lib.vm.command.ICommand;
import com.teamwizardry.wizardry.lib.vm.command.ICommandGenerator;
import com.teamwizardry.wizardry.lib.vm.command.program.CommandBuildContext;
import com.teamwizardry.wizardry.lib.vm.command.program.CommandBuildResult;
import com.teamwizardry.wizardry.lib.vm.command.program.CommandStream;
import com.teamwizardry.wizardry.lib.vm.command.program.CommandStreamBuilder;
import com.teamwizardry.wizardry.lib.vm.command.program.UnsatisfiedLinkException;
import com.teamwizardry.wizardry.lib.vm.command.program.factory.ProgramSequence.AbstractForkableFrameEntry;
import com.teamwizardry.wizardry.lib.vm.command.program.factory.ProgramSequence.AbstractFrameEntry;
import com.teamwizardry.wizardry.lib.vm.command.program.factory.ProgramSequence.CallFrameEntry;
import com.teamwizardry.wizardry.lib.vm.command.program.factory.ProgramSequence.CallGeneratorFrameEntry;
import com.teamwizardry.wizardry.lib.vm.command.program.factory.ProgramSequence.CommandFrameEntry;
import com.teamwizardry.wizardry.lib.vm.command.program.factory.ProgramSequence.Frame;
import com.teamwizardry.wizardry.lib.vm.command.program.factory.ProgramSequence.NopFrameEntry;
import com.teamwizardry.wizardry.lib.vm.command.program.factory.ProgramSequence.ReturnFrameEntry;
import com.teamwizardry.wizardry.lib.vm.command.program.factory.ProgramSequence.StopFrameEntry;
import com.teamwizardry.wizardry.lib.vm.command.type.Int3Command;
import com.teamwizardry.wizardry.lib.vm.command.type.NopCommand;
import com.teamwizardry.wizardry.lib.vm.command.type.NullCommand;

public class ProgramBuilder implements IProgramSequence {
	private ProgramSequence prototype = new ProgramSequence(); 
	
	private Frame mainFrame = null;
	private Frame curFrame = null;
	
	public ProgramBuilder() {
	}

	@Override
	public ProgramBuilder importProgram(ProgramSequence other) {
		prototype.importProgram(other);
		return this;
	}

	@Override
	public ProgramBuilder addFrameAlias(String aliasName, String asFrameName) {
		prototype.addFrameAlias(aliasName, asFrameName);
		return this;
	}

	@Override
	public ProgramBuilder editFrame(String frameName) throws UnsatisfiedLinkException {
		prototype.editFrame(frameName);
		return this;
	}
	
	@Override
	public ProgramBuilder seekTo(String labelName, int offset) throws UnsatisfiedLinkException {
		prototype.seekTo(labelName, offset);
		return this;
	}
	
	public ProgramBuilder beginMainFrame() {
		if( mainFrame != null )
			throw new ProgramSequenceException("A main frame is already existing.");
		curFrame = prototype.beginFrameSpecial("_main");
		mainFrame = curFrame;
		return this;
	}

	@Override
	public ProgramBuilder beginFrame(String frameName) {
		curFrame = prototype.beginFrameSpecial(frameName);
		return this;
	}

	@Override
	public ProgramBuilder endFrame() throws UnsatisfiedLinkException {
		boolean isMainFrame = mainFrame == curFrame;
		prototype.endFrameSpecial(isMainFrame, !isMainFrame);
		curFrame = null;
		return this;
	}
	
	@Override
	public boolean hasOpenFrame() {
		return prototype.hasOpenFrame();
	}

	@Override
	public ProgramBuilder addCommand(ICommand cmd) {
		prototype.addCommand(cmd);
		return this;
	}

	@Override
	public ProgramBuilder assignFork(String... toLabels) {
		prototype.assignFork(toLabels);
		return this;
	}

	@Override
	public ProgramBuilder addForkTo(String... toLabels) {
		prototype.addForkTo(toLabels);
		return this;
	}

	@Override
	public ProgramBuilder addLabel(String labelName) {
		prototype.addLabel(labelName);
		return this;
	}

	@Override
	public ProgramBuilder addFrameCall(String frameName) {
		prototype.addFrameCall(frameName);
		return this;
	}

	@Override
	public ProgramBuilder addReturn() {
		prototype.addReturn();
		return this;
	}

	@Override
	public ProgramBuilder addReturn(int toForkIdx) {
		prototype.addReturn(toForkIdx);
		return this;
	}

	@Override
	public ProgramBuilder addStop(boolean isInt3) {
		prototype.addStop(isInt3);
		return this;
	}
	
	@Override
	public IProgramSequence addNop(int countTicks) {
		prototype.addNop(countTicks);
		return this;
	}

	@Override
	public ProgramBuilder addCallGenerator(ICommandGenerator commandGenerator) {
		prototype.addCallGenerator(commandGenerator);
		return this;
	}
	
	public CommandStream build() throws UnsatisfiedLinkException {
		if( hasOpenFrame() )
			throw new ProgramSequenceException("Last frame must be closed before.");
		
		CommandStreamBuilder<FrameTraversing> builder = new CommandStreamBuilder<>();
		if( mainFrame != null ) {
			// Configure program builder
			builder.processOpen(n -> getNextCommand(n));
		}
		
		// Run old Turtle4 program builder
		return builder.build();
	}
	
	///////////////////
	
	private CommandBuildResult getNextCommand(CommandBuildContext<FrameTraversing> context) {
		try {
			FrameTraversing trav = context.getContext();
			if( trav == null ) {
				trav = FrameTraversing.makeStart(this);
			}
	
			List<AbstractFrameEntry> instructions = trav.getSubFrame().getLinkedInstructions();
			if( instructions.isEmpty() )
				return context.make(NullCommand.SINGLETON, null, 0);
	
			AbstractFrameEntry nextEntry;
			if( trav.isStart() ) {
				// Beginning
				nextEntry = instructions.get(0);
			}
			else {
				AbstractFrameEntry entry = trav.getInstruction();
				int[] forkPositions;
				int toForkIdx;			
				
				if( entry instanceof AbstractForkableFrameEntry ) {
					AbstractForkableFrameEntry forkableEntry = (AbstractForkableFrameEntry)entry;
					forkPositions = forkableEntry.getForkPositions();
					toForkIdx = context.getParentFollowerIdx();
				}
				else if( entry instanceof ReturnFrameEntry ) {
					ReturnFrameEntry returnEntry = (ReturnFrameEntry)entry;
					CallFrameEntry hotspot = trav.getHotspot();
					FrameTraversing parent = trav.getParent();
					if( hotspot == null || parent == null )
						throw new UnsatisfiedLinkException("Unsatisfied link: Illegal return.");
					
					forkPositions = hotspot.getForkPositions();
					toForkIdx = returnEntry.getToForkIdx();
					
					trav = parent;
					instructions = trav.getSubFrame().getLinkedInstructions();
				}
				else
					throw new IllegalStateException("Couldn't get next instruction. Unknown entry type.");
	
				if( toForkIdx < 0 || toForkIdx >= forkPositions.length ) {
					throw new UnsatisfiedLinkException("Unsatisfied link: Unknown branching index " + toForkIdx +
						". Only " + (forkPositions.length - 1) + " is allowed." );
				}
				nextEntry = instructions.get(forkPositions[toForkIdx]);
			}
			
			if( nextEntry instanceof CommandFrameEntry ) {
				CommandFrameEntry cmdEntry = (CommandFrameEntry)nextEntry;
				trav = trav.makeGotoNextCommand(nextEntry);
				return context.make(cmdEntry.getCommand(), trav, cmdEntry.getForkPositions().length);
			}
			else if( nextEntry instanceof CallFrameEntry ) {
				CallFrameEntry callEntry = (CallFrameEntry)nextEntry;
				trav = trav.makeEnterSubFrame(this, callEntry);
				return context.make(NullCommand.SINGLETON, trav, 1);
			}
			else if( nextEntry instanceof ReturnFrameEntry ) {
				trav = trav.makeGotoNextCommand(nextEntry);
				return context.make(NullCommand.SINGLETON, trav, 1);
			}
			else if( nextEntry instanceof StopFrameEntry ) {
				StopFrameEntry stopEntry = (StopFrameEntry)nextEntry;
				if( stopEntry.isInt3() )
					return context.make(Int3Command.SINGLETON, null, 0);
				return context.make(NullCommand.SINGLETON, null, 0);
			}
			else if( nextEntry instanceof NopFrameEntry ) {
				NopFrameEntry nopEntry = (NopFrameEntry)nextEntry;
				trav = trav.makeGotoNextCommand(nextEntry);
				return context.make(new NopCommand(nopEntry.getCountTicks()), trav, 1);
			}
			else if( nextEntry instanceof CallGeneratorFrameEntry ) {
				CallGeneratorFrameEntry callGenEnry = (CallGeneratorFrameEntry)nextEntry;
				return context.make(callGenEnry.getCommandGenerator());
			}
			else
				throw new IllegalStateException("Couldn't build next instruction. Unknown entry type.");
		}
		catch(UnsatisfiedLinkException exc) {
			return context.make(exc);
		}
	}
	
	/////////////
	
	private static class FrameTraversing {
		private final FrameTraversing parent;
		private final CallFrameEntry hotspot;
		private final AbstractFrameEntry instruction;
		private final Frame subFrame;
		
		private FrameTraversing(FrameTraversing parent, CallFrameEntry hotspot, Frame subFrame, AbstractFrameEntry instruction) {
			this.parent = parent;
			this.hotspot = hotspot;
			this.instruction = instruction;
			this.subFrame = subFrame;
		}
		
		static FrameTraversing makeStart(ProgramBuilder builder) {
			return new FrameTraversing(null, null, builder.mainFrame, null);
		}
		
		FrameTraversing makeEnterSubFrame(ProgramBuilder builder, CallFrameEntry hotspot) throws UnsatisfiedLinkException {
			String frameName = hotspot.getCalledFrameName();
			String translatedFrameName = builder.prototype.getAliasedName(frameName);
			if( translatedFrameName == null )
				translatedFrameName = frameName;
			
			Frame subFrame = builder.prototype.getFrame(translatedFrameName);
			if( subFrame == null )
				throw new UnsatisfiedLinkException("Unsatisfied link: frame " + translatedFrameName + " is not existing, referenced by alias " + frameName + "." );
			return new FrameTraversing(this, hotspot, subFrame, null);
		}
		
		FrameTraversing makeGotoNextCommand(AbstractFrameEntry instruction) {
			return new FrameTraversing(parent, hotspot, subFrame, instruction);
		}

		FrameTraversing getParent() {
			return parent;
		}

		CallFrameEntry getHotspot() {
			return hotspot;
		}
		
		Frame getSubFrame() {
			return subFrame;
		}

		AbstractFrameEntry getInstruction() {
			return instruction;
		}
		
		boolean isStart() {
			return instruction == null;	// NOTE: created from pattern at makeStart(). Make sure no ambiguity arise! 
		}
	}


}
