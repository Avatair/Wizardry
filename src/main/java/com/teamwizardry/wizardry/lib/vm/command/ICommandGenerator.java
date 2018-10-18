package com.teamwizardry.wizardry.lib.vm.command;

public interface ICommandGenerator {

	CommandInstance[] getNextInstances(CommandInstance curNode);

}
