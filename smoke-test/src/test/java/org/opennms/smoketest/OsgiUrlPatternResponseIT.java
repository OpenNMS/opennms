package org.opennms.smoketest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests if sites with /osgi/* context can be registered.
 * See #NMS-7785 for details.
 */
public class OsgiUrlPatternResponseIT extends OpenNMSSeleniumTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(OsgiUrlPatternResponseIT.class);

    @Test
    public void testOsgiUrlPatternResponse() throws IOException {
        final String[] paths = new String[]{
                "jmx-config-tool",
                "vaadin-surveillance-views?dashboard=true",
                "vaadin-surveillance-views?dashboard=false",
                "vaadin-surveillance-views-config",
                "wallboard-config",
                "bsm-admin-page",
                "node-maps"
        };

        for (final String eachPath : paths) {
            final String urlString = String.format("http://%s:%s/opennms/osgi/%s", getServerAddress(), getServerHttpPort(), eachPath);
            LOG.info("Verifying url '{}' ...", urlString);

            final URL url = new URL(urlString);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty ("Authorization", createBasicAuthHeader());
            connection.setConnectTimeout(250);
            connection.setUseCaches(false);
            connection.setDoOutput(false);
            connection.setInstanceFollowRedirects(false); // we do not want to follow redirects, otherwise 200 OK might be returned
            connection.connect();
            LOG.info("Status: {} {}", connection.getResponseCode(), connection.getResponseMessage());

            // All unauthorized requests are forwarded to login.jsp which will result in 200 OK, we
            // therefore fail, because we should have been logged in correctly.
            if (302 == connection.getResponseCode()) {
                LOG.info("Try redirecting to: {}", connection.getHeaderField("Location"));
                Assert.fail("Request was forwarded. This is not allowed.");
            }

            // Valid request, now check for OK
            final String errorMessage = String.format("URL: %s: status %s (%s) expected but %s (%s) received.",
                urlString, 200, "OK", connection.getResponseCode(),  connection.getResponseMessage());
            Assert.assertEquals(errorMessage, 200, connection.getResponseCode());
            LOG.info("OK");
        }
    }

}
