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
package org.opennms.netmgt.snmp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SnmpRowResult {
    private final Map<SnmpObjId, SnmpResult> m_results = new TreeMap<SnmpObjId,SnmpResult>();
    private SnmpInstId m_instance;
    private int m_columnCount;

    public SnmpRowResult(int columnCount, SnmpInstId instance) {
        m_instance = instance;
        m_columnCount = columnCount;
    }

    public boolean isComplete(SnmpObjId... ignoreColumns) {
        if (m_results.size() == m_columnCount) {
            return true;
        } else if (m_results.size() > 0 && ignoreColumns.length > 0) {
            /* 
             * short-circuit if the table result is telling us to consider
             * certain SnmpObjId's as finished
             */
            int total = m_results.size();
            for (SnmpObjId col : ignoreColumns) {
                if (!m_results.containsKey(col)) {
                    total++;
                }
            }
            return total == m_columnCount;
        }
        return false;
    }

    public int getColumnCount() {
        return m_columnCount;
    }

    public List<SnmpResult> getResults() {
        return new ArrayList<SnmpResult>(m_results.values());
    }
    
    public void addResult(SnmpObjId column, SnmpResult result) {
        assertTrue(m_instance.equals(result.getInstance()), "unexpected result %s passed to row with instance %s", result, m_instance);
        m_results.put(column, result);
    }
    
    public SnmpInstId getInstance() {
        return m_instance;
    }

    /**
     * @param base
     * @return
     */
    public SnmpValue getValue(SnmpObjId base) {
        for(SnmpResult result : getResults()) {
            if (base.equals(result.getBase())) {
                return result.getValue();
            }
        }
        
        return null;
    }

    private void assertTrue(boolean b, String fmt, Object... args) {
    	if (!b) {
    		throw new IllegalArgumentException(String.format(fmt, args));
    	}
    }
    

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("columnCount", m_columnCount)
            .append("results", m_results)
            .toString();
    }

}
