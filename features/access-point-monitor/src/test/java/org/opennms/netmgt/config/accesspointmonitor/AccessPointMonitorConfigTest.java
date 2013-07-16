/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.accesspointmonitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.test.FileAnticipator;
import org.xml.sax.SAXException;

public class AccessPointMonitorConfigTest {
    private FileAnticipator fa;
    private AccessPointMonitorConfig apmc;

    static private class TestOutputResolver extends SchemaOutputResolver {
        private final File m_schemaFile;

        public TestOutputResolver(File schemaFile) {
            m_schemaFile = schemaFile;
        }

        @Override
        public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
            return new StreamResult(m_schemaFile);
        }
    }

    @Before
    public void setUp() throws Exception {
        fa = new FileAnticipator();

        ServiceTemplate svcTemplate = new ServiceTemplate();
        svcTemplate.setName("IsAPAdoptedOnController-Template");
        svcTemplate.setInterval(120000L);
        svcTemplate.setStatus("off");
        svcTemplate.addParameter(new Parameter("retry", "2"));
        svcTemplate.addParameter(new Parameter("oid", ".1.3.6.1.4.1.14823.2.2.1.5.2.1.4.1.19"));
        svcTemplate.addParameter(new Parameter("operator", "="));
        svcTemplate.addParameter(new Parameter("operand", "1"));
        svcTemplate.addParameter(new Parameter("match", "true"));

        Service svc = new Service();
        svc.setName("IsAPAdoptedOnController");
        svc.setTemplateName("IsAPAdoptedOnController-Template");
        svc.setStatus("on");
        svc.addParameter(new Parameter("retry", "3"));

        Package pkg = new Package();
        pkg.setName("default");
        pkg.setFilter("IPADDR != '0.0.0.0'");
        pkg.addSpecific("172.23.1.1");
        pkg.addIncludeRange(new IpRange("192.168.0.0", "192.168.255.255"));
        pkg.addExcludeRange(new IpRange("192.168.1.0", "192.168.1.255"));
        pkg.setService(svc);

        Monitor monitor = new Monitor();
        monitor.setService("IsAPAdoptedOnController");
        monitor.setClassName("org.opennms.netmgt.accesspointmonitor.poller.InstanceStrategy");

        apmc = new AccessPointMonitorConfig();
        apmc.setThreads(30);
        apmc.setPackageScanInterval(1800000L);
        apmc.addServiceTemplate(svcTemplate);
        apmc.addPackage(pkg);
        apmc.addMonitor(monitor);

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setNormalize(true);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void generateSchema() throws Exception {
        File schemaFile = fa.expecting("access-point-monitor-configuration.xsd");
        JAXBContext c = JAXBContext.newInstance(AccessPointMonitorConfig.class);
        c.generateSchema(new TestOutputResolver(schemaFile));
        if (fa.isInitialized()) {
            fa.deleteExpected();
        }
    }

    @Test
    public void generateXML() throws Exception {
        // Marshal the test object to an XML string
        StringWriter objectXML = new StringWriter();
        JaxbUtils.marshal(apmc, objectXML);

        // Read the example XML from src/test/resources
        StringBuffer exampleXML = new StringBuffer();
        File apmConfig = new File(ClassLoader.getSystemResource("access-point-monitor-configuration.xml").getFile());
        assertTrue("access-point-monitor-configuration.xml is readable", apmConfig.canRead());
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(apmConfig), "UTF-8"));
        String line;
        while (true) {
            line = reader.readLine();
            if (line == null) {
                reader.close();
                break;
            }
            exampleXML.append(line).append("\n");
        }
        System.err.println("========================================================================");
        System.err.println("Object XML:");
        System.err.println("========================================================================");
        System.err.print(objectXML.toString());
        System.err.println("========================================================================");
        System.err.println("Example XML:");
        System.err.println("========================================================================");
        System.err.print(exampleXML.toString());
        DetailedDiff myDiff = getDiff(objectXML, exampleXML);
        assertEquals("Number of XMLUnit differences between the example XML and the mock object XML is 0", 0, myDiff.getAllDifferences().size());
    }

    @Test
    public void readXML() throws Exception {
        // Retrieve the file we're parsing.
        File apmConfig = new File(ClassLoader.getSystemResource("access-point-monitor-configuration.xml").getFile());
        assertTrue("access-point-monitor-configuration.xml is readable", apmConfig.canRead());

        AccessPointMonitorConfig exampleApmc = JaxbUtils.unmarshal(AccessPointMonitorConfig.class, apmConfig);

        assertTrue("Compare Access Point Monitor Config objects.", apmc.equals(exampleApmc));
    }

    @Test
    public void testServiceTemplate() throws Exception {
        // Get the service on the first package
        Service svc = apmc.getPackages().get(0).getService();
        svc = apmc.getPackages().get(0).getEffectiveService();

        assertEquals("Service should inherit service template parameters", 1, ParameterMap.getKeyedInteger(svc.getParameterMap(), "operand", 0));

        assertEquals("Service parameters should override template parameters", 3, ParameterMap.getKeyedInteger(svc.getParameterMap(), "retry", 0));

        apmc.setServiceTemplates(null);
        svc = apmc.getPackages().get(0).getEffectiveService();

        assertEquals("Services should reflect template changes", 0, ParameterMap.getKeyedInteger(svc.getParameterMap(), "operand", 0));
    }

    @Test
    public void testDefaultPassiveServiceName() throws Exception {
        // Get the service on the first package
        Service svc = apmc.getPackages().get(0).getEffectiveService();

        assertEquals("Passive service name should default to the service name if not set", svc.getName(), svc.getPassiveServiceName());

        String passiveServiceName = "Not" + svc.getName();
        svc.setPassiveServiceName(passiveServiceName);

        assertEquals("Passive service name should not return the service name if set", passiveServiceName, svc.getPassiveServiceName());
    }

    @Test
    public void testSpecialValuesInFilter() throws Exception {
        Package pkg = new Package();
        pkg.setName("default");
        pkg.setFilter("pollerCategory == '%packageName%'");
        assertEquals("The package name should be replaced in the filter.", "pollerCategory == 'default'", pkg.getEffectiveFilter());

        pkg.setFilter("packageName");
        assertEquals("The package name should not be replaced in the filter.", "packageName", pkg.getEffectiveFilter());
    }

    @SuppressWarnings("unchecked")
    private DetailedDiff getDiff(StringWriter objectXML, StringBuffer exampleXML) throws SAXException, IOException {
        DetailedDiff myDiff = new DetailedDiff(XMLUnit.compareXML(exampleXML.toString(), objectXML.toString()));
        List<Difference> allDifferences = myDiff.getAllDifferences();
        if (allDifferences.size() > 0) {
            for (Difference d : allDifferences) {
                System.err.println(d);
            }
        }
        return myDiff;
    }
}
