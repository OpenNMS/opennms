/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

import static junit.framework.TestCase.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.db.DataSourceFactory;

import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.dao.api.CdpElementDao;
import org.opennms.netmgt.dao.api.CdpLinkDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OnmsDao;
import org.opennms.netmgt.model.CdpElement;
import org.opennms.netmgt.model.CdpLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OspfElement;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml"
})
@JUnitConfigurationEnvironment
public class TopologyGeneratorToolIT implements InitializingBean {

    private final static int AMOUNT_NODES = 2028;
    private final static int AMOUNT_ELEMENTS = 1844;
    private final static int AMOUNT_LINKS = 35717;

    private final static Logger LOG = LoggerFactory.getLogger(TopologyGeneratorToolIT.class);

    static {
        setUpBeforeAll();
    }

    @Autowired
    private MonitoringLocationDao monitoringLocationDao;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private CdpLinkDao cdpLinkDao;

    @Autowired
    private CdpElementDao cdpElementDao;

    private Random random;

    private static void setUpBeforeAll(){
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/opennms");
        config.setUsername("opennms");
        config.setPassword("opennms");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        HikariDataSource ds = new HikariDataSource(config);
        DataSourceFactory.setInstance(ds);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        org.opennms.core.spring.BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() {
        random = new Random(42);

        // do basic checks to get configuration right:
        assertTrue("we need at least as many nodes as elements", AMOUNT_NODES > AMOUNT_ELEMENTS);
        assertTrue("we need at least 2 nodes", AMOUNT_NODES >= 2);
        assertTrue("we need at least 2 elements", AMOUNT_ELEMENTS >= 2);
        assertTrue("we need at least 1 link", AMOUNT_LINKS > 0);
    }

    @Test
    public void createCdpNetwork() {
        List<OnmsNode> nodes = createNodes();
        save(nodes, nodeDao);
        List<CdpElement> cdpElements = createCdpElements(nodes);
        save(cdpElements, cdpElementDao);
        List<CdpLink> links = createCdpLinks(cdpElements);
        save(links, cdpLinkDao);
    }

    private <T, K extends Serializable> void save(List<T> elements, OnmsDao<T, K> dao){
        int count = 0;
        for(T element : elements){
            dao.save(element);
            count++;
            if(count%100==0){
                LOG.info("created {} {}s", count, element.getClass().getSimpleName());
            }
        }
        dao.flush();
    }


    private List<OnmsNode> createNodes() {
        return createNodes(AMOUNT_NODES);
    }

    private List<OnmsNode> createNodes(int amount) {
        OnmsMonitoringLocation location = monitoringLocationDao.getDefaultLocation();
        ArrayList<OnmsNode> nodes = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            OnmsNode node = new OnmsNode();
            node.setLabel("myNode"+i);
            node.setLocation(location);
            nodes.add(node);
        }
        return nodes;
    }

    private List<CdpElement> createCdpElements(List<OnmsNode> nodes) {
        ArrayList<CdpElement> cdpElements = new ArrayList<>();
        for (int i = 0; i < AMOUNT_ELEMENTS; i++) {
            OnmsNode node = nodes.get(i);
            cdpElements.add(createCdpElement(node));
        }
        return cdpElements;
    }

    private CdpElement createCdpElement(OnmsNode node) {
        CdpElement cdpElement = new CdpElement();
        cdpElement.setNode(node);
        cdpElement.setCdpGlobalDeviceId("CdpElementForNode"+node.getId());
        cdpElement.setCdpGlobalRun(OspfElement.TruthValue.FALSE);
        cdpElement.setCdpNodeLastPollTime(new Date());
        return cdpElement;
    }

    private List<CdpLink> createCdpLinks(List<CdpElement> cdpElements) {
        List<CdpLink> links = new ArrayList<>();
        for (int i = 0; i < AMOUNT_LINKS; i++) {

            CdpLink cdpLink = createCdpLink(i,
                    getRandom(cdpElements).getNode(),
                    getRandom(cdpElements).getCdpGlobalDeviceId(),
                    Integer.toString(AMOUNT_LINKS -i-1),
                    Integer.toString(i));
            links.add(cdpLink);
        }
        return links;
    }

    private CdpLink createCdpLink(int id, OnmsNode node, String cdpCacheDeviceId, String cdpInterfaceName, String cdpCacheDevicePort) {
        CdpLink link = new CdpLink();
        link.setId(id);
        link.setCdpCacheDeviceId(cdpCacheDeviceId);
        link.setCdpInterfaceName(cdpInterfaceName);
        link.setCdpCacheDevicePort(cdpCacheDevicePort);
        link.setNode(node);
        link.setCdpCacheAddressType(CdpLink.CiscoNetworkProtocolType.chaos);
        link.setCdpCacheAddress("CdpCacheAddress");
        link.setCdpCacheDeviceIndex(33);
        link.setCdpCacheDevicePlatform("CdpCacheDevicePlatform");
        link.setCdpCacheIfIndex(33);
        link.setCdpCacheVersion("CdpCacheVersion");
        link.setCdpLinkLastPollTime(new Date());
        return link;
    }

    private <E> E getRandom(List<E> list) {
        return list.get(random.nextInt(list.size()));
    }

}
