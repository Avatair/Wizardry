package com.teamwizardry.wizardry.common.network;

import com.teamwizardry.librarianlib.features.network.PacketBase;
import com.teamwizardry.librarianlib.features.saving.Save;
import com.teamwizardry.wizardry.api.spell.module.ModuleInstance;
import com.teamwizardry.wizardry.api.spell.module.ModuleRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nonnull;
import java.util.ArrayList;

/**
 * Created by Demoniaque.
 */
public class PacketSyncModules extends PacketBase {

	@Save
	public ArrayList<ModuleInstance> modules;

	public PacketSyncModules() {
	}

	public PacketSyncModules(ArrayList<ModuleInstance> modules) {
		this.modules = modules;
	}

	@Override
	public void handle(@Nonnull MessageContext messageContext) {
		ModuleRegistry.INSTANCE.modules = modules;
	}
}
