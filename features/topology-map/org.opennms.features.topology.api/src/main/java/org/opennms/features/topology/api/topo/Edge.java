package org.opennms.features.topology.api.topo;

import com.vaadin.data.Item;

public interface Edge extends EdgeRef {

	String getKey();

	Item getItem();

	Connector getSource();

	Connector getTarget();

	String getLabel();

	String getTooltipText();

	String getStyleName();

}
