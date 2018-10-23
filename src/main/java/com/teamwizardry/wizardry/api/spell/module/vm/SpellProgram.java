package com.teamwizardry.wizardry.api.spell.module.vm;

import java.io.IOException;
import java.util.List;

import com.teamwizardry.wizardry.lib.vm.command.ICommandGenerator;
import com.teamwizardry.wizardry.lib.vm.command.program.factory.MagicScriptBuilder;
import com.teamwizardry.wizardry.lib.vm.command.program.factory.ProgramSequence;
import com.teamwizardry.wizardry.lib.vm.command.utils.RunUtils;
import com.teamwizardry.wizardry.lib.vm.utils.parser.ScriptParserException;

public class SpellProgram {
	private static final String GENERICS_SOURCE = "/assets/wizardry/modules/scripts/generics.mgs";

	private static ProgramSequence generics = null;
	
	private final ScriptKey configuration;
	private WizardryOperable initialState = null;
	private ICommandGenerator initRoutine;
	
	SpellProgram(ScriptKey configuration) {
		// Only initializable from ProgramCache
		
		this.configuration = configuration;
	}

	void initProgram() {
		// NOTE: If exceptions are thrown. Don't quit minecraft!
		
		initRoutine = null;
		initialState = null;
		
		// Load scripts
		try {
			ProgramSequence[] assemblies = loadSources();
			
			initRoutine = RunUtils.compileProgram("initMain", assemblies);
			// TODO: Add more routines here ...
			
			initialState = new WizardryOperable();
			RunUtils.runProgram(initialState, initRoutine);	// TODO: Handle exceptions
		} catch (Exception e) {
			
			// TODO: Handle proper way!
			e.printStackTrace();
		}
	}
	
	public boolean runProgram(ExecutionPhase phase) {
		if( initialState == null )
			return false; // If something was unsuccessful when loading.
		
		if( ExecutionPhase.INITIALIZATION.equals(phase) ) {
/*			if( initRoutine == null )
				throw new IllegalStateException("Init routine must exist.");
			initialState = new WizardryOperable();
			RunUtils.runProgram(initialState, initRoutine);
			
			// TODO: Process state output */
		}
		else
			throw new IllegalArgumentException("Unknown execution phase " + phase);
		
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
}
