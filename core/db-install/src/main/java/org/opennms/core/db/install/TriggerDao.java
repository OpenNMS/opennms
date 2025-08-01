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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TriggerDao {
    private Map<String, Trigger> m_nameMap;
    private Map<String, List<Trigger>> m_tableMap;

    /**
     * <p>Constructor for TriggerDao.</p>
     */
    public TriggerDao() {
        reset();
    }

    /**
     * <p>reset</p>
     */
    public void reset() {
        m_nameMap = new LinkedHashMap<String, Trigger>();
        m_tableMap = new HashMap<String, List<Trigger>>();
    }

    /**
     * <p>add</p>
     *
     * @param t a {@link org.opennms.netmgt.dao.db.Trigger} object.
     */
    public void add(Trigger t) {
        String lowerName = t.getName().toLowerCase();
        if (m_nameMap.containsKey(lowerName)) {
            throw new IllegalArgumentException("Trigger with name of '"
                                               + lowerName
                                               + "' already exists.");
        }
        
        m_nameMap.put(lowerName, t);
        
        getTriggersForTableCreateIfEmpty(t.getTable().toLowerCase()).add(t);
    }
    
    private List<Trigger> getTriggersForTableCreateIfEmpty(String table) {
        if (!m_tableMap.containsKey(table)) {
            m_tableMap.put(table, new LinkedList<Trigger>());
        }
        return m_tableMap.get(table);
    }
    
    /**
     * <p>getTriggersForTable</p>
     *
     * @param table a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public List<Trigger> getTriggersForTable(String table) {
        String lowerName = table.toLowerCase();
        if (!m_tableMap.containsKey(lowerName)) {
            return new LinkedList<Trigger>();
        }
        return m_tableMap.get(lowerName);
    }
}
