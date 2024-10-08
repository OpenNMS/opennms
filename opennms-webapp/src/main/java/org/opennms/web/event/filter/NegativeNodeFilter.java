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
import org.opennms.web.filter.NotEqualOrNullFilter;
import org.opennms.web.filter.SQLType;
import org.springframework.context.ApplicationContext;

/**
 * Encapsulates all node filtering functionality.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class NegativeNodeFilter extends NotEqualOrNullFilter<Integer> {
    /** Constant <code>TYPE="nodenot"</code> */
    public static final String TYPE = "nodenot";
    private ServletContext m_servletContext;
    private ApplicationContext m_appContext;

    /**
     * <p>Constructor for NegativeNodeFilter.</p>
     *
     * @param nodeId a int.
     */
    public NegativeNodeFilter(int nodeId, ServletContext servletContext) {
        super(TYPE, SQLType.INT, "EVENTS.NODEID", "node.id", nodeId);
        m_servletContext = servletContext;
    }
    
    public NegativeNodeFilter(int nodeId, ApplicationContext appContext) {
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
            nodeName = Integer.toString(getValue());
        }

        return ("node is not " + nodeName);
    }

    private String getNodeLabel() {
        return m_servletContext != null ? NetworkElementFactory.getInstance(m_servletContext).getNodeLabel(getValue()) : NetworkElementFactory.getInstance(m_appContext).getNodeLabel(getValue());
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return ("<WebEventRepository.NegativeNodeFilter: " + this.getDescription() + ">");
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
        if (!(obj instanceof NegativeNodeFilter)) return false;
        return (this.toString().equals(obj.toString()));
    }
}
