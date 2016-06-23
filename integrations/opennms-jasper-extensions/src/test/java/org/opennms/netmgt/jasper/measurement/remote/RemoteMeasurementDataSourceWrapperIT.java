/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
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

        // Everything else is automatically bound to a 404
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
            Assert.assertTrue(jre.toString().contains("500 (Internal Server Error)"));
            Assert.assertTrue(jre.toString().endsWith("This did not work as you might have expected, ugh?"));
        }
    }
}
