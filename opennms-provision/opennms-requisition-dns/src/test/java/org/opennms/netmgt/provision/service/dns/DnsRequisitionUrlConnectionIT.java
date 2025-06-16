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
package org.opennms.netmgt.provision.service.dns;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.dns.JUnitDNSServerExecutionListener;
import org.opennms.core.test.dns.annotations.DNSEntry;
import org.opennms.core.test.dns.annotations.DNSZone;
import org.opennms.core.test.dns.annotations.JUnitDNSServer;
import org.opennms.core.utils.url.GenericURLFactory;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * This class tests the new "dns" protocol handling created for the Provisioner.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/empty-context.xml"})
@TestExecutionListeners(listeners={
                        JUnitDNSServerExecutionListener.class
})
public class DnsRequisitionUrlConnectionIT {

    private static final String TEST_URL = "dns://localhost:9153/example.com";

    @BeforeClass
    static public void setUp() {
        MockLogAppender.setupLogging();
    }

    @Before
    public void registerFactory() {
        GenericURLFactory.initialize();
    }

    @Test
    public void dwoValidateMultipathUrl() throws MalformedURLException {
        DnsRequisitionUrlConnection c = new DnsRequisitionUrlConnection(new URL("dns://localhost/opennms"));
        Assert.assertEquals("opennms", c.getRequest().getZone());

        c = new DnsRequisitionUrlConnection(new URL("dns://localhost/opennms/raleigh"));
        Assert.assertEquals("opennms", c.getRequest().getZone());
        Assert.assertEquals("raleigh", c.getRequest().getForeignSource());

        MalformedURLException e = null;
        try {
            new DnsRequisitionUrlConnection(new URL("dns://localhost/org/opennms/raleigh"));
        } catch (MalformedURLException e1) {
            e = e1;
        }
        Assert.assertNotNull(e);
        Assert.assertEquals("The specified DNS URL contains invalid path: dns://localhost/org/opennms/raleigh", e.getLocalizedMessage());
    }

    @Test
    public void dwoParseZone() throws MalformedURLException {
        URL url = new URL("dns://localhost/opennms");
        Assert.assertEquals("opennms", DnsRequisitionUrlConnection.parseZone(url));
        Assert.assertEquals("opennms", DnsRequisitionUrlConnection.parseForeignSource(url));

        url = new URL("dns://localhost/opennms/raleigh");
        Assert.assertEquals("opennms", DnsRequisitionUrlConnection.parseZone(url));
        Assert.assertEquals("raleigh", DnsRequisitionUrlConnection.parseForeignSource(url));

        url = new URL("dns://localhost/opennms/?expression=abc[123]");
        Assert.assertEquals("opennms", DnsRequisitionUrlConnection.parseZone(url));
        Assert.assertEquals("opennms", DnsRequisitionUrlConnection.parseForeignSource(url));

        url = new URL("dns://localhost/opennms/raleigh/?expression=abc[123]");
        Assert.assertEquals("opennms", DnsRequisitionUrlConnection.parseZone(url));
        Assert.assertEquals("raleigh", DnsRequisitionUrlConnection.parseForeignSource(url));
    }

    @Test
    public void dwoValidateInvalidQueryParmInUrl() throws MalformedURLException {
        new DnsRequisitionUrlConnection(new URL("dns://localhost/opennms/?expression=abc[123]"));
        new DnsRequisitionUrlConnection(new URL("dns://localhost/opennms/raleigh/?expression=abc[123]"));

        MalformedURLException e = null;
        try {
            new DnsRequisitionUrlConnection(new URL("dns://localhost/opennms/?string=abc[123]"));
        } catch (MalformedURLException e1) {
            e = e1;
        }
        Assert.assertNotNull(e);
        Assert.assertEquals("The specified DNS URL contains an invalid query string: dns://localhost/opennms/?string=abc[123]", e.getLocalizedMessage());

    }

    @Test
    public void dwoParseUrlForMatchingExpression() throws MalformedURLException {
        URL url = new URL("dns://localhost/localhost/?expression=abc[0-9]");

        String expression = url.getPath();
        Assert.assertNotNull(expression);
        Assert.assertEquals("/localhost/", expression);
        Assert.assertEquals("expression=abc[0-9]", url.getQuery());

        String urlExpression = DnsRequisitionUrlConnection.determineExpressionFromUrl(url);
        Assert.assertEquals("abc[0-9]", urlExpression);
    }

