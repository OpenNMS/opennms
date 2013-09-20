package org.opennms.features.vaadin.nodemaps.internal.gwt.client;

import java.util.LinkedList;
import java.util.List;

import com.vaadin.shared.AbstractComponentState;

/**
 * @author Marcus Hellberg (marcus@vaadin.com)
 */
public class NodeMapState extends AbstractComponentState {
    private static final long serialVersionUID = -5049901629584902848L;
    public String initialSearch;
    public List<MapNode> nodes = new LinkedList<MapNode>();
}
