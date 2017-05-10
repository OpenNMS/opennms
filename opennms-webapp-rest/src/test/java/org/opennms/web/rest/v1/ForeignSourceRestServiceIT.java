/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2024 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2024 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXB;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.model.foreignsource.DetectorPluginConfigEntity;
import org.opennms.netmgt.model.foreignsource.PluginConfigEntity;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSourceCollection;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import com.google.common.collect.Lists;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class ForeignSourceRestServiceIT extends AbstractSpringJerseyRestTestCase {

    @Autowired
    private GenericPersistenceAccessor genericPersistenceAccessor;

    @Test
    public void testGetDefaultForeignSources() throws Exception {
        String url = "/foreignSources/default";
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("name=\"default\""));
        assertTrue(xml.contains("ICMP"));

        sendRequest(DELETE, url, 202);
        sendRequest(DELETE, "/foreignSources/deployed/default", 202);
    }
    
    @Test
    public void testGetDeployedForeignSources() throws Exception {
        String url = "/foreignSources/deployed";
        String xml = sendRequest(GET, url, 200);

        url = "/foreignSources/deployed/count";
        xml = sendRequest(GET, url, 200);
        assertEquals(xml, "0", xml);
    }
    
    @Test
    public void testForeignSources() throws Exception {
        createForeignSource();
        String url = "/foreignSources";
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("ICMP"));

        url = "/foreignSources/count";
        xml = sendRequest(GET, url, 200);
        assertEquals(xml, "1", xml);

        url = "/foreignSources/test";
        sendPut(url, "scanInterval=1h", 202, "/foreignSources/test");
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<scan-interval>1h</scan-interval>"));
        
        url = "/foreignSources/test";
        sendPut(url, "scanInterval=1h", 202, "/foreignSources/test");
        sendRequest(DELETE, url, 202);
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<scan-interval>1d</scan-interval>"));
        
        sendRequest(DELETE, url, 202);
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<scan-interval>1d</scan-interval>"));
    }
    
    @Test
    public void testDetectors() throws Exception {
        createForeignSource();

        String url = "/foreignSources/test/detectors";
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml, xml.contains("<detectors "));
        assertTrue(xml, xml.contains("<detector "));
        assertTrue(xml, xml.contains("name=\"ICMP\""));
        
        url = "/foreignSources/test/detectors/HTTP";
        xml = sendRequest(GET, url, 200);
        assertTrue(xml, xml.contains("org.opennms.netmgt.provision.detector.simple.HttpDetector"));

        xml = sendRequest(DELETE, url, 202);
        xml = sendRequest(GET, url, 404);
    }

    @Test
    public void testPolicies() throws Exception {
        createForeignSource();

        String url = "/foreignSources/test/policies";
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml, xml.contains("<policies "));
        assertTrue(xml, xml.contains("<policy "));
        assertTrue(xml, xml.contains("name=\"lower-case-node\""));
        assertTrue(xml, xml.contains("value=\"Lower-Case-Nodes\""));
        
        url = "/foreignSources/test/policies/all-ipinterfaces";
        xml = sendRequest(GET, url, 200);
        assertTrue(xml, xml.contains("org.opennms.netmgt.provision.persist.policies.InclusiveInterfacePolicy"));
        
        xml = sendRequest(DELETE, url, 202);
        xml = sendRequest(GET, url, 404);
    }

    /**
     * Verifies that updating certain elements within the graph works,
     * e.g. add/remove/edit plugins, as well as parameters
     */
    @Test
    public void verifyUpdateGraph() throws Exception {
        createForeignSource();
        ForeignSource foreignSource = get("test");
        foreignSource.setPolicies(new ArrayList<>()); // remove policies
        post(foreignSource);

        // Verify the entries in the database
        List<PluginConfigEntity> pluginConfigs = findPlugins();
        Assert.assertEquals(4, pluginConfigs.size());
        List parameters = findPluginParameters();
        Assert.assertEquals(0, parameters.size());

        // Verify the rest API
        foreignSource = get("test");
        Assert.assertEquals(0, foreignSource.getPolicies().size());
        Assert.assertEquals(4, foreignSource.getDetectors().size());
        Assert.assertEquals(0, foreignSource.getDetectors().stream().mapToLong(d -> d.getParameters().size()).sum());

        // Add Policy and Edit Detector
        foreignSource.addPolicy(new PluginConfig("dummy", "dummy.class"));
        foreignSource.getDetector("DNS").setName("DNS2");
        foreignSource.getDetector("HTTP").addParameter("port", "8980");
        post(foreignSource);

        // Verify
        pluginConfigs = findPlugins();
        Assert.assertEquals(5, pluginConfigs.size());
        parameters = findPluginParameters();
        Assert.assertEquals(1, parameters.size());

        foreignSource = get("test");
        Assert.assertEquals(1, foreignSource.getPolicies().size());
        Assert.assertEquals(4, foreignSource.getDetectors().size());
        Assert.assertNotNull(foreignSource.getDetector("DNS2"));
        Assert.assertNull(foreignSource.getDetector("DNS"));
        Assert.assertEquals(1, foreignSource.getDetector("HTTP").getParameters().size());
        Assert.assertEquals("8980", foreignSource.getDetector("HTTP").getParameter("port"));

        // Remove ForeignSource
        delete("test");
        Assert.assertEquals(0, findPlugins().size());
        Assert.assertEquals(0, findPluginParameters().size());
    }

    @Test
    public void verifyOrder() throws Exception {
        createForeignSource();
        ForeignSource foreignSource = get("test");

        // Change order
        foreignSource.setDetectors(foreignSource.getDetectors().stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList()));
        foreignSource.setPolicies(foreignSource.getPolicies().stream().sorted(Comparator.comparing(PluginConfig::getPluginClass)).collect(Collectors.toList()));
        post(foreignSource);

        // Verify change
        foreignSource = get("test");
        Assert.assertEquals(
                Lists.newArrayList("ICMP", "HTTPS", "HTTP", "DNS"),
                foreignSource.getDetectors().stream().map(d -> d.getName()).collect(Collectors.toList()));
        Assert.assertEquals(
                Lists.newArrayList("org.opennms.netmgt.provision.persist.policies.InclusiveInterfacePolicy", "org.opennms.netmgt.provision.persist.policies.NodeCategoryPolicy"),
                foreignSource.getPolicies().stream().map(p -> p.getPluginClass()).collect(Collectors.toList()));

        // Verify again and see it is updated as well
        foreignSource.setDetectors(foreignSource.getDetectors().stream().sorted(Comparator.comparing(PluginConfig::getName)).collect(Collectors.toList()));
        post(foreignSource);
        Assert.assertEquals(
                Lists.newArrayList("DNS", "HTTP", "HTTPS", "ICMP"),
                foreignSource.getDetectors().stream().map(d -> d.getName()).collect(Collectors.toList()));
    }

    private void createForeignSource() throws Exception {
        // Be sure there are no foreign-sources defined
        System.err.println("[createForeignSource] : deleting existing foreign source definitions");
        ForeignSourceCollection collection = JaxbUtils.unmarshal(ForeignSourceCollection.class, sendRequest(GET, "/foreignSources", 200));
        collection.getForeignSources().forEach(f -> {
            try {
                System.err.printf("[createForeignSource] : deleting %s\n", f.getName());
                sendRequest(DELETE, "/foreignSources/" + f.getName(), 202);
                sendRequest(DELETE, "/requisitions/" + f.getName(), 202);
            } catch (Exception e) {
                fail(e.getMessage());
            }
        });
        // Create sample foreign source
        System.err.println("[createForeignSource] : creating sample foreign source");
        String fs =
            "<foreign-source xmlns=\"http://xmlns.opennms.org/xsd/config/foreign-source\" name=\"test\">" +
                "<scan-interval>1d</scan-interval>" +
                "<detectors>" + 
                    "<detector class=\"org.opennms.netmgt.provision.detector.datagram.DnsDetector\" name=\"DNS\"/>" +
                    "<detector class=\"org.opennms.netmgt.provision.detector.simple.HttpDetector\" name=\"HTTP\"/>" +
                    "<detector class=\"org.opennms.netmgt.provision.detector.simple.HttpsDetector\" name=\"HTTPS\"/>" +
                    "<detector class=\"org.opennms.netmgt.provision.detector.icmp.IcmpDetector\" name=\"ICMP\"/>" +
                "</detectors>" +
                "<policies>" +
                    "<policy name=\"lower-case-node\" class=\"org.opennms.netmgt.provision.persist.policies.NodeCategoryPolicy\">" +
                        "<parameter key=\"label\" value=\"~^[a-z]$\" />" +
                        "<parameter key=\"category\" value=\"Lower-Case-Nodes\" />" +
                    "</policy>" +
                    "<policy name=\"all-ipinterfaces\" class=\"org.opennms.netmgt.provision.persist.policies.InclusiveInterfacePolicy\" />" +
                "</policies>" +
            "</foreign-source>";
        MockHttpServletResponse response = sendPost("/foreignSources", fs, 202, "/foreignSources/test");
        System.err.println("response = " + stringifyResponse(response));
    }

    private ForeignSource get(String foreignSource) throws Exception {
        return unmarshal(sendRequest(GET, "/foreignSources/" + foreignSource, 200));
    }

    private void post(ForeignSource input) throws Exception {
        sendPost("/foreignSources", marshal(input), 202, "/foreignSources/" + input.getName());
    }

    private void delete(String foreignSource) throws Exception {
        sendRequest(DELETE, "/foreignSources/" + foreignSource, 202);
    }

    private List<PluginConfigEntity> findPlugins() {
        return genericPersistenceAccessor.find("select c from PluginConfigEntity c");
    }

    private List findPluginParameters() {
        return genericPersistenceAccessor.find("select new list(elements(c.parameters)) from PluginConfigEntity c");
    }

    private static ForeignSource unmarshal(String input) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes())) {
            return JAXB.unmarshal(in, ForeignSource.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String marshal(ForeignSource foreignSource) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            JAXB.marshal(foreignSource, out);
            return out.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
