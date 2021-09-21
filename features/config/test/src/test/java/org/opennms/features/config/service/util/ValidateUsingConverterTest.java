/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.dao.api.ConfigConverter;
import org.opennms.features.config.dao.api.ValidationSchema;
import org.opennms.features.config.dao.impl.util.ValidateUsingConverter;
import org.opennms.features.config.service.config.FakeXsdForTest;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.opennms.netmgt.config.trapd.Snmpv3User;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.skyscreamer.jsonassert.JSONAssert;

import com.google.common.io.Resources;

@JUnitConfigurationEnvironment
public class ValidateUsingConverterTest {
    final static String FOREIGN_SOURCES = "/opt/opennms/etc/foreign-sources";

    @Test
    public void testConverter() throws IOException, JAXBException {
        final ValidateUsingConverter<ProvisiondConfiguration> converter = new ValidateUsingConverter<>(ProvisiondConfiguration.class);
        final String sourceXml = Resources.toString(
                Resources.getResource("provisiond-configuration.xml"), StandardCharsets.UTF_8);
        final String expectedJson = Resources.toString(
                Resources.getResource("provisiond.json"), StandardCharsets.UTF_8);

        // Verify the rendered JSON
        final String convertedJson = converter.xmlToJson(sourceXml);
        JSONAssert.assertEquals(expectedJson, convertedJson, true);

        // Verify the rendered JSON
        final ProvisiondConfiguration objectFromMappedJson = JaxbUtils.unmarshal(ProvisiondConfiguration.class, converter.jsonToXml(convertedJson));

        Assert.assertEquals("json importThreads Value is not correct", 11L, (long) objectFromMappedJson.getImportThreads());
        Assert.assertTrue("json foreign-source-dir is not correct. " + objectFromMappedJson.getForeignSourceDir(), FOREIGN_SOURCES.equals(objectFromMappedJson.getForeignSourceDir()));

        // compare Object from json to object from source xml
        final ProvisiondConfiguration objectFromSourceXml = JaxbUtils.unmarshal(ProvisiondConfiguration.class, sourceXml);
        assertThat(objectFromMappedJson, equalTo(objectFromSourceXml));

        // check xml > json > xml > object
        String convertedXml = converter.jsonToXml(convertedJson);
        ProvisiondConfiguration objectFromConvertedXml =  JaxbUtils.unmarshal(ProvisiondConfiguration.class, convertedXml);
        Assert.assertEquals("Object ImportThreads Value is not correct", 11L, (long) objectFromConvertedXml.getImportThreads());
        Assert.assertTrue("Object ForeignSourceDir is not correct. " + objectFromConvertedXml.getForeignSourceDir(), FOREIGN_SOURCES.equals(objectFromConvertedXml.getForeignSourceDir()));
    }

    @Test
    public void testXsdSearch() throws IOException, JAXBException {
        // check if xsd is not located in xsds path
        ValidateUsingConverter<FakeXsdForTest> converter = new ValidateUsingConverter<>(FakeXsdForTest.class);
        ValidationSchema schema = converter.getValidationSchema();
        Assert.assertNotNull("Fail to find schema!!!", schema);
    }

    @Test
    public void testValidate() throws JAXBException, IOException {
        ValidateUsingConverter<FakeXsdForTest> converter = new ValidateUsingConverter<>(FakeXsdForTest.class);
        FakeXsdForTest test = new FakeXsdForTest(1024, "127.0.0.1");
        Snmpv3User user = new Snmpv3User();
        user.setSecurityName("SecurityName");
        test.addSnmpv3User(user);
        test.setUseAddressFromVarbind(true);
        converter.validate(test);
        String xmlStr = converter.jaxbObjectToXml(test);
        FakeXsdForTest convertedTest =  JaxbUtils.unmarshal(FakeXsdForTest.class, xmlStr);
        Assert.assertEquals("Trap port is wrong after conversion!",
                1024, convertedTest.getSnmpTrapPort());
        Assert.assertEquals("SnmpTrapAddress is wrong after conversion!",
                "127.0.0.1", convertedTest.getSnmpTrapAddress());
        Assert.assertEquals("Snmpv3User is wrong after conversion!",
                "SecurityName", convertedTest.getSnmpv3User(0).getSecurityName());
        converter.validate(convertedTest);
    }

    /**
     * it is expected to have exception due to not xsd validation. importThreads > 0
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Test(expected = RuntimeException.class)
    public void testValidateFail() throws JAXBException, IOException {
        ValidateUsingConverter<FakeXsdForTest> converter = new ValidateUsingConverter<>(FakeXsdForTest.class);
        FakeXsdForTest test = new FakeXsdForTest();
        test.setSnmpTrapPort(-1);
        converter.validate(test);
    }

    /**
     * it is expected to have exception due to not xsd validation.
     * @throws IOException
     * @throws ClassNotFoundException
     */

    @Test(expected = RuntimeException.class)
    public void testJsonValidateFail() throws JAXBException, IOException {
        final String invalidJson = Resources.toString(
                Resources.getResource("provisiond_invalid.json"), StandardCharsets.UTF_8);
        ValidateUsingConverter<ProvisiondConfiguration> converter = new ValidateUsingConverter<>(ProvisiondConfiguration.class);
        converter.validate(converter.jsonToXml(invalidJson), ConfigConverter.SCHEMA_TYPE.XML);
    }
}
