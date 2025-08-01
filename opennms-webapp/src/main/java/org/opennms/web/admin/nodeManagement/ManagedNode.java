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
package org.opennms.web.admin.nodeManagement;

import java.util.ArrayList;
import java.util.List;

/**
 * A servlet that stores node, interface, service information
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class ManagedNode {
    /**
     */
    protected int nodeID;

    /**
     */
    protected String nodeLabel;

    /**
     * 
     */
    protected List<ManagedInterface> interfaces;

    /**
     * <p>Constructor for ManagedNode.</p>
     */
    public ManagedNode() {
        interfaces = new ArrayList<>();
    }

    /**
     * <p>Setter for the field <code>nodeID</code>.</p>
     *
     * @param id a int.
     */
    public void setNodeID(int id) {
        nodeID = id;
    }

    /**
     * <p>Setter for the field <code>nodeLabel</code>.</p>
     *
     * @param label a {@link java.lang.String} object.
     */
    public void setNodeLabel(String label) {
        nodeLabel = label;
    }

    /**
     * <p>addInterface</p>
     *
     * @param newInterface a {@link org.opennms.web.admin.nodeManagement.ManagedInterface} object.
     */
    public void addInterface(ManagedInterface newInterface) {
        interfaces.add(newInterface);
    }

    /**
     * <p>getInterfaceCount</p>
     *
     * @return a int.
     */
    public int getInterfaceCount() {
        return interfaces.size();
    }

    /**
     * <p>Getter for the field <code>nodeID</code>.</p>
     *
     * @return a int.
     */
    public int getNodeID() {
        return nodeID;
    }

    /**
     * <p>Getter for the field <code>nodeLabel</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNodeLabel() {
        return nodeLabel;
    }

    /**
     * <p>Getter for the field <code>interfaces</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<ManagedInterface> getInterfaces() {
        return interfaces;
    }
}
