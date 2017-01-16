/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd.wmi;

import java.io.File;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.config.collector.ServiceParameters;
import org.opennms.netmgt.model.RrdRepository;

/**
 * <p>WmiMultiInstanceCollectionResource class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class WmiMultiInstanceCollectionResource extends WmiCollectionResource {
    private String m_inst;
    private String m_resourceLabel;
    private WmiResourceType m_resourceType;

    /**
     * <p>Constructor for WmiMultiInstanceCollectionResource.</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @param instance a {@link java.lang.String} object.
     * @param name a {@link java.lang.String} object.
     */
    public WmiMultiInstanceCollectionResource(final CollectionAgent agent, final String instance, final WmiResourceType type) {
        super(agent);
        m_inst = instance;
        m_resourceType = type;
    }
    
    @Override
    public boolean shouldPersist(ServiceParameters params) {
        return m_resourceType.getPersistenceSelectorStrategy().shouldPersist(this);
    }

    /** {@inheritDoc} */
    @Override
    public File getResourceDir(final RrdRepository repository) {
        String resourcePath = m_resourceType.getStorageStrategy().getRelativePathForAttribute(getParent(), getInterfaceLabel(), null);
        File resourceDir = new File(repository.getRrdBaseDir(), resourcePath);
        LogUtils.debugf(this, "getResourceDir: %s", resourceDir);
        return resourceDir;
    }

    /**
     * <p>getResourceTypeName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getResourceTypeName() {
        return m_resourceType.getName();
    }

    /**
     * <p>getInstance</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getInstance() {
        return m_inst;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Node[" + m_agent.getNodeId() + "]/type["+ getResourceTypeName() +"]/instance[" + m_inst +"]";
    }

    public String getInterfaceLabel(){
        return (m_resourceLabel == null 
                ? m_resourceType.getStorageStrategy().getResourceNameFromIndex(this)
                : m_resourceLabel);
    }
}
