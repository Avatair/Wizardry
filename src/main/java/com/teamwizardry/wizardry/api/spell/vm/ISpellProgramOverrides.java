package com.teamwizardry.wizardry.api.spell.vm;

import com.teamwizardry.wizardry.api.spell.annotation.ModuleOverrideInterface;

public interface ISpellProgramOverrides {
	@ModuleOverrideInterface("generic_append_script")
	void appendScript(ScriptKey key);
}
