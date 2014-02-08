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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.collector.CollectionSet;
import org.opennms.netmgt.config.collector.CollectionSetVisitor;

// TODO: Auto-generated Javadoc
/**
 * The Class JMXCollectionSet.
 */
public class JMXCollectionSet implements CollectionSet {

    /** The m_agent. */
    private final CollectionAgent m_agent;

    /** The status. */
    private int m_status;

    /** The timestamp. */
    private Date m_timestamp;

    /** The collection resource. */
    private List<JMXCollectionResource> m_collectionResources;

    /**
     * Instantiates a new JMX collection set.
     *
     * @param agent            the agent
     */
    JMXCollectionSet(CollectionAgent agent) {
        m_agent = agent;
        m_status = ServiceCollector.COLLECTION_FAILED;
        m_collectionResources = new ArrayList<JMXCollectionResource>();
    }

    public CollectionAgent getAgent() {
        return m_agent;
    }

    /**
     * Gets the resource.
     *
     * @return the resource
     */
    public List<JMXCollectionResource> getCollectionResources() {
        return m_collectionResources;
    }

    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.config.collector.CollectionSet#getCollectionTimestamp()
     */
    @Override
    public Date getCollectionTimestamp() {
        return m_timestamp;
    }

    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.config.collector.CollectionSet#getStatus()
     */
    @Override
    public int getStatus() {
        return m_status;
    }

    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.config.collector.CollectionSet#ignorePersist()
     */
    @Override
    public boolean ignorePersist() {
        return false;
    }

    /**
     * Sets the collection resources.
     *
     * @param collectionResources the new collection resources
     */
    public void setCollectionResources(List<JMXCollectionResource> collectionResources) {
        m_collectionResources = collectionResources;
    }

    /**
     * Sets the collection timestamp.
     *
     * @param timestamp
     *            the new collection timestamp
     */
    public void setCollectionTimestamp(Date timestamp) {
        this.m_timestamp = timestamp;
    }

    /**
     * Sets the status.
     *
     * @param status
     *            the new status
     */
    public void setStatus(int status) {
        m_status = status;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.opennms.netmgt.config.collector.CollectionSet#visit(org.opennms.netmgt.config.collector.CollectionSetVisitor)
     */
    @Override
    public void visit(CollectionSetVisitor visitor) {
        visitor.visitCollectionSet(this);
        for (CollectionResource resource : getCollectionResources()) {
            resource.visit(visitor);
        }
        visitor.completeCollectionSet(this);
    }

}
