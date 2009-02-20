/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 */

package org.opennms.netmgt.provision.persist;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.springframework.util.Assert;

/**
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 *
 */
public class MockForeignSourceRepository extends AbstractForeignSourceRepository {
    private final Map<String,Requisition> m_requisitions = new HashMap<String,Requisition>();
    private final Map<String,ForeignSource> m_foreignSources = new HashMap<String,ForeignSource>();
    
    public int getForeignSourceCount() {
        return m_foreignSources.size();
    }
    
    public Set<ForeignSource> getForeignSources() {
        return new TreeSet<ForeignSource>(m_foreignSources.values());
    }
    
    public ForeignSource getForeignSource(String foreignSourceName) {
        Assert.notNull(foreignSourceName);
        ForeignSource foreignSource = m_foreignSources.get(foreignSourceName);
        if (foreignSource == null) {
            foreignSource = getDefaultForeignSource();
        }
        return foreignSource;
    }

    public void save(ForeignSource foreignSource) {
        Assert.notNull(foreignSource);
        Assert.notNull(foreignSource.getName());
        m_foreignSources.put(foreignSource.getName(), foreignSource);
    }

    public void delete(ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        m_foreignSources.remove(foreignSource.getName());
    }

    public Set<Requisition> getRequisitions() throws ForeignSourceRepositoryException {
        return new TreeSet<Requisition>(m_requisitions.values());
    }

    public Requisition getRequisition(String foreignSourceName) {
        Assert.notNull(foreignSourceName);
        return m_requisitions.get(foreignSourceName);
    }

    public Requisition getRequisition(ForeignSource foreignSource) {
        Assert.notNull(foreignSource);
        Assert.notNull(foreignSource.getName());
        return getRequisition(foreignSource.getName());
    }

    public void save(Requisition requisition) {
        Assert.notNull(requisition);
        Assert.notNull(requisition.getForeignSource());
        m_requisitions.put(requisition.getForeignSource(), requisition);
    }

    public void delete(Requisition requisition) throws ForeignSourceRepositoryException {
        m_requisitions.remove(requisition.getForeignSource());
    }

    public URL getRequisitionURL(String foreignSource) {
        throw new UnsupportedOperationException("no URL in the mock repository");
    }

}
