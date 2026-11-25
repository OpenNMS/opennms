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
package org.opennms.web.rest.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.config.exception.ConfigAlreadyExistsException;
import org.opennms.features.config.exception.ConfigNotFoundException;
import org.opennms.features.config.exception.ValidationException;
import org.opennms.netmgt.config.ResourceTypesDaoCm;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.ResourceTypes;
import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.v2.model.ParameterDto;
import org.opennms.web.rest.v2.model.ResourceTypeDto;
import org.opennms.web.rest.v2.model.ResourceTypeStrategyDto;
import org.opennms.web.rest.v2.model.ResourceTypesConfigDto;
import org.opennms.web.rest.v2.model.ResourceTypesConfigSummaryDto;
import org.opennms.web.rest.v2.model.ResourceTypesMetadataDto;

public class ResourceTypesConfigRestServiceTest {

    private static final String PERSIST_ALL = "org.opennms.netmgt.collection.support.PersistAllSelectorStrategy";
    private static final String INDEX_STORAGE = "org.opennms.netmgt.collection.support.IndexStorageStrategy";

    private ResourceTypesConfigRestService restService;
    private FakeResourceTypesDao dao;

    @Before
    public void setUp() throws Exception {
        dao = new FakeResourceTypesDao();
        dao.configs.put("postgresql-resource", types(type("pgTableSpace"), type("pgDatabase")));
        dao.configs.put("jmx-resource", types(type("kafkaLag")));

        restService = new ResourceTypesConfigRestService();
        final Field field = ResourceTypesConfigRestService.class.getDeclaredField("resourceTypesDao");
        field.setAccessible(true);
        field.set(restService, dao);
    }

    // ------------------------------------------------------------------- reads

    @Test
    @SuppressWarnings("unchecked")
    public void listsConfigurationsSortedById() {
        try (Response response = restService.getConfigs(adminContext())) {
            assertEquals(200, response.getStatus());
            final List<ResourceTypesConfigSummaryDto> configs = (List<ResourceTypesConfigSummaryDto>) response.getEntity();
            assertEquals(List.of("jmx-resource", "postgresql-resource"), configs.stream().map(ResourceTypesConfigSummaryDto::getId).toList());
            assertEquals(List.of("pgTableSpace", "pgDatabase"), configs.get(1).getResourceTypeNames());
        }
    }

    @Test
    public void getsAConfigurationWithItsVersion() {
        try (Response response = restService.getConfig("postgresql-resource", adminContext())) {
            assertEquals(200, response.getStatus());
            final ResourceTypesConfigDto dto = (ResourceTypesConfigDto) response.getEntity();
            assertEquals("postgresql-resource", dto.getId());
            assertEquals(2, dto.getResourceTypes().size());
            assertEquals(PERSIST_ALL, dto.getResourceTypes().get(0).getPersistenceSelectorStrategy().getClassName());
            assertEquals("${index}", dto.getResourceTypes().get(0).getResourceLabel());
            assertNotNull(dto.getVersion());
            assertEquals(dto.getVersion(), response.getEntityTag().getValue());
        }
    }

    @Test
    public void unknownConfigurationIsNotFound() {
        try (Response response = restService.getConfig("nope", adminContext())) {
            assertEquals(404, response.getStatus());
        }
    }

    @Test
    public void metadataNamesStrategiesAndOtherSources() {
        try (Response response = restService.getMetadata(adminContext())) {
            final ResourceTypesMetadataDto dto = (ResourceTypesMetadataDto) response.getEntity();
            assertTrue(dto.getStorageStrategies().contains(INDEX_STORAGE));
            assertTrue(dto.getPersistenceSelectorStrategies().contains(PERSIST_ALL));
            assertEquals(List.of("hrStorageIndex"), dto.getDataCollectionTypeNames());
            assertEquals(List.of("fromExtension"), dto.getExtensionTypeNames());
        }
    }

    // ------------------------------------------------------------------ create

    @Test
    public void createsAConfiguration() {
        try (Response response = restService.createConfig(config("my-types", typeDto("myType")), adminContext())) {
            assertEquals(201, response.getStatus());
            assertTrue(response.getLocation().toString().endsWith("resource-types/configs/my-types"));
        }
        assertEquals("myType", dao.configs.get("my-types").getResourceTypes().get(0).getName());
    }

    @Test
    public void createRejectsATakenId() {
        try (Response response = restService.createConfig(config("jmx-resource", typeDto("other")), adminContext())) {
            assertEquals(409, response.getStatus());
        }
    }

