/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.AccessPointStatus;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsAccessPoint;
import org.opennms.netmgt.model.OnmsAccessPointCollection;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * AccessPointDaoTest
 * </p>
 * 
 * @author <a href="mailto:jwhite@datavalet.com">Jesse White</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "classpath:/META-INF/opennms/applicationContext-soa.xml",
    "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
    "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
    "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
    "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
    "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
    "classpath*:/META-INF/opennms/component-dao.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class AccessPointDaoTest implements InitializingBean {
    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private AccessPointDao m_accessPointDao;

    private final static String AP1_MAC = "00:01:02:03:04:05";

    private final static String AP2_MAC = "07:08:09:0A:0B:0C";

    private final static String AP3_MAC = "0C:0D:0E:0F:01:02";

    @Override
    public void afterPropertiesSet() throws Exception {
        assertNotNull(m_nodeDao);
        assertNotNull(m_accessPointDao);
    }

    @Before
    public void setUp() throws Exception {

    }

    private void addNewAccessPoint(String name, String mac, String pkg) {
        NetworkBuilder nb = new NetworkBuilder();

        nb.addNode(name).setForeignSource("apmd").setForeignId(name);
        nb.addInterface("169.254.0.1");
        m_nodeDao.save(nb.getCurrentNode());

        final OnmsAccessPoint ap1 = new OnmsAccessPoint(mac, nb.getCurrentNode().getId(), pkg);
        ap1.setStatus(AccessPointStatus.UNKNOWN);
        m_accessPointDao.save(ap1);

        m_nodeDao.flush();
        m_accessPointDao.flush();
    }

    @Test
    @Transactional
    public void testFindByPhysAddr() {
        addNewAccessPoint("ap1", AP1_MAC, "default-package");
        addNewAccessPoint("ap2", AP2_MAC, "not-default-package");

        OnmsAccessPoint ap1 = m_accessPointDao.get(AP1_MAC);
        assertEquals("default-package", ap1.getPollingPackage());

        OnmsAccessPoint ap2 = m_accessPointDao.get(AP2_MAC);
        assertEquals("not-default-package", ap2.getPollingPackage());
    }

    @Test
    @Transactional
    public void testFindByPackage() {
        addNewAccessPoint("ap1", AP1_MAC, "package1");
        addNewAccessPoint("ap2", AP2_MAC, "package1");
        addNewAccessPoint("ap3", AP3_MAC, "package2");

        OnmsAccessPointCollection apsInPkg = m_accessPointDao.findByPackage("package1");

        assertEquals("There should be two APs in the package.", 2, apsInPkg.size());

        List<String> unqPkgNames = m_accessPointDao.findDistinctPackagesLike("%ack%");

        assertEquals(2, unqPkgNames.size());
        assertEquals(true, unqPkgNames.contains("package1"));
        assertEquals(true, unqPkgNames.contains("package2"));
    }
}
