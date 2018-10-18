package com.teamwizardry.wizardry.lib.vm.command.program.factory;

import com.teamwizardry.wizardry.lib.vm.command.ICommand;
import com.teamwizardry.wizardry.lib.vm.command.ICommandGenerator;

public interface IProgramSequence {

	IProgramSequence importProgram(ProgramSequence other);
	IProgramSequence addFrameAlias(String aliasName, String asFrameName);
	
//	IScriptedSequence injectCallAtLabel(String frameName, String labelName, String toFrameName);
	
	IProgramSequence beginFrame(String frameName);
	IProgramSequence editFrame(String frameName);
	IProgramSequence seekTo(String labelName, int offset);
	IProgramSequence endFrame();
	
	IProgramSequence addCommand(ICommand cmd);
	IProgramSequence assignFork(String ... toLabels);
	IProgramSequence addForkTo(String ... toLabels);
	IProgramSequence addLabel(String labelName);
	IProgramSequence addFrameCall(String frameName);
	IProgramSequence addReturn();
	IProgramSequence addReturn(int toForkIdx);
	IProgramSequence addStop(boolean isInt3);
	IProgramSequence addNop(int countTicks);
	IProgramSequence addCallGenerator(ICommandGenerator commandGenerator);

	boolean hasOpenFrame();
}
