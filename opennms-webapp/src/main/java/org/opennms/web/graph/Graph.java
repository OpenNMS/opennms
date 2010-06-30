//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.web.graph;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.model.RrdGraphAttribute;

/**
 * <p>Graph class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.6.12
 */
public class Graph implements Comparable<Graph> {
    private PrefabGraph m_graph = null;
    private OnmsResource m_resource;
    private Date m_start = null;
    private Date m_end = null;
    
    /**
     * <p>Constructor for Graph.</p>
     *
     * @param graph a {@link org.opennms.netmgt.model.PrefabGraph} object.
     * @param resource a {@link org.opennms.netmgt.model.OnmsResource} object.
     * @param start a {@link java.util.Date} object.
     * @param end a {@link java.util.Date} object.
     */
    public Graph(PrefabGraph graph, OnmsResource resource,
            Date start, Date end) {
        m_graph = graph;
        m_resource = resource;
        m_start = start;
        m_end = end;
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
     * <p>getStart</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getStart() {
        return m_start;
    }

    /**
     * <p>getEnd</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getEnd() {
        return m_end;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_graph.getName();
    }
    
    /**
     * <p>getTitle</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTitle() {
        return m_graph.getTitle();
    }

    /**
     * <p>compareTo</p>
     *
     * @param other a {@link org.opennms.web.graph.Graph} object.
     * @return a int.
     */
    public int compareTo(Graph other) {
        if (other == null) {
            return -1;
        }

        return m_graph.compareTo(other.m_graph);
    }

    /**
     * <p>getGraphWidth</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getGraphWidth() {
        return m_graph.getGraphWidth();
    }

    /**
     * <p>getGraphHeight</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getGraphHeight() {
        return m_graph.getGraphHeight();
    }
    
    /**
     * <p>getPrefabGraph</p>
     *
     * @return a {@link org.opennms.netmgt.model.PrefabGraph} object.
     */
    public PrefabGraph getPrefabGraph() {
        return m_graph;
    }
    
    /**
     * <p>getReport</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getReport() {
        return m_graph.getName();
    }
    
    /**
     * <p>getRequiredRrGraphdAttributes</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<RrdGraphAttribute> getRequiredRrGraphdAttributes() {
        Map<String, RrdGraphAttribute> available = m_resource.getRrdGraphAttributes();
        Set<RrdGraphAttribute> reqAttrs = new LinkedHashSet<RrdGraphAttribute>();
        for(String attrName : m_graph.getColumns()) {
            RrdGraphAttribute attr = available.get(attrName);
            if (attr != null) {
                reqAttrs.add(attr);
            }
        }
        return reqAttrs;
    }

}
