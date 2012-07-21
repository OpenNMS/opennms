package org.opennms.features.topology.api;

import com.vaadin.Application;
import com.vaadin.ui.Component;

/**
 * A simple view UI contribution
 */
public interface IViewContribution {
	
	public Component getView(Application application);

}
