package com.teamwizardry.wizardry.api.spell.vm;

import java.io.IOException;
import java.util.List;

import com.teamwizardry.wizardry.api.spell.SpellRing;
import com.teamwizardry.wizardry.lib.vm.Action;
import com.teamwizardry.wizardry.lib.vm.ActionProcessor;
import com.teamwizardry.wizardry.lib.vm.command.ICommandGenerator;
import com.teamwizardry.wizardry.lib.vm.command.operable.IMagicCommandOperable;
import com.teamwizardry.wizardry.lib.vm.command.program.factory.MagicScriptBuilder;
import com.teamwizardry.wizardry.lib.vm.command.program.factory.ProgramSequence;
import com.teamwizardry.wizardry.lib.vm.command.utils.RunUtils;
import com.teamwizardry.wizardry.lib.vm.utils.parser.ScriptParserException;

public class SpellProgramHandler {
	private static final String GENERICS_SOURCE = "/assets/wizardry/modules/scripts/generics.mgs";

	private static final String ROUTINE_INITMAIN = "initMain";

	private static final String HOOK_RUNSPELL = "hooks.onCasted";

	private static ProgramSequence generics = null;
	
	private final ScriptKey configuration;
	private WizardryOperable initialState = null;
	
	private ICommandGenerator initRoutine;
	private ICommandGenerator runRoutine;
	
	public SpellProgramHandler(SpellRing spellChain) {
		if( spellChain.getParentRing() != null )
			throw new IllegalArgumentException("passed spellRing is not a root.");
		
		// Should be only initialized within a SpellRing
		this.configuration = scanProgramConfiguration( spellChain );
		
		initProgram();
	}

	private void initProgram() {
		// NOTE: If exceptions are thrown. Don't quit minecraft!

		initialState = null;

		initRoutine = null;
		runRoutine = null;
		
		// Load scripts
		try {
			ProgramSequence[] assemblies = loadSources();
			
			initRoutine = RunUtils.compileProgram(ROUTINE_INITMAIN, assemblies);
			
			// Call initialization
			initialState = new WizardryOperable();
			ActionProcessor proc = RunUtils.runProgram(initialState, initRoutine);	// TODO: Handle exceptions
			logExceptions(proc);
			
			// Retrieve hooks
			String hook = getValue_String(initialState, HOOK_RUNSPELL, null);
			if( hook != null ) {
				runRoutine = RunUtils.compileProgram(hook, assemblies);
			}
			
			// TODO: Add more routines here ...

		} catch (Exception e) {
			initialState = null;
			
			// TODO: Handle proper way!
			e.printStackTrace();
		}
	}
	
	public boolean runProgram(ExecutionPhase phase) {
		if( initialState == null )
			return false; // If something was unsuccessful when loading.
		
		ActionProcessor proc;
		if( ExecutionPhase.RUN_TICK.equals(phase) ) {
			proc = RunUtils.runProgram(initialState, runRoutine);
		}
		else
			throw new IllegalArgumentException("Unknown execution phase " + phase);
		logExceptions(proc);
		
		return true;
	}
	
	private ProgramSequence[] loadSources() throws IOException, ScriptParserException {
		List<String> scripts = configuration.getScripts();
		
		ProgramSequence generics = getGenericsSource();
		
		if( scripts.size() > 0 ) {
			ProgramSequence[] result = new ProgramSequence[scripts.size()];
			for( int i = 0; i < scripts.size(); i ++ ) {
				String scriptFile = scripts.get(i);
	// TODO:			ResourceLocation loc = new ResourceLocation(scriptFile);
				result[i] = MagicScriptBuilder.createFromResource(scriptFile).provideDependency("generics", generics).build();
			}
			return result;
		}
		else {
			return new ProgramSequence[] {generics};
		}
	}
	
	private static ProgramSequence getGenericsSource() throws ScriptParserException, IOException {
		if( generics == null ) {
			generics = MagicScriptBuilder.createFromResource(GENERICS_SOURCE).build();
		}
		return generics;
	}

	private static ScriptKey scanProgramConfiguration(SpellRing spellRing) {
		ScriptKey key = new ScriptKey();

		ISpellProgramOverrides overrides = spellRing.getOverrideHandler().getConsumerInterface(ISpellProgramOverrides.class);
		overrides.appendScript(key);
		
		return key;
	}
	
	// TODO: Move to utils, all below!
	
	private static void logExceptions(ActionProcessor proc) {
		for( Action failedAction : proc.getFailedActions() ) {
			Exception exc = failedAction.getException();
			
			exc.printStackTrace();  // TODO: Handle proper way!
		}
	}
	
	private static String getValue_String(IMagicCommandOperable operable, String key, String defaultValue) {
		Object value = operable.getValue(key);
		if( value == null )
			return defaultValue;
		return value.toString();
	}
	

}
