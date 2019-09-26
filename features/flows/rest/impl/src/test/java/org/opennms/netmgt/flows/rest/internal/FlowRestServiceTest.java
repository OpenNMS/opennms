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

package org.opennms.netmgt.flows.rest.internal;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.flows.api.FlowRepository;
import org.opennms.netmgt.flows.filter.api.ExporterNodeFilter;
import org.opennms.netmgt.flows.filter.api.Filter;
import org.opennms.netmgt.flows.filter.api.NodeCriteria;
import org.opennms.netmgt.flows.filter.api.SnmpInterfaceIdFilter;
import org.opennms.netmgt.flows.filter.api.TimeRangeFilter;
import org.opennms.netmgt.flows.rest.FlowRestService;
import org.opennms.netmgt.flows.rest.model.FlowGraphUrlInfo;
import org.springframework.transaction.support.TransactionOperations;

public class FlowRestServiceTest {

    @Test
    public void canGenerateFiltersFromQueryString() {
        MultivaluedHashMap<String, String> queryParams = new MultivaluedHashMap<>();
        List<Filter> filters = FlowRestServiceImpl.getFiltersFromQueryString(queryParams);

        // We should generate a single time range filter when no query parameters are specified
        assertThat(filters, hasSize(1));
        assertThat(filters, hasItem(isA(TimeRangeFilter.class)));

        // If we set a start and end, we just should get a time range filter with those timestamps
        queryParams.putSingle("start", "0");
        queryParams.putSingle("end", "1");
        filters = FlowRestServiceImpl.getFiltersFromQueryString(queryParams);
        assertThat(filters, contains(new TimeRangeFilter(0, 1)));

        // If we set an ifIndex, we just should get an ifindex filter
        queryParams.putSingle("start", "0");
        queryParams.putSingle("end", "1");
        queryParams.putSingle("ifIndex", "99");
        filters = FlowRestServiceImpl.getFiltersFromQueryString(queryParams);
        assertThat(filters, contains(new TimeRangeFilter(0, 1),
                new SnmpInterfaceIdFilter(99)));

        // If we set an exporterNode, we just should get an export node filter
        queryParams.putSingle("exporterNode", "FS:FID");
        filters = FlowRestServiceImpl.getFiltersFromQueryString(queryParams);
        assertThat(filters, contains(new TimeRangeFilter(0, 1),
                new SnmpInterfaceIdFilter(99),
                new ExporterNodeFilter(new NodeCriteria("FS:FID"))));
    }

    @Test
    public void canGenerateFlowGraphUrlInfo() {
        // Mock the dependencies
        FlowRepository flowRepository = mock(FlowRepository.class);
        when(flowRepository.getFlowCount(any())).thenReturn(CompletableFuture.completedFuture(1L));
        NodeDao nodeDao = mock(NodeDao.class);
        SnmpInterfaceDao snmpInterfaceDao = mock(SnmpInterfaceDao.class);
        TransactionOperations transactionOperations = mock(TransactionOperations.class);
        FlowRestServiceImpl flowRestService = new FlowRestServiceImpl(flowRepository, nodeDao, snmpInterfaceDao, transactionOperations);

        // Set the URL
        flowRestService.setFlowGraphUrl("https://grafana:3000/d/eWsVEL6zz/flows?orgId=1&var-node=$nodeId&var-interface=$ifIndex&from=$start&to=$end");

        // Build the URL from an empty list attributes
        MultivaluedHashMap<String, String> queryParms = new MultivaluedHashMap<>();
        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getQueryParameters()).thenReturn(queryParms);
        FlowGraphUrlInfo flowGraphUrlInfo = flowRestService.getFlowGraphUrlInfo(uriInfo);

        // Verify
        assertThat(flowGraphUrlInfo.getFlowCount(), equalTo(1L));
        assertThat(flowGraphUrlInfo.getFlowGraphUrl(), equalTo("https://grafana:3000/d/eWsVEL6zz/flows?orgId=1&var-node=&var-interface=&from=&to="));

        // Now build the URL from a fully populated set of attributes
        queryParms = new MultivaluedHashMap<>();
        queryParms.add("exporterNode", "1");
        queryParms.add("ifIndex", "2");
        queryParms.add("start", "3");
        queryParms.add("end", "4");
        when(uriInfo.getQueryParameters()).thenReturn(queryParms);
        flowGraphUrlInfo = flowRestService.getFlowGraphUrlInfo(uriInfo);

        // Verify
        assertThat(flowGraphUrlInfo.getFlowCount(), equalTo(1L));
        assertThat(flowGraphUrlInfo.getFlowGraphUrl(), equalTo("https://grafana:3000/d/eWsVEL6zz/flows?orgId=1&var-node=1&var-interface=2&from=3&to=4"));
    }
}
