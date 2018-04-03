/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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
