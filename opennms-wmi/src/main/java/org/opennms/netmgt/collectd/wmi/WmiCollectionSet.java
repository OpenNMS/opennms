//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.collectd.wmi;

import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.ServiceCollector;
import org.opennms.netmgt.collectd.CollectionSetVisitor;
import org.opennms.netmgt.collectd.CollectionSet;
import org.opennms.netmgt.collectd.CollectionResource;

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

    /**
     * <p>Constructor for WmiCollectionSet.</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     */
    public WmiCollectionSet(CollectionAgent agent) {
        m_status = ServiceCollector.COLLECTION_FAILED;
        m_collectionResources = new ArrayList<WmiCollectionResource>();
    }

    /**
     * <p>getStatus</p>
     *
     * @return a int.
     */
    public int getStatus() {
        return m_status;
    }

    /**
     * <p>setStatus</p>
     *
     * @param status a int.
     */
    public void setStatus(int status) {
        m_status = status;
    }

    /** {@inheritDoc} */
    public void visit(CollectionSetVisitor visitor) {
        visitor.visitCollectionSet(this);

        for(CollectionResource resource : getResources())
                resource.visit(visitor);

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
    public boolean ignorePersist() {
        return false;
    }
}
