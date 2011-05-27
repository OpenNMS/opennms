package org.opennms.netmgt.model.updates;

import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;

public class CategoryUpdate {

	private NodeUpdate m_nodeUpdate;
	private MonitoredServiceUpdate m_monitoredServiceUpdate;
	private final String m_categoryName;

	public CategoryUpdate(final String categoryName) {
		m_categoryName = categoryName;
	}

	public CategoryUpdate(final NodeUpdate nodeUpdate, final String categoryName) {
		m_nodeUpdate = nodeUpdate;
		m_categoryName = categoryName;
	}

	public CategoryUpdate(final MonitoredServiceUpdate monitoredServiceUpdate, final String categoryName) {
		m_monitoredServiceUpdate = monitoredServiceUpdate;
		m_categoryName = categoryName;
	}

	public String getCategoryName() {
		return m_categoryName;
	}

	public void apply(final OnmsNode node) {
		OnmsCategory category = node.getCategoryByName(getCategoryName());
		if (category == null) {
			category = new OnmsCategory(getCategoryName());
		}
		node.addCategory(category);
	}
}
