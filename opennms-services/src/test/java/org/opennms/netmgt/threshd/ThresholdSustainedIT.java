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

package org.opennms.netmgt.threshd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.collection.test.MockCollectionAgent;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.config.ThreshdConfigFactory;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThresholdSustainedIT {
    private static final Logger LOG = LoggerFactory.getLogger(ThresholdSustainedIT.class);

    final MockCollectionAgent agent = new MockCollectionAgent(1, "n1", InetAddressUtils.ONE_TWENTY_SEVEN);

    MockEventIpcManager eventIpcManager;
    EventAnticipator eventAnticipator;
    ThresholdingVisitor thresholdingVisitor;
    long timestamp = 0;

    @Before
    public void setUp() throws Exception {
        // Use a mock FilterDao that always returns 127.0.0.1 in the active IP list
        FilterDao filterDao = mock(FilterDao.class);
        when(filterDao.getActiveIPAddressList(any(String.class)))
                .thenReturn(Collections.singletonList(InetAddressUtils.ONE_TWENTY_SEVEN));
        FilterDaoFactory.setInstance(filterDao);

        // Wire in the MockEventIpcManager
        eventIpcManager = new MockEventIpcManager();
        eventAnticipator = eventIpcManager.getEventAnticipator();
        EventIpcManagerFactory.setIpcManager(eventIpcManager);


        // Setup the thresholding visitor
        initFactories("/threshd-configuration-sustained.xml","/test-thresholds-sustained.xml");
        RrdRepository repository = new RrdRepository();
        ServiceParameters params = new ServiceParameters(Collections.emptyMap());
        ResourceStorageDao resourceStorageDao = mock(ResourceStorageDao.class);
        thresholdingVisitor = ThresholdingVisitor.create(agent.getNodeId(), agent.getHostAddress(), "HTTP",
                repository, params, resourceStorageDao);
    }

    @Test
    public void canGenerateThresholdSustainedEventsForHighThreshold() {
        Event e = generateCollectionSetAndVisitWithThresholder("coffee-high", 6);
        assertThat(e.getUei(), equalTo(EventConstants.HIGH_THRESHOLD_EVENT_UEI));

        e = generateCollectionSetAndVisitWithThresholder("coffee-high", 6);
        assertThat(e.getUei(), equalTo(EventConstants.HIGH_THRESHOLD_SUSTAINED_EVENT_UEI));

        e = generateCollectionSetAndVisitWithThresholder("coffee-high", 3);
        assertThat(e.getUei(), equalTo(EventConstants.HIGH_THRESHOLD_REARM_EVENT_UEI));
    }

    @Test
    public void canGenerateThresholdSustainedEventsForLowThreshold() {
        Event e = generateCollectionSetAndVisitWithThresholder("coffee-low" , 1);
        assertThat(e.getUei(), equalTo(EventConstants.LOW_THRESHOLD_EVENT_UEI));

        e = generateCollectionSetAndVisitWithThresholder("coffee-low", 1);
        assertThat(e.getUei(), equalTo(EventConstants.LOW_THRESHOLD_SUSTAINED_EVENT_UEI));

        e = generateCollectionSetAndVisitWithThresholder("coffee-low", 3);
        assertThat(e.getUei(), equalTo(EventConstants.LOW_THRESHOLD_REARM_EVENT_UEI));
    }

    private Event generateCollectionSetAndVisitWithThresholder(String attribute, int value) {
        eventAnticipator.reset();
        NodeLevelResource nodeLevelResource = new NodeLevelResource(agent.getNodeId());
        CollectionSet collectionSet = new CollectionSetBuilder(agent)
                .withTimestamp(new Date(timestamp))
                .withNumericAttribute(nodeLevelResource, "beverages", attribute, value, AttributeType.GAUGE)
                .build();
        timestamp += TimeUnit.MINUTES.toMillis(5);
        collectionSet.visit(thresholdingVisitor);

        if (eventAnticipator.getUnanticipatedEvents().isEmpty()) {
            return null;
        } else if (eventAnticipator.getUnanticipatedEvents().size() == 1) {
            return eventAnticipator.getUnanticipatedEvents().get(0);
        } else {
            throw new IllegalStateException("Expected 0 or 1 events to be generated, but got: " + eventAnticipator.getUnanticipatedEvents());
        }
    }

    private void initFactories(String threshd, String thresholds) throws Exception {
        LOG.info("Initialize Threshold Factories");
        ThresholdingConfigFactory.setInstance(new ThresholdingConfigFactory(getClass().getResourceAsStream(thresholds)));
        ThreshdConfigFactory.setInstance(new ThreshdConfigFactory(getClass().getResourceAsStream(threshd)));
    }
}
