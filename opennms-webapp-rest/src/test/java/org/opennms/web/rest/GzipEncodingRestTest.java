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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import javax.servlet.ServletContext;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

/**
 * Verifies that gzip encoding is supported when making
 * REST API calls.
 *
 * @author jwhite
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-jersey.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class GzipEncodingRestTest extends AbstractSpringJerseyRestTestCase {

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private DatabasePopulator populator;

    @Autowired
    private ServletContext m_context;

    @Before
    @Override
    public void setUp() throws Throwable {
        super.setUp();
        Assert.assertNotNull(populator);
        Assert.assertNotNull(applicationDao);

        populator.populateDatabase();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        populator.resetDatabase();
    }

    @Test
    public void testGzippedEncodedReponse() throws Exception {
        // Retrieve the results of request without any encoding headers set
        final MockHttpServletRequest request = createRequest(m_context, GET, "/nodes");
        String xml = sendRequest(request, 200);

        // Now set the header, and re-issue that same request
        request.addHeader("Accept-Encoding", "gzip");
        MockHttpServletResponse response = createResponse();
        dispatch(request, response);
        assertEquals(200, response.getStatus());

        // Decompress the response
        assertEquals("gzip", response.getHeader("Content-Encoding"));
        InputStream gzip = new GZIPInputStream(new ByteArrayInputStream(response.getContentAsByteArray()));
        String ungzippedXml = CharStreams.toString(new InputStreamReader(gzip, Charsets.UTF_8));

        // The resulting strings from both requests should match
        assertEquals(xml, ungzippedXml);
    }
}