    @Test
    @JUnitDNSServer(port=9153, zones={
        @DNSZone(name = "example.com", entries = {
            @DNSEntry(hostname = "www", data = "72.14.204.99")
        })
    })
    public void dwoUrlAsResource() throws IOException, JAXBException {
        Resource resource = new UrlResource(TEST_URL);

        Assert.assertEquals(TEST_URL, resource.getURL().toString());

        Requisition req = null;
        Assert.assertNotNull(resource);
        InputStream resourceStream = resource.getInputStream();
        JAXBContext context = JAXBContext.newInstance(Requisition.class);
        Unmarshaller um = context.createUnmarshaller();
        um.setSchema(null);
        req = (Requisition) um.unmarshal(resourceStream);

        Assert.assertEquals("should have 2 A records: 1 for example.com, and 1 for www.example.com", 2, req.getNodeCount());
        resourceStream.close();
    }

    @Test
    @JUnitDNSServer(port=9153, zones={
        @DNSZone(name = "example.com", entries = {
            @DNSEntry(hostname = "www", data = "72.14.204.99")
            ,
            @DNSEntry(hostname = "monkey", data = "72.14.204.99")
        })
    })
    public void dwoUrlAsResourceUsingMatchingExpression() throws IOException, JAXBException {
        String urlString = "dns://localhost:9153/example.com/?expression=[Ww]ww.*";
        Resource resource = new UrlResource(urlString);

        Assert.assertEquals(urlString, resource.getURL().toString());

        Requisition req = null;
        Assert.assertNotNull(resource);
        InputStream resourceStream = resource.getInputStream();
        JAXBContext context = JAXBContext.newInstance(Requisition.class);
        Unmarshaller um = context.createUnmarshaller();
        um.setSchema(null);
        req = (Requisition) um.unmarshal(resourceStream);

        Assert.assertEquals(1, req.getNodeCount());
        resourceStream.close();
    }

    @Test
    @JUnitDNSServer(port=9153, zones={
        @DNSZone(name = "example.com", entries = {
            @DNSEntry(hostname = "www", data = "72.14.204.99")
            ,
            @DNSEntry(hostname = "monkey", data = "72.14.204.99")
        })
    })
    public void dwoUrlAsResourceUsingNonMatchingExpression() throws IOException, JAXBException {
        String urlString = "dns://localhost:9153/example.com/?expression=Local.*";
        Resource resource = new UrlResource(urlString);

        Assert.assertEquals(urlString, resource.getURL().toString());

        Requisition req = null;
        Assert.assertNotNull(resource);
        InputStream resourceStream = resource.getInputStream();
        JAXBContext context = JAXBContext.newInstance(Requisition.class);
        Unmarshaller um = context.createUnmarshaller();
        um.setSchema(null);
        req = (Requisition) um.unmarshal(resourceStream);

        Assert.assertEquals(0, req.getNodeCount());
        resourceStream.close();
    }

    @Test
    @JUnitDNSServer(port=9153, zones={
        @DNSZone(name = "example.com", entries = {
            @DNSEntry(hostname = "www", data = "72.14.204.99")
        })
    })
    public void dwoUrlAsResourceUsingComplexMatchingExpression() throws IOException, JAXBException {
        String urlString = "dns://localhost:9153/example.com/?expression=(%3Fi)^WWW.EXAM.*";
        Resource resource = new UrlResource(urlString);

        Assert.assertEquals(urlString, resource.getURL().toString());

        Requisition req = null;
        Assert.assertNotNull(resource);
        InputStream resourceStream = resource.getInputStream();
        JAXBContext context = JAXBContext.newInstance(Requisition.class);
        Unmarshaller um = context.createUnmarshaller();
        um.setSchema(null);
        req = (Requisition) um.unmarshal(resourceStream);

        Assert.assertEquals(1, req.getNodeCount());
        resourceStream.close();
    }

    @Test
    public void dwoDnsRequisitionUrlConnection() throws MalformedURLException {
        URLConnection c = new DnsRequisitionUrlConnection(new URL(TEST_URL));
        Assert.assertNotNull(c);
    }

    /**
     * Just test that connection doesn't get implementation without fixing this test
     * @throws IOException
     */
    @Test
    @JUnitDNSServer(port=9153, zones={
        @DNSZone(name = "example.com", entries = {
            @DNSEntry(hostname = "www", data = "72.14.204.99")
        })
    })
    public void dwoConnect() throws IOException {
        URLConnection c = new DnsRequisitionUrlConnection(new URL(TEST_URL));
        c.connect();
    }

    @Test
    @JUnitDNSServer(port=9153, zones={
        @DNSZone(name = "example.com", entries = {
            @DNSEntry(hostname = "www", data = "72.14.204.99")
        })
    })
    public void dwoGetInputStream() throws IOException {
        URLConnection c = new DnsRequisitionUrlConnection(new URL(TEST_URL));
        InputStream s = c.getInputStream();
        Assert.assertNotNull(s);
        s.close();
    }

