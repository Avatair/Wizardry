package com.teamwizardry.wizardry.lib.vm.command.program.factory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.teamwizardry.wizardry.lib.vm.command.ICommand;
import com.teamwizardry.wizardry.lib.vm.command.ICommandGenerator;
import com.teamwizardry.wizardry.lib.vm.command.program.UnsatisfiedLinkException;
import com.teamwizardry.wizardry.lib.vm.command.type.NullCommand;

public class ProgramSequence implements IProgramSequence {
	
	private LinkedList<WeakReference<ProgramSequence>> importers = new LinkedList<>();

	private LinkedList<ProgramSequence> importedPrograms = new LinkedList<>();
	private HashMap<String, String> frameAliasMap = new HashMap<>();
	
	private LinkedList<ProgramSequence> lazy_flatImportedPrograms = null;

	private HashMap<String, Frame> frames = new HashMap<>();
	private Frame curFrame = null;
	private boolean isModeCreation = false;	// TODO: Make an enum here
	private int atNextIndex = -1;
	
	public ProgramSequence() {
	}
	
	@Override
	public ProgramSequence importProgram(ProgramSequence other) {
		if( !importedPrograms.contains(other) ) {
			importedPrograms.addFirst(other);
			other.importers.add(new WeakReference<>(this));
			
			invalidateImporters();
		}
		return this;
	}
	
	public List<ProgramSequence> getRecursiveImportedPrograms() {
		// NOTE: No cyclical dependency check?
		if( lazy_flatImportedPrograms == null ) {
			synchronized(this) {
				if( lazy_flatImportedPrograms != null )
					return lazy_flatImportedPrograms;	// If after the lock, some other thread generated the data.

				LinkedList<ProgramSequence> flatList = new LinkedList<>();

				for( ProgramSequence importedProgram : importedPrograms ) {
					if( !flatList.contains(importedProgram) ) {
						List<ProgramSequence> otherImportedPrograms = importedProgram.getRecursiveImportedPrograms();
						for( ProgramSequence otherImportedProgram : otherImportedPrograms ) {
							if( !flatList.contains(otherImportedProgram) )
								flatList.addLast(otherImportedProgram);
						}

						flatList.addLast(importedProgram);
					}
				}

				lazy_flatImportedPrograms = flatList;
			}
		}

		return Collections.unmodifiableList(lazy_flatImportedPrograms);
	}

	private synchronized void invalidateImporters() {
		lazy_flatImportedPrograms = null;
		
		Iterator<WeakReference<ProgramSequence>> iter = importers.iterator();
		while( iter.hasNext() ) {
			WeakReference<ProgramSequence> refImporter = iter.next();
			ProgramSequence importedProgram = refImporter.get();
			if( importedProgram == null ) {
				iter.remove();
				continue;
			}
			
			importedProgram.invalidateImporters();
		}		
	}
	
	@Override
	public ProgramSequence addFrameAlias(String aliasName, String asFrameName) {
		frameAliasMap.put(aliasName,  asFrameName);
		return this;
	}
	
	@Override
	public ProgramSequence editFrame(String frameName) throws UnsatisfiedLinkException {
		if( curFrame != null )
			throw new ProgramSequenceException("A frame is still open.");
		Frame frame = frames.get(frameName);	// NOTE: not using getFrame() ! its injection at local frames.
		if( frame == null )
			throw new UnsatisfiedLinkException("Frame " + frameName + " is not existing.");
		
		curFrame = frame;
		atNextIndex = frame.getEntries().size();
		isModeCreation = false;
		
		return this;
	}
	
