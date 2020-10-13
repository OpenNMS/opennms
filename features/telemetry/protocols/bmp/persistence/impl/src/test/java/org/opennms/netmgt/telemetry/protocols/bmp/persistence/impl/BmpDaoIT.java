/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.bmp.persistence.impl;

import static org.junit.Assert.fail;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpCollector;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpCollectorDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpGlobalIpRib;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpGlobalIpRibDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpPeerDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRouter;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRouterDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpUnicastPrefixDao;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class BmpDaoIT {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private BmpCollectorDao bmpCollectorDao;

    @Autowired
    private BmpRouterDao bmpRouterDao;

    @Autowired
    private BmpPeerDao bmpPeerDao;

    @Autowired
    private BmpUnicastPrefixDao bmpUnicastPrefixDao;

    @Autowired
    private BmpGlobalIpRibDao bmpGlobalIpRibDao;


    @Test
    public void testPersistence() {

        BmpCollector bmpCollector = new BmpCollector();
        bmpCollector.setHashId("91e3a7ff9f5676ed6ae6fcd8a6b455ec");
        bmpCollector.setState(true);
        bmpCollector.setAdminId("admin1");
        bmpCollector.setName("collector1");
        bmpCollector.setRoutersCount(2);
        bmpCollector.setTimestamp(new Date());
        Long id = bmpCollectorDao.save(bmpCollector);
        Assert.assertNotNull(id);
        BmpCollector retrieved = bmpCollectorDao.findByCollectorHashId("91e3a7ff9f5676ed6ae6fcd8a6b455ec");
        Assert.assertEquals(bmpCollector.getName(), retrieved.getName());
        Assert.assertEquals(bmpCollector.getHashId(), retrieved.getHashId());
        BmpRouter bmpRouter = new BmpRouter();
        bmpRouter.setState(true);
        bmpRouter.setHashId("81e4a7ff8f5673ed6ae6fcd9a3b452bg");
        bmpRouter.setName("router-1");
        bmpRouter.setIpAddress("10.1.4.10");
        bmpRouter.setTimestamp(new Date());
        bmpRouter.setBmpCollector(bmpCollector);
        bmpRouterDao.saveOrUpdate(bmpRouter);
        BmpRouter persistedRouter = bmpRouterDao.findByRouterHashId("81e4a7ff8f5673ed6ae6fcd9a3b452bg");
        Assert.assertEquals(bmpRouter.getName(), persistedRouter.getName());
        Assert.assertEquals(bmpRouter.getHashId(), persistedRouter.getHashId());
        List<BmpRouter> routers = bmpRouterDao.findRoutersByCollectorHashId("91e3a7ff9f5676ed6ae6fcd8a6b455ec");
        Assert.assertFalse(routers.isEmpty());
        BmpRouter result = routers.get(0);
        Assert.assertEquals(bmpRouter.getHashId(), result.getHashId());
    }

    @Test
    public void testGlobalIpRibsPersistence() {

        BmpGlobalIpRib bmpGlobalIpRib = new BmpGlobalIpRib();
        bmpGlobalIpRib.setPrefix("10.0.0.1");
        bmpGlobalIpRib.setRecvOriginAs(64512L);
        bmpGlobalIpRib.setPrefixLen(1);
        bmpGlobalIpRib.setTimeStamp(Date.from(Instant.now()));

        bmpGlobalIpRibDao.saveOrUpdate(bmpGlobalIpRib);

        List<BmpGlobalIpRib> result = bmpGlobalIpRibDao.findAll();
        Assert.assertFalse(result.isEmpty());


        BmpGlobalIpRib bmpGlobalIpRib2 = new BmpGlobalIpRib();
        bmpGlobalIpRib2.setPrefix("10.0.0.1");
        bmpGlobalIpRib2.setPrefixLen(1);
        bmpGlobalIpRib2.setTimeStamp(Date.from(Instant.now()));
        bmpGlobalIpRib2.setRecvOriginAs(64512L);

        // This should fail.
        try {
            bmpGlobalIpRibDao.saveOrUpdate(bmpGlobalIpRib2);
            fail();
        } catch (Exception e) {

        }
        result = bmpGlobalIpRibDao.findAll();
        Assert.assertEquals(1, result.size());

    }
}
