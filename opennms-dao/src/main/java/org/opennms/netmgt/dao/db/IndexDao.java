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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.dao.db;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <p>IndexDao class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class IndexDao {
    private Map<String, Index> m_nameMap;
    private Map<String, List<Index>> m_tableMap;

    /**
     * <p>Constructor for IndexDao.</p>
     */
    public IndexDao() {
        reset();
    }
    
    /**
     * <p>reset</p>
     */
    public void reset() {
        m_nameMap = new LinkedHashMap<String, Index>();
        m_tableMap = new HashMap<String, List<Index>>();
    }
    
    /**
     * <p>add</p>
     *
     * @param i a {@link org.opennms.netmgt.dao.db.Index} object.
     */
    public void add(Index i) {
        String lowerName = i.getName().toLowerCase();
        if (m_nameMap.containsKey(lowerName)) {
            throw new IllegalArgumentException("Index with name of '"
                                               + lowerName
                                               + "' already exists.");
        }
        
        m_nameMap.put(lowerName, i);
        
        getIndexesForTableCreateIfEmpty(i.getTable().toLowerCase()).add(i);
    }
    
    private List<Index> getIndexesForTableCreateIfEmpty(String table) {
        if (!m_tableMap.containsKey(table)) {
            m_tableMap.put(table, new LinkedList<Index>());
        }
        return m_tableMap.get(table);
    }
    
    /**
     * <p>getIndexesForTable</p>
     *
     * @param table a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public List<Index> getIndexesForTable(String table) {
        String lowerName = table.toLowerCase();
        if (!m_tableMap.containsKey(lowerName)) {
            return new LinkedList<Index>();
        }
        return m_tableMap.get(lowerName);
    }
    
    /**
     * <p>getAllIndexes</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<Index> getAllIndexes() {
        return m_nameMap.values();
    }
    
    /**
     * <p>remove</p>
     *
     * @param indexName a {@link java.lang.String} object.
     */
    public void remove(String indexName) {
        String lowerName = indexName.toLowerCase();
        
        Index index = m_nameMap.remove(lowerName);
        if (index == null) {
            throw new IllegalArgumentException("Index with name of '"
                                               + lowerName
                                               + "' does not exist.");
        }
        
        for (List<Index> indexes : m_tableMap.values()) {
            for (Index i : indexes) {
                if (index.equals(i)) {
                    indexes.remove(i);
                    return;
                }
            }
        }
    }
}