	@Override
	public ProgramSequence seekTo(String labelName, int offset) throws UnsatisfiedLinkException {
		checkForOpenFrame();
		int nextIndex;
		if( labelName != null ) {
			LabelFrameEntry label = curFrame.getLabelEntryByName(labelName);
			if( label == null )
				throw new UnsatisfiedLinkException("No label " + labelName + " exists at frame " + curFrame.getFrameName());
			nextIndex = label.getCodePosition() + offset;
		}
		else {
			nextIndex = curFrame.getEntries().size() + offset;
		}
		if( nextIndex < 0 || nextIndex > curFrame.getEntries().size() )
			throw new ProgramSequenceException("bad offset " + offset + " at label " + labelName + " resulting in " + nextIndex + " out of bounds between [0, " + curFrame.getEntries().size() + "]." );
		atNextIndex = nextIndex;
		
		return this;
	}
	
	Frame beginFrameSpecial(String frameName) {
		if( frameName == null || frameName.isEmpty() )
			throw new IllegalArgumentException("A frame must have a name");
		if( frames.containsKey(frameName) )
			throw new ProgramSequenceException("Frame " + frameName + " already exists.");
		if( curFrame != null )
			throw new ProgramSequenceException("A frame is still open.");

		curFrame = new Frame(frameName);
		atNextIndex = 0;
		isModeCreation = true;

		return curFrame;
	}
	
	@Override
	public ProgramSequence beginFrame( String frameName ) {
		beginFrameSpecial(frameName);
		return this;
	}
	
	Frame endFrameSpecial(boolean isOpenEnded, boolean isRegistered) throws UnsatisfiedLinkException {
		checkForOpenFrame();

		if( isModeCreation ) {
			addCommand(NullCommand.SINGLETON);	// Otherwise returns won't work if last command in main frame is a call. 
			if( !isOpenEnded )
				addStop(true);
		}
		
		try {
			finalizeFrame(curFrame);
		} catch (Exception e) {
			// Rollback last changes to program.
			int removeCount = 0;
			if( isModeCreation ) {
				removeCount ++;
				if( !isOpenEnded )
					removeCount ++;
			}
			
			// TODO: Move to a routine
			while( removeCount > 0 ) {
				atNextIndex --;
				curFrame.removeEntry(atNextIndex);
				removeCount --;
			}

			if( e instanceof UnsatisfiedLinkException )
				throw (UnsatisfiedLinkException)e;
			if( e instanceof RuntimeException)
				throw e;
			else
				throw new IllegalStateException("Unknown exception.", e);	// Unsually this should never happen.
		}

		if( isModeCreation ) {
			if( isRegistered )
				frames.put(curFrame.getFrameName(), curFrame);
		}
		
		Frame saveCurFrame = curFrame;
		curFrame = null;
		
		return saveCurFrame;
	}	
	
	@Override
	public ProgramSequence endFrame() throws UnsatisfiedLinkException {
		endFrameSpecial(false, true);
		return this;
	}
	
	@Override
	public boolean hasOpenFrame() {
		return curFrame != null;
	}
	
	@Override
	public ProgramSequence addCommand(ICommand cmd) {
		checkForOpenFrame();
		
		curFrame.addEntry(new CommandFrameEntry(curFrame, cmd), atNextIndex);
		atNextIndex ++;

		return this;
	}
	
	@Override
	public ProgramSequence assignFork( String ... toLabels ) {
		checkForOpenFrame();
		
		if( atNextIndex <= 0 )
			throw new ProgramSequenceException("Illegal call at an empty program.");
		
		AbstractFrameEntry entry = curFrame.getEntries().get(atNextIndex - 1);
		if( entry instanceof AbstractForkableFrameEntry ) {
			AbstractForkableFrameEntry forkableEntry = (AbstractForkableFrameEntry) entry;
			forkableEntry.setForks(toLabels);
		}
		else
			throw new ProgramSequenceException("Illegal call at non forkable entry.");

		return this;
	}
	
	@Override
	public IProgramSequence addForkTo(String ... toLabels) {
		addCommand(NullCommand.SINGLETON).assignFork(toLabels);

		return this;
	}

