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
package org.opennms.netmgt.provision.persist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSourceCollection;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.test.FileAnticipator;
import org.xml.sax.SAXException;

public class PersistenceSerializationTest {
    private ForeignSourceCollection fsw;
    private AbstractForeignSourceRepository fsr;
    private Marshaller m;
    private JAXBContext c;
    private ForeignSource fs;
    private FileAnticipator fa;

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
        MockLogAppender.setupLogging();

        fa = new FileAnticipator();

        fsr = new MockForeignSourceRepository();
        fsr.save(new ForeignSource("cheese"));
        fsr.flush();

        fs = fsr.getForeignSource("cheese");
//        fs.setScanInterval(scanInterval)
        XMLGregorianCalendar cal = DatatypeFactory.newInstance().newXMLGregorianCalendar("2009-02-25T12:45:38.800-05:00");
        fs.setDateStamp(cal);

        List<PluginConfig> detectors = new ArrayList<>();
        final PluginConfig detector = new PluginConfig("food", "org.opennms.netmgt.provision.persist.detectors.FoodDetector");
        detector.addParameter("type", "cheese");
        detector.addParameter("density", "soft");
        detector.addParameter("sharpness", "mild");
        detectors.add(detector);
        fs.setDetectors(detectors);

        List<PluginConfig> policies = new ArrayList<>();
        PluginConfig policy = new PluginConfig("lower-case-node", "org.opennms.netmgt.provision.persist.policies.NodeCategoryPolicy");
        policy.addParameter("label", "~^[a-z]$");
        policy.addParameter("category", "Lower-Case-Nodes");
        policies.add(policy);
        policy = new PluginConfig("all-ipinterfaces", "org.opennms.netmgt.provision.persist.policies.InclusiveInterfacePolicy");
        policies.add(policy);
        policy = new PluginConfig("10-ipinterfaces", "org.opennms.netmgt.provision.persist.policies.MatchingInterfacePolicy");
        policy.addParameter("ipaddress", "~^10\\..*$");
        policies.add(policy);
        policy = new PluginConfig("cisco-snmp-interfaces", "org.opennms.netmgt.provision.persist.policies.MatchingSnmpInterfacePolicy");
        policy.addParameter("ifdescr", "~^(?i:LEC).*$");
        policies.add(policy);
        fs.setPolicies(policies);

        fsw = new ForeignSourceCollection();
        fsw.getForeignSources().addAll(fsr.getForeignSources());
        c = JAXBContext.newInstance(ForeignSourceCollection.class, ForeignSource.class);
        m = c.createMarshaller();
        
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setNormalize(true);
    }

    @After
    public void tearDown() throws Exception {
        fa.tearDown();
    }

    @Test
    public void generateSchema() throws Exception {
        File schemaFile = fa.expecting("foreign-sources.xsd");
        c.generateSchema(new TestOutputResolver(schemaFile));
        if (fa.isInitialized()) {
            fa.deleteExpected();
        }
    }
    
    @Test
    public void generateXML() throws Exception {
        // Marshal the test object to an XML string
        StringWriter objectXML = new StringWriter();
        m.marshal(fsw, objectXML);

        // Read the example XML from src/test/resources
        final StringBuilder exampleXML = new StringBuilder();
        File foreignSources = new File(ClassLoader.getSystemResource("foreign-sources.xml").getFile());
        assertTrue("foreign-sources.xml is readable", foreignSources.canRead());
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(foreignSources), StandardCharsets.UTF_8));
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
        assertEquals("number of XMLUnit differences between the example XML and the mock object XML is 0", 0, myDiff.getAllDifferences().size());
    }

    @SuppressWarnings("unchecked")
    private static DetailedDiff getDiff(StringWriter objectXML, StringBuilder exampleXML) throws SAXException, IOException {
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
