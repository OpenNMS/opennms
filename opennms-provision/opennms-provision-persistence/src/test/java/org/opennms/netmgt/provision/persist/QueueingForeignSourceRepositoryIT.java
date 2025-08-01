/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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

public class QueueingForeignSourceRepositoryIT extends ForeignSourceRepositoryTestCase {
    private String m_defaultForeignSourceName;

    @Autowired
    @Qualifier("queueing")
    private ForeignSourceRepository m_foreignSourceRepository;

    @Before
    public void setUp() {
        m_defaultForeignSourceName = "imported-";
        m_foreignSourceRepository.clear();
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
        String names = "", separator = "";
        for (ForeignSource fs : foreignSources) {
            names += (separator + "\"" + fs.getName() + "\"");
            separator = ", ";
        }
        assertEquals("number of foreign sources must be 1: " + names, 1, foreignSources.size());
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
        List<String> fsNames = new ArrayList<>();
        for (PluginConfig config : defaultForeignSource.getDetectors()) {
            fsNames.add(config.getName());
        }
        assertEquals("detector list must match expected defaults", detectorList, fsNames);
        assertTrue("foreign source must be tagged as default", defaultForeignSource.isDefault());
    }
}
