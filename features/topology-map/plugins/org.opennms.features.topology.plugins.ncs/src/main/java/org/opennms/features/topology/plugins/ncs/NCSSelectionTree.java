package org.opennms.features.topology.plugins.ncs;

import org.opennms.features.topology.api.support.FilterableHierarchicalContainer;
import org.opennms.features.topology.api.support.SelectionTree;

public class NCSSelectionTree extends SelectionTree {

	private static final long serialVersionUID = 8778577903128733601L;

	public NCSSelectionTree(FilterableHierarchicalContainer container) {
		super(container);
	}

	@Override
	public String getTitle() {
		return "Services";
	}

}
