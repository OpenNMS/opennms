package org.opennms.netmgt.jasper.measurement;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRRewindableDataSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

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
        JRRewindableDataSource dataSource = new RemoteMeasurementDataSourceWrapper(false, "http://localhost:9999/opennms/rest/measurements", null, null).createDataSource("<dummy request>");
        Assert.assertNotNull(dataSource);
        Assert.assertEquals(MeasurementDataSource.class, dataSource.getClass());
    }

    @Test
    public void testRedirection() {
        try {
            new RemoteMeasurementDataSourceWrapper(false, "http://localhost:9999/opennms/rest/forward/me", null, null).createDataSource("<dummy request>");
            Assert.fail("JRException was expected, but was not thrown");
        } catch (JRException jre) {
            Assert.assertTrue(jre.toString().contains("Request was redirected. This is not supported."));
        }
    }

    @Test
    public void test404() throws JRException {
        JRRewindableDataSource dataSource = new RemoteMeasurementDataSourceWrapper(false, "http://localhost:9999/opennms/rest/doesNotExist", null, null).createDataSource("<dummy request>");
        Assert.assertNotNull(dataSource);
        Assert.assertEquals(EmptyJRDataSource.class, dataSource.getClass());
    }

    @Test
    public void testError() {
        try {
            new RemoteMeasurementDataSourceWrapper(false, "http://localhost:9999/opennms/rest/bad/request", null, null).createDataSource("<dummy request>");
            Assert.fail("JRException was expected, but was not thrown");
        } catch (JRException jre) {
            Assert.assertTrue(jre.toString().contains("Invalid request. Response was"));
            Assert.assertTrue(jre.toString().contains("500 (Internal Server Error)"));
            Assert.assertTrue(jre.toString().endsWith("This did not work as you might have expected, ugh?"));
        }
    }
}