    @Test
    public void dwoGetURL() throws MalformedURLException {
        URLConnection c = new DnsRequisitionUrlConnection(new URL(TEST_URL));
        Assert.assertEquals(53, c.getURL().getDefaultPort());
        Assert.assertEquals("localhost", c.getURL().getHost());
        Assert.assertEquals("/example.com", c.getURL().getPath());
        Assert.assertEquals(DnsRequisitionUrlConnection.PROTOCOL, c.getURL().getProtocol());
        Assert.assertNull(c.getURL().getUserInfo());
    }

    @Test
    public void dwoToString() throws MalformedURLException {
        URLConnection c = new DnsRequisitionUrlConnection(new URL(TEST_URL));
        Assert.assertEquals(TEST_URL, c.getURL().toString());
    }

    @Test
    @JUnitDNSServer(port=9153, zones={
            @DNSZone(name = "example.com", entries = {
                    @DNSEntry(hostname = "www", data = "72.14.204.99")
                    ,
                    @DNSEntry(hostname = "monkey", data = "72.14.204.99")
            })
    })
    public void testFixedLocation() throws IOException, JAXBException {
        final String urlString = "dns://localhost:9153/example.com/?location=Fulda";
        final Resource resource = new UrlResource(urlString);

        Assert.assertEquals(urlString, resource.getURL().toString());
        Assert.assertNotNull(resource);

        final InputStream resourceStream = resource.getInputStream();
        final JAXBContext context = JAXBContext.newInstance(Requisition.class);
        final Unmarshaller unmarshaller = context.createUnmarshaller();
        unmarshaller.setSchema(null);
        final Requisition requisition = (Requisition) unmarshaller.unmarshal(resourceStream);

        Assert.assertEquals(3, requisition.getNodeCount());

        for(final RequisitionNode node : requisition.getNodes()) {
            Assert.assertEquals("Fulda", node.getLocation());
        }

        resourceStream.close();
    }

    @Test
    @JUnitDNSServer(port=9153, zones={
            @DNSZone(name = "hs-fulda.de", entries = {
                    @DNSEntry(hostname = "g51onms.g51", data = "72.14.204.2"),
                    @DNSEntry(hostname = "e46onms.e46", data = "72.14.204.3"),
            })
    })
    public void testParsedLocation() throws IOException, JAXBException {
        final String urlString = "dns://localhost:9153/hs-fulda.de/?expression=^.*\\.[a-z][0-9][0-9]\\.hs-fulda\\.de\\.$&location=~"+ URLEncoder.encode("^(?:.*\\.|)(.*?)\\.hs-fulda\\.de\\.$","UTF-8");
        final Resource resource = new UrlResource(urlString);

        Assert.assertEquals(urlString, resource.getURL().toString());
        Assert.assertNotNull(resource);

        final InputStream resourceStream = resource.getInputStream();
        final JAXBContext context = JAXBContext.newInstance(Requisition.class);
        final Unmarshaller unmarshaller = context.createUnmarshaller();
        unmarshaller.setSchema(null);
        final Requisition requisition = (Requisition) unmarshaller.unmarshal(resourceStream);

        Assert.assertEquals(2, requisition.getNodeCount());

        final RequisitionNode node1 = requisition.getNodes().stream().filter(e->e.getNodeLabel().contains("e46")).findFirst().get();
        final RequisitionNode node2 = requisition.getNodes().stream().filter(e->e.getNodeLabel().contains("g51")).findFirst().get();
        Assert.assertEquals("e46", node1.getLocation());
        Assert.assertEquals("g51", node2.getLocation());

        resourceStream.close();
    }

    @Test
    @JUnitDNSServer(port=9153, zones={
            @DNSZone(name = "example.com", entries = {
                    @DNSEntry(hostname = "www", data = "72.14.204.99")
                    ,
                    @DNSEntry(hostname = "monkey", data = "72.14.204.99")
            })
    })
    public void testBrokenRegex() throws IOException, JAXBException {
        final String urlString = "dns://localhost:9153/example.com/?location=~"+ URLEncoder.encode("^(.*\\.|)(.*?)\\.com\\.$","UTF-8");
        final Resource resource = new UrlResource(urlString);

        Assert.assertEquals(urlString, resource.getURL().toString());
        Assert.assertNotNull(resource);

        final InputStream resourceStream = resource.getInputStream();
        final JAXBContext context = JAXBContext.newInstance(Requisition.class);
        final Unmarshaller unmarshaller = context.createUnmarshaller();
        unmarshaller.setSchema(null);
        final Requisition requisition = (Requisition) unmarshaller.unmarshal(resourceStream);

        Assert.assertEquals(3, requisition.getNodeCount());

        for(final RequisitionNode node : requisition.getNodes()) {
            Assert.assertNull(node.getLocation());
        }

        Assert.assertEquals(3, requisition.getNodeCount());
        resourceStream.close();
    }
}
