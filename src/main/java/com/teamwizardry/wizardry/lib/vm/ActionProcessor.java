package com.teamwizardry.wizardry.lib.vm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.teamwizardry.wizardry.lib.vm.command.utils.DebugUtils;

public class ActionProcessor {
	private HashMap<Integer, Action> registry = new HashMap<Integer, Action>();
	private ArrayList<Action> actionQueue = new ArrayList<Action>();
	private LinkedList<Integer> newActionIDs = new LinkedList<Integer>();
	private LinkedList<Action> failedActions = new LinkedList<Action>();
	private int nextID = 1;
	
	private boolean bIsActive;
	
	public ActionProcessor() {
	}
	
	public int startAction(Action act) {
		if( act.registeredAt != null )
			throw new IllegalArgumentException("Action is already registered.");
		if( act.isDead() )
			throw new IllegalArgumentException("Action is dead.");
		act.registerID = nextID;
		act.registeredAt = this;		
		registry.put(nextID, act);
		newActionIDs.add(nextID);
		return nextID ++;
	}
	
	public boolean updateTick() {
		bIsActive = false;
		
		// Add new actions to queue
		for( Integer actID : newActionIDs ) {
			Action act = registry.get(actID);
			actionQueue.add(act);
			
			callHandler(act, ActionEventType.STARTED);
		}
		newActionIDs.clear();
		
		// process current actions
		LinkedList<Action> deadEntries = new LinkedList<Action>();
		for( Action act : actionQueue ) {
			if( act.isDead() ) {
				deadEntries.add( act );
				continue;
			}
			callHandler(act, ActionEventType.UPDATE_TICK);
		}
		
		// remove dead actions
		for( Action act : deadEntries ) {
			callHandler(act, ActionEventType.FINALIZED);
			actionQueue.remove(act);

			act.registeredAt = null;
			registry.remove(act.getID());
		}
		
		return bIsActive;
	}

	void callHandler(Action act, ActionEventType evtType) {
		try {
			if( evtType != ActionEventType.EXCEPTION )
				bIsActive |= act.handleEvent(evtType);
			else
				act.handleExceptionEvent(act.getException());
		}
		catch(Exception exc) {
			Exception storedException = act.getException();
			if( evtType == ActionEventType.EXCEPTION || storedException != null ) {
				DebugUtils.printDebug("FAULT", "Second exception occurred for action:", exc);
				if( storedException != null )
					DebugUtils.printDebug("FAULT", "First exception was:", storedException);
				else
					DebugUtils.printDebug("FAULT", "First exception is unknown.");	// NOTE: Should never occur if Action.dieByException() is
																					//       the only place to kill an action by exception
			}
			else {
				act.dieByException(exc);
				failedActions.add(act);
			}
		}
	}
	
	public List<Action> getFailedActions() {
		return Collections.unmodifiableList(failedActions);
	}
	
	public void clearFailedActions() {
		failedActions.clear();
	}
}
