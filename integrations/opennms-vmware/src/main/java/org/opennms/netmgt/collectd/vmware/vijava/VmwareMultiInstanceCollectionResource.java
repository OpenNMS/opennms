/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd.vmware.vijava;

import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.model.ResourcePath;

public class VmwareMultiInstanceCollectionResource extends VmwareCollectionResource {

    private final String m_inst;
    private final String m_name;

    public VmwareMultiInstanceCollectionResource(final CollectionAgent agent, final String instance, final String name) {
        super(agent);
        m_inst = instance;
        m_name = name;
    }

    @Override
    public ResourcePath getPath() {
        return ResourcePath.get(m_agent.getStorageResourcePath(),
                                m_name,
                                m_inst.replaceAll("/", "_").replaceAll("\\s+", "_").replaceAll(":", "_").replaceAll("\\\\", "_").replaceAll("[\\[\\]]", "_"));
    }

    @Override
    public String getResourceTypeName() {
        return m_name;
    }

    @Override
    public String getInstance() {
        return m_inst;
    }

    @Override
    public String toString() {
        return "Node[" + m_agent.getNodeId() + "]/type[" + m_name + "]/instance[" + m_inst + "]";
    }
}
