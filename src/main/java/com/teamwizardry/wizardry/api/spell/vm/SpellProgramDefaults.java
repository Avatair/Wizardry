package com.teamwizardry.wizardry.api.spell.vm;

import com.teamwizardry.wizardry.api.spell.annotation.ModuleOverride;
import com.teamwizardry.wizardry.api.spell.annotation.RegisterOverrideDefaults;

@RegisterOverrideDefaults
public class SpellProgramDefaults {
	
	@ModuleOverride("generic_append_script")
	public void appendScript(ScriptKey key) {
		// Override me!
	}
}
