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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

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
@JUnitTemporaryDatabase(dirtiesContext=false)
public class TopologyPerformanceIT implements InitializingBean {

    private final static int AMOUNT_LINKS = 100000;
    private final static int AMOUNT_ELEMENTS = 20;
    private final static int AMOUNT_NODES = 5;


    @Autowired
    private MonitoringLocationDao monitoringLocationDao;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private CdpLinkDao cdpLinkDao;

    @Autowired
    private CdpElementDao cdpElementDao;

    @Autowired
    private DatabasePopulator populator;

    private Random random;

    @Override
    public void afterPropertiesSet() throws Exception {
        org.opennms.core.spring.BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() {
        // populator.populateDatabase();
        random = new Random(42);
    }

    @After
    public void tearDown() {
        populator.resetDatabase();
    }


    @Test
    @Transactional
    public void createCdpNetwork() {
        List<OnmsNode> nodes = createNodes();
        save(nodes, nodeDao);
        List<CdpElement> cdpElements = createCdpElements(nodes);
        save(cdpElements, cdpElementDao);
        List<CdpLink> links = createCdpLinks(cdpElements);
        save(links, cdpLinkDao);

        // TODO: patrick.schweizer: now what?
    }

    private <T, K extends Serializable> void save(List<T> elements, OnmsDao<T, K> dao){
        for(T element : elements){
            dao.save(element);
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
            node.setId(i);
            node.setLabel("myNode"+i);
            node.setLocation(location);
            nodes.add(node);
        }
        return nodes;
    }

    private List<CdpElement> createCdpElements(List<OnmsNode> nodes) {
        ArrayList<CdpElement> cdpElements = new ArrayList<>();
        for (int i = 0; i < AMOUNT_ELEMENTS; i++) {
            cdpElements.add(createCdpElement(getRandom(nodes), "CdpElement"+i));
        }
        return cdpElements;
    }

    private CdpElement createCdpElement(OnmsNode node, String globalDeviceId) {
        CdpElement cdpElement = new CdpElement();
        cdpElement.setNode(node);
        cdpElement.setCdpGlobalDeviceId(globalDeviceId);
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
