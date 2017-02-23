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

package org.opennms.protocols.xml.collector;

import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.model.ResourcePath;

/**
 * The Class XmlMultiInstanceCollectionResource.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class XmlMultiInstanceCollectionResource extends XmlCollectionResource {

    /** The collection resource instance. */
    private final String m_instance;

    /** The resource label. */
    private String m_resourceLabel;

    /** The collection resource type name. */
    private final XmlResourceType m_resourceType;

    /**
     * Instantiates a new XML Multi-instance collection resource.
     *
     * @param agent the collection agent
     * @param instance the resource instance
     * @param type the XML resource type
     */
    public XmlMultiInstanceCollectionResource(CollectionAgent agent, String instance, XmlResourceType type) {
        super(agent);
        m_resourceType = type;
        m_instance = instance;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.AbstractCollectionResource#shouldPersist(org.opennms.netmgt.config.collector.ServiceParameters)
     */
    @Override
    public boolean shouldPersist(ServiceParameters params) {
        return m_resourceType.getPersistenceSelectorStrategy().shouldPersist(this);
    }

    @Override
    public ResourcePath getPath() {
        return m_resourceType.getStorageStrategy().getRelativePathForAttribute(getParent(), getInterfaceLabel());
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.XmlCollectionResource#getResourceTypeName()
     */
    @Override
    public String getResourceTypeName() {
        return m_resourceType.getName();
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.XmlCollectionResource#getInstance()
     */
    @Override
    public String getInstance() {
        return m_instance;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "node[" + m_agent.getNodeId() + "]." + getResourceTypeName() + "[" + getInterfaceLabel() +"]";
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.AbstractCollectionResource#getLabel()
     */
    @Override
    public String getInterfaceLabel() {
        if (m_resourceLabel == null) {
            m_resourceLabel = m_resourceType.getStorageStrategy().getResourceNameFromIndex(this);
        }
        return m_resourceLabel;
    }

}
