/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.api.StorageStrategy;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.snmp.SnmpInstId;

/**
 * <p>GenericIndexResource class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class GenericIndexResource extends SnmpCollectionResource {

    private final SnmpInstId m_inst;
    private final String m_name;
    private String m_resourceLabel;

    /**
     * <p>Constructor for GenericIndexResource.</p>
     *
     * @param def a {@link org.opennms.netmgt.collectd.ResourceType} object.
     * @param name a {@link java.lang.String} object.
     * @param inst a {@link org.opennms.netmgt.snmp.SnmpInstId} object.
     */
    public GenericIndexResource(ResourceType def, String name, SnmpInstId inst) {
        super(def);
        m_name = name;
        m_inst = inst;
    }

    @Override
    public ResourcePath getPath() {
        return getStrategy().getRelativePathForAttribute(getParent(), getInterfaceLabel());
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    // NMS-5062: Avoid call getLabel here, otherwise the SiblingColumnStorageStrategy will fail if DEBUG is enabled for Collectd.
    @Override
    public String toString() {
        return "node["+getCollectionAgent().getNodeId() + "]." + getResourceTypeName() + "[" + getInstance() + "]";
    }


    /** {@inheritDoc} */
    @Override
    public int getSnmpIfType() {
        return -1;	// XXX is this right?
    }

    /** {@inheritDoc} */
    @Override
    public boolean shouldPersist(ServiceParameters params) {
        return ((GenericIndexResourceType)getResourceType()).getPersistenceSelectorStrategy().shouldPersist(this);
    }

    /**
     * <p>getResourceTypeName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getResourceTypeName() {
        return m_name;
    }
    
    /**
     * <p>getInstance</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getInstance() {
        return m_inst.toString();
    }

    private StorageStrategy getStrategy() {
        return ((GenericIndexResourceType)getResourceType()).getStorageStrategy();
    }

    @Override
    public ResourcePath getParent() {
        return getCollectionAgent().getStorageResourcePath();
    }

    /*
     * Because call getResourceNameFromIndex could be expensive.
     * This class save the returned value from Strategy on a local variable.
     */
    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getInterfaceLabel() {
        if (m_resourceLabel == null) {
            m_resourceLabel = getStrategy().getResourceNameFromIndex(this);
        }
        return m_resourceLabel;
    }
}

