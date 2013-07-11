/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.http.JUnitHttpServerExecutionListener;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.core.test.http.annotations.Webapp;
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

    @Test
    @JUnitHttpServer(port=9162)
    public void testServer() throws HttpException, IOException {
        HttpClient client = new DefaultHttpClient();
        HttpUriRequest method = new HttpGet("http://localhost:9162/test.html");
        HttpResponse response = client.execute(method);
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
        HttpClient client = new DefaultHttpClient();
        HttpUriRequest method = new HttpGet("http://localhost:9162/testContext/index.html");
        HttpResponse response = client.execute(method);
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
        HttpClient client = new DefaultHttpClient();
        HttpUriRequest method = new HttpGet("http://localhost:9162/testContext/monkey");
        HttpResponse response = client.execute(method);
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
    	final DefaultHttpClient client = new DefaultHttpClient();
    	final HttpUriRequest method = new HttpGet("http://localhost:9162/testContext/monkey");
        
    	final CredentialsProvider cp = client.getCredentialsProvider();
    	final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("admin", "istrator");
        cp.setCredentials(new AuthScope("localhost", 9162), credentials);
        
        final HttpResponse response = client.execute(method);
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
    	final DefaultHttpClient client = new DefaultHttpClient();
    	final HttpUriRequest method = new HttpGet("http://localhost:9162/testContext/monkey");
        
    	final CredentialsProvider cp = client.getCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("admin", "sucks");
        cp.setCredentials(new AuthScope("localhost", 9162), credentials);
        
        final HttpResponse response = client.execute(method);
        assertEquals(401, response.getStatusLine().getStatusCode());
    }

}
