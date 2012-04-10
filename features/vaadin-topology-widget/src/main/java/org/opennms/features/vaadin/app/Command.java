package org.opennms.features.vaadin.app;

public interface Command {
	
	public void doCommand();
	
	public void undoCommand();

}
