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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.util.Assert;

/**
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 *
 */
public class MockForeignSourceRepository extends AbstractForeignSourceRepository {
    private final Map<String,OnmsRequisition> m_requisitions = new HashMap<String,OnmsRequisition>();
    private final Map<String,OnmsForeignSource> m_foreignSources = new HashMap<String,OnmsForeignSource>();
    
    public Set<OnmsForeignSource> getForeignSources() {
        return new TreeSet<OnmsForeignSource>(m_foreignSources.values());
    }
    
    public OnmsForeignSource getForeignSource(String foreignSourceName) {
        Assert.notNull(foreignSourceName);
        return m_foreignSources.get(foreignSourceName);
    }

    public void save(OnmsForeignSource foreignSource) {
        Assert.notNull(foreignSource);
        Assert.notNull(foreignSource.getName());
        m_foreignSources.put(foreignSource.getName(), foreignSource);
    }

    public void delete(OnmsForeignSource foreignSource) throws ForeignSourceRepositoryException {
        m_foreignSources.remove(foreignSource);
    }

    public Set<OnmsRequisition> getRequisitions() throws ForeignSourceRepositoryException {
        return new TreeSet<OnmsRequisition>(m_requisitions.values());
    }

    public OnmsRequisition getRequisition(String foreignSourceName) {
        Assert.notNull(foreignSourceName);
        return m_requisitions.get(foreignSourceName);
    }

    public OnmsRequisition getRequisition(OnmsForeignSource foreignSource) {
        Assert.notNull(foreignSource);
        Assert.notNull(foreignSource.getName());
        return getRequisition(foreignSource.getName());
    }

    public void save(OnmsRequisition requisition) {
        Assert.notNull(requisition);
        Assert.notNull(requisition.getForeignSource());
        m_requisitions.put(requisition.getForeignSource(), requisition);
    }

    public void delete(OnmsRequisition requisition) throws ForeignSourceRepositoryException {
        m_requisitions.remove(requisition);
    }

}
