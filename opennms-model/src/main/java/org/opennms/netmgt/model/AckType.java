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
package org.opennms.netmgt.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * <p>AckType class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public enum AckType {
    UNSPECIFIED(1, "Unspecified"),
    ALARM(2, "Alarm"),
    NOTIFICATION(3, "Notification");
    
    /** Constant <code>m_idMap</code> */
    private static final Map<Integer, AckType> m_idMap; 
    private static final List<Integer> m_ids;


    private int m_id;
    private String m_label;
    
    static {
        m_ids = new ArrayList<Integer>(values().length);
        m_idMap = new HashMap<Integer, AckType>(values().length);
        for (AckType action : values()) {
            m_ids.add(action.getId());
            m_idMap.put(action.getId(), action);
        }
    }


    private AckType(int id, String label) {
        m_id = id;
        m_label = label;
    }
    
    public Integer getId() {
        return m_id;
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return m_label;
    }

    /**
     * <p>get</p>
     *
     * @param id a int.
     * @return a {@link org.opennms.netmgt.model.AckType} object.
     */
    public static AckType get(int id) {
        if (m_idMap.containsKey(id)) {
            return m_idMap.get(id);
        } else {
            throw new IllegalArgumentException("Cannot create AckType from unknown ID: " + id);
        }
    }
}