    @Test
    public void createRejectsANameDefinedByAnotherConfiguration() {
        try (Response response = restService.createConfig(config("my-types", typeDto("kafkaLag")), adminContext())) {
            assertEquals(409, response.getStatus());
            assertEquals("Resource type 'kafkaLag' is already defined in configuration 'jmx-resource'.", response.getEntity());
        }
        assertFalse(dao.configs.containsKey("my-types"));
    }

    @Test
    public void createRejectsInvalidInputBeforeStoring() {
        final ResourceTypesConfigDto config = config("my-types", typeDto("myType"));
        config.getResourceTypes().get(0).setStorageStrategy(null);
        try (Response response = restService.createConfig(config, adminContext())) {
            assertEquals(400, response.getStatus());
            assertTrue(((String) response.getEntity()).contains("storage strategy"));
        }
        assertFalse(dao.configs.containsKey("my-types"));
    }

    @Test
    public void validationFailureOfTheConfigurationManagerIsABadRequest() {
        dao.validationFailure = "label is required";
        try (Response response = restService.createConfig(config("my-types", typeDto("myType")), adminContext())) {
            assertEquals(400, response.getStatus());
            assertEquals("label is required", response.getEntity());
        }
    }

    // ------------------------------------------------------------------ update

    @Test
    public void updatesAConfigurationInFull() {
        final String version = versionOf("postgresql-resource");
        try (Response response = restService.updateConfig("postgresql-resource",
                config("postgresql-resource", typeDto("pgDatabase")), quoted(version), adminContext())) {
            assertEquals(204, response.getStatus());
        }
        assertEquals(List.of("pgDatabase"), names(dao.configs.get("postgresql-resource")));
    }

    @Test
    public void updateStoresAnEmptyConfiguration() {
        try (Response response = restService.updateConfig("jmx-resource", config("jmx-resource"), null, adminContext())) {
            assertEquals(204, response.getStatus());
        }
        assertTrue(dao.configs.get("jmx-resource").getResourceTypes().isEmpty());
    }

    @Test
    public void updateWithAStaleVersionFails() {
        try (Response response = restService.updateConfig("postgresql-resource",
                config("postgresql-resource", typeDto("pgDatabase")), "\"stale\"", adminContext())) {
            assertEquals(412, response.getStatus());
        }
        assertEquals(2, dao.configs.get("postgresql-resource").getResourceTypes().size());
    }

    @Test
    public void updateOfAnUnknownConfigurationIsNotFound() {
        try (Response response = restService.updateConfig("nope", config("nope"), null, adminContext())) {
            assertEquals(404, response.getStatus());
        }
    }

    @Test
    public void updateMayKeepItsOwnNamesButNotTakeOthers() {
        try (Response response = restService.updateConfig("postgresql-resource",
                config("postgresql-resource", typeDto("pgTableSpace"), typeDto("kafkaLag")), null, adminContext())) {
            assertEquals(409, response.getStatus());
        }
    }

    @Test
    public void updateWithAnotherIdRenames() {
        try (Response response = restService.updateConfig("jmx-resource", config("kafka-resource", typeDto("kafkaLag")),
                quoted(versionOf("jmx-resource")), adminContext())) {
            assertEquals(204, response.getStatus());
        }
        assertFalse(dao.configs.containsKey("jmx-resource"));
        assertEquals(List.of("kafkaLag"), names(dao.configs.get("kafka-resource")));
    }

    @Test
    public void renameToATakenIdFails() {
        try (Response response = restService.updateConfig("jmx-resource", config("postgresql-resource"), null, adminContext())) {
            assertEquals(409, response.getStatus());
        }
        assertTrue(dao.configs.containsKey("jmx-resource"));
    }

    @Test
    public void failedRenameKeepsTheOldConfigurationOnly() {
        dao.failDeleteOf = "jmx-resource";
        try (Response response = restService.updateConfig("jmx-resource", config("kafka-resource", typeDto("kafkaLag")), null, adminContext())) {
            assertEquals(500, response.getStatus());
        }
        assertTrue(dao.configs.containsKey("jmx-resource"));
        assertFalse(dao.configs.containsKey("kafka-resource"));
    }

    // ------------------------------------------------------------------ delete

    @Test
    public void deletesAConfiguration() {
        try (Response response = restService.deleteConfig("jmx-resource", quoted(versionOf("jmx-resource")), adminContext())) {
            assertEquals(204, response.getStatus());
        }
        assertFalse(dao.configs.containsKey("jmx-resource"));
    }

