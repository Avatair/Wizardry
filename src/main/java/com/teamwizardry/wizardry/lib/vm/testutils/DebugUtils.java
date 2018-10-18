package com.teamwizardry.wizardry.lib.vm.testutils;

import java.util.HashSet;
import java.util.Set;

public class DebugUtils {
	private static Set<String> disabledMessageAreas = new HashSet<String>();
	
	public static void disableArea(String area) {
		disabledMessageAreas.add(area.toLowerCase());
	}
	
	public static void enableArea(String area) {
		disabledMessageAreas.remove(area.toLowerCase());
	}
	
	public static void printDebug(String area, String message) {
		if( disabledMessageAreas.contains(area.toLowerCase()) )
			return;
		System.out.println(message);
	}
	
	public static void printDebug(String area, String message, Throwable thr) {
		if( disabledMessageAreas.contains(area.toLowerCase()) )
			return;
		System.err.println(message);
		thr.printStackTrace();
	}
	
}
