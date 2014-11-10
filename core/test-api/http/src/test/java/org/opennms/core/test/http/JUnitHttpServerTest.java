/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.test.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.core.test.http.annotations.Webapp;
import org.opennms.core.web.HttpClientWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    JUnitHttpServerExecutionListener.class
})
public class JUnitHttpServerTest {
    private static final Logger LOG = LoggerFactory.getLogger(JUnitHttpServerTest.class);

    private HttpClientWrapper m_clientWrapper;

    @Before
    public void setUp() {
        m_clientWrapper = HttpClientWrapper.create();
    }

    @After
    public void tearDown() throws IOException {
        m_clientWrapper.close();
    }

    @Test
    @JUnitHttpServer(port=9162)
    public void testServer() throws HttpException, IOException {
        HttpUriRequest method = new HttpGet("http://localhost:9162/test.html");
        final CloseableHttpResponse response = m_clientWrapper.execute(method);
        String responseString = EntityUtils.toString(response.getEntity());
        LOG.debug("got response:\n{}", responseString);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertTrue(responseString.contains("Purple monkey dishwasher."));
    }

    @Test
    @JUnitHttpServer(port=9162, webapps={
            @Webapp(context="/testContext", path="src/test/resources/test-webapp")
    })
    public void testWebapp() throws Exception {
        HttpUriRequest method = new HttpGet("http://localhost:9162/testContext/index.html");
        final CloseableHttpResponse response = m_clientWrapper.execute(method);
        String responseString = EntityUtils.toString(response.getEntity());
        LOG.debug("got response:\n{}", responseString);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertTrue(responseString.contains("This is a webapp."));
    }

    @Test
    @JUnitHttpServer(port=9162, webapps={
            @Webapp(context="/testContext", path="src/test/resources/test-webapp")
    })
    public void testWebappWithServlet() throws Exception {
        HttpUriRequest method = new HttpGet("http://localhost:9162/testContext/monkey");
        final CloseableHttpResponse response = m_clientWrapper.execute(method);
        String responseString = EntityUtils.toString(response.getEntity());
        LOG.debug("got response:\n{}", responseString);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertTrue(responseString.contains("You are reading this from a servlet!"));
    }

    @Test
    @JUnitHttpServer(port=9162, basicAuth=true, webapps={
            @Webapp(context="/testContext", path="src/test/resources/test-webapp")
    })
    public void testBasicAuthSuccess() throws Exception {
        final HttpUriRequest method = new HttpGet("http://localhost:9162/testContext/monkey");

        m_clientWrapper.addBasicCredentials("admin", "istrator");

        final HttpResponse response = m_clientWrapper.execute(method);
        final String responseString = EntityUtils.toString(response.getEntity());
        LOG.debug("got response:\n{}", responseString);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertTrue(responseString.contains("You are reading this from a servlet!"));
    }

    @Test
    @JUnitHttpServer(port=9162, basicAuth=true, webapps={
            @Webapp(context="/testContext", path="src/test/resources/test-webapp")
    })
    public void testBasicAuthFailure() throws Exception {
        final HttpUriRequest method = new HttpGet("http://localhost:9162/testContext/monkey");

        m_clientWrapper.addBasicCredentials("admin", "sucks");

        final HttpResponse response = m_clientWrapper.execute(method);
        assertEquals(401, response.getStatusLine().getStatusCode());
    }

}
