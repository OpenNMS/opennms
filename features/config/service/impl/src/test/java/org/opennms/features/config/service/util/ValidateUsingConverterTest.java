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

import com.google.common.io.Resources;
import org.eclipse.persistence.jpa.jpql.Assert;
import org.junit.Test;
import org.opennms.features.config.dao.api.ServiceSchema;
import org.opennms.features.config.service.config.FakeXsdForTest;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


@JUnitConfigurationEnvironment
public class ValidateUsingConverterTest {
    final static String FOREIGN_SOURCES = "/opt/opennms/etc/foreign-sources";

    @Test
    public void testConverter() throws IOException {
        final ValidateUsingConverter<ProvisiondConfiguration> converter = new ValidateUsingConverter<>(ProvisiondConfiguration.class);
        final String sourceXml = Resources.toString(
                Resources.getResource("provisiond-configuration.xml"), StandardCharsets.UTF_8);
        final String expectedJson = Resources.toString(
                Resources.getResource("provisiond.json"), StandardCharsets.UTF_8);

        // Verify the rendered JSON
        final String convertedJson = converter.xmlTOJson(sourceXml);
        JSONAssert.assertEquals(expectedJson, convertedJson, true);

        // Verify the rendered JSON
        final ProvisiondConfiguration objectFromMappedJson = converter.jsonToJaxbObject(convertedJson);
        Assert.isEqual(11L, objectFromMappedJson.getImportThreads(), "json importThreads Value is not correct");
        Assert.isTrue(FOREIGN_SOURCES.equals(objectFromMappedJson.getForeignSourceDir()),
                "json foreign-source-dir is not correct. " + objectFromMappedJson.getForeignSourceDir());

        // compare Object from json to object from source xml
        final ProvisiondConfiguration objectFromSourceXml = converter.xmlToJaxbObject(sourceXml);
        assertThat(objectFromMappedJson, equalTo(objectFromSourceXml));

        // check xml > json > xml > object
        String convertedXml = converter.jsonToXml(convertedJson);
        ProvisiondConfiguration objectFromConvertedXml = converter.xmlToJaxbObject(convertedXml);
        Assert.isEqual(11L, objectFromConvertedXml.getImportThreads(), "Object ImportThreads Value is not correct");
        Assert.isTrue(FOREIGN_SOURCES.equals(objectFromConvertedXml.getForeignSourceDir()),
                "Object ForeignSourceDir is not correct. " + objectFromConvertedXml.getForeignSourceDir());
    }

    @Test
    public void testXsdSearch() throws IOException {
        // check if xsd is not located in xsds path
        ValidateUsingConverter<FakeXsdForTest> converter = new ValidateUsingConverter<>(FakeXsdForTest.class);
        ServiceSchema schema = converter.getServiceSchema();
    }
}
