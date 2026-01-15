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
package org.opennms.web.rest.v2;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.datacollection.SystemDef;
import org.opennms.netmgt.dao.api.SnmpCollectionMibGroupDao;
import org.opennms.netmgt.dao.api.SnmpCollectionResourceTypeDao;
import org.opennms.netmgt.dao.api.SnmpCollectionSourceDao;
import org.opennms.netmgt.dao.api.SnmpCollectionSystemDefDao;
import org.opennms.netmgt.model.SnmpCollectionMibGroup;
import org.opennms.netmgt.model.SnmpCollectionResourceType;
import org.opennms.netmgt.model.SnmpCollectionSource;
import org.opennms.netmgt.model.SnmpCollectionSystemDef;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=integration")
@JUnitTemporaryDatabase
@Transactional
public class DataCollectionConfPersistenceServiceIT {

    @Autowired
    private DataCollectionConfPersistenceService dataCollectionConfPersistenceService;

    @Autowired
    private SnmpCollectionSourceDao snmpCollectionSourceDao;

    @Autowired
    private SnmpCollectionResourceTypeDao snmpCollectionResourceTypeDao;

    @Autowired
    private SnmpCollectionMibGroupDao snmpCollectionMibGroupDao;

    @Autowired
    private SnmpCollectionSystemDefDao snmpCollectionSystemDefDao;

    private int defaultSourceCount, defaultResourceTypeCount, defaultMibGroupCount, defaultSystemDefCount;

    @Before
    @Transactional
    public void setUp() {
        defaultSourceCount = snmpCollectionSourceDao.findAll().size();
        defaultResourceTypeCount = snmpCollectionResourceTypeDao.findAll().size();
        defaultMibGroupCount = snmpCollectionMibGroupDao.findAll().size();
        defaultSystemDefCount = snmpCollectionSystemDefDao.findAll().size();
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testAddDataCollectionConfigWith3ComData() {
        String fileName = "3com-datacollection.xml";
        String userName = "testuser";
        Date now = new Date();

        DatacollectionGroup dataCollectionGroup = build3ComDatacollectionGroup();

        Integer srcId = dataCollectionConfPersistenceService.addDataCollectionConfig(
                fileName, userName, dataCollectionGroup, now);

        // Persisted source
        List<SnmpCollectionSource> sources = snmpCollectionSourceDao.findAll();
        assertEquals(defaultSourceCount + 1, sources.size());
        SnmpCollectionSource persistedSource = snmpCollectionSourceDao.get(srcId);
        assertNotNull(persistedSource);
        assertEquals(fileName, persistedSource.getName());
        assertEquals("3Com", persistedSource.getVendor());
        assertEquals(userName, persistedSource.getUploadedBy());
        assertTrue(persistedSource.getEnabled());

        // Mib Groups
        List<SnmpCollectionMibGroup> mibGroups = snmpCollectionMibGroupDao.findAll();
        assertEquals(defaultMibGroupCount + 2, mibGroups.size());
        boolean foundGroup1 = false, foundGroup2 = false;
        for (SnmpCollectionMibGroup m : mibGroups) {
            if (m.getName().equals("3com-router-perf")) foundGroup1 = true;
            if (m.getName().equals("3com-router-sys")) foundGroup2 = true;
        }
        assertTrue(foundGroup1);
        assertTrue(foundGroup2);

        // SystemDefs
        List<SnmpCollectionSystemDef> systemDefs = snmpCollectionSystemDefDao.findAll();
        assertEquals(defaultSystemDefCount + 1, systemDefs.size());
        SnmpCollectionSystemDef sysDef = systemDefs.get(systemDefs.size()-1);
        assertEquals("3Com Routers", sysDef.getName());
        assertEquals(".1.3.6.1.4.1.43.", sysDef.getSysoidMask());
        assertNotNull(sysDef.getMibGroupNames());

        // No resource types in data
        List<SnmpCollectionResourceType> resourceTypes = snmpCollectionResourceTypeDao.findAll();
        assertEquals(defaultResourceTypeCount, resourceTypes.size());
    }

    public static DatacollectionGroup build3ComDatacollectionGroup() {
        Group group1 = new Group();
        group1.setName("3com-router-perf");
        group1.setIfType("ignore");
        group1.setMibObjs(Arrays.asList(
                createMibObj(".1.3.6.1.4.1.43.2.33.1.1.2.1.4", "0", "a3perfBufMemAvail", "integer"),
                createMibObj(".1.3.6.1.4.1.43.2.33.1.1.2.1.5", "0", "a3perfBufMemFailed", "integer"),
                createMibObj(".1.3.6.1.4.1.43.2.33.1.1.2.1.3", "0", "a3perfBufMemTotal", "integer"),
                createMibObj(".1.3.6.1.4.1.43.2.33.1.1.1",     "0", "a3perfBufMemTotAvl", "integer")
        ));

        Group group2 = new Group();
        group2.setName("3com-router-sys");
        group2.setIfType("ignore");
        group2.setMibObjs(Arrays.asList(
                createMibObj(".1.3.6.1.4.1.43.2.13.3.1.1.5", "0", "a3sysMemSize", "integer"),
                createMibObj(".1.3.6.1.4.1.43.2.13.8.4",     "0", "a3sysCpuUtil", "integer")
        ));

        SystemDef systemDef = new SystemDef();
        systemDef.setName("3Com Routers");
        systemDef.setSysoidMask(".1.3.6.1.4.1.43.");


        DatacollectionGroup group = new DatacollectionGroup();
        group.setName("3Com");
        group.setGroups(Arrays.asList(group1, group2));
        group.setSystemDefs(List.of(systemDef));
        return group;
    }

    private static MibObj createMibObj(String oid, String instance, String alias, String type) {
        MibObj m = new MibObj();
        m.setOid(oid);
        m.setInstance(instance);
        m.setAlias(alias);
        m.setType(type);
        return m;
    }
}