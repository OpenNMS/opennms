package org.opennms.features.topology.api;

import com.vaadin.terminal.Resource;
import com.vaadin.ui.Component;

/**
 * A simple view UI contribution
 */
public interface IViewContribution {
	
	public Component getView();
	public String getTitle();
	public Resource getIcon();

}
