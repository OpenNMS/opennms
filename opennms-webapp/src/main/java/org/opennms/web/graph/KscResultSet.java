/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

/**
 * <p>KscResultSet class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
package org.opennms.web.graph;

import java.util.Date;

import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
public class KscResultSet {
    private final String m_title;
    private final Date m_start;
    private final Date m_end;
    private final OnmsResource m_resource;
    private final PrefabGraph m_prefabGraph;
    
    /**
     * <p>Constructor for KscResultSet.</p>
     *
     * @param title a {@link java.lang.String} object.
     * @param start a java$util$Date object.
     * @param end a java$util$Date object.
     * @param resource a {@link org.opennms.netmgt.model.OnmsResource} object.
     * @param prefabGraph a {@link org.opennms.netmgt.model.PrefabGraph} object.
     */
    public KscResultSet(String title, Date start, Date end, OnmsResource resource, PrefabGraph prefabGraph) {
        m_title = title;
        m_start = start;
        m_end = end;
        m_resource = resource;
        m_prefabGraph = prefabGraph;
    }
    
    /**
     * <p>getTitle</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTitle() {
        return m_title;
    }
    
    /**
     * <p>getStart</p>
     *
     * @return a java$util$Date object.
     */
    public Date getStart() {
        return m_start;
    }
    
    /**
     * <p>getEnd</p>
     *
     * @return a java$util$Date object.
     */
    public Date getEnd() {
        return m_end;
    }
    
    /**
     * <p>getResource</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsResource} object.
     */
    public OnmsResource getResource() {
        return m_resource;
    }
    
    /**
     * <p>getPrefabGraph</p>
     *
     * @return a {@link org.opennms.netmgt.model.PrefabGraph} object.
     */
    public PrefabGraph getPrefabGraph() {
        return m_prefabGraph;
    }
}
