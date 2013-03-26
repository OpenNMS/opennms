/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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


package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Access point status enumeration.
 * 
 * @author <a href="mailto:jwhite@datavalet.com">Jesse White</a>
 */
public enum AccessPointStatus implements Serializable {
    // Keep this ordered by ID so we can use the internal enum compareTo
    UNKNOWN(0, "Unknown"), ONLINE(1, "Online"), OFFLINE(3, "Offline");

    private static final Map<Integer, AccessPointStatus> ID_MAP;
    private int m_id;
    private String m_label;

    static {
        ID_MAP = new HashMap<Integer, AccessPointStatus>(values().length);
        for (final AccessPointStatus status : values()) {
            ID_MAP.put(status.getId(), status);
        }
    }

    private AccessPointStatus(final int id, final String label) {
        m_id = id;
        m_label = label;
    }

    /**
     * <p>
     * getId
     * </p>
     * 
     * @return a int.
     */
    public int getId() {
        return m_id;
    }

    /**
     * <p>
     * getLabel
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public String getLabel() {
        return m_label;
    }

    /**
     * <p>
     * get
     * </p>
     * 
     * @param id
     *            a int.
     * @return a {@link org.opennms.netmgt.model.AccessPointStatus} object.
     */
    public static AccessPointStatus get(final int id) {
        if (ID_MAP.containsKey(id)) {
            return ID_MAP.get(id);
        } else {
            throw new IllegalArgumentException("Cannot create AccessPointStatus from unknown ID " + id);
        }
    }

    /**
     * <p>
     * get
     * </p>
     * 
     * @param label
     *            a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.AccessPointStatus} object.
     */
    public static AccessPointStatus get(final String label) {
        for (final Integer key : ID_MAP.keySet()) {
            if (ID_MAP.get(key).getLabel().equalsIgnoreCase(label)) {
                return ID_MAP.get(key);
            }
        }
        return AccessPointStatus.UNKNOWN;
    }

    public static List<String> names() {
        final List<String> names = new ArrayList<String>();
        for (final AccessPointStatus value : values()) {
            names.add(value.toString());
        }
        return names;
    }
}
