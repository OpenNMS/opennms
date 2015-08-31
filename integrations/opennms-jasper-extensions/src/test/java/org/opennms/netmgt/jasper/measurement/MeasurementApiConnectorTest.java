package org.opennms.netmgt.jasper.measurement;

import com.github.tomakehurst.wiremock.client.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.io.ByteStreams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Verifies that the {@link MeasurementApiConnector} connects accordingly to the OpenNMS Measurement API and may
 * deal with OpenNMS specifics.
 */
// TODO MVR make this an IT
public class MeasurementApiConnectorTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9999, 9443);

    @Before
    public void before() {
        // OK Requests
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/opennms/rest/measurements"))
                .withHeader("Accept", WireMock.equalTo("application/xml"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<response>Some content</response>")));

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
    public void test200() throws IOException {
        Result result = new MeasurementApiConnector().execute("http://localhost:9999/opennms/rest/measurements", null, null, "<dummy request>");
        Assert.assertTrue(result.wasSuccessful());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteStreams.copy(result.getInputStream(), outputStream);
        Assert.assertEquals("<response>Some content</response>", outputStream.toString());

        verifyWiremock();
    }

    // TODO MVR make this test green
    @Test
    public void test200UsingHttps() throws IOException {
        Result result = new MeasurementApiConnector().execute("https://localhost:9999/opennms/rest/measurements", null, null, "<dummy request>");
        Assert.assertTrue(result.wasSuccessful());
        Assert.assertNotNull(result.getInputStream());
        Assert.assertNull(result.getErrorStream());

        verifyWiremock();
    }

    @Test
    public void test404() throws IOException {
        Result result = new MeasurementApiConnector().execute("http://localhost:9999/opennms/rest/doesNotExist", null, null, "<dummy request>");
        Assert.assertFalse(result.wasSuccessful());
        Assert.assertEquals(404, result.getResponseCode());
        Assert.assertEquals("Not Found", result.getResponseMessage());

        verifyWiremock("/opennms/rest/doesNotExist");
    }

    /**
     * OpenNMS sometimes forwards to the index.jsp if not logged in, which would result in a success of the request
     * in general. We do not want that. This test checks that a 302 is not automatically forwarded.
     */
    @Test
    public void test302() throws IOException {
        Result result = new MeasurementApiConnector().execute("http://localhost:9999/opennms/rest/forward/me", null, null, "<dummy request>");
        Assert.assertFalse(result.wasSuccessful());
        Assert.assertTrue(result.wasRedirection());
        Assert.assertEquals(302, result.getResponseCode());
        Assert.assertNull(result.getInputStream());
        Assert.assertNull(result.getErrorStream());

        verifyWiremock("/opennms/rest/forward/me");
    }

    @Test
    public void test500() throws IOException {
        Result result = new MeasurementApiConnector().execute("http://localhost:9999/opennms/rest/bad/request", null, null, "<dummy request>");
        Assert.assertFalse(result.wasSuccessful());
        Assert.assertFalse(result.wasRedirection());
        Assert.assertEquals(500, result.getResponseCode());
        Assert.assertNull(result.getInputStream());
        Assert.assertNotNull(result.getErrorStream());

        verifyWiremock("/opennms/rest/bad/request");
    }

    @Test
    public void testAuthentication() throws IOException {
        Result result = new MeasurementApiConnector().execute("http://localhost:9999/opennms/rest/measurements", "admin", "admin", "<dummy request>");
        Assert.assertTrue(result.wasSuccessful());
        Assert.assertFalse(result.wasRedirection());
        Assert.assertEquals(200, result.getResponseCode());
        Assert.assertNotNull(result.getInputStream());
        Assert.assertNull(result.getErrorStream());

        RequestPatternBuilder requestPatternBuilder = createDefaultRequestPatternBuilder("/opennms/rest/measurements");
        requestPatternBuilder.withHeader("Authorization", WireMock.matching("Basic .*"));

        verifyWiremock(requestPatternBuilder);
    }

    private void verifyWiremock() {
       verifyWiremock("/opennms/rest/measurements");
    }

    private void verifyWiremock(String url) {
        verifyWiremock(createDefaultRequestPatternBuilder(url));
    }

    private void verifyWiremock(RequestPatternBuilder builder) {
        WireMock.verify(builder);
    }

    private RequestPatternBuilder createDefaultRequestPatternBuilder(String url) {
        return WireMock.postRequestedFor(WireMock.urlMatching(url))
                .withRequestBody(WireMock.matching("<dummy request>"))
                .withHeader("Content-Type", WireMock.equalTo("application/xml"))
                .withHeader("Accept-Charset", WireMock.equalTo("UTF-8"));
    }

    // TODO MVR test instead of IT
    @Test
    public void testAuthenticationRequired() {
        Assert.assertFalse(MeasurementApiConnector.isAuthenticationRequired(null, null));
        Assert.assertFalse(MeasurementApiConnector.isAuthenticationRequired(null, ""));
        Assert.assertFalse(MeasurementApiConnector.isAuthenticationRequired("", null));
        Assert.assertFalse(MeasurementApiConnector.isAuthenticationRequired("", ""));
        Assert.assertFalse(MeasurementApiConnector.isAuthenticationRequired("dummy", null));
        Assert.assertFalse(MeasurementApiConnector.isAuthenticationRequired("dummy", ""));
        Assert.assertFalse(MeasurementApiConnector.isAuthenticationRequired(null, "dummy"));
        Assert.assertFalse(MeasurementApiConnector.isAuthenticationRequired("", "dummy"));
        Assert.assertTrue(MeasurementApiConnector.isAuthenticationRequired("dummy", "dummy"));
    }
}
