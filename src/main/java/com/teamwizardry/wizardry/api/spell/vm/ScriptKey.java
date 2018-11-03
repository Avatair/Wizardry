package com.teamwizardry.wizardry.api.spell.vm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScriptKey {
	private final ArrayList<String> scripts = new ArrayList<>();
	
	ScriptKey() {
		// Only initializable within package
	}
	
	public void appendScript(String scriptName) {
		scripts.add(scriptName);
	}
	
	public List<String> getScripts() {
		return Collections.unmodifiableList(scripts);
	}
	
	//
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((scripts == null) ? 0 : scripts.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScriptKey other = (ScriptKey) obj;
		if (scripts == null) {
			if (other.scripts != null)
				return false;
		} else if (!scripts.equals(other.scripts))
			return false;
		return true;
	}
}
