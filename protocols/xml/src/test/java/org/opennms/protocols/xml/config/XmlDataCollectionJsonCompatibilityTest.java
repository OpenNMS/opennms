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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.netmgt.collection.api.AttributeType;

/**
 * The XML collector configuration is imported into the configuration manager the way the upgrade's
 * {@code importConfig} / {@code importConfigDirectory} do it: XML converted to JSON through the XSD, validated
 * against the schema built from the XSD. {@link org.opennms.protocols.xml.dao.cm.XmlDataCollectionConfigDaoCm} reads
 * it back with {@link ConfigConvertUtil}. Every shipped, example and test configuration has to survive that trip
 * unchanged.
 */
@RunWith(Parameterized.class)
public class XmlDataCollectionJsonCompatibilityTest {

    private static final String XSD = "xml-datacollection-config.xsd";

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> files() {
        return Arrays.asList(new Object[][]{
                {"src/main/etc/xml-datacollection-config.xml", XmlDataCollectionConfig.class},
                {"src/main/etc/examples/3gpp-juniper/xml-datacollection-config.xml", XmlDataCollectionConfig.class},
                {"src/test/resources/etc/xml-datacollection-config.xml", XmlDataCollectionConfig.class},
                {"src/test/resources/xml-datacollection-config.xml", XmlDataCollectionConfig.class},
                {"src/test/resources/3gpp-xml-datacollection-config.xml", XmlDataCollectionConfig.class},
                {"src/test/resources/http-datacollection-config.xml", XmlDataCollectionConfig.class},
                {"src/test/resources/json-array-datacollection-config.xml", XmlDataCollectionConfig.class},
                {"src/test/resources/namespace-datacollection-config.xml", XmlDataCollectionConfig.class},
                {"src/test/resources/node-level-datacollection-config.xml", XmlDataCollectionConfig.class},
                {"src/test/resources/NMS-14084-xml-datacollection-config.xml", XmlDataCollectionConfig.class},
                {"src/test/resources/NMS-14559-xml-datacollection-config.xml", XmlDataCollectionConfig.class},
                {"src/test/resources/NMS-15222-xml-datacollection-config.xml", XmlDataCollectionConfig.class},
                {"src/test/resources/rpc-reply-datacollection-config.xml", XmlDataCollectionConfig.class},
                {"src/test/resources/solaris-zones-datacollection-config.xml", XmlDataCollectionConfig.class},
                {"src/main/etc/xml-datacollection/elasticsearch-cluster-stats.xml", XmlGroups.class},
                {"src/main/etc/examples/3gpp-juniper/xml-datacollection/3gpp.full.xml", XmlGroups.class},
                {"src/test/resources/etc/xml-datacollection/3gpp.core.xml", XmlGroups.class},
                {"src/test/resources/etc/xml-datacollection/3gpp.platform.xml", XmlGroups.class},
        });
    }

    private final String file;
    private final Class<?> type;

    public XmlDataCollectionJsonCompatibilityTest(final String file, final Class<?> type) {
        this.file = file;
        this.type = type;
    }

    @Test
    public void xmlImportedThroughTheSchemaReadsBackUnchanged() throws IOException {
        final String xml = Files.readString(Paths.get(file), StandardCharsets.UTF_8);
        final String rootElement = type == XmlGroups.class ? "xml-groups" : "xml-datacollection-config";

        // Same conversion and validation the upgrade runs, built from the definition importSchemaFromXsd registers.
        final ConfigDefinition definition = XsdHelper.buildConfigDefinition("test", XSD, rootElement,
                ConfigurationManagerService.BASE_PATH, type == XmlGroups.class);
        final String importedJson = XsdHelper.getConverter(definition).xmlToJson(xml);
        definition.validate(importedJson);

        final Object fromXml = JaxbUtils.unmarshal(type, xml);
        final Object fromJson = ConfigConvertUtil.jsonToObject(importedJson, type);
        assertThat(JaxbUtils.marshal(fromJson), equalTo(JaxbUtils.marshal(fromXml)));

        // ... and what the DAO (or a later UI) writes back is read the same way and still valid.
        final String writtenJson = ConfigConvertUtil.objectToJson(fromJson);
        definition.validate(writtenJson);
        assertThat(JaxbUtils.marshal(ConfigConvertUtil.jsonToObject(writtenJson, type)), equalTo(JaxbUtils.marshal(fromXml)));
    }

    @Test
    public void attributeTypesAreStoredWithTheirXmlNames() throws IOException {
        final String xml = Files.readString(Paths.get(file), StandardCharsets.UTF_8);
        final Object config = JaxbUtils.unmarshal(type, xml);
        final String json = ConfigConvertUtil.objectToJson(config);
        for (final AttributeType attributeType : AttributeType.values()) {
            assertThat(json.contains("\"type\":\"" + attributeType.name() + "\""), equalTo(false));
        }
        assertThat(ConfigConvertUtil.jsonToObject(json, type), notNullValue());
    }
}
