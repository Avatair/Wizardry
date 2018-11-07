package com.teamwizardry.wizardry.api.spell.vm;

public class NullObject {
	
	public static final NullObject ME = new NullObject(); 
	
	private NullObject() {}

	public String toString() {
		return "*null*";
	}
}
