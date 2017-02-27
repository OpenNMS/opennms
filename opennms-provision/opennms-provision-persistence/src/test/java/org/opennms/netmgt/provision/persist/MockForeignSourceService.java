/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.netmgt.model.requisition.OnmsForeignSource;
import org.opennms.netmgt.provision.support.PluginWrapper;
import org.springframework.util.Assert;

public class MockForeignSourceService implements ForeignSourceService {

    private final Map<String,OnmsForeignSource> m_foreignSources = new HashMap<>();

    @Override
    public Set<String> getActiveForeignSourceNames() {
        final Set<String> fsNames = new TreeSet<>();
        fsNames.addAll(m_foreignSources.keySet());
        return fsNames;
    }

    @Override
    public int getForeignSourceCount() {
        return m_foreignSources.size();
    }

    @Override
    public Set<OnmsForeignSource> getAllForeignSources() {
        return new HashSet<>(m_foreignSources.values());
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
    public void saveForeignSource(final OnmsForeignSource foreignSource) {
        Assert.notNull(foreignSource);
        Assert.notNull(foreignSource.getName());

        // TODO MVR ??
//        validate(foreignSource);

        m_foreignSources.put(foreignSource.getName(), foreignSource);
    }

    @Override
    public void deleteForeignSource(final String foreignSource) {
        m_foreignSources.remove(foreignSource);
    }

    @Override
    public OnmsForeignSource getDefaultForeignSource() {
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
    public void putDefaultForeignSource(final OnmsForeignSource foreignSource) {
        Objects.requireNonNull(foreignSource);
        foreignSource.setDefault(true);
        foreignSource.setName("default");

        saveForeignSource(foreignSource);
    }

    @Override
    public void resetDefaultForeignSource() {
        m_foreignSources.remove("default");
    }

    @Override
    public Map<String, String> getDetectorTypes() {
        return new HashMap<>();
    }

    @Override
    public Map<String, String> getPolicyTypes() {
        return new HashMap<>();
    }

    @Override
    public Map<String, PluginWrapper> getWrappers() {
        return new HashMap<>();
    }

}
