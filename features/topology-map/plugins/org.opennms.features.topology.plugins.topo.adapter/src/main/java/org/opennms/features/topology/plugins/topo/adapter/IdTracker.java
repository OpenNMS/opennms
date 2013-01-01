package org.opennms.features.topology.plugins.topo.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

class IdTracker {
	Map<String, Object> m_id2ItemId = new HashMap<String , Object>();
	Map<Object, String> m_itemId2Id = new HashMap<Object, String>();
	
	public IdTracker(Collection<?> itemIds) {
		for (Object itemId : itemIds) {
			getId(itemId);
		}
	}
	
	private String findAvailableId(Object itemId) {
		// first try toStringing.. works well with string ids
		String idCandidate = itemId.toString();
		
		if (m_id2ItemId.containsKey(idCandidate)) {
			// if that fails add the hashCode to the end.. .should make them unique
			idCandidate = idCandidate+System.identityHashCode(itemId);
		}
		
		// just in case append underscores until we find one
		while(m_id2ItemId.containsKey(idCandidate)) {
			idCandidate += "_";
		}
		

		return idCandidate;
	}
	
	private synchronized String removeItemId(Object itemId) {
		String id = m_itemId2Id.remove(itemId);
		if (id != null) {
			m_id2ItemId.remove(id);
		}
		return id;
	}
	
	public synchronized String getId(Object itemId) {
		String id = m_itemId2Id.get(itemId);
		if (id == null) {
			id = findAvailableId(itemId);
			m_itemId2Id.put(itemId, id);
			m_id2ItemId.put(id, itemId);
		}
		return id;
	}
	
	public synchronized Object getItemId(String id) {
		return m_id2ItemId.get(id);
	}
	
	
	public synchronized ChangeSet setItemIds(Collection<?> itemIds) {
		final List<String> added = new ArrayList<String>();
		final List<String> updated = new ArrayList<String>();
		final List<String> removed = new ArrayList<String>();
		
		HashSet<Object> existing = new HashSet<Object>(m_id2ItemId.keySet());
		for(Object itemId : itemIds) {
			if (existing.contains(itemId)) {
				updated.add(getId(itemId));
				existing.remove(itemId);
			} else {
				added.add(getId(itemId));
			}
		}
		
		for(Object itemId : existing) {
			String id = removeItemId(itemId);
			removed.add(id);
		}
		
		return new ChangeSet(added, updated, removed);

	}
	

}