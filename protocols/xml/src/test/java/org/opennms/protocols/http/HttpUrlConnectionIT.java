/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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
package org.opennms.protocols.http;

import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.opennms.core.test.http.JUnitHttpServerExecutionListener;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.core.test.http.annotations.Webapp;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.protocols.http.HttpUrlConnection;
import org.opennms.protocols.xml.config.Content;
import org.opennms.protocols.xml.config.Request;

import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The Test Class for HttpUrlConnection.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    JUnitHttpServerExecutionListener.class
})
public class HttpUrlConnectionTest {

    /**
     * Test the Servlet with a simple POST Request based on XML Data.
     *
     * @throws Exception the exception
     */
    @Test
    @JUnitHttpServer(port=10342, https=false, webapps={
            @Webapp(context="/junit", path="src/test/resources/test-webapp")
    })
    public void testServlet() throws Exception {
        String xml = "<person><firstName>Alejandro</firstName></person>";
        DefaultHttpClient client = new DefaultHttpClient();
        StringEntity entity = new StringEntity(xml, ContentType.APPLICATION_XML);
        HttpPost method = new HttpPost("http://localhost:10342/junit/test/sample");
        method.setEntity(entity);
        HttpResponse response = client.execute(method);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        Assert.assertEquals("OK!", EntityUtils.toString(response.getEntity()));
    }

    /**
     * Test POST Request based on XML Data.
     *
     * @throws Exception the exception
     */
    @Test
    @JUnitHttpServer(port=10342, https=false, webapps={
            @Webapp(context="/junit", path="src/test/resources/test-webapp")
    })
    public void testXml() throws Exception {
        String xml = "<person><firstName>Alejandro</firstName><lastName>Galue</lastName></person>";
        Request req = buildRequest("application/xml", xml);
        executeRequest(req);
    }

    /**
     * Test POST Request based on JSON Data.
     *
     * @throws Exception the exception
     */
    @Test
    @JUnitHttpServer(port=10342, https=false, webapps={
            @Webapp(context="/junit", path="src/test/resources/test-webapp")
    })
    public void testJson() throws Exception {
        String json = "{ person: { firstName: 'Alejandro', lastName: 'Galue' } }";
        Request req = buildRequest("application/json", json);
        executeRequest(req);
    }

    /**
     * Test POST Request based on Form Data.
     *
     * @throws Exception the exception
     */
    @Test
    @JUnitHttpServer(port=10342, https=false, webapps={
            @Webapp(context="/junit", path="src/test/resources/test-webapp")
    })
    public void testForm() throws Exception {
        String json = "<form-fields><form-field name='firstName'>Alejandro</form-field><form-field name='lastName'>Galue</form-field></form-fields>";
        Request req = buildRequest("application/x-www-form-urlencoded", json);
        executeRequest(req);
    }

    /**
     * Builds the request.
     *
     * @param contentType the content type
     * @param contentData the content data
     * @return the request
     */
    private Request buildRequest(String contentType, String contentData) {
        Request req = new Request();
        req.setMethod("POST");
        req.addParameter("timeout", "3000");
        req.addParameter("retries", "2");
        req.addHeader("User-Agent", "FireFox 22.0");
        req.setContent(new Content(contentType, contentData));
        return req;
    }

    /**
     * Execute request.
     *
     * @param request the request
     * @throws Exception the exception
     */
    private void executeRequest(Request request) throws Exception {
        URL url = new URL("http://localhost:10342/junit/test/post");
        HttpUrlConnection connection = new HttpUrlConnection(url, request);
        connection.connect();
        String output = IOUtils.toString(connection.getInputStream());
        SampleData data = JaxbUtils.unmarshal(SampleData.class, output);
        Assert.assertNotNull(data);
        Assert.assertEquals(2, data.getParameters().size());
        Assert.assertEquals("Alejandro", data.getParameter("firstName"));
        Assert.assertEquals("Galue", data.getParameter("lastName"));
    }
}
