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

package org.opennms.features.topology.plugins.topo.linkd.internal;

import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.test.MockLogger;
import org.opennms.netmgt.model.CdpElement;
import org.opennms.netmgt.model.CdpLink;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;

public class LinkdTopologyProviderTest {


    private final static int AMOUNT_LINKS = 100000;
    private final static int AMOUNT_ELEMENTS = 20;
    private final static int AMOUNT_NODES = 5;

    private final static Logger LOG = LoggerFactory.getLogger(LinkdTopologyProviderTest.class);

    private List<OnmsNode> nodes;
    private List<CdpElement> cdpElements;
    private List<CdpLink> allLinks;
    private Random random;

    @Before
    public void createTestData() {
        random = new Random();
        nodes = createNodes();
        cdpElements = createCdpElements(nodes);
        allLinks = createLinks(cdpElements);
        // We don't want the debug messages to take time:
        System.setProperty(MockLogger.LOG_KEY_PREFIX + LinkdTopologyProvider.class.getName(), "INFO");
    }

    @Test
    public void newMethodShouldProvideSameOutputAsOldMethod() {
        MetricRegistry registry = Mockito.mock(MetricRegistry.class);
        LinkdTopologyProvider provider = new LinkdTopologyProvider(registry);

        Instant start = Instant.now();
        List<Pair<CdpLink, CdpLink>> matchesOld = provider.matchCdpLinks(cdpElements, allLinks);
        LOG.info("Finished matchCdpLinksOld() in {} ms, found {} matches", Duration.between(start, Instant.now()).toMillis(), matchesOld.size());

        start = Instant.now();
        List<Pair<CdpLink, CdpLink>> matchesNew = provider.matchCdpLinksNew(cdpElements, allLinks);
        LOG.info("Finished matchCdpLinksNew() in {} ms, found {} matches", Duration.between(start, Instant.now()).toMillis(), matchesNew.size());

        // not a very elegant comparator but hey we are only an innocent little unit test
        Comparator<Pair<CdpLink, CdpLink>> comparator = Comparator
                .comparing(pair -> pair.getKey().printTopology() + pair.getRight().printTopology());
        matchesOld.sort(comparator);
        matchesNew.sort(comparator);
        assertEquals(matchesOld, matchesNew);
    }

    private List<OnmsNode> createNodes() {
        ArrayList<OnmsNode> nodes = new ArrayList<>();
        for (int i = 0; i < AMOUNT_NODES; i++) {
            OnmsNode node = new OnmsNode();
            node.setId(i);
            nodes.add(node);
        }
        return nodes;
    }

    private List<CdpElement> createCdpElements(List<OnmsNode> nodes) {
        ArrayList<CdpElement> cdpElements = new ArrayList<>();
        for (int i = 0; i < AMOUNT_ELEMENTS; i++) {
            CdpElement cdpElement = new CdpElement();
            cdpElement.setNode(nodes.get(random.nextInt(nodes.size())));
            cdpElement.setCdpGlobalDeviceId("CdpElement"+i);
            cdpElements.add(cdpElement);
        }
        return cdpElements;
    }

    private List<CdpLink> createLinks(List<CdpElement> cdps) {
        List<CdpLink> links = new ArrayList<>();
        for (int i = 0; i < AMOUNT_LINKS; i++) {

            CdpLink cdpLink = new CdpLink();
            cdpLink.setId(i);
            cdpLink.setCdpCacheDeviceId(getRandom(cdpElements).getCdpGlobalDeviceId());
            // for even AMOUNT_LINKS we have a link that points at itself but that shouldn't be a problem for the test:
            cdpLink.setCdpInterfaceName(Integer.toString(AMOUNT_LINKS -i-1));
            cdpLink.setCdpCacheDevicePort(Integer.toString(i));
            cdpLink.setNode(getRandom(cdps).getNode());
            cdpLink.setCdpCacheAddressType(CdpLink.CiscoNetworkProtocolType.chaos);
            links.add(cdpLink);
        }
        return links;
    }

    private <E> E getRandom(List<E> list) {
        return list.get(random.nextInt(list.size()));
    }

}