/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
