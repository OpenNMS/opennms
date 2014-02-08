/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import java.io.File;

import org.opennms.netmgt.config.collector.ServiceParameters;
import org.opennms.netmgt.model.RrdRepository;

/**
 * The Class JMXMultiInstanceCollectionResource.
 */
public class JMXMultiInstanceCollectionResource extends JMXCollectionResource {
    /** The collection resource instance. */
    private String m_instance;
    /** The resource label. */
    private String m_resourceLabel;
    /** The collection resource type name. */
    private JMXResourceType m_resourceType;

    /**
     * Instantiates a new JMX multi instance collection resource.
     *
     * @param agent the agent
     * @param instance the instance
     * @param type the type
     */
    public JMXMultiInstanceCollectionResource(CollectionAgent agent, String instance, JMXResourceType type) {
        super(agent);
        m_resourceType = type;
        m_instance = instance;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.JMXCollectionResource#getInstance()
     */
    @Override
    public String getInstance() {
        return m_instance;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.AbstractCollectionResource#getLabel()
     */
    public String getLabel() {
        if (m_resourceLabel == null) {
            m_resourceLabel = m_resourceType.getStorageStrategy().getResourceNameFromIndex(this);
        }
        return m_resourceLabel;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.AbstractCollectionResource#getResourceDir(org.opennms.netmgt.model.RrdRepository)
     */
    @Override
    public File getResourceDir(RrdRepository repository) {
        String resourcePath = m_resourceType.getStorageStrategy().getRelativePathForAttribute(getParent(), getLabel(), null);
        File resourceDir = new File(repository.getRrdBaseDir(), resourcePath);
        return resourceDir;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.JMXCollectionResource#getResourceTypeName()
     */
    @Override
    public String getResourceTypeName() {
        return m_resourceType.getName();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.JMXCollectionResource#shouldPersist(org.opennms.netmgt.config.collector.ServiceParameters)
     */
    @Override
    public boolean shouldPersist(ServiceParameters params) {
        return m_resourceType.getPersistenceSelectorStrategy().shouldPersist(this);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "node[" + m_agent.getNodeId() + "]." + getResourceTypeName() + "[" + getLabel() +"]";
    }
}
