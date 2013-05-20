/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd.vmware.vijava;

import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.ServiceCollector;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.collector.CollectionSet;
import org.opennms.netmgt.config.collector.CollectionSetVisitor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VmwareCollectionSet implements CollectionSet {
    private int m_status;
    private List<VmwareCollectionResource> m_collectionResources;
    private Date m_timestamp;

    public VmwareCollectionSet(final CollectionAgent agent) {
        m_status = ServiceCollector.COLLECTION_FAILED;
        m_collectionResources = new ArrayList<VmwareCollectionResource>();
    }

    @Override
    public int getStatus() {
        return m_status;
    }

    public void setStatus(final int status) {
        m_status = status;
    }

    @Override
    public void visit(final CollectionSetVisitor visitor) {
        visitor.visitCollectionSet(this);

        for (final CollectionResource resource : getResources()) {
            resource.visit(visitor);
        }

        visitor.completeCollectionSet(this);
    }

    public List<VmwareCollectionResource> getResources() {
        return m_collectionResources;
    }

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
