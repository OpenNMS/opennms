/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.measurements.impl;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.Test;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.measurements.api.FetchResults;
import org.opennms.netmgt.measurements.model.Source;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.newts.api.Measurement;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Results;
import org.opennms.newts.api.Results.Row;
import org.opennms.newts.api.SampleRepository;
import org.opennms.newts.api.Timestamp;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class NewtsFetchStrategyTest {

    @Test
    public void fetchIt() throws Exception {
        RrdGraphAttribute attribute = new RrdGraphAttribute("icmp", "", "response:127.0.0.1:icmp");

        Set<OnmsAttribute> attributes = Sets.newHashSet(attribute);
        OnmsResourceType type = EasyMock.createNiceMock(OnmsResourceType.class);
        OnmsResource resource = new OnmsResource("", "", type, attributes, ResourcePath.get("foo"));

        ResourceDao resourceDao = EasyMock.createNiceMock(ResourceDao.class);
        EasyMock.expect(resourceDao.getResourceById("nodeSource[NODES:1430502148137].responseTime[127.0.0.1]")).andReturn(resource);

        SampleRepository sampleRepository = EasyMock.createNiceMock(SampleRepository.class);
        Results<Measurement> results = new Results<Measurement>();
        Resource res = new Resource("");
        Row<Measurement> row = new Row<Measurement>(Timestamp.fromEpochSeconds(0), res);
        Measurement measurement = new Measurement(Timestamp.fromEpochSeconds(0), res, "", 0.0d);
        row.addElement(measurement);
        results.addRow(row);

        EasyMock.expect(sampleRepository.select(
                EasyMock.anyObject(), EasyMock.anyObject(), EasyMock.anyObject(), EasyMock.anyObject(), EasyMock.anyObject()
                )).andReturn(results);

        EasyMock.replay(resourceDao, sampleRepository);

        NewtsFetchStrategy nfs = new NewtsFetchStrategy();
        nfs.setResourceDao(resourceDao);
        nfs.setSampleRepository(sampleRepository);

        Source source = new Source();
        source.setAggregation("AVERAGE");
        source.setAttribute("icmp");
        source.setLabel("icmp");
        source.setResourceId("nodeSource[NODES:1430502148137].responseTime[127.0.0.1]");
        source.setTransient(false);

        List<Source> sources = Lists.newArrayList(source);

        FetchResults fetchResults = nfs.fetch(1431047069000L - (60 * 60 * 1000), 1431047069000L, 300 * 1000, 0, sources);
        assertEquals(1, fetchResults.getColumns().keySet().size());

        EasyMock.verify(resourceDao, sampleRepository);
    }
}
