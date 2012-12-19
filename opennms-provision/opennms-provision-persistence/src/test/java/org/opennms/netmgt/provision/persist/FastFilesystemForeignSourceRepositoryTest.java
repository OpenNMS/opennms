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

package org.opennms.netmgt.provision.persist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;

public class FastFilesystemForeignSourceRepositoryTest extends ForeignSourceRepositoryTestCase {
    private String m_defaultForeignSourceName;

    @Autowired
    @Qualifier("fast")
    private ForeignSourceRepository m_foreignSourceRepository;

    @Before
    public void setUp() {
        m_defaultForeignSourceName = "imported:";
    }

    private Requisition createRequisition() throws Exception {
        Requisition r = m_foreignSourceRepository.importResourceRequisition(new ClassPathResource("/requisition-test.xml"));
        m_foreignSourceRepository.save(r);
        m_foreignSourceRepository.flush();
        return r;
    }

    private ForeignSource createForeignSource(String foreignSource) throws Exception {
        ForeignSource fs = new ForeignSource(foreignSource);
        fs.addDetector(new PluginConfig("HTTP", "org.opennms.netmgt.provision.detector.simple.HttpDetector"));
        fs.addPolicy(new PluginConfig("all-ipinterfaces", "org.opennms.netmgt.provision.persist.policies.InclusiveInterfacePolicy"));
        m_foreignSourceRepository.save(fs);
        m_foreignSourceRepository.flush();
        return fs;
    }

    @Test
    public void testRequisition() throws Exception {
        createRequisition();
        Requisition r = m_foreignSourceRepository.getRequisition(m_defaultForeignSourceName);
        TestVisitor v = new TestVisitor();
        r.visit(v);
        assertEquals("number of nodes visited", 2, v.getNodeReqs().size());
        assertEquals("node name matches", "apknd", v.getNodeReqs().get(0).getNodeLabel());
    }

    @Test
    public void testForeignSource() throws Exception {
        createRequisition();
        ForeignSource foreignSource = createForeignSource(m_defaultForeignSourceName);
        Set<ForeignSource> foreignSources = m_foreignSourceRepository.getForeignSources();
        assertEquals("number of foreign sources must be 1", 1, foreignSources.size());
        assertEquals("getAll() foreign source name must match", m_defaultForeignSourceName, foreignSources.iterator().next().getName());
        
        // check that the foreign source matches
        final ForeignSource newForeignSource = m_foreignSourceRepository.getForeignSource(m_defaultForeignSourceName);
        
        assertEquals(foreignSource.getName(), newForeignSource.getName());
        assertEquals(foreignSource.getDateStampAsDate(), newForeignSource.getDateStampAsDate());
        assertEquals(foreignSource.getDetectorNames(), newForeignSource.getDetectorNames());
        assertEquals(foreignSource.getScanInterval(), newForeignSource.getScanInterval());
    }

    @Test
    public void testGetRequisition() throws Exception {
        Requisition requisition = createRequisition();
        ForeignSource foreignSource = createForeignSource(m_defaultForeignSourceName);
        assertRequisitionsMatch("requisitions must match", m_foreignSourceRepository.getRequisition(m_defaultForeignSourceName), m_foreignSourceRepository.getRequisition(foreignSource));
        assertRequisitionsMatch("foreign source is the expected one", requisition, m_foreignSourceRepository.getRequisition(foreignSource));
    }

    @Test
    public void testDefaultForeignSource() throws Exception {
        createRequisition();
        List<String> detectorList = Arrays.asList(new String[]{ "DNS", "FTP", "HTTP", "HTTPS", "ICMP", "IMAP", "LDAP", "NRPE", "POP3", "SMTP", "SNMP", "SSH" });
        String uuid = UUID.randomUUID().toString();
        ForeignSource defaultForeignSource = m_foreignSourceRepository.getForeignSource(uuid);
        assertEquals("name must match requested foreign source repository name", uuid, defaultForeignSource.getName());
        assertEquals("scan-interval must be 1 day", 86400000, defaultForeignSource.getScanInterval().getMillis());
        assertEquals("foreign source must have no default policies", 0, defaultForeignSource.getPolicies().size());
        List<String> fsNames = new ArrayList<String>();
        for (PluginConfig config : defaultForeignSource.getDetectors()) {
            fsNames.add(config.getName());
        }
        assertEquals("detector list must match expected defaults", detectorList, fsNames);
        assertTrue("foreign source must be tagged as default", defaultForeignSource.isDefault());
    }
}
