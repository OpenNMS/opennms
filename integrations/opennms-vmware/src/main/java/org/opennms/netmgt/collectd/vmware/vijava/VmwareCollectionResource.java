/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd.vmware.vijava;

import org.opennms.netmgt.collectd.AbstractCollectionResource;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.config.collector.CollectionAttributeType;
import org.opennms.netmgt.config.collector.ServiceParameters;

public abstract class VmwareCollectionResource extends AbstractCollectionResource {

    private int m_nodeId;

    public VmwareCollectionResource(CollectionAgent agent) {
        super(agent);
        m_nodeId = agent.getNodeId();
    }

    public int getType() {
        return -1; //Is this right?
    }

    public boolean rescanNeeded() {
        return false;
    }

    public boolean shouldPersist(final ServiceParameters params) {
        return true;
    }

    public void setAttributeValue(final CollectionAttributeType type, final String value) {
        final VmwareCollectionAttribute attr = new VmwareCollectionAttribute(this, type, type.getName(), value);
        addAttribute(attr);
    }

    public abstract String getResourceTypeName();

    public abstract String getInstance();

    public String getParent() {
        return Integer.toString(m_nodeId);
    }
}
