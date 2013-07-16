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

import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.AbstractCollectionResource;
import org.opennms.netmgt.config.collector.CollectionAttributeType;
import org.opennms.netmgt.config.collector.ServiceParameters;

/**
 * <p>Abstract WmiCollectionResource class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class WmiCollectionResource extends AbstractCollectionResource {
    
    protected int m_nodeId;
    protected CollectionAgent m_agent;

    /**
     * <p>Constructor for WmiCollectionResource.</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     */
    public WmiCollectionResource(CollectionAgent agent) {
        super(agent);
        m_agent = agent;
        m_nodeId = agent.getNodeId();
    }

    /**
     * <p>getType</p>
     *
     * @return a int.
     */
    @Override
    public int getType() {
        return -1; //Is this right?
    }

    //A rescan is never needed for the WmiCollector, at least on resources
    /**
     * <p>rescanNeeded</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean rescanNeeded() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean shouldPersist(final ServiceParameters params) {
        return true;
    }

    /**
     * <p>setAttributeValue</p>
     *
     * @param type a {@link org.opennms.netmgt.config.collector.CollectionAttributeType} object.
     * @param value a {@link java.lang.String} object.
     */
    public void setAttributeValue(final CollectionAttributeType type, final String value) {
        final WmiCollectionAttribute attr = new WmiCollectionAttribute(this, type, type.getName(), value);
        addAttribute(attr);
    }

    /**
     * <p>getResourceTypeName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public abstract String getResourceTypeName();


    /**
     * <p>getInstance</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public abstract String getInstance();
    
    @Override
    public String getParent() {
        return m_agent.getStorageDir().toString();
    }
}
