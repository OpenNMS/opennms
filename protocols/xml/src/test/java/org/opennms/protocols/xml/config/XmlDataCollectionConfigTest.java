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
package org.opennms.protocols.xml.config;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.netmgt.collection.api.AttributeType;

/**
 * The Class XmlDataCollectionConfigTest.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class XmlDataCollectionConfigTest extends XmlTestNoCastor<XmlDataCollectionConfig> {

    private final XmlDataCollectionConfig sampleObject;

    public XmlDataCollectionConfigTest(XmlDataCollectionConfig sampleObject, Object sampleXml, String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
        this.sampleObject = sampleObject;
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {
                getXmlDataCollectionConfig(),
                new File("src/test/resources/", XmlDataCollectionConfig.XML_DATACOLLECTION_CONFIG_FILE),
                "src/main/resources/xsds/xml-datacollection-config.xsd"
            }
        });
    }

    @Test
    public void canClone() {
        XmlDataCollectionConfig clone = sampleObject.clone();
        assertEquals(sampleObject, clone);
    }

    public static XmlDataCollectionConfig getXmlDataCollectionConfig() {
        // Mock up a XmlDataCollectionConfig class.      
        XmlRrd xmlRrd = new XmlRrd();
        xmlRrd.addRra("RRA:AVERAGE:0.5:1:8928");
        xmlRrd.addRra("RRA:AVERAGE:0.5:12:8784");
        xmlRrd.addRra("RRA:MIN:0.5:12:8784");
        xmlRrd.addRra("RRA:MAX:0.5:12:8784");
        xmlRrd.setStep(300);

        XmlObject cpu = new XmlObject();
        cpu.setName("cpuUtilization");
        cpu.setDataType(AttributeType.GAUGE);
        cpu.setXpath("r[@p=1]");

        XmlObject mem = new XmlObject();
        mem.setName("memUtilization");
        mem.setDataType(AttributeType.GAUGE);
        mem.setXpath("r[@p=2]");

        XmlObject suspect = new XmlObject();
        suspect.setName("suspect");
        suspect.setDataType(AttributeType.STRING);
        suspect.setXpath("suspect");

        XmlGroup group = new XmlGroup();
        group.setName("platform-system-resource");
        group.setResourceType("platformSystemResource");
        group.setResourceXpath("/measCollecFile/measData/measInfo[@measInfoId='platform-system|resource']/measValue");
        group.setKeyXpath("@measObjLdn");
        group.setTimestampXpath("/measCollecFile/fileFooter/measCollec/@endTime");
        group.setTimestampFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        group.addXmlObject(cpu);
        group.addXmlObject(mem);
        group.addXmlObject(suspect);

        XmlSource source = new XmlSource();
        source.setUrl("sftp.3gpp://opennms:Op3nNMS!@{ipaddr}/opt/3gpp/data/?step={step}&neId={foreignId}");
        source.addXmlGroup(group);

        XmlDataCollection xmlDataCollection = new XmlDataCollection();
        xmlDataCollection.setXmlRrd(xmlRrd);
        xmlDataCollection.addXmlSource(source);
        xmlDataCollection.setName("3GPP");

        XmlDataCollectionConfig xmlDataCollectionConfig = new XmlDataCollectionConfig();
        xmlDataCollectionConfig.setRrdRepository("/opt/opennms/share/rrd/snmp/");
        xmlDataCollectionConfig.addDataCollection(xmlDataCollection);

        return xmlDataCollectionConfig;
    }
}
