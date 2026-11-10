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
