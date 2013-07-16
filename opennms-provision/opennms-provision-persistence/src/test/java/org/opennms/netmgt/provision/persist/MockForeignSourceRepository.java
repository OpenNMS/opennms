/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist;

import java.net.URL;
import java.util.Date;
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

    @Override
    public Set<String> getActiveForeignSourceNames() {
    	final Set<String> fsNames = new TreeSet<String>();
        fsNames.addAll(m_requisitions.keySet());
        fsNames.addAll(m_foreignSources.keySet());
        return fsNames;
    }

    @Override
    public int getForeignSourceCount() {
        return m_foreignSources.size();
    }
    
    @Override
    public Set<ForeignSource> getForeignSources() {
        return new TreeSet<ForeignSource>(m_foreignSources.values());
    }

    @Override
    public ForeignSource getForeignSource(final String foreignSourceName) {
        Assert.notNull(foreignSourceName);
        final ForeignSource foreignSource = m_foreignSources.get(foreignSourceName);
        if (foreignSource == null) {
        	if (foreignSourceName == "default") {
        		return super.getDefaultForeignSource();
        	} else {
        		return getDefaultForeignSource();
        	}
        }
        return foreignSource;
    }

    @Override
    public void save(final ForeignSource foreignSource) {
        Assert.notNull(foreignSource);
        Assert.notNull(foreignSource.getName());

        validate(foreignSource);

        foreignSource.updateDateStamp();
        m_foreignSources.put(foreignSource.getName(), foreignSource);
    }

    @Override
    public void delete(final ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        m_foreignSources.remove(foreignSource.getName());
    }

    @Override
    public Set<Requisition> getRequisitions() throws ForeignSourceRepositoryException {
        return new TreeSet<Requisition>(m_requisitions.values());
    }

    @Override
    public Requisition getRequisition(final String foreignSourceName) {
        Assert.notNull(foreignSourceName);
        return m_requisitions.get(foreignSourceName);
    }

    @Override
    public Requisition getRequisition(final ForeignSource foreignSource) {
        Assert.notNull(foreignSource);
        Assert.notNull(foreignSource.getName());
        return getRequisition(foreignSource.getName());
    }

    @Override
    public void save(final Requisition requisition) {
        Assert.notNull(requisition);
        Assert.notNull(requisition.getForeignSource());
        
        validate(requisition);

        requisition.updateDateStamp();
        m_requisitions.put(requisition.getForeignSource(), requisition);
    }

    @Override
    public void delete(final Requisition requisition) throws ForeignSourceRepositoryException {
        m_requisitions.remove(requisition.getForeignSource());
    }

    @Override
    public Date getRequisitionDate(final String foreignSource) {
        final Requisition requisition = m_requisitions.get(foreignSource);
        return requisition == null? null : requisition.getDate();
    }

    @Override
    public URL getRequisitionURL(final String foreignSource) {
        throw new UnsupportedOperationException("no URL in the mock repository");
    }

    @Override
    public ForeignSource getDefaultForeignSource() throws ForeignSourceRepositoryException {
    	final ForeignSource fs = getForeignSource("default");
    	if (fs == null) {
    		return super.getDefaultForeignSource();
    	}
    	return fs;
    }

    @Override
    public void putDefaultForeignSource(final ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        if (foreignSource == null) {
            throw new ForeignSourceRepositoryException("foreign source was null");
        }
        foreignSource.setDefault(true);
        foreignSource.setName("default");
        foreignSource.updateDateStamp();
        
        save(foreignSource);
    }

    @Override
    public void flush() throws ForeignSourceRepositoryException {
        // Unnecessary, there is no caching/delayed writes in MockForeignSourceRepository
    }
}
