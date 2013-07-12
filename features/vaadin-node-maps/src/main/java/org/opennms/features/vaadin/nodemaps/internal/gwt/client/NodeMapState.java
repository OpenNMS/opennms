package org.opennms.features.vaadin.nodemaps.internal.gwt.client;

import com.vaadin.shared.AbstractComponentState;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Marcus Hellberg (marcus@vaadin.com)
 */
public class NodeMapState extends AbstractComponentState {

    public String initialSearch;
    public List<MapNode> nodes = new LinkedList<MapNode>();
}