	@Override
	public ProgramSequence addLabel(String labelName) {
		checkForOpenFrame();
		
		curFrame.addEntry(new LabelFrameEntry(curFrame, labelName), atNextIndex);
		atNextIndex ++;

		return this;
	}
	
	@Override
	public ProgramSequence addFrameCall( String frameName ) {
		checkForOpenFrame();
		
		curFrame.addEntry(new CallFrameEntry(curFrame, frameName), atNextIndex);
		atNextIndex ++;

		return this;
	}
	
	@Override
	public ProgramSequence addReturn() {
		addReturn(0);

		return this;
	}
	
	@Override
	public ProgramSequence addReturn( int toForkIdx ) {
		checkForOpenFrame();
		
		curFrame.addEntry(new ReturnFrameEntry(curFrame, toForkIdx), atNextIndex);
		atNextIndex ++;

		return this;
	}
	
	@Override
	public IProgramSequence addStop(boolean isInt3) {
		checkForOpenFrame();
		
		curFrame.addEntry(new StopFrameEntry(isInt3, curFrame), atNextIndex);
		atNextIndex ++;
		
		return this;
	}

	@Override
	public IProgramSequence addNop(int countTicks) {
		checkForOpenFrame();
		
		curFrame.addEntry(new NopFrameEntry(curFrame, countTicks), atNextIndex);
		atNextIndex ++;
		
		return this;
	}
	
	@Override
	public ProgramSequence addCallGenerator( ICommandGenerator commandGenerator ) {
		checkForOpenFrame();
		
		curFrame.addEntry(new CallGeneratorFrameEntry(curFrame, commandGenerator), atNextIndex);
		atNextIndex ++;
		
		return this;
	}
	
	Frame getFrame(String frameName, boolean bIncludeDeps ) {
		Frame frame = frames.get(frameName);
		if( frame != null )
			return frame;
		if( !bIncludeDeps )
			return null;
		
		Iterator<ProgramSequence> iter = getRecursiveImportedPrograms().iterator(); 
		while( frame == null && iter.hasNext() ) {
			ProgramSequence otherSequence = iter.next();
			frame = otherSequence.getFrame(frameName, false);
		}
		return frame;
	}
	
	String getAliasedName(String frameName, boolean bIncludeDeps) {
		String trn = frameAliasMap.get(frameName);
		if( trn != null )
			return trn;
		if( !bIncludeDeps )
			return null;

		
		Iterator<ProgramSequence> iter = getRecursiveImportedPrograms().iterator(); 
		while( trn == null && iter.hasNext() ) {
			ProgramSequence otherSequence = iter.next();
			trn = otherSequence.getAliasedName(frameName, false);
		}
		return trn;
	}

	/////////////
	
	private void finalizeFrame(Frame frame) throws UnsatisfiedLinkException {
		int iCurPosition = 0;	// Relative to frame!
		
		frame.clearFinalized();
		
		// Calculate position offsets
		for( AbstractFrameEntry entry : frame.getEntries() ) {
			entry.setCodePosition(iCurPosition);
			if( (entry instanceof CommandFrameEntry) ||
					(entry instanceof CallFrameEntry) ||
					(entry instanceof ReturnFrameEntry) ||
					(entry instanceof StopFrameEntry) ||
					(entry instanceof NopFrameEntry) ||
					(entry instanceof CallGeneratorFrameEntry) ) {
				iCurPosition = frame.appendLinkedInstruction(entry);
			}
			else if( entry instanceof LabelFrameEntry ) {
				// No increment
			}
			else
				throw new IllegalStateException("entry class not supported: " + entry.getClass().getName());
		}
		
		// Calculate branching offsets
		for( AbstractFrameEntry entry : frame.getEntries() ) {
			if( entry instanceof AbstractForkableFrameEntry ) {
				AbstractForkableFrameEntry brEntry = (AbstractForkableFrameEntry)entry;
				
				String[] jumps = brEntry.getForks();
				if( jumps != null ) {
					for( int i = 0; i < jumps.length; i ++ ) {
						String jumpLabelName = jumps[i];
						LabelFrameEntry labelEntry = frame.getLabelEntryByName(jumpLabelName);
						if( labelEntry == null ) {
							throw new UnsatisfiedLinkException(
									"Unsatisfied link to label " +
									jumpLabelName + " at frame " +
									frame.getFrameName() );
						}
						
						int targetPos = labelEntry.getCodePosition();
						if( targetPos < 0 )
							throw new IllegalStateException("Internal: Uninitialized position found.");
						
						brEntry.setForkPosition(i, targetPos);
					}
				}
				else if( !brEntry.isLast() ) {
					brEntry.setForkPosition(0, brEntry.getCodePosition() + 1);
				}
			}
		}

	}

