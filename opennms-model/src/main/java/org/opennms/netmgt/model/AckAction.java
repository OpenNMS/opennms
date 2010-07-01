/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 26, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>AckAction class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public enum AckAction {
    UNSPECIFIED(1, "Unspecified"),
    ACKNOWLEDGE(2, "Acknowledge"),
    UNACKNOWLEDGE(3, "Unacknowledge"),
    ESCALATE(4, "Escalate"),
    CLEAR(5, "Clear");
    
    /** Constant <code>m_idMap</code> */
    private static final Map<Integer, AckAction> m_idMap; 
    private static final List<Integer> m_ids;

    
    private int m_id;
    private String m_label;
    
    static {
        m_ids = new ArrayList<Integer>(values().length);
        m_idMap = new HashMap<Integer, AckAction>(values().length);
        for (AckAction action : values()) {
            m_ids.add(action.getId());
            m_idMap.put(action.getId(), action);
        }
    }


    private AckAction(int id, String label) {
        m_id = id;
        m_label = label;
    }

    private Integer getId() {
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
     * @return a {@link org.opennms.netmgt.model.AckAction} object.
     */
    public static AckAction get(int id) {
        if (m_idMap.containsKey(id)) {
            return m_idMap.get(id);
        } else {
            throw new IllegalArgumentException("Cannot create AckAction from unknown ID " + id);
        }
    }

    
}
