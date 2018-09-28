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

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import java.util.List;

import javax.ws.rs.core.MultivaluedHashMap;

import org.junit.Test;
import org.opennms.netmgt.flows.filter.api.ExporterNodeFilter;
import org.opennms.netmgt.flows.filter.api.Filter;
import org.opennms.netmgt.flows.filter.api.NodeCriteria;
import org.opennms.netmgt.flows.filter.api.SnmpInterfaceIdFilter;
import org.opennms.netmgt.flows.filter.api.TimeRangeFilter;

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
}
