/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.collectd.wmi;

import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.dao.support.GenericIndexResourceType;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.config.StorageStrategy;
import org.opennms.netmgt.collectd.AbstractCollectionResource;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.ServiceParameters;
import org.opennms.netmgt.collectd.CollectionAttributeType;
import org.opennms.core.utils.ThreadCategory;
import org.apache.log4j.Category;

import java.util.List;
import java.io.File;

public class WmiMultiInstanceCollectionResource extends WmiCollectionResource {
    private String m_inst;
    private String m_name;

    public WmiMultiInstanceCollectionResource(CollectionAgent agent, String instance, String name) {
        super(agent);
        m_inst = instance;
        m_name = name;
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    @Override
    public File getResourceDir(RrdRepository repository) {
        File rrdBaseDir = repository.getRrdBaseDir();
        File nodeDir = new File(rrdBaseDir, String.valueOf(m_agent.getNodeId()));
        File typeDir = new File(nodeDir, m_name);
        File instDir = new File(typeDir, m_inst.replaceAll("\\s+", "_").replaceAll(":", "_").replaceAll("\\\\", "_").replaceAll("[\\[\\]]", "_"));
        if (log().isDebugEnabled()) {
            log().debug("getResourceDir: " + instDir.toString());
        }
        return instDir;
    }

    public String getResourceTypeName() {
        return m_name;
    }

    public String getInstance() {
        return m_inst;
    }

    @Override
    public String toString() {
        return "Node[" + m_agent.getNodeId() + "]/type["+ m_name+"]/instance[" + m_inst +"]";
    }

}