    @Test
    public void deleteWithAStaleVersionFails() {
        try (Response response = restService.deleteConfig("jmx-resource", "\"stale\"", adminContext())) {
            assertEquals(412, response.getStatus());
        }
        assertTrue(dao.configs.containsKey("jmx-resource"));
    }

    @Test
    public void deleteOfAnUnknownConfigurationIsNotFound() {
        try (Response response = restService.deleteConfig("nope", null, adminContext())) {
            assertEquals(404, response.getStatus());
        }
    }

    // ------------------------------------------------------- download / upload

    @Test
    public void downloadsXmlInTheFormerFileFormat() {
        try (Response response = restService.downloadConfig("jmx-resource", "xml", adminContext())) {
            assertEquals(200, response.getStatus());
            assertEquals("attachment; filename=\"jmx-resource.xml\"", response.getHeaderString("Content-Disposition"));
            final String xml = new String((byte[]) response.getEntity(), StandardCharsets.UTF_8);
            assertTrue(xml, xml.contains("<resource-types"));
            assertTrue(xml, xml.contains("name=\"kafkaLag\""));
        }
    }

    @Test
    public void downloadsJson() {
        try (Response response = restService.downloadConfig("jmx-resource", null, adminContext())) {
            assertEquals(200, response.getStatus());
            final String json = new String((byte[]) response.getEntity(), StandardCharsets.UTF_8);
            assertTrue(json, json.contains("\"kafkaLag\""));
        }
    }

    @Test
    public void downloadNeedsAdminAndAKnownFormat() {
        try (Response response = restService.downloadConfig("jmx-resource", "xml", userContext())) {
            assertEquals(403, response.getStatus());
        }
        try (Response response = restService.downloadConfig("jmx-resource", "yaml", adminContext())) {
            assertEquals(400, response.getStatus());
        }
    }

    @Test
    public void uploadOfAFormerFileCreatesAConfiguration() {
        final String xml = """
                <?xml version="1.0"?>
                <resource-types xmlns="http://xmlns.opennms.org/xsd/config/datacollection">
                  <resourceType name="uploaded" label="Uploaded" resourceLabel="${index}">
                    <persistenceSelectorStrategy class="org.opennms.netmgt.collection.support.PersistRegexSelectorStrategy">
                      <parameter key="match-expression" value="#name matches '.*'"/>
                    </persistenceSelectorStrategy>
                    <storageStrategy class="org.opennms.netmgt.collection.support.IndexStorageStrategy"/>
                  </resourceType>
                </resource-types>
                """;
        try (Response response = restService.uploadConfigXml("uploaded-resource", attachment(xml), adminContext())) {
            assertEquals(200, response.getStatus());
        }
        final ResourceType stored = dao.configs.get("uploaded-resource").getResourceTypes().get(0);
        assertEquals("uploaded", stored.getName());
        assertEquals("match-expression", stored.getPersistenceSelectorStrategy().getParameters().get(0).getKey());
    }

    @Test
    public void uploadOfJsonReplacesAnExistingConfigurationUnderThePathId() {
        final String json = """
                {"id": "ignored", "resourceTypes": [{"name": "kafkaLag2", "label": "Lag",
                  "persistenceSelectorStrategy": {"className": "%s"},
                  "storageStrategy": {"className": "%s"}}]}
                """.formatted(PERSIST_ALL, INDEX_STORAGE);
        try (Response response = restService.uploadConfig("jmx-resource", attachment(json), adminContext())) {
            assertEquals(200, response.getStatus());
        }
        assertFalse(dao.configs.containsKey("ignored"));
        assertEquals(List.of("kafkaLag2"), names(dao.configs.get("jmx-resource")));
    }

    @Test
    public void uploadRejectsBrokenFilesAndClashingNames() {
        try (Response response = restService.uploadConfigXml("x", attachment("<resource-types"), adminContext())) {
            assertEquals(400, response.getStatus());
        }
        final String clashing = """
                {"resourceTypes": [{"name": "pgDatabase", "label": "Clash",
                  "persistenceSelectorStrategy": {"className": "%s"},
                  "storageStrategy": {"className": "%s"}}]}
                """.formatted(PERSIST_ALL, INDEX_STORAGE);
        try (Response response = restService.uploadConfig("x", attachment(clashing), adminContext())) {
            assertEquals(409, response.getStatus());
        }
        try (Response response = restService.uploadConfig("bad/id", attachment(clashing), adminContext())) {
            assertEquals(400, response.getStatus());
        }
        try (Response response = restService.uploadConfig("x", attachment(clashing), userContext())) {
            assertEquals(403, response.getStatus());
        }
        assertFalse(dao.configs.containsKey("x"));
    }

    // ----------------------------------------------------------------- helpers

