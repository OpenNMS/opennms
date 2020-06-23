/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.web.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.config.attrsummary.Summary;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.web.svclayer.model.SummarySpecification;
import org.opennms.web.svclayer.rrd.support.DefaultRrdSummaryService;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.ImmutableSortedMap;

public class RrdSummaryControllerTest {

    private RrdSummaryController m_controller;
    private DefaultRrdSummaryService m_rrdSummaryService;

    @Before
    public void setUp() {
        m_rrdSummaryService = new DefaultRrdSummaryService();

        m_controller = new RrdSummaryController();
        m_controller.setRrdSummaryService(m_rrdSummaryService);
    }

    /**
     * Verifies that an empty summary can be generated so an existing
     * node without any resources.
     */
    @Test
    public void canGenerateEmptySummary() {
        // Return a single node when called using the given filter
        String rule = "ipaddr iplike 172.20.1.1";
        FilterDao filterDao = mock(FilterDao.class);
        when(filterDao.getNodeMap(rule)).thenReturn(ImmutableSortedMap.of(1, "node1"));

        OnmsNode node = mock(OnmsNode.class);
        NodeDao nodeDao = mock(NodeDao.class);
        when(nodeDao.load(1)).thenReturn(node);

        OnmsResource resource = mock(OnmsResource.class);
        ResourceDao resourceDao = mock(ResourceDao.class);
        when(resourceDao.getResourceForNode(node)).thenReturn(resource);

        // Use our mocks
        m_rrdSummaryService.setFilterDao(filterDao);
        m_rrdSummaryService.setNodeDao(nodeDao);
        m_rrdSummaryService.setResourceDao(resourceDao);

        // Building the summary spec.
        SummarySpecification summarySpec = new SummarySpecification();
        summarySpec.setFilterRule("ipaddr iplike 172.20.1.1");
        summarySpec.setStartTime(1472746964);
        summarySpec.setEndTime(1473265364);
        summarySpec.setAttributeSieve(".*");

        // Invoke the controller
        HttpServletResponse response = new MockHttpServletResponse();
        ModelAndView mv = m_controller.processFormSubmission(response, summarySpec);
        Summary summary = (Summary)mv.getModel().get("summary");

        // Verify the response
        assertEquals(0, summary.getResources().size());
    }
}
