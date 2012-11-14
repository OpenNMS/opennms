package org.opennms.features.topology.app.internal;

import org.opennms.features.topology.api.Layout;

public class DefaultLayout implements Layout {

	private SimpleGraphContainer m_simpleGraphContainer;

	public DefaultLayout(SimpleGraphContainer simpleGraphContainer) {
		m_simpleGraphContainer = simpleGraphContainer;
	}

	@Override
	public int getX(Object vertexId) {
		return m_simpleGraphContainer.getX(vertexId);
	}

	@Override
	public int getY(Object vertexId) {
		return m_simpleGraphContainer.getY(vertexId);
	}

}
