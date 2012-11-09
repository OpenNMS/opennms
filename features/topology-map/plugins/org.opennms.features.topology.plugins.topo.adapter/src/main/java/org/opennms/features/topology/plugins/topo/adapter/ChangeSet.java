package org.opennms.features.topology.plugins.topo.adapter;

import java.util.List;

class ChangeSet {
	private List<String> m_added;
	private List<String> m_updated;
	private List<String> m_removed;
	public ChangeSet(List<String> added, List<String> updated, List<String> removed) {
		m_added = added;
		m_updated = updated;
		m_removed = removed;
	}
	public List<String> getAddedIds() { return m_added; }
	public List<String> getUpdatedIds() { return m_updated; }
	public List<String> getRemovedIds() { return m_removed; }
}