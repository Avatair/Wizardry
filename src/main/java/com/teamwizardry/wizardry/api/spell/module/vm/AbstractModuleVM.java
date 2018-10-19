package com.teamwizardry.wizardry.api.spell.module.vm;

import javax.annotation.Nonnull;

import com.teamwizardry.wizardry.api.spell.SpellData;
import com.teamwizardry.wizardry.api.spell.SpellRing;
import com.teamwizardry.wizardry.api.spell.module.IModule;

public abstract class AbstractModuleVM implements IModule {
	
	@Override
	public final boolean ignoreResultForRendering(@Nonnull SpellData spell, @Nonnull SpellRing spellRing) {
		// TODO: Test if child is not a VM Module. In this case it should favor children traversing
		// TODO: Disable for an active VM Module. Active means, that some hooks have been overridden during initialization.
		return defaultIgnoreResultForRendering(spell, spellRing);
	}
	
	public boolean defaultIgnoreResultForRendering(@Nonnull SpellData spell, @Nonnull SpellRing spellRing) {
		return false;
	}

	public void appendConfiguration(ScriptKey conf, @Nonnull SpellRing spellRing) {
		// Override me.
	}

}