    private String versionOf(final String configId) {
        try (Response response = restService.getConfig(configId, adminContext())) {
            return ((ResourceTypesConfigDto) response.getEntity()).getVersion();
        }
    }

    private static String quoted(final String version) {
        return "\"" + version + "\"";
    }

    private static List<String> names(final ResourceTypes types) {
        return types.getResourceTypes().stream().map(ResourceType::getName).toList();
    }

    private static ResourceTypes types(final ResourceType... types) {
        final ResourceTypes resourceTypes = new ResourceTypes();
        resourceTypes.setResourceTypes(new ArrayList<>(List.of(types)));
        return resourceTypes;
    }

    private static ResourceType type(final String name) {
        final ResourceType type = new ResourceType();
        type.setName(name);
        type.setLabel(name + " label");
        type.setResourceLabel("${index}");
        type.setPersistenceSelectorStrategy(new PersistenceSelectorStrategy(PERSIST_ALL));
        type.setStorageStrategy(new StorageStrategy(INDEX_STORAGE));
        return type;
    }

    private static ResourceTypesConfigDto config(final String id, final ResourceTypeDto... types) {
        final ResourceTypesConfigDto dto = new ResourceTypesConfigDto();
        dto.setId(id);
        dto.setResourceTypes(new ArrayList<>(List.of(types)));
        return dto;
    }

    static ResourceTypeDto typeDto(final String name) {
        final ResourceTypeDto dto = new ResourceTypeDto();
        dto.setName(name);
        dto.setLabel(name + " label");
        dto.setPersistenceSelectorStrategy(strategy(PERSIST_ALL));
        dto.setStorageStrategy(strategy(INDEX_STORAGE));
        return dto;
    }

    static ResourceTypeStrategyDto strategy(final String className, final String... keyValues) {
        final ResourceTypeStrategyDto dto = new ResourceTypeStrategyDto();
        dto.setClassName(className);
        final List<ParameterDto> parameters = new ArrayList<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            final ParameterDto parameter = new ParameterDto();
            parameter.setKey(keyValues[i]);
            parameter.setValue(keyValues[i + 1]);
            parameters.add(parameter);
        }
        dto.setParameters(parameters);
        return dto;
    }

    private static Attachment attachment(final String body) {
        final Attachment attachment = mock(Attachment.class);
        final InputStream stream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        when(attachment.getObject(InputStream.class)).thenReturn(stream);
        return attachment;
    }

    private static SecurityContext adminContext() {
        final SecurityContext context = mock(SecurityContext.class);
        when(context.isUserInRole(Authentication.ROLE_ADMIN)).thenReturn(true);
        return context;
    }

    private static SecurityContext userContext() {
        final SecurityContext context = mock(SecurityContext.class);
        when(context.isUserInRole(Authentication.ROLE_ADMIN)).thenReturn(false);
        return context;
    }

    /**
     * Keeps the configurations in memory, as the configuration manager would, without its schema validation.
     */
    private static class FakeResourceTypesDao extends ResourceTypesDaoCm {
        final SortedMap<String, ResourceTypes> configs = new TreeMap<>();
        String validationFailure;
        String failDeleteOf;

        @Override
        public SortedMap<String, ResourceTypes> getConfigurations() {
            return new TreeMap<>(configs);
        }

        @Override
        public Optional<ResourceTypes> getConfiguration(final String configId) {
            return Optional.ofNullable(configs.get(configId));
        }

        @Override
        public void createConfiguration(final String configId, final ResourceTypes resourceTypes) {
            if (configs.containsKey(configId)) {
                throw new ConfigAlreadyExistsException(configId);
            }
            validate();
            configs.put(configId, resourceTypes);
        }

        @Override
        public void updateConfiguration(final String configId, final ResourceTypes resourceTypes) {
            if (!configs.containsKey(configId)) {
                throw new ConfigNotFoundException(configId);
            }
            validate();
            configs.put(configId, resourceTypes);
        }

        @Override
        public void deleteConfiguration(final String configId) {
            if (configId.equals(failDeleteOf)) {
                throw new IllegalStateException("database down");
            }
            if (configs.remove(configId) == null) {
                throw new ConfigNotFoundException(configId);
            }
        }

        @Override
        public Set<String> getDataCollectionResourceTypeNames() {
            return Set.of("hrStorageIndex");
        }

        @Override
        public Set<String> getExtensionResourceTypeNames() {
            return Set.of("fromExtension");
        }

        private void validate() {
            if (validationFailure != null) {
                throw new ValidationException(validationFailure);
            }
        }
    }
}
