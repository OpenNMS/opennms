/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.protocols.xml.collector;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.ServiceCollector;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.collector.CollectionSet;
import org.opennms.netmgt.config.collector.CollectionSetVisitor;

/**
 * The Class XmlCollectionSet.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class XmlCollectionSet implements CollectionSet {

    /** The collection status. */
    private int m_status;

    /** The list of XML collection resources. */
    private List<XmlCollectionResource> m_collectionResources;

    /** The collection timestamp. */
    private Date m_timestamp;

    /**
     * Instantiates a new XML collection set.
     *
     * @param agent the agent
     */
    public XmlCollectionSet(CollectionAgent agent) {
        m_status = ServiceCollector.COLLECTION_FAILED;
        m_collectionResources = new ArrayList<XmlCollectionResource>();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.collector.CollectionSet#getStatus()
     */
    @Override
    public int getStatus() {
        return m_status;
    }

    /**
     * Sets the status.
     *
     * @param status the new status
     */
    public void setStatus(int status) {
        m_status = status;
    }

    /**
     * Gets the collection resources.
     *
     * @return the collection resources
     */
    public List<XmlCollectionResource> getCollectionResources() {
        return m_collectionResources;
    }

    /**
     * Sets the collection resources.
     *
     * @param collectionResources the new collection resources
     */
    public void setCollectionResources(List<XmlCollectionResource> collectionResources) {
        m_collectionResources = collectionResources;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.collector.CollectionSet#visit(org.opennms.netmgt.config.collector.CollectionSetVisitor)
     */
    @Override
    public void visit(CollectionSetVisitor visitor) {
        visitor.visitCollectionSet(this);

        for(CollectionResource resource : getCollectionResources())
            resource.visit(visitor);

        visitor.completeCollectionSet(this);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.collector.CollectionSet#ignorePersist()
     */
    @Override
    public boolean ignorePersist() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.collector.CollectionSet#getCollectionTimestamp()
     */
    @Override
    public Date getCollectionTimestamp() {
        return m_timestamp;
    }

    /**
     * Sets the collection timestamp.
     *
     * @param timestamp the new collection timestamp
     */
    public void setCollectionTimestamp(Date timestamp) {
        this.m_timestamp = timestamp;
    }

}
