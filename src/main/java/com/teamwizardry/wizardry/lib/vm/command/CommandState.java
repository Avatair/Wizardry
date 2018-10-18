package com.teamwizardry.wizardry.lib.vm.command;

public class CommandState {
	private final CommandInstance cmd;
	private int timeToLive;

	public CommandState(CommandInstance cmd, int timeToLive) {
		this.cmd = cmd;
		this.timeToLive = timeToLive;
	}

	public CommandInstance getInstance() {
		return cmd;
	}

	public int getTimeToLive() {
		return timeToLive;
	}
	
	public void setTimeToLive(int timeToLive) {
		this.timeToLive = timeToLive;
	}
}
