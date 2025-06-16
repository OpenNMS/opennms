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
package org.opennms.netmgt.provision.service.operations;

import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeLabelSource;

/**
 * <p>ScanResource class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ScanResource {
    private String m_type;
    private OnmsNode m_node = null;
    private final Map<String,String> m_attributes = new HashMap<String,String>();

    /**
     * <p>Constructor for ScanResource.</p>
     *
     * @param type a {@link java.lang.String} object.
     */
    public ScanResource(String type) {
        m_type = type;
    }
    
    /**
     * <p>getType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getType() {
        return m_type;
    }

    /**
     * <p>setNode</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public void setNode(OnmsNode node) {
        m_node = node;
    }

    /**
     * <p>getNode</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public OnmsNode getNode() {
        return m_node;
    }

    // TODO: change node comparison to use spring
    /**
     * <p>setAttribute</p>
     *
     * @param key a {@link java.lang.String} object.
     * @param value a {@link java.lang.String} object.
     */
    public void setAttribute(String key, String value) {
        m_attributes.put(key, value);
        if (m_node != null) {
            if (key.equals("sysContact")) {
                m_node.setSysContact(value);
            } else if (key.equals("sysDescription")) {
                m_node.setSysDescription(value);
            } else if (key.equals("sysLocation")) {
                m_node.setSysLocation(value);
            } else if (key.equals("sysObjectId")) {
                m_node.setSysObjectId(value);
            } else if (key.equals("sysName")) {
                m_node.setSysName(value);

                // If the sysName isn't null or empty...
                if (value != null && !"".equals(value.trim())) {
                    // If the node is labeled as just the IP address from the newSuspect that created it,
                    // use the SNMP sysName value instead and update the label source to indicate this
                    if (m_node.getLabelSource() == NodeLabelSource.ADDRESS) {
                        m_node.setLabel(value);
                        m_node.setLabelSource(NodeLabelSource.SYSNAME);
                    }
                }
            }
            
        }
    }

    /**
     * <p>getAttribute</p>
     *
     * @param key a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getAttribute(String key) {
        return m_attributes.get(key);
    }
}
