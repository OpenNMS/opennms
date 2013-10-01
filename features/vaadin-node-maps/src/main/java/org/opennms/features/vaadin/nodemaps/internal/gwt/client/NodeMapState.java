package org.opennms.features.vaadin.nodemaps.internal.gwt.client;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.vaadin.shared.AbstractComponentState;

/**
 * @author Marcus Hellberg (marcus@vaadin.com)
 */
public class NodeMapState extends AbstractComponentState {
    private static final long serialVersionUID = 1457336633817744522L;
    public String searchString;
    public List<MapNode> nodes = new LinkedList<MapNode>();
    public List<Integer> nodeIds = new ArrayList<Integer>();
    public int minimumSeverity;
}
