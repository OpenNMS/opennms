/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.netmgt.model.requisition.OnmsForeignSource;
import org.opennms.netmgt.model.requisition.OnmsRequisition;
import org.springframework.util.Assert;

/**
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 *
 */
// TODO MVR implement
public class MockForeignSourceRepository implements ForeignSourceRepository {
    private final Map<String,OnmsRequisition> m_requisitions = new HashMap<>();
    private final Map<String,OnmsForeignSource> m_foreignSources = new HashMap<>();


    @Override
    public Set<String> getActiveForeignSourceNames() {
    	final Set<String> fsNames = new TreeSet<>();
        fsNames.addAll(m_requisitions.keySet());
        fsNames.addAll(m_foreignSources.keySet());
        return fsNames;
    }

    @Override
    public int getForeignSourceCount() {
        return m_foreignSources.size();
    }

    @Override
    public Set<OnmsForeignSource> getForeignSources() {
        return new TreeSet<>(m_foreignSources.values());
    }

    @Override
    public OnmsForeignSource getForeignSource(final String foreignSourceName) {
        Assert.notNull(foreignSourceName);
        final OnmsForeignSource foreignSource = m_foreignSources.get(foreignSourceName);
        if (foreignSource == null) {
            return getDefaultForeignSource();
        }
        return foreignSource;
    }

    @Override
    public void save(final OnmsForeignSource foreignSource) {
        Assert.notNull(foreignSource);
        Assert.notNull(foreignSource.getName());

        validate(foreignSource);

        m_foreignSources.put(foreignSource.getName(), foreignSource);
    }

    @Override
    public void delete(final OnmsForeignSource foreignSource) throws ForeignSourceRepositoryException {
        m_foreignSources.remove(foreignSource.getName());
    }

    @Override
    public Set<OnmsRequisition> getRequisitions() throws ForeignSourceRepositoryException {
        return new TreeSet<>(m_requisitions.values());
    }

    @Override
    public OnmsRequisition getRequisition(final String foreignSourceName) {
        Assert.notNull(foreignSourceName);
        return m_requisitions.get(foreignSourceName);
    }

    @Override
    public void save(final OnmsRequisition requisition) {
        Assert.notNull(requisition);
        Assert.notNull(requisition.getForeignSource());

        validate(requisition);

        m_requisitions.put(requisition.getForeignSource(), requisition);
    }

    @Override
    public void delete(final OnmsRequisition requisition) throws ForeignSourceRepositoryException {
        m_requisitions.remove(requisition.getForeignSource());
    }

    @Override
    public void validate(OnmsForeignSource foreignSource) throws ForeignSourceRepositoryException {

    }

    @Override
    public void validate(OnmsRequisition requisition) throws ForeignSourceRepositoryException {

    }

    @Override
    public void triggerImport(ImportRequest web) {
        // TODO MVR send event
    }

    @Override
    public OnmsForeignSource getDefaultForeignSource() throws ForeignSourceRepositoryException {
    	final OnmsForeignSource fs = getForeignSource("default");
    	if (fs == null) {
    	    // TODO MVR map... this is duplicated code, we may re-use this anyways ...
//            fs = JAXB.unmarshal(ForeignSource.class, new ClassPathResource("/org/opennms/netmgt/provision/persist/default-foreign-source.xml"));
//            fs.setDefault(true);
            return fs;
    	}
    	return fs;
    }

    @Override
    public void putDefaultForeignSource(final OnmsForeignSource foreignSource) throws ForeignSourceRepositoryException {
        if (foreignSource == null) {
            throw new ForeignSourceRepositoryException("foreign source was null");
        }
        foreignSource.setDefault(true);
        foreignSource.setName("default");

        save(foreignSource);
    }

    @Override
    public void resetDefaultForeignSource() throws ForeignSourceRepositoryException {
        m_foreignSources.remove("default");
    }

}