	private void checkForOpenFrame() {
		if( curFrame == null )
			throw new ProgramSequenceException("No frame is open.");
	}
		
	/////////////
	
	static class Frame {
		// Fixed data
		private final String frameName;
		private ArrayList<AbstractFrameEntry> entries = new ArrayList<>();	// LinkedList, because required: no seeking, fast appending.
		private HashMap<String, LabelFrameEntry> nameToLabel = new HashMap<>();
		
		// Finalized data
		private ArrayList<AbstractFrameEntry> linkedInstructions = null;
		
		Frame(String frameName) {
			this.frameName = frameName;
		}

		String getFrameName() {
			return frameName;
		}
		
		void clearFinalized() {
			linkedInstructions = null;
			for( AbstractFrameEntry entry : entries ) {
				entry.clearFinalized();
			}
		}
		
		void addEntry(AbstractFrameEntry entry, int atIndex) {
			if( entry instanceof LabelFrameEntry ) {
				LabelFrameEntry label = (LabelFrameEntry)entry;
				nameToLabel.put(label.getLabelName(), label);
			}
			
			entry.setIndex(atIndex);
			entries.add(atIndex, entry);
			for( int i = atIndex + 1; i < entries.size(); i ++ )
				entries.get(i).setIndex(i);
		}
		
		void removeEntry(int atIndex) {
			if( atIndex < 0 || atIndex >= entries.size() )
				throw new IllegalArgumentException("Index out of bounds. Tried to access " + atIndex + " of a size " + entries.size());
			AbstractFrameEntry entry = entries.get(atIndex);
			
			// Maybe undo changes on the labels registry 
			if( entry instanceof LabelFrameEntry ) {
				LabelFrameEntry label = (LabelFrameEntry)entry;
				nameToLabel.remove(label.getLabelName());
			}
			
			entries.remove(atIndex);
		}
		
		AbstractFrameEntry getLastEntry() {
			if( entries.isEmpty() )
				return null;
			return entries.get(entries.size() - 1);
		}
		
		List<AbstractFrameEntry> getEntries() {
			return entries;
		}
		
		LabelFrameEntry getLabelEntryByName(String name) {
			return nameToLabel.get(name);
		}
		
		int appendLinkedInstruction(AbstractFrameEntry entry) {
			maybeInitLinkedInstructions();
			linkedInstructions.add(entry);
			return linkedInstructions.size();
		}
		
		List<AbstractFrameEntry> getLinkedInstructions() {
			maybeInitLinkedInstructions();
			return linkedInstructions;
		}
		
		private void maybeInitLinkedInstructions() {
			if( linkedInstructions == null ) {
				int dimension = entries.size();	// TODO: Calculate regarding subframes
				linkedInstructions = new ArrayList<>(dimension);
			}
		}
	}
	
	static abstract class AbstractFrameEntry {
		// Fixed data
		private final Frame parentFrame;
		private int index = -1;
		
		// Finalized data
		private int iCodePosition = -1;
		
		protected AbstractFrameEntry(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}
		
		public void setIndex(int index) {
			this.index = index;			
		}
		
		public int getIndex() {
			return index;
		}

