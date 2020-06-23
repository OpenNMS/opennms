/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
