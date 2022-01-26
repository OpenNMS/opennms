/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.features.config.service.util;

import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.dao.impl.util.JaxbXmlConverter;
import org.opennms.features.config.dao.impl.util.XmlSchema;
import org.opennms.features.config.service.config.FakeXsdForTest;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.opennms.netmgt.config.trapd.Snmpv3User;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@JUnitConfigurationEnvironment
public class JaxbXmlConverterTest {
    final static String FOREIGN_SOURCES = "/opt/opennms/etc/foreign-sources";

    @Test
    public void testConverter() throws IOException {
        final JaxbXmlConverter converter = new JaxbXmlConverter("provisiond-configuration.xsd", "provisiond-configuration", null);
        final String sourceXml = Resources.toString(
                Resources.getResource("provisiond-configuration.xml"), StandardCharsets.UTF_8);
        final String expectedJson = Resources.toString(
                Resources.getResource("provisiond.json"), StandardCharsets.UTF_8);

        // Verify the rendered JSON
        final String convertedJson = converter.xmlToJson(sourceXml);
        JSONAssert.assertEquals(expectedJson, convertedJson, true);

        // Verify the rendered JSON
        final ProvisiondConfiguration objectFromMappedJson = ConfigConvertUtil.jsonToObject(convertedJson, ProvisiondConfiguration.class);

        Assert.assertEquals("json importThreads Value is not correct", 11L, (long) objectFromMappedJson.getImportThreads());
        Assert.assertEquals("json foreign-source-dir is not correct. " + objectFromMappedJson.getForeignSourceDir(), FOREIGN_SOURCES, objectFromMappedJson.getForeignSourceDir());

        // compare Object from json to object from source xml
        final ProvisiondConfiguration objectFromSourceXml = JaxbUtils.unmarshal(ProvisiondConfiguration.class, sourceXml);
        assertThat(objectFromMappedJson, equalTo(objectFromSourceXml));
    }

    @Test
    public void testXsdSearch() throws IOException {
        // check if xsd is not located in xsds path
        JaxbXmlConverter converter = new JaxbXmlConverter("trapd-configuration.xsd", "trapd-configuration", null);
        XmlSchema schema = converter.getXmlSchema();
        Assert.assertNotNull("Fail to find schema!!!", schema);
    }

    @Test
    public void testValidate() {
        FakeXsdForTest test = new FakeXsdForTest(1024, "127.0.0.1");
        Snmpv3User user = new Snmpv3User();
        user.setSecurityName("SecurityName");
        test.addSnmpv3User(user);
        test.setUseAddressFromVarbind(true);
        String xmlStr = JaxbUtils.marshal(test);

        FakeXsdForTest convertedTest = JaxbUtils.unmarshal(FakeXsdForTest.class, xmlStr);
        Assert.assertEquals("Trap port is wrong after conversion!",
                1024, convertedTest.getSnmpTrapPort());
        Assert.assertEquals("SnmpTrapAddress is wrong after conversion!",
                "127.0.0.1", convertedTest.getSnmpTrapAddress());
        Assert.assertEquals("Snmpv3User is wrong after conversion!",
                "SecurityName", convertedTest.getSnmpv3User(0).getSecurityName());
    }
}