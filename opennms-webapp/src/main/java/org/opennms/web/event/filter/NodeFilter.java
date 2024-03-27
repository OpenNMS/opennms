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
package org.opennms.web.event.filter;

import javax.servlet.ServletContext;

import org.opennms.web.element.NetworkElementFactory;
import org.opennms.web.filter.EqualsFilter;
import org.opennms.web.filter.SQLType;
import org.springframework.context.ApplicationContext;

/**
 * Encapsulates all node filtering functionality.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class NodeFilter extends EqualsFilter<Integer> {
    /** Constant <code>TYPE="node"</code> */
    public static final String TYPE = "node";
    private ServletContext m_servletContext;
    private ApplicationContext m_appContext;

    /**
     * <p>Constructor for NodeFilter.</p>
     *
     * @param nodeId a int.
     */
    public NodeFilter(int nodeId, ServletContext servletContext) {
        super(TYPE, SQLType.INT, "EVENTS.NODEID", "node.id", nodeId);
        m_servletContext = servletContext;
    }
    
    public NodeFilter(int nodeId, ApplicationContext appContext) {
        super(TYPE, SQLType.INT, "EVENTS.NODEID", "node.id", nodeId);
        m_appContext = appContext;
    }

    /**
     * <p>getTextDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getTextDescription() {
        String nodeName = getNodeLabel();
        
        if(nodeName == null) {
            nodeName = Integer.toString(getNodeId());
        }
            
        return (TYPE + "=" + nodeName);
    }

    private String getNodeLabel() {
        return m_servletContext != null ? NetworkElementFactory.getInstance(m_servletContext).getNodeLabel(getNodeId()) : NetworkElementFactory.getInstance(m_appContext).getNodeLabel(getNodeId());
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return ("<WebEventRepository.NodeFilter: " + getDescription() + ">");
    }

    /**
     * <p>getNodeId</p>
     *
     * @return a int.
     */
    public int getNodeId() {
        return getValue();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof NodeFilter)) return false;
        return (this.toString().equals(obj.toString()));
    }
}
