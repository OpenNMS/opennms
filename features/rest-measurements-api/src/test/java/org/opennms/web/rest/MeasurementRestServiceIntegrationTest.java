/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2015The OpenNMS Group, Inc.
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

package org.opennms.web.rest;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URLEncoder;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.support.DefaultResourceDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;

/**
 * Used to make calls to an instance of MeasurementRestService.
 *
 * @author jwhite
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-measurements-test-jrb.xml",
        "classpath:/META-INF/opennms/applicationContext-jersey.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class MeasurementRestServiceIntegrationTest extends AbstractSpringJerseyRestTestCase {

    @Autowired
    protected DefaultResourceDao m_resourceDao;

    @Autowired
    protected NodeDao m_nodeDao;

    @Before
    public void setUp() throws Throwable {
        super.setUp();

        BeanUtils.assertAutowiring(this);

        OnmsNode node = new OnmsNode();
        node.setId(1);
        node.setLabel("node1");
        m_nodeDao.save(node);
        m_nodeDao.flush();

        File rrdDirectory = new File("src/test/resources/share/jrb");
        assertTrue(rrdDirectory.canRead());

        m_resourceDao.setRrdDirectory(rrdDirectory);
        System.setProperty("rrd.base.dir", rrdDirectory.getAbsolutePath());
    }

    @Test
    public void notFoundOnMissingResource() throws Exception {
        sendRequest(GET, "/measurements/should_not_/exist", 404);
    }

    /**
     * Used to test the marshaling of the result set to both
     * XML and JSON.
     */
    @Test
    public void canRetrieveMeasurementsAsXmlOrJson() throws Exception {
        final String url = String.format("/measurements/%s/%s",
                URLEncoder.encode("node[1].interfaceSnmp[eth0-04013f75f101]", Charsets.UTF_8.name()),
                "ifInOctets");

        final Map<String, String> parameters = Maps.newHashMap();
        parameters.put("start", Long.toString(1414602000000L));
        parameters.put("end", Long.toString(1417046400000L));

        final MockHttpServletRequest request = createRequest(getServletContext(), GET, url);
        request.setParameters(parameters);
        String xml = sendRequest(request, 200);

        // Now set Accept header and re-issue the request
        request.addHeader("Accept", MediaType.APPLICATION_JSON);
        String json = sendRequest(request, 200);

        assertTrue(xml.contains("<columns>"));
        assertTrue(json.contains("\"columns\":"));
    }
}
