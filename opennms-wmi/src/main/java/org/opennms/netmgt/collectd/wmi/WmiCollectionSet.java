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
import org.opennms.netmgt.collectd.ServiceCollector;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.collector.CollectionSet;
import org.opennms.netmgt.config.collector.CollectionSetVisitor;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * <p>WmiCollectionSet class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class WmiCollectionSet implements CollectionSet {
    private int m_status;
    private List<WmiCollectionResource> m_collectionResources;
    private Date m_timestamp;

    /**
     * <p>Constructor for WmiCollectionSet.</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     */
    public WmiCollectionSet(final CollectionAgent agent) {
        m_status = ServiceCollector.COLLECTION_FAILED;
        m_collectionResources = new ArrayList<WmiCollectionResource>();
    }

    /**
     * <p>getStatus</p>
     *
     * @return a int.
     */
    @Override
    public int getStatus() {
        return m_status;
    }

    /**
     * <p>setStatus</p>
     *
     * @param status a int.
     */
    public void setStatus(final int status) {
        m_status = status;
    }

    /** {@inheritDoc} */
    @Override
    public void visit(final CollectionSetVisitor visitor) {
        visitor.visitCollectionSet(this);

        for(final CollectionResource resource : getResources()) {
                resource.visit(visitor);
        }

        visitor.completeCollectionSet(this);
    }

    /**
     * <p>getResources</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<WmiCollectionResource> getResources() {
        return m_collectionResources;
    }

    /**
     * <p>ignorePersist</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean ignorePersist() {
        return false;
    }
    
    @Override
	public Date getCollectionTimestamp() {
		return m_timestamp;
	}
    public void setCollectionTimestamp(Date timestamp) {
    	this.m_timestamp = timestamp;
	}

}
