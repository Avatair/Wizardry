package com.teamwizardry.wizardry.lib.vm.command.program;

import com.teamwizardry.wizardry.lib.vm.command.ICommand;
import com.teamwizardry.wizardry.lib.vm.command.ICommandGenerator;

public abstract class CommandBuildResult {
	static class CommandTargetBuildResult<T> extends CommandBuildResult {
		private final ICommand commandData;
		private final T nextContext;
		private final int nextAmountFollowers;
		
		CommandTargetBuildResult(ICommand commandData, T nextContext, int nextAmountFollowers) {
			super();
			this.commandData = commandData;
			this.nextContext = nextContext;
			this.nextAmountFollowers = nextAmountFollowers;
		}
		
		public ICommand getCommandData() {
			return commandData;
		}

		public T getNextContext() {
			return nextContext;
		}

		public int getNextAmountFollowers() {
			return nextAmountFollowers;
		}
	}
	
	static class CommandGenTargetBuildResult extends CommandBuildResult {
		private final ICommandGenerator commandGenerator;
		
		CommandGenTargetBuildResult(ICommandGenerator commandGenerator) {
			super();
			this.commandGenerator = commandGenerator;
		}

		public ICommandGenerator getCommandGenerator() {
			return commandGenerator;
		}
	}
}
