/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.topo.linkd.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.opennms.features.topology.api.VertexContainer;

public class LinkdVertexContainer extends VertexContainer<String, LinkdVertex> {
	/**
     * 
     */
    private static final long serialVersionUID = 1L;

    public LinkdVertexContainer() {
		super(LinkdVertex.class);
		setBeanIdProperty("id");
	}

    @Override
    public Collection<?> getChildren(Object itemId) {
        if (!containsId(itemId)) return Collections.EMPTY_LIST;
        LinkdVertex v = getItem(itemId).getBean();
        if (v.isLeaf()) {
                return Collections.EMPTY_LIST;
        } else {
            LinkdGroup g = (LinkdGroup)v;
            List<String> memberIds = new ArrayList<String>();
            for(LinkdVertex member : g.getMembers()) {
                    memberIds.add(member.getId());
            }
            return memberIds;
        }
    }

    @Override
    public Object getParent(Object itemId) {
        if (!containsId(itemId)) return null;
        
        LinkdGroup g = getItem(itemId).getBean().getParent();
        return g == null ? null : g.getId();    
    }

    @Override
    public Collection<?> rootItemIds() {
        List<Object> rootItemIds = new ArrayList<Object>();
        
        for(Object itemId : getItemIds()) {
                if (getItem(itemId).getBean().getParent() == null) {
                        rootItemIds.add(itemId);
                }
        }
        return rootItemIds;
    }

    @Override
    public boolean setParent(Object itemId, Object newParentId)
            throws UnsupportedOperationException {
        if (!containsId(itemId)) return false;
        
        LinkdVertex v  = getItem(itemId).getBean();
        
        if (newParentId == null) {
                v.setParent(null);
                return true;
        }
        
        if (!containsId(newParentId)) return false;
        
        LinkdVertex p = getItem(newParentId).getBean();
        
        if (p.isLeaf()) return false;
        
        LinkdGroup g = (LinkdGroup)p;
        
        v.setParent(g);
        return true;
    }

    @Override
    public boolean areChildrenAllowed(Object itemId) {
        if (!containsId(itemId)) return false;
        LinkdVertex v = getItem(itemId).getBean();
        return !v.isLeaf();    
    }

    @Override
    public boolean setChildrenAllowed(Object itemId,
            boolean areChildrenAllowed) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("this operation is not allowed");
    }

    @Override
    public boolean isRoot(Object itemId) {
        if (!containsId(itemId)) return false;        
        return (getParent(itemId) == null);
    }

    @Override
    public boolean hasChildren(Object itemId) {
        if (!containsId(itemId)) return false;
        LinkdVertex v = getItem(itemId).getBean();
        if (v.isLeaf()) return false;
        LinkdGroup g = (LinkdGroup) v;
        return !g.getMembers().isEmpty();
   }
	
	
}
