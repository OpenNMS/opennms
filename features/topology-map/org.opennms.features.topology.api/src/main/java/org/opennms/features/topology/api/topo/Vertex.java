package org.opennms.features.topology.api.topo;

import com.vaadin.data.Item;

public interface Vertex extends VertexRef {

	/**
	 * @deprecated Use namespace/id tuple
	 */
	String getKey();

	Item getItem();

	String getLabel();

	String getTooltipText();

	String getIconKey();

	String getStyleName();

	boolean isLeaf();

	/**
	 * TODO: To support Many-to-Many grouping, this function will need to be
	 * enhanced add to a list of parents.
	 */
	void setParent(VertexRef parent);

	/**
	 * TODO: To support Many-to-Many grouping, this function will need to be
	 * enhanced to return an array of vertices.
	 */
	VertexRef getParent();

	Integer getX();

	Integer getY();

	boolean isLocked();

	boolean isSelected();

	String getIpAddress();

	Integer getNodeID();
}
