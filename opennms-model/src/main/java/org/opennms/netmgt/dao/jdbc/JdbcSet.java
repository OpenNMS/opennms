//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.dao.jdbc;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.opennms.netmgt.util.IteratorIterator;

public class JdbcSet extends AbstractSet {
    
    LinkedHashSet m_added = new LinkedHashSet();
    LinkedHashSet m_entries = new LinkedHashSet();
    LinkedHashSet m_removed = new LinkedHashSet();
    
    public JdbcSet(Collection c) {
        m_entries.addAll(c);
    }
    
    protected JdbcSet() {
    	
    }
    
    protected void setElements(Collection c) {
    	m_entries.addAll(c);
    }
    
    class JdbcSetIterator extends  IteratorIterator {

        Object m_last;
        
        JdbcSetIterator(Iterator entriesIter, Iterator addedIter) {
            super(Arrays.asList(new Object[] { entriesIter, addedIter }));
        }

        public Object next() {
            m_last = super.next();
            return m_last;
        }

        public void remove() {
            m_removed.add(m_last);
            super.remove();
        }
        
    }

    public Iterator iterator() {
        return new JdbcSetIterator(m_entries.iterator(), m_added.iterator());
    }

    public int size() {
        return m_added.size()+m_entries.size();
    }

    public boolean add(Object o) {
        if (contains(o)) return false;
        m_added.add(o);
        return true;
    }
    
    public Set getRemoved() {
        return m_removed;
    }
    
    public Set getAdded() {
        return m_added;
    }
    
    public Set getRemaining() {
        return m_entries;
    }
    
    public void reset() {
        m_entries.addAll(m_added);
        m_added.clear();
        m_removed.clear();
    }


}
