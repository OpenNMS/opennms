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
package org.opennms.features.config.service.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.opennms.core.xml.JacksonUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.netmgt.config.datacollection.Parameter;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.ResourceTypes;
import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * The resource types of {@code etc/resource-types.d/} move to the configuration manager, schema
 * {@code resource-types} built from {@code resource-types.xsd}. The upgrade converts the XML files to JSON through
 * the schema, the DAO reads that JSON with {@link ConfigConvertUtil}, and core publishes it to the JSON store with
 * Jackson 1 for Sentinel. All three must agree with what JAXB reads from the files.
 */
public class ResourceTypesJsonCompatibilityTest {

    private static final Path ASSEMBLY_RESOURCE_TYPES = Path.of("../../../opennms-base-assembly/src/main/filtered/etc/resource-types.d");
    private static final Path XML_COLLECTOR_RESOURCE_TYPES = Path.of("../../../protocols/xml/src/main/etc/resource-types.d/xml-collector.xml");

    private static final String STORED_JSON = "{\"resourceType\":[{"
            + "\"name\":\"pgDatabase\",\"label\":\"PostgreSQL Database\",\"resourceLabel\":\"${datname}\","
            + "\"persistenceSelectorStrategy\":{\"class\":\"org.opennms.netmgt.collection.support.PersistRegexSelectorStrategy\","
            + "\"parameter\":[{\"key\":\"match-expression\",\"value\":\"#datname matches '^p.*'\"}]},"
            + "\"storageStrategy\":{\"class\":\"org.opennms.netmgt.dao.support.SiblingColumnStorageStrategy\","
            + "\"parameter\":[{\"key\":\"sibling-column-name\",\"value\":\"datname\"},{\"key\":\"replace-all\",\"value\":\"s/,/_/\"}]}"
            + "},{"
            + "\"name\":\"pgTableSpace\",\"label\":\"PostgreSQL Tablespace\","
            + "\"persistenceSelectorStrategy\":{\"class\":\"org.opennms.netmgt.collection.support.PersistAllSelectorStrategy\"},"
            + "\"storageStrategy\":{\"class\":\"org.opennms.netmgt.collection.support.IndexStorageStrategy\"}"
            + "}]}";

    @Test
    public void shippedFilesImportThroughTheSchemaAndReadBackUnchanged() throws IOException {
        final ConfigDefinition definition = definition();
        final List<Path> files = shippedFiles();
        assertFalse("no shipped files found", files.isEmpty());
        for (final Path file : files) {
            final String xml = Files.readString(file, StandardCharsets.UTF_8);
            // Same conversion the upgrade's importConfigDir runs
            final String importedJson = XsdHelper.getConverter(definition).xmlToJson(xml);
            definition.validate(importedJson);

            final ResourceTypes fromXml = JaxbUtils.unmarshal(ResourceTypes.class, xml);
            final ResourceTypes fromJson = ConfigConvertUtil.jsonToObject(importedJson, ResourceTypes.class);
            assertFalse(file + " has resource types", fromXml.getResourceTypes().isEmpty());
            assertThat(file.toString(), fromJson.getResourceTypes(), equalTo(fromXml.getResourceTypes()));

            // what the DAO and a later UI write must pass the schema and read back unchanged
            final String writtenJson = ConfigConvertUtil.objectToJson(fromJson);
            definition.validate(writtenJson);
            assertThat(file.toString(), ConfigConvertUtil.jsonToObject(writtenJson, ResourceTypes.class).getResourceTypes(),
                    equalTo(fromXml.getResourceTypes()));
        }
    }

    @Test
    public void storedJsonIsUnchanged() throws Exception {
        final String written = ConfigConvertUtil.objectToJson(fullResourceTypes());
        JSONAssert.assertEquals(STORED_JSON, written, true);
        assertThat("property order is unchanged too", written, equalTo(STORED_JSON));
        assertThat(ConfigConvertUtil.jsonToObject(STORED_JSON, ResourceTypes.class).getResourceTypes(),
                equalTo(fullResourceTypes().getResourceTypes()));
        definition().validate(STORED_JSON);
    }

