package com.teamwizardry.wizardry.api.spell.module;

import javax.annotation.Nullable;

/**
 * Base interface for all spell module implementations. <br />
 * <b>NOTE</b>: Shouldn't be derived directly from. Instead use one of interface derivatives from same package.
 * 
 * @see {@link IModuleEffect} for effect modules
 * @see {@link IModuleModifier} for modifier modules
 * @see {@link IModuleShape} for shape modules
 * @see {@link IModuleEvent} for event modules
 * @author Avatair
 */
public interface IModule {

	/**
	 * Specify all applicable modifiers that can be applied to this module.
	 *
	 * @return Any set with IDs of applicable IModuleModifier instances.
	 */
	@Nullable
	default String[] compatibleModifiers() {
		return null;
	}
	
	default boolean ignoreResultForRendering() {
		return false;
	}
	
	/**
	 * Returns if the module is enabled and all derived module instances should appear in the worktable 
	 * 
	 * @return <code>true</code> iff yes.
	 */
	default boolean isAvailable() {
		return true;
	}
}
