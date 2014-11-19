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
import org.opennms.netmgt.rrd.RrdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class VmwareMultiInstanceCollectionResource extends VmwareCollectionResource {

    /**
     * logging for VMware data collection
     */
    private final Logger logger = LoggerFactory.getLogger("OpenNMS.VMware." + VmwareMultiInstanceCollectionResource.class.getName());

    private final String m_inst;
    private final String m_name;

    public VmwareMultiInstanceCollectionResource(final CollectionAgent agent, final String instance, final String name) {
        super(agent);
        m_inst = instance;
        m_name = name;
    }

    @Override
    public File getResourceDir(RrdRepository repository) {
        final File rrdBaseDir = repository.getRrdBaseDir();
        final File nodeDir = new File(rrdBaseDir, getParent());
        final File typeDir = new File(nodeDir, m_name);
        final File instDir = new File(typeDir, m_inst.replaceAll("/", "_").replaceAll("\\s+", "_").replaceAll(":", "_").replaceAll("\\\\", "_").replaceAll("[\\[\\]]", "_"));
        logger.debug("getResourceDir: '{}'", instDir);
        return instDir;
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
