package com.teamwizardry.wizardry.lib.vm.command.utils;

import java.util.HashSet;
import java.util.Set;

public class DebugUtils {
	private static Set<String> enabledMessageAreas = new HashSet<String>();

	private DebugUtils() {}
	
	public static void disableArea(String area) {
		enabledMessageAreas.remove(area.toLowerCase());
	}
	
	public static void enableArea(String area) {
		enabledMessageAreas.add(area.toLowerCase());
	}
	
	public static void disableAll() {
		enabledMessageAreas.clear();
	}
	
	public static void printDebug(String area, String message) {
		if( !enabledMessageAreas.contains(area.toLowerCase()) )
			return;
		System.out.println(message);
	}
	
	public static void printDebug(String area, String message, Throwable thr) {
		if( !enabledMessageAreas.contains(area.toLowerCase()) )
			return;
		System.err.println(message);
		thr.printStackTrace();
	}
	
}
