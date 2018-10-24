package com.teamwizardry.wizardry.lib.vm.command.utils;

import com.teamwizardry.wizardry.lib.vm.ActionProcessor;
import com.teamwizardry.wizardry.lib.vm.command.CommandDispatcherAction;
import com.teamwizardry.wizardry.lib.vm.command.CommandInstance;
import com.teamwizardry.wizardry.lib.vm.command.ICommandGenerator;
import com.teamwizardry.wizardry.lib.vm.command.operable.ICommandOperable;
import com.teamwizardry.wizardry.lib.vm.command.program.factory.ProgramSequence;
import com.teamwizardry.wizardry.lib.vm.command.program.UnsatisfiedLinkException;
import com.teamwizardry.wizardry.lib.vm.command.program.factory.ProgramBuilder;

public class RunUtils {
	private RunUtils() {}
	
	public static ICommandGenerator compileProgram(String entryPoint, ProgramSequence ... config) throws UnsatisfiedLinkException {
		ProgramBuilder builder = new ProgramBuilder();
		for( ProgramSequence seq : config ) {
			builder.importProgram(seq);
		}
		builder.beginMainFrame().addFrameCall(entryPoint).endFrame();
		
		return builder.build();
	}
	
	public static void runProgram(ICommandOperable initialStateTemplate, String entryPoint, ProgramSequence ... config) throws UnsatisfiedLinkException {
		ICommandGenerator program = compileProgram(entryPoint, config);
		runProgram(initialStateTemplate, program);
	}
	
	public static ActionProcessor runProgram(ICommandOperable initialStateTemplate, ICommandGenerator program) {
		ActionProcessor actProc = new ActionProcessor();
		
		CommandInstance[] cmds = program.getNextInstances(null);
		for( CommandInstance cmd : cmds ) 
			actProc.startAction(new CommandDispatcherAction(initialStateTemplate.makeCopy(true), cmd, program, 0) );
		processorLoop(actProc);
		
		return actProc;
	}
	
	public static void processorLoop(ActionProcessor actProc) {
		boolean bTicked = false;
		int tickCounter = 1;
		do {
			DebugUtils.printDebug("TICK", "TICK " + tickCounter + ":");
			bTicked = actProc.updateTick();
			if( !bTicked )
				DebugUtils.printDebug("TICK", "\tFINAL!");
			tickCounter ++;
		}
		while( bTicked );
	}
}