		void clearFinalized() {
			iCodePosition = -1;
		}
		
		void setCodePosition(int iPosition) {
			this.iCodePosition = iPosition;
		}
		
		int getCodePosition() {
			return iCodePosition;
		}
		
		boolean isLast() {
			return parentFrame.getLastEntry() == this;
		}
	}
	
	static class ReturnFrameEntry extends AbstractFrameEntry {
		// Fixed data
		private final int toForkIdx;
		
		ReturnFrameEntry(Frame parentFrame, int toForkIdx) {
			super(parentFrame);
			this.toForkIdx = toForkIdx;
		}

		public int getToForkIdx() {
			return toForkIdx;
		}
	}
	
	static class StopFrameEntry extends AbstractFrameEntry {
		private final boolean isInt3;
		
		StopFrameEntry(boolean isInt3, Frame parentFrame) {
			super(parentFrame);
			this.isInt3 = isInt3;
		}
		
		public boolean isInt3() {
			return this.isInt3;
		}
	}
	
	static class CallGeneratorFrameEntry extends AbstractFrameEntry {
		private final ICommandGenerator commandGenerator;
		
		CallGeneratorFrameEntry(Frame parentFrame, ICommandGenerator commandGenerator) {
			super(parentFrame);
			this.commandGenerator = commandGenerator;
		}

		public ICommandGenerator getCommandGenerator() {
			return commandGenerator;
		}
	}
	
	static class LabelFrameEntry extends AbstractFrameEntry {
		private final String labelName;
				
		LabelFrameEntry(Frame parentFrame, String labelName) {
			super(parentFrame);
			this.labelName = labelName;
		}
		
		public String getLabelName() {
			return labelName;
		}
	}
	
	static abstract class AbstractForkableFrameEntry extends AbstractFrameEntry {
		private String[] forks = null;
		
		// Runtime
		private int[] forkPositions = null;

		AbstractForkableFrameEntry(Frame parentFrame) {
			super(parentFrame);
		}
		
		@Override
		void clearFinalized() {
			super.clearFinalized();
			forkPositions = null;
		}
		
		void setForks(String[] forks) {
			this.forks = forks;			
		}
		
		String[] getForks() {
			return forks;
		}
		
		void setForkPosition(int i, int pos) {
			maybeInitForkPositions();
			if( i >= forkPositions.length ) {
				throw new IllegalArgumentException(
						"branching " + i + " is not existing. Maximal " +
						(forkPositions.length - 1) + " is allowed" );
			}
			
			forkPositions[i] = pos;
		}
		
		int[] getForkPositions() {
			maybeInitForkPositions();
			return forkPositions;
		}
		
		private void maybeInitForkPositions() {
			if( forkPositions == null ) {
				if( forks != null ) {
					forkPositions = new int[forks.length];
				}
				else if( !isLast() ) {
					forkPositions = new int[1];
				}
				else
					forkPositions = new int[0];
				Arrays.fill(forkPositions, -1);
			}
		}
	}
	
	static class CommandFrameEntry extends AbstractForkableFrameEntry {
		private final ICommand command;
		
		CommandFrameEntry(Frame parentFrame, ICommand command) {
			super(parentFrame);
			this.command = command;
		}

		public ICommand getCommand() {
			return command;
		}
	}
	
	static class NopFrameEntry extends AbstractForkableFrameEntry {
		private final int countTicks;
		
		NopFrameEntry(Frame parentFrame, int countTicks) {
			super(parentFrame);
			this.countTicks = countTicks;
		}
		
		public int getCountTicks() {
			return this.countTicks;
		}
	}
	
	static class CallFrameEntry extends AbstractForkableFrameEntry {
		private final String calledFrameName;
		
		CallFrameEntry(Frame parentFrame, String calledFrameName) {
			super(parentFrame);
			this.calledFrameName = calledFrameName;
		}

		public String getCalledFrameName() {
			return calledFrameName;
		}
	}
}
