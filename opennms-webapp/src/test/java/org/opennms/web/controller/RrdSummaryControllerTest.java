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
