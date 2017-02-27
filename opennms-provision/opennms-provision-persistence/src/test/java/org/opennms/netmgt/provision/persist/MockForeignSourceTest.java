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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.model.foreignsource.DetectorPluginConfigEntity;
import org.opennms.netmgt.model.foreignsource.ForeignSourceEntity;
import org.opennms.netmgt.model.foreignsource.PolicyPluginConfigEntity;

public class MockForeignSourceTest extends ForeignSourceRepositoryTestCase {

    private ForeignSourceService m_foreignSourceRepository;

    @Before
    public void setUp() {
        m_foreignSourceRepository = new MockForeignSourceService();
    }

    private ForeignSourceEntity createForeignSource(String foreignSource) throws Exception {
        ForeignSourceEntity fs = new ForeignSourceEntity(foreignSource);
        fs.addDetector(new DetectorPluginConfigEntity("HTTP", "org.opennms.netmgt.provision.detector.simple.HttpDetector"));
        fs.addPolicy(new PolicyPluginConfigEntity("all-ipinterfaces", "org.opennms.netmgt.provision.persist.policies.InclusiveInterfacePolicy"));
        m_foreignSourceRepository.saveForeignSource(fs);
        return fs;
    }

    @Test
    public void testForeignSource() throws Exception {
        ForeignSourceEntity foreignSource = createForeignSource("imported:");
        List<ForeignSourceEntity> foreignSources = new ArrayList<>(m_foreignSourceRepository.getAllForeignSources());
        assertEquals("number of foreign sources", 1, foreignSources.size());
        assertEquals("getAll() foreign source name matches", "imported:", foreignSources.get(0).getName());
        assertEquals("get() returns the foreign source", foreignSource, m_foreignSourceRepository.getForeignSource("imported:"));
    }
}