    /**
     * Extensions provide parameters with their own implementation of the parameter interface.
     */
    @Test
    public void foreignParametersAreWrittenAndReadBackAsModelParameters() {
        final StorageStrategy strategy = new StorageStrategy("org.opennms.netmgt.dao.support.SiblingColumnStorageStrategy");
        strategy.addParameter(new org.opennms.netmgt.collection.api.Parameter() {
            @Override
            public String getKey() {
                return "sibling-column-name";
            }

            @Override
            public String getValue() {
                return "name";
            }
        });
        final String json = ConfigConvertUtil.objectToJson(strategy);
        assertThat(json, equalTo("{\"class\":\"org.opennms.netmgt.dao.support.SiblingColumnStorageStrategy\","
                + "\"parameter\":[{\"key\":\"sibling-column-name\",\"value\":\"name\"}]}"));

        final StorageStrategy read = ConfigConvertUtil.jsonToObject(json, StorageStrategy.class);
        assertThat(read.getParameters().get(0), instanceOf(Parameter.class));
        assertThat(read.getParameters(), equalTo(List.of(new Parameter("sibling-column-name", "name"))));
    }

    /**
     * Core publishes the resource types to the JSON store with Jackson 1, Sentinel reads them back with it.
     */
    @Test
    public void jsonStoreDocumentReadsBackUnchanged() throws IOException {
        final org.codehaus.jackson.map.ObjectMapper mapper = JacksonUtils.createDefaultObjectMapper();
        final ResourceTypes all = new ResourceTypes();
        for (final Path file : shippedFiles()) {
            all.getResourceTypes().addAll(JaxbUtils.unmarshal(ResourceTypes.class, Files.readString(file, StandardCharsets.UTF_8)).getResourceTypes());
        }
        all.getResourceTypes().addAll(fullResourceTypes().getResourceTypes());

        final ResourceTypes read = mapper.readValue(mapper.writeValueAsString(all), ResourceTypes.class);
        assertThat(read.getResourceTypes(), equalTo(all.getResourceTypes()));
    }

    private static ConfigDefinition definition() {
        // what importSchemaFromXsd registers
        return XsdHelper.buildConfigDefinition("resource-types", "resource-types.xsd", "resource-types", "/rest/cm", true);
    }

    private static List<Path> shippedFiles() throws IOException {
        final List<Path> files;
        try (Stream<Path> stream = Files.list(ASSEMBLY_RESOURCE_TYPES)) {
            files = stream.filter(p -> p.toString().endsWith(".xml")).sorted().collect(Collectors.toCollection(ArrayList::new));
        }
        files.add(XML_COLLECTOR_RESOURCE_TYPES);
        return files;
    }

    private static ResourceTypes fullResourceTypes() {
        final ResourceType database = new ResourceType();
        database.setName("pgDatabase");
        database.setLabel("PostgreSQL Database");
        database.setResourceLabel("${datname}");
        final PersistenceSelectorStrategy regex = new PersistenceSelectorStrategy("org.opennms.netmgt.collection.support.PersistRegexSelectorStrategy");
        regex.addParameter(new Parameter("match-expression", "#datname matches '^p.*'"));
        database.setPersistenceSelectorStrategy(regex);
        final StorageStrategy sibling = new StorageStrategy("org.opennms.netmgt.dao.support.SiblingColumnStorageStrategy");
        sibling.addParameter(new Parameter("sibling-column-name", "datname"));
        sibling.addParameter(new Parameter("replace-all", "s/,/_/"));
        database.setStorageStrategy(sibling);

        final ResourceType tableSpace = new ResourceType();
        tableSpace.setName("pgTableSpace");
        tableSpace.setLabel("PostgreSQL Tablespace");
        tableSpace.setPersistenceSelectorStrategy(new PersistenceSelectorStrategy("org.opennms.netmgt.collection.support.PersistAllSelectorStrategy"));
        tableSpace.setStorageStrategy(new StorageStrategy("org.opennms.netmgt.collection.support.IndexStorageStrategy"));

        final ResourceTypes types = new ResourceTypes();
        types.getResourceTypes().add(database);
        types.getResourceTypes().add(tableSpace);
        return types;
    }
}
