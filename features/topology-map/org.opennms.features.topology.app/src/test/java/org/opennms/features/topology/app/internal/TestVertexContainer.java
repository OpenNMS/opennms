/**
 * 
 */
package org.opennms.features.topology.app.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.opennms.features.topology.api.VertexContainer;

public class TestVertexContainer extends VertexContainer<String, TestVertex> {
	public TestVertexContainer() {
		super(TestVertex.class);
		setBeanIdProperty("id");
	}
	
	public void fireLayoutChange() {
		fireItemSetChange();
	}

	public boolean areChildrenAllowed(Object itemId) {
	    assertVertex(itemId);
		if (!containsId(itemId)) return false;
		TestVertex v = getItem(itemId).getBean();
		return !v.isLeaf();
	}

	private void assertVertex(Object itemId) {
	    assertTrue(containsId(itemId));
    }

    public Collection<?> getChildren(Object itemId) {
        assertVertex(itemId);
		if (!containsId(itemId)) return Collections.EMPTY_LIST;
		TestVertex v = getItem(itemId).getBean();
		if (v.isLeaf()) {
			return Collections.EMPTY_LIST;
		}
		else {
			TestGroup g = (TestGroup)v;
			List<String> memberIds = new ArrayList<String>();
			for(TestVertex member : g.getMembers()) {
				memberIds.add(member.getId());
			}
			return memberIds;
		}
	}

	public Object getParent(Object itemId) {
	    assertVertex(itemId);
		if (!containsId(itemId)) return null;
		
		TestGroup g = getItem(itemId).getBean().getParent();
		return g == null ? null : g.getId();
	}

	public boolean hasChildren(Object itemId) {
	    assertVertex(itemId);
		if (!containsId(itemId)) return false;
		TestVertex v = getItem(itemId).getBean();
		return !v.isLeaf();
	}

	public boolean isRoot(Object itemId) {
	    assertVertex(itemId);
		if (!containsId(itemId)) return false;
		
		return (getParent(itemId) == null);
	}

	public Collection<?> rootItemIds() {
		List<Object> rootItemIds = new ArrayList<Object>();
		
		for(Object itemId : getItemIds()) {
			if (getItem(itemId).getBean().getParent() == null) {
				rootItemIds.add(itemId);
			}
		}
		return rootItemIds;
	}

	public boolean setChildrenAllowed(Object itemId, boolean areChildrenAllowed) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("this operation is not allowed");
	}

	public boolean setParent(Object itemId, Object newParentId) throws UnsupportedOperationException {
	    assertVertex(itemId);
	    assertGroup(newParentId);
		if (!containsId(itemId)) return false;
		
		TestVertex v  = getItem(itemId).getBean();
		
		if (newParentId == null) {
			v.setParent(null);
			fireItemSetChange();
			return true;
		}
		
		if (!containsId(newParentId)) return false;
		
		TestVertex p = getItem(newParentId).getBean();
		
		if (p.isLeaf()) return false;
		
		TestGroup g = (TestGroup)p;
		
		v.setParent(g);
		fireItemSetChange();
		return true;
		
	}

    private void assertGroup(Object newParentId) {
        assertVertex(newParentId);
        TestVertex group = getItem(newParentId).getBean();
        assertFalse(group.isLeaf());
        
    }
	
}