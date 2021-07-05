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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpCollector;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpCollectorDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpGlobalIpRib;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpGlobalIpRibDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpIpRibLogDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpPeer;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpPeerDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRouter;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRouterDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRpkiInfo;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRpkiInfoDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpUnicastPrefix;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpUnicastPrefixDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.State;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.StatsIpOrigins;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.StatsPeerRib;
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
@JUnitTemporaryDatabase(reuseDatabase = false, dirtiesContext = true)
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

    @Autowired
    private BmpIpRibLogDao bmpIpRibLogDao;

    @Autowired
    private BmpRpkiInfoDao bmpRpkiInfoDao;


    @Test
    public void testPersistence() {

        BmpCollector bmpCollector = new BmpCollector();
        bmpCollector.setHashId("91e3a7ff9f5676ed6ae6fcd8a6b455ec");
        bmpCollector.setState(State.UP);
        bmpCollector.setAdminId("admin1");
        bmpCollector.setName("collector1");
        bmpCollector.setRoutersCount(2);
        bmpCollector.setTimestamp(new Date());
        Long id = bmpCollectorDao.save(bmpCollector);
        Assert.assertNotNull(id);
        BmpCollector retrieved = bmpCollectorDao.findByCollectorHashId("91e3a7ff9f5676ed6ae6fcd8a6b455ec");
        assertEquals(bmpCollector.getName(), retrieved.getName());
        assertEquals(bmpCollector.getHashId(), retrieved.getHashId());
        BmpRouter bmpRouter = new BmpRouter();
        bmpRouter.setState(State.UP);
        bmpRouter.setHashId("81e4a7ff8f5673ed6ae6fcd9a3b452bg");
        bmpRouter.setName("router-1");
        bmpRouter.setIpAddress("10.1.4.10");
        bmpRouter.setTimestamp(new Date());
        bmpRouter.setCollectorHashId("91e3a7ff9f5676ed6ae6fcd8a6b455ec");
        bmpRouterDao.saveOrUpdate(bmpRouter);
        BmpRouter persistedRouter = bmpRouterDao.findByRouterHashId("81e4a7ff8f5673ed6ae6fcd9a3b452bg");
        assertEquals(bmpRouter.getName(), persistedRouter.getName());
        assertEquals(bmpRouter.getHashId(), persistedRouter.getHashId());
        List<BmpRouter> routers = bmpRouterDao.findRoutersByCollectorHashId("91e3a7ff9f5676ed6ae6fcd8a6b455ec");
        Assert.assertFalse(routers.isEmpty());
        BmpRouter result = routers.get(0);
        assertEquals(bmpRouter.getHashId(), result.getHashId());
    }

    @Test
    public void testGlobalIpRibsDeletion() {

        Instant thirtySecondsBefore = Instant.now().minusSeconds(30);

        BmpGlobalIpRib bmpGlobalIpRib = new BmpGlobalIpRib();
        bmpGlobalIpRib.setPrefix("10.0.0.1");
        bmpGlobalIpRib.setRecvOriginAs(64512L);
        bmpGlobalIpRib.setPrefixLen(22);
        bmpGlobalIpRib.setTimeStamp(Date.from(thirtySecondsBefore));
        bmpGlobalIpRib.setIrrSource("ARIN-WHOIS");
        bmpGlobalIpRib.setIrrOriginAs(2314L);
        bmpGlobalIpRib.setNumPeers(4);
        bmpGlobalIpRib.setShouldDelete(true);

        BmpGlobalIpRib bmpGlobalIpRib1 = new BmpGlobalIpRib();
        bmpGlobalIpRib1.setPrefix("10.0.0.2");
        bmpGlobalIpRib1.setRecvOriginAs(6452L);
        bmpGlobalIpRib1.setPrefixLen(12);
        bmpGlobalIpRib1.setTimeStamp(Date.from(thirtySecondsBefore));
        bmpGlobalIpRib1.setIrrSource("ARIN-WHOIS");
        bmpGlobalIpRib1.setIrrOriginAs(2316L);
        bmpGlobalIpRib1.setNumPeers(4);
        bmpGlobalIpRib1.setShouldDelete(true);

        bmpGlobalIpRibDao.saveOrUpdate(bmpGlobalIpRib);
        bmpGlobalIpRibDao.saveOrUpdate(bmpGlobalIpRib1);

        List<BmpGlobalIpRib> result = bmpGlobalIpRibDao.findAll();
        Assert.assertFalse(result.isEmpty());

        BmpGlobalIpRib bmpGlobalIpRib2 = new BmpGlobalIpRib();
        bmpGlobalIpRib2.setPrefix("10.0.0.2");
        bmpGlobalIpRib2.setPrefixLen(1);
        bmpGlobalIpRib2.setTimeStamp(Date.from(Instant.now()));
        bmpGlobalIpRib2.setRecvOriginAs(64512L);
        bmpGlobalIpRib2.setNumPeers(2);
        bmpGlobalIpRib2.setIrrSource("ARIN-WHOIS");
        bmpGlobalIpRib2.setIrrOriginAs(2315L);

        bmpGlobalIpRibDao.saveOrUpdate(bmpGlobalIpRib2);
        result = bmpGlobalIpRibDao.findAll();
        assertEquals(3, result.size());
        List<StatsIpOrigins> statsIpOrigins = bmpGlobalIpRibDao.getStatsIpOrigins();
        assertEquals(2, statsIpOrigins.size());
        StatsIpOrigins stats = statsIpOrigins.get(0);
        assertEquals(2L, stats.getV4prefixes().longValue());
        assertEquals(0L, stats.getV6withrpki().longValue());
        List<BigInteger> asnList = bmpGlobalIpRibDao.getAsnsNotExistInAsnInfo();
        assertThat(asnList, Matchers.hasSize(2));

        result = bmpGlobalIpRibDao.findGlobalRibsBeforeGivenTime(5L);
        assertThat(result, Matchers.hasSize(2));
        int deleted = bmpGlobalIpRibDao.deleteGlobalRibsBeforeGivenTime(5L);
        assertThat(deleted, Matchers.equalTo(2));
    }


    @Test
    public void testPeerRibPrefixCount() {

        long oneMinBack = new Date().getTime() - 60000;
        Date lastUpdated = new Date(oneMinBack);
        BmpUnicastPrefix bmpUnicastPrefix = buildBmpUnicastPrefix(lastUpdated);
        BmpPeer bmpPeer = buildBmpPeer(lastUpdated);
        BmpRouter bmpRouter = buildBmpRouter(lastUpdated);
        String collectorHashId = "91e3a7ff9f5676ed6ae6fcd8a6b455ec";
        BmpCollector bmpCollector = buildBmpCollector(collectorHashId, lastUpdated);
        bmpCollectorDao.save(bmpCollector);
        List<BmpCollector> collectors = bmpCollectorDao.findAll();
        Assert.assertFalse(collectors.isEmpty());
        bmpRouter.setCollectorHashId(collectorHashId);
        bmpRouterDao.saveOrUpdate(bmpRouter);
        List<BmpRouter> routers = bmpRouterDao.findAll();
        Assert.assertFalse(routers.isEmpty());
        bmpPeer.setBmpRouter(bmpRouter);
        bmpPeerDao.saveOrUpdate(bmpPeer);
        List<BmpPeer> peers = bmpPeerDao.findAll();
        Assert.assertFalse(peers.isEmpty());
        bmpUnicastPrefix.setBmpPeer(bmpPeer);
        bmpUnicastPrefixDao.saveOrUpdate(bmpUnicastPrefix);
        List<BmpUnicastPrefix> prefixes = bmpUnicastPrefixDao.findAll();
        Assert.assertFalse(prefixes.isEmpty());
        List<StatsPeerRib> statsPeerRibs = bmpUnicastPrefixDao.getPeerRibCountsByPeer();
        Assert.assertFalse(statsPeerRibs.isEmpty());
        assertEquals(1L, statsPeerRibs.get(0).getV4prefixes().longValue());
    }

    @Test
    public void testRpkiValidator() {

        BmpRpkiInfo bmpRpkiInfo = new BmpRpkiInfo();
        bmpRpkiInfo.setPrefix("10.1.23.34");
        bmpRpkiInfo.setPrefixLen(22);
        bmpRpkiInfo.setPrefixLenMax(24);
        bmpRpkiInfo.setOriginAs(5645L);
        bmpRpkiInfo.setTimestamp(new Date());
        bmpRpkiInfoDao.saveOrUpdate(bmpRpkiInfo);
        BmpRpkiInfo retrieved = bmpRpkiInfoDao.findBmpRpkiInfoWith("10.1.23.34", 24, 5645L);
        Assert.assertNotNull(retrieved);


    }

    private BmpCollector buildBmpCollector(String collectorHashId, Date lastUpdated) {
        BmpCollector bmpCollector = new BmpCollector();
        bmpCollector.setHashId(collectorHashId);
        bmpCollector.setState(State.UP);
        bmpCollector.setAdminId("admin1");
        bmpCollector.setName("collector1");
        bmpCollector.setRoutersCount(2);
        bmpCollector.setTimestamp(lastUpdated);
        return bmpCollector;
    }

    private BmpRouter buildBmpRouter(Date lastUpdated) {
        BmpRouter bmpRouter = new BmpRouter();
        bmpRouter.setState(State.UP);
        bmpRouter.setHashId("81e4a7ff8f5673ed6ae6fcd9a3b452bg");
        bmpRouter.setName("router-1");
        bmpRouter.setIpAddress("10.1.4.10");
        bmpRouter.setTimestamp(lastUpdated);
        return bmpRouter;
    }


    private BmpPeer buildBmpPeer(Date lastUpdated) {
        BmpPeer bmpPeer = new BmpPeer();
        bmpPeer.setHashId("61e5a7ff9f5433ed6ae6fcd9a2b432gf");
        bmpPeer.setPeerRd("0:0");
        bmpPeer.setIpv4(true);
        bmpPeer.setPeerAddr("10.0.0.3");
        bmpPeer.setState(State.UP);
        bmpPeer.setL3VPNPeer(false);
        bmpPeer.setPrePolicy(true);
        bmpPeer.setLocRib(false);
        bmpPeer.setLocRibFiltered(false);
        bmpPeer.setPeerAsn(2083L);
        bmpPeer.setTimestamp(lastUpdated);
        return bmpPeer;
    }

    private BmpUnicastPrefix buildBmpUnicastPrefix(Date lastUpdated) {
        BmpUnicastPrefix bmpUnicastPrefix = new BmpUnicastPrefix();
        bmpUnicastPrefix.setHashId("83e12a7ff8f5673es6ae6fcd9a3b345uy");
        bmpUnicastPrefix.setPrefix("10.0.0.1");
        bmpUnicastPrefix.setPrefixLen(15);
        bmpUnicastPrefix.setWithDrawn(false);
        bmpUnicastPrefix.setFirstAddedTimestamp(lastUpdated);
        bmpUnicastPrefix.setBaseAttrHashId("23212a7ff9f5433ed6ae6fcd9a2b432gf");
        bmpUnicastPrefix.setAdjRibIn(false);
        bmpUnicastPrefix.setPrePolicy(true);
        bmpUnicastPrefix.setIpv4(true);
        bmpUnicastPrefix.setTimestamp(lastUpdated);
        return bmpUnicastPrefix;
    }


}
