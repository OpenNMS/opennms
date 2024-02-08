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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenNMS severity enumeration.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public enum OnmsSeverity implements Serializable {
    // Keep this ordered by ID so we can use the internal enum compareTo
    INDETERMINATE(1, "Indeterminate", "lightblue"),
    CLEARED(2, "Cleared", "white"),
    NORMAL(3, "Normal", "green"),
    WARNING(4, "Warning", "cyan"),
    MINOR(5, "Minor", "yellow"),
    MAJOR(6, "Major", "orange"),
    CRITICAL(7, "Critical", "red");
    
    private static final Map<Integer, OnmsSeverity> m_idMap; 
    
    private int m_id;
    private String m_label;
    private String m_color;

    static {
        m_idMap = new HashMap<Integer, OnmsSeverity>(values().length);
        for (final OnmsSeverity severity : values()) {
            m_idMap.put(severity.getId(), severity);
        }
    }

    private OnmsSeverity(final int id, final String label, final String color) {
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
    public boolean isLessThan(final OnmsSeverity other) {
        return compareTo(other) < 0;
    }

    /**
     * <p>isLessThanOrEqual</p>
     *
     * @param other a {@link org.opennms.netmgt.model.OnmsSeverity} object.
     * @return a boolean.
     */
    public boolean isLessThanOrEqual(final OnmsSeverity other) {
        return compareTo(other) <= 0;
    }

    /**
     * <p>isGreaterThan</p>
     *
     * @param other a {@link org.opennms.netmgt.model.OnmsSeverity} object.
     * @return a boolean.
     */
    public boolean isGreaterThan(final OnmsSeverity other) {
        return compareTo(other) > 0;
    }
    
    /**
     * <p>isGreaterThanOrEqual</p>
     *
     * @param other a {@link org.opennms.netmgt.model.OnmsSeverity} object.
     * @return a boolean.
     */
    public boolean isGreaterThanOrEqual(final OnmsSeverity other) {
        return compareTo(other) >= 0;
    }
    
    /**
     * <p>get</p>
     *
     * @param id a int.
     * @return a {@link org.opennms.netmgt.model.OnmsSeverity} object.
     */
    public static OnmsSeverity get(final int id) {
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
    public static OnmsSeverity get(final String label) {
        for (final Integer key : m_idMap.keySet()) {
            if (m_idMap.get(key).getLabel().equalsIgnoreCase(label)) {
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
    public static OnmsSeverity escalate(final OnmsSeverity sev) {
        if (sev.isLessThan(OnmsSeverity.CRITICAL)) {
            return OnmsSeverity.get(sev.getId()+1);
        } else {
            return OnmsSeverity.get(sev.getId());
        }
    }

    public static List<String> names() {
        final List<String> names = new ArrayList<>();
        for (final OnmsSeverity value : values()) {
            names.add(value.toString());
        }
        return names;
    }
}
