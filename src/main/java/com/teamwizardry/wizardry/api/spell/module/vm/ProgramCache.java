package com.teamwizardry.wizardry.api.spell.module.vm;

import java.util.HashMap;

public class ProgramCache {
	
	private HashMap<ScriptKey, SpellProgram> cache = new HashMap<>();
	
	public static final ProgramCache INSTANCE = new ProgramCache(); 
	
	private ProgramCache() {
	}
	
	public SpellProgram getProgram(ScriptKey key) {
		SpellProgram program = cache.get(key);
		if( program == null ) {
			program = new SpellProgram(key);
			cache.put(key, program);

			// Initialize program ...
			program.compileProgram();
			program.runProgram(ExecutionPhase.INITIALIZATION);
		}
		
		return program;
	}
	
}