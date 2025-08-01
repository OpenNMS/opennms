/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.core.db.install;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
        return Collections.unmodifiableCollection(m_nameMap.values());
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
