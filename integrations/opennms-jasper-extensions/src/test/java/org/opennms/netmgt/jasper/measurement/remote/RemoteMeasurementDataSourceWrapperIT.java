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
package org.opennms.netmgt.jasper.measurement.remote;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRRewindableDataSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.netmgt.jasper.measurement.EmptyJRDataSource;
import org.opennms.netmgt.jasper.measurement.MeasurementDataSource;

public class RemoteMeasurementDataSourceWrapperIT {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9999);

    @Before
    public void before() {
        // OK Requests
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/opennms/rest/measurements"))
                .withHeader("Accept", WireMock.equalTo("application/xml"))
                .willReturn(WireMock.aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/xml")
                                .withBody("<query-response/>") // minimal parsable response
                ));

        // Forward Requests
        WireMock.stubFor(WireMock.post(WireMock.urlMatching("/opennms/rest/forward/.*"))
                .willReturn(WireMock.aResponse()
                        .withStatus(302)));

        // 500 Requests
        WireMock.stubFor(WireMock.post(WireMock.urlMatching("/opennms/rest/bad/.*"))
                .willReturn(WireMock.aResponse()
                        .withStatus(500)
                        .withBody("This did not work as you might have expected, ugh?")));

        // Map everything else to a 404 Response
        WireMock.stubFor(WireMock.any(WireMock.anyUrl())
                .atPriority(10)
                .willReturn(WireMock.aResponse()
                        .withStatus(404)
                        .withBody("{\"status\":\"Error\",\"message\":\"Endpoint not found\"}")));
    }

    @Test
    public void testOk() throws JRException {
        JRRewindableDataSource dataSource = new RemoteMeasurementDataSourceWrapper(false, "http://localhost:9999/opennms/rest/measurements", null, null).createDataSource("<query-request />");
        Assert.assertNotNull(dataSource);
        Assert.assertEquals(MeasurementDataSource.class, dataSource.getClass());
    }

    @Test
    public void testRedirection() {
        try {
            new RemoteMeasurementDataSourceWrapper(false, "http://localhost:9999/opennms/rest/forward/me", null, null).createDataSource("<query-request />");
            Assert.fail("JRException was expected, but was not thrown");
        } catch (JRException jre) {
            Assert.assertTrue(jre.toString().contains("Request was redirected. This is not supported."));
        }
    }

    @Test
    public void test404() throws JRException {
        JRRewindableDataSource dataSource = new RemoteMeasurementDataSourceWrapper(false, "http://localhost:9999/opennms/rest/doesNotExist", null, null).createDataSource("<query-request />");
        Assert.assertNotNull(dataSource);
        Assert.assertEquals(EmptyJRDataSource.class, dataSource.getClass());
    }

    @Test
    public void testError() {
        try {
            new RemoteMeasurementDataSourceWrapper(false, "http://localhost:9999/opennms/rest/bad/request", null, null).createDataSource("<query-request />");
            Assert.fail("JRException was expected, but was not thrown");
        } catch (JRException jre) {
            Assert.assertTrue(jre.toString().contains("Invalid request. Response was"));
            Assert.assertTrue(jre.toString().contains("500 (Server Error)"));
            Assert.assertTrue(jre.toString().endsWith("This did not work as you might have expected, ugh?"));
        }
    }
}
