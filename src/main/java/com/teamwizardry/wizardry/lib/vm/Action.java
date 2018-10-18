package com.teamwizardry.wizardry.lib.vm;

public abstract class Action {
	ActionProcessor registeredAt = null;
	int registerID = 0;

	private Exception diedByException = null;
	private DeadState deadState = DeadState.ALIVE;
	
	public Action() {
	}

	public abstract boolean handleEvent(ActionEventType type);	// Is not called for ActionEventType.EXCEPTION. Use handleExceptionEvent instead

	public void handleExceptionEvent(Exception exc) {
		exc.printStackTrace();
	}
	
	protected final void startAction(Action act) {
		if( registeredAt == null )
			throw new IllegalArgumentException("No processor available.");
		registeredAt.startAction(act);
	}
	
	protected final void stopThisAction() {
		setDead(DeadState.FINISHED);
	}
	
	public final int getID() {
		return registerID;
	}
	
	public final Exception getException() {
		return diedByException;
	}

	public final boolean isDead() {
		return deadState != DeadState.ALIVE;
	}
	
	public final boolean isActive() {
		return !isDead() && registeredAt != null;
	}
	
	public final void interruptAction() {
		registeredAt.callHandler(this, ActionEventType.INTERRUPTED);
		setDead(DeadState.INTERRUPTED);
	}
	
	public final void dieByException( Exception exc ) {
		diedByException = exc;
		registeredAt.callHandler(this, ActionEventType.EXCEPTION);
		setDead(DeadState.EXCEPTION);
	}
	
	private final void setDead( DeadState reason ) {
		if( reason == DeadState.ALIVE )
			throw new IllegalArgumentException("Reason ALIVE is not a valid dead state.");
		registeredAt.callHandler(this, ActionEventType.STOPPED);
		deadState = reason;
	}

	public final ActionProcessor getRegisteredAt() {
		return registeredAt;
	}
}
