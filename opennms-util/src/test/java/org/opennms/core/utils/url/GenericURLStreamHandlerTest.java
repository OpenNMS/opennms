package org.opennms.core.utils.url;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * <p>GenericURLStreamHandlerTest class.</p>
 *
 * @author Ronny Trommer <ronny@opennms.org>
 * @version $Id: $
 * @since 1.8.1
 */
public class GenericURLStreamHandlerTest {
    private GenericURLFactory m_genericURLFactory = GenericURLFactory.getInstance();

    private GenericURLStreamHandler m_generGenericURLStreamHandler;

    private GenericURLStreamHandler m_generGenericURLStreamHandler_port;

    private Class<? extends URLConnection> m_testClass;

    private int m_defaultPort = 42;

    @Before
    public void setUp() throws Exception {
        m_testClass = org.opennms.core.utils.url.StubGenericURLConnection.class;
    }

    /**
     * Test should expect a NoSuchMethodException and this exception is thrown. I don't know why this test will not work.
     *
     * TODO indigo: Fix this test to verify NoSuchMethodException
     *
     * java.lang.AssertionError: Expected exception: java.lang.NoSuchMethodException
     * java.lang.NoSuchMethodException: org.opennms.core.utils.url.ProtectedStubGenericURLConnection.<init>(java.net.URL)
     *
     */
    @Ignore
    @Test(expected = NoSuchMethodException.class)
    public void testFailToCreateURLStreamHandler() {
        URL testUrl = null;
        try {
            testUrl = new URL("http://myhost");
        } catch (MalformedURLException e) {
            Assert.fail("Test URL in testCreateURLStreamHandler test is incorrect. Error message: " + e.getMessage());
        }

        try {
           Class<? extends URLConnection> c = org.opennms.core.utils.url.ProtectedStubGenericURLConnection.class;
           m_generGenericURLStreamHandler = new GenericURLStreamHandler(c);
           m_generGenericURLStreamHandler.openConnection(testUrl);
        } catch (IOException e2) {
            Assert.fail("Could not open connection. Error message: " + e2.getMessage());
        }
    }

    @Test
    public void testGetDefaultPort() {
        Assert.assertEquals("Default should be -1", new GenericURLStreamHandler(m_testClass).getDefaultPort(), -1);
        Assert.assertEquals("Default should be 42", new GenericURLStreamHandler(m_testClass, m_defaultPort).getDefaultPort(), m_defaultPort);
    }

    @Test
    public void testOpenUrlConnection() {

        try {
            URL testUrl = new URL("http://myhost");
            Assert.assertNotNull(new GenericURLStreamHandler(m_testClass).openConnection(testUrl));
        } catch (MalformedURLException e) {
            Assert.fail("Test URL in testOpenUrlConnection test is incorrect. Error message: " + e.getMessage());
        } catch (IOException e) {
            Assert.fail("Could open connection in testOpenUrlConnection. Error message: " + e.getMessage());
        }
    }
}
