package com.teamwizardry.wizardry.api.spell.vm;

public class SpellProgramException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 54607909326530966L;

	public SpellProgramException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public SpellProgramException(String message, Exception[] vmExceptions) {
		super(message);
		
		if( vmExceptions.length == 1 ) {
			initCause(vmExceptions[0]);
		}
		else {
			// TODO: ...
		}
	}

	public SpellProgramException(String message) {
		super(message);
	}

}
