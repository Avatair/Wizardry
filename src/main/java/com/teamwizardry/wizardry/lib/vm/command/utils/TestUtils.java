package com.teamwizardry.wizardry.lib.vm.command.utils;

import com.teamwizardry.wizardry.lib.vm.ActionProcessor;
import com.teamwizardry.wizardry.lib.vm.command.CommandDispatcherAction;
import com.teamwizardry.wizardry.lib.vm.command.CommandInstance;
import com.teamwizardry.wizardry.lib.vm.command.ICommandGenerator;
import com.teamwizardry.wizardry.lib.vm.command.operable.ICommandOperable;
import com.teamwizardry.wizardry.lib.vm.command.program.factory.ProgramSequence;
import com.teamwizardry.wizardry.lib.vm.command.program.factory.ProgramSequenceBuilder;

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
		processorLoop(actProc, true);
	}
	
	public static void processorLoop(ActionProcessor actProc, boolean bPrint) {
		boolean bTicked = false;
		int tickCounter = 1;
		do {
			if( bPrint )
				DebugUtils.printDebug("TICK", "TICK " + tickCounter + ":");
			bTicked = actProc.updateTick();
			if( !bTicked && bPrint )
				DebugUtils.printDebug("TICK", "\tFINAL!");
			tickCounter ++;
		}
		while( bTicked );
	}
}
