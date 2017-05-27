package com.teamwizardry.wizardry.client.gui.worktable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.teamwizardry.librarianlib.features.gui.GuiBase;
import com.teamwizardry.librarianlib.features.gui.GuiComponent;
import com.teamwizardry.librarianlib.features.gui.components.ComponentGrid;
import com.teamwizardry.librarianlib.features.gui.components.ComponentSprite;
import com.teamwizardry.librarianlib.features.gui.components.ComponentVoid;
import com.teamwizardry.librarianlib.features.sprite.Sprite;
import com.teamwizardry.librarianlib.features.sprite.Texture;
import com.teamwizardry.wizardry.Wizardry;
import com.teamwizardry.wizardry.api.spell.Module;
import com.teamwizardry.wizardry.api.spell.ModuleRegistry;
import com.teamwizardry.wizardry.api.spell.ModuleType;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

/**
 * Created by Saad on 6/17/2016.
 */
public class WorktableGui extends GuiBase {
	private static final Texture BACKGROUND_TEXTURE = new Texture(new ResourceLocation(Wizardry.MODID, "textures/gui/worktable/table_background.png"));
	private static final Sprite BACKGROUND_SPRITE = BACKGROUND_TEXTURE.getSprite("bg", 480, 224);

	ComponentVoid paper;
	BiMap<GuiComponent, UUID> paperComponents = HashBiMap.create();
	HashMultimap<GuiComponent, UUID> componentLinks = HashMultimap.create();

	public WorktableGui() {
		super(480, 224);

		ComponentSprite background = new ComponentSprite(BACKGROUND_SPRITE, 0, 0);
		getMainComponents().add(background);

		paper = new ComponentVoid(180, 19, 180, 188);
		getMainComponents().add(paper);

		ComponentVoid effects = new ComponentVoid(32, 35, 48, 80);
		addModules(effects, ModuleType.SHAPE);
		getMainComponents().add(effects);

		ComponentVoid shapes = new ComponentVoid(96, 35, 48, 80);
		addModules(effects, ModuleType.EFFECT);
		getMainComponents().add(shapes);

		ComponentVoid events = new ComponentVoid(32, 127, 48, 80);
		addModules(events, ModuleType.EVENT);
		getMainComponents().add(events);

		ComponentVoid modiifers = new ComponentVoid(96, 127, 48, 80);
		addModules(modiifers, ModuleType.MODIFIER);
		getMainComponents().add(modiifers);

		ComponentVoid selected = new ComponentVoid(0, 0, 16, 16);
		selected.addTag("selected");
		paper.add(selected);
	}

	private void addModules(ComponentVoid parent, ModuleType type) {
		ComponentGrid grid = new ComponentGrid(0, 0, 16, 16, 3);
		parent.add(grid);

		for (Module module : ModuleRegistry.INSTANCE.getModules(type)) {
			TableModule item = new TableModule(this, module, false);
			grid.add(item.component);
		}
	}

	@Override
	public boolean doesGuiPauseGame() {
		return true;
	}
}
