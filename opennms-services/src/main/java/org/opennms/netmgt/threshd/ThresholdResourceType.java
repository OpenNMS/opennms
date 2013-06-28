/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.threshd;

import java.util.Map;
import java.util.Set;

/**
 * <p>ThresholdResourceType class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class ThresholdResourceType {
    
    private final String m_dsType;

    private Map<String, Set<ThresholdEntity>> m_thresholdMap;
    
    /**
     * <p>Constructor for ThresholdResourceType.</p>
     *
     * @param type a {@link java.lang.String} object.
     */
    public ThresholdResourceType(final String type) {
        m_dsType = type.intern();
    }

    /**
     * <p>getDsType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDsType() {
        return m_dsType;
    }
    
    /**
     * <p>getThresholdMap</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, Set<ThresholdEntity>> getThresholdMap() {
        return m_thresholdMap;
    }
    
    /**
     * <p>setThresholdMap</p>
     *
     * @param thresholdMap a {@link java.util.Map} object.
     */
    public void setThresholdMap(Map<String, Set<ThresholdEntity>> thresholdMap) {
    	m_thresholdMap = thresholdMap;
    }


}
