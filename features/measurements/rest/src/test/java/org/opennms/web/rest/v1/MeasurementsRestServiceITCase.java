/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2015 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v1;

import javax.ws.rs.WebApplicationException;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.support.FilesystemResourceStorageDao;
import org.opennms.netmgt.measurements.model.QueryRequest;
import org.opennms.netmgt.measurements.model.Source;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

/**
 * Contains tests that can run irrespective of the fetch strategy.
 *
 * @author Jesse White <jesse@opennms.org>
 */
public abstract class MeasurementsRestServiceITCase {

    @Autowired
    protected MeasurementsRestService m_svc;

    @Autowired
    protected NodeDao m_nodeDao;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    protected FilesystemResourceStorageDao m_resourceStorageDao;

    public void setUp() {
        BeanUtils.assertAutowiring(this);

        OnmsNode node = new OnmsNode();
        node.setId(1);
        node.setLabel("node1");
        m_nodeDao.save(node);
        m_nodeDao.flush();
    }

    @Test
    public void notFoundOnMissingResource() {
        final QueryRequest request = buildRequest();
        request.getSources().get(0).setResourceId("node[99].interfaceSnmp[eth0-04013f75f101]");

        exception.expect(exceptionWithResponseCode(404));
        m_svc.query(request);
    }

    @Test
    public void notFoundOnMissingAttribute() {
        final QueryRequest request = buildRequest();
        request.getSources().get(0).setAttribute("n0tIfInOctets");

        exception.expect(exceptionWithResponseCode(404));
        m_svc.query(request);
    }

    @Test
    public void badRequestOnMissingLabel() {
        final QueryRequest request = buildRequest();
        request.getSources().get(0).setLabel(null);

        exception.expect(exceptionWithResponseCode(400));
        m_svc.query(request);
    }

    @Test
    public void badRequestOnMissingAttribute() {
        final QueryRequest request = buildRequest();
        request.getSources().get(0).setResourceId(null);

        exception.expect(exceptionWithResponseCode(400));
        m_svc.query(request);
    }

    @Test
    public void badRequestOnMissingResourceId() {
        final QueryRequest request = buildRequest();
        request.getSources().get(0).setResourceId(null);

        exception.expect(exceptionWithResponseCode(400));
        m_svc.query(request);
    }

    private static Matcher<?> exceptionWithResponseCode(final int status) {
        return new BaseMatcher<WebApplicationException>() {
            @Override
            public boolean matches(Object o) {
                if (!(o instanceof WebApplicationException)) {
                    return false;
                }
                WebApplicationException e = (WebApplicationException)o;
                return e.getResponse().getStatus() == status;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("invalid status code");
            }
        };
    }

    private static QueryRequest buildRequest() {
        final QueryRequest request = new QueryRequest();
        request.setStart(1414602000000L);
        request.setEnd(1417046400000L);
        request.setStep(1000L);

        final Source source = new Source();
        source.setResourceId("node[1].interfaceSnmp[eth0-04013f75f101]");
        source.setAttribute("ifInOctets");
        source.setAggregation("AVERAGE");
        source.setLabel("octetsIn");
        request.setSources(Lists.newArrayList(source));

        return request;
    }
}
