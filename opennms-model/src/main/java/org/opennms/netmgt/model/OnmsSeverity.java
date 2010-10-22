/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008-2009 The OpenNMS Group, Inc. All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: September 27, 2008.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Embeddable;

/**
 * OpenNMS severity enumeration.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
@Embeddable
public enum OnmsSeverity implements Serializable {
    // Keep this ordered by ID so we can use the internal enum compareTo
    INDETERMINATE(1, "Indeterminate", "lightblue"),
    CLEARED(2, "Cleared", "white"),
    NORMAL(3, "Normal", "green"),
    WARNING(4, "Warning", "cyan"),
    MINOR(5, "Minor", "yellow"),
    MAJOR(6, "Major", "orange"),
    CRITICAL(7, "Critical", "red");
    
    /** Constant <code>m_idMap</code> */
    private static final Map<Integer, OnmsSeverity> m_idMap; 
    private static final List<Integer> m_ids;
    
    private int m_id;
    private String m_label;
    private String m_color;

    static {
        m_ids = new ArrayList<Integer>(values().length);
        m_idMap = new HashMap<Integer, OnmsSeverity>(values().length);
        for (OnmsSeverity severity : values()) {
            m_ids.add(severity.getId());
            m_idMap.put(severity.getId(), severity);
        }
    }

    private OnmsSeverity(int id, String label, String color) {
        m_id = id;
        m_label = label;
        m_color = color;
    }
    
    /**
     * <p>getId</p>
     *
     * @return a int.
     */
    public int getId() {
        return m_id;
    }
    
    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLabel() {
        return m_label;
    }

    /**
     * <p>getColor</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getColor() {
        return m_color;
    }

    /**
     * <p>isLessThan</p>
     *
     * @param other a {@link org.opennms.netmgt.model.OnmsSeverity} object.
     * @return a boolean.
     */
    public boolean isLessThan(OnmsSeverity other) {
        return compareTo(other) < 0;
    }

    /**
     * <p>isLessThanOrEqual</p>
     *
     * @param other a {@link org.opennms.netmgt.model.OnmsSeverity} object.
     * @return a boolean.
     */
    public boolean isLessThanOrEqual(OnmsSeverity other) {
        return compareTo(other) <= 0;
    }

    /**
     * <p>isGreaterThan</p>
     *
     * @param other a {@link org.opennms.netmgt.model.OnmsSeverity} object.
     * @return a boolean.
     */
    public boolean isGreaterThan(OnmsSeverity other) {
        return compareTo(other) > 0;
    }
    
    /**
     * <p>isGreaterThanOrEqual</p>
     *
     * @param other a {@link org.opennms.netmgt.model.OnmsSeverity} object.
     * @return a boolean.
     */
    public boolean isGreaterThanOrEqual(OnmsSeverity other) {
        return compareTo(other) >= 0;
    }
    
    /**
     * <p>get</p>
     *
     * @param id a int.
     * @return a {@link org.opennms.netmgt.model.OnmsSeverity} object.
     */
    public static OnmsSeverity get(int id) {
        if (m_idMap.containsKey(id)) {
            return m_idMap.get(id);
        } else {
            throw new IllegalArgumentException("Cannot create OnmsSeverity from unknown ID " + id);
        }
    }

    /**
     * <p>get</p>
     *
     * @param label a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsSeverity} object.
     */
    public static OnmsSeverity get(String label) {
        for (Integer key : m_idMap.keySet()) {
            if (m_idMap.get(key).getLabel().equals(label)) {
                return m_idMap.get(key);
            }
        }
        return OnmsSeverity.INDETERMINATE;
    }

/**
 * <p>escalate</p>
 *
 * @param sev a {@link org.opennms.netmgt.model.OnmsSeverity} object.
 * @return a {@link org.opennms.netmgt.model.OnmsSeverity} object.
 */
public static OnmsSeverity escalate(OnmsSeverity sev) {
        if (sev.isLessThan(OnmsSeverity.CRITICAL)) {
            return OnmsSeverity.get(sev.getId()+1);
        } else {
            return OnmsSeverity.get(sev.getId());
        }
    }
}
