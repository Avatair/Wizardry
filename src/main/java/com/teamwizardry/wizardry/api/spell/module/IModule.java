package com.teamwizardry.wizardry.api.spell.module;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.teamwizardry.wizardry.api.spell.SpellData;
import com.teamwizardry.wizardry.api.spell.SpellRing;

public interface IModule {

	/**
	 * Specify all applicable modifiers that can be applied to this module.
	 *
	 * @return Any set with applicable ModuleModifiers.
	 */
	@Nullable
	default IModuleModifier[] applicableModifiers() {
		return null;
	}
	
	default boolean ignoreResultForRendering(@Nonnull SpellData spell, @Nonnull SpellRing spellRing) {
		return false;
	}

	/**
	 * A lower case snake_case string id that reflects the module to identify it during serialization/deserialization.
	 *
	 * @return A lower case snake_case string.
	 */
	String getClassID();
}
