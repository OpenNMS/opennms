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
package org.opennms.web.svclayer.model;

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
 * @since 1.8.1
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
     * @param other a {@link org.opennms.web.svclayer.model.Graph} object.
     * @return a int.
     */
    @Override
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
        Set<RrdGraphAttribute> reqAttrs = new LinkedHashSet<>();
        for(String attrName : m_graph.getColumns()) {
            RrdGraphAttribute attr = available.get(attrName);
            if (attr != null) {
                reqAttrs.add(attr);
            }
        }
        return reqAttrs;
    }

}
