package de.razorjack.newstuff2.action.command4.utils;

import de.razorjack.newstuff2.Main;
import de.razorjack.newstuff2.action.ActionProcessor;
import de.razorjack.newstuff2.action.command4.CommandDispatcherAction;
import de.razorjack.newstuff2.action.command4.CommandInstance;
import de.razorjack.newstuff2.action.command4.ICommandGenerator;
import de.razorjack.newstuff2.action.command4.operable.ICommandOperable;
import de.razorjack.newstuff2.action.command4.program.factory.ProgramSequenceBuilder;
import de.razorjack.newstuff2.action.command4.program.factory.ProgramSequence;

public class TestUtils {
	public static void runProgram(ICommandOperable initialStateTemplate, String entryPoint, ProgramSequence ... config) {
		ProgramSequenceBuilder builder = new ProgramSequenceBuilder();
		for( ProgramSequence seq : config ) {
			builder.importProgram(seq);
		}
		builder.beginMainFrame().addFrameCall(entryPoint).endFrame();
		runProgram(initialStateTemplate, builder.build());
	}
	
	public static void runProgram(ICommandOperable initialStateTemplate, ICommandGenerator program) {
		ActionProcessor actProc = new ActionProcessor();
		
		CommandInstance[] cmds = program.getNextInstances(null);
		for( CommandInstance cmd : cmds ) 
			actProc.startAction(new CommandDispatcherAction(initialStateTemplate.makeCopy(cmds.length > 1), cmd, program, 0) );
		Main.processorLoop(actProc, true);
	}
}
