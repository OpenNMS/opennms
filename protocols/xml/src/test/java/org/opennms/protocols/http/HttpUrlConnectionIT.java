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
package org.opennms.protocols.http;

import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.http.JUnitHttpServerExecutionListener;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.core.test.http.annotations.Webapp;
import org.opennms.core.web.HttpClientWrapper;
import org.opennms.core.xml.JaxbUtils;
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
public class HttpUrlConnectionIT {

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
        final HttpClientWrapper clientWrapper = HttpClientWrapper.create();
        try {
            StringEntity entity = new StringEntity(xml, ContentType.APPLICATION_XML);
            HttpPost method = new HttpPost("http://localhost:10342/junit/test/sample");
            method.setEntity(entity);
            CloseableHttpResponse response = clientWrapper.execute(method);
            Assert.assertEquals(200, response.getStatusLine().getStatusCode());
            Assert.assertEquals("OK!", EntityUtils.toString(response.getEntity()));
        } finally {
            IOUtils.closeQuietly(clientWrapper);
        }
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
