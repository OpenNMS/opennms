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
package org.opennms.netmgt.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.core.xml.JacksonUtils;
import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.exception.ValidationException;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.EventType;
import org.opennms.features.config.service.api.JsonAsString;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.dao.common.api.ConfigDaoConstants;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.ResourceTypes;
import org.opennms.netmgt.config.datacollection.StorageStrategy;

public class ResourceTypesDaoCmTest {

    private static final Date DC_LAST_UPDATE = new Date(1000);

    private final Map<String, JSONObject> configs = new HashMap<>();
    private final Map<EventType, Consumer<ConfigUpdateInfo>> handlers = new EnumMap<>(EventType.class);
    private ConfigurationManagerService cms;
    private JsonStore jsonStore;
    private ResourceTypesDaoCm dao;

    @Before
    public void setUp() {
        final DataCollectionConfigDao dcDao = mock(DataCollectionConfigDao.class);
        when(dcDao.getConfiguredResourceTypes()).thenReturn(Map.of(
                "fromDc", type("fromDc", "Data Collection"),
                "overridden", type("overridden", "Data Collection")));
        when(dcDao.getLastUpdate()).thenReturn(DC_LAST_UPDATE);
        DataCollectionConfigFactory.setInstance(dcDao);

        cms = mock(ConfigurationManagerService.class);
        when(cms.getConfigData(ResourceTypesDaoCm.CONFIG_NAME)).thenAnswer(i -> {
            final ConfigData<JSONObject> data = new ConfigData<>();
            data.setConfigs(new HashMap<>(configs));
            return Optional.of(data);
        });
        doAnswer(i -> {
            final ConfigUpdateInfo info = i.getArgument(1);
            assertEquals(ResourceTypesDaoCm.CONFIG_NAME, info.getConfigName());
            assertEquals(ConfigUpdateInfo.WILDCARD_ID, info.getConfigId());
            handlers.put(i.getArgument(0), i.getArgument(2));
            return null;
        }).when(cms).registerEventHandler(any(EventType.class), any(ConfigUpdateInfo.class), any());
        jsonStore = mock(JsonStore.class);

        dao = new ResourceTypesDaoCm();
    }

    @After
    public void tearDown() {
        DataCollectionConfigFactory.setInstance(null);
    }

    @Test
    public void unboundKnowsDataCollectionTypesOnly() {
        assertEquals(List.of("fromDc", "overridden"), sortedNames(dao.getResourceTypes()));
        assertEquals(DC_LAST_UPDATE, dao.getLastUpdate());
    }

    @Test
    public void mergesConfigurationsSortedByIdLaterWins() {
        configs.put("b-file", json(type("shared", "from b"), type("onlyB", "B")));
        configs.put("a-file", json(type("shared", "from a"), type("overridden", "CM")));

        dao.bind(cms, jsonStore);

        final Map<String, ResourceType> types = dao.getResourceTypes();
        assertEquals(List.of("fromDc", "onlyB", "overridden", "shared"), sortedNames(types));
        assertEquals("from b", types.get("shared").getLabel());
        // configuration manager types override the ones of the data collection configuration
        assertEquals("CM", dao.getResourceTypeByName("overridden").getLabel());
        assertTrue(dao.getLastUpdate().after(DC_LAST_UPDATE));
    }

    @Test
    public void registersForAllConfigurationsAndReloadsOnChange() {
        configs.put("a-file", json(type("first", "First")));
        dao.bind(cms, jsonStore);
        assertTrue(handlers.keySet().containsAll(List.of(EventType.VALIDATE, EventType.CREATE, EventType.UPDATE, EventType.DELETE)));

        final Date before = dao.getLastUpdate();
        configs.put("b-file", json(type("second", "Second")));
        handlers.get(EventType.CREATE).accept(new ConfigUpdateInfo(ResourceTypesDaoCm.CONFIG_NAME, "b-file"));
        assertEquals("Second", dao.getResourceTypeByName("second").getLabel());
        final Date afterCreate = dao.getLastUpdate();
        assertTrue("last update increases strictly", afterCreate.after(before));

        configs.remove("a-file");
        handlers.get(EventType.DELETE).accept(new ConfigUpdateInfo(ResourceTypesDaoCm.CONFIG_NAME, "a-file"));
        assertNull(dao.getResourceTypeByName("first"));
        assertTrue("last update increases strictly", dao.getLastUpdate().after(afterCreate));
    }

    @Test
    public void bindingTwiceRegistersOnce() {
        dao.bind(cms, jsonStore);
        dao.bind(cms, jsonStore);
        verify(cms, times(1)).registerEventHandler(eq(EventType.UPDATE), any(ConfigUpdateInfo.class), any());
    }

    @Test
    public void validationRejectsUnreadableConfigurations() {
        dao.bind(cms, jsonStore);
        final Consumer<ConfigUpdateInfo> validate = handlers.get(EventType.VALIDATE);

        validate.accept(new ConfigUpdateInfo(ResourceTypesDaoCm.CONFIG_NAME, "ok", json(type("t", "T"))));
        assertThrows(ValidationException.class,
                () -> validate.accept(new ConfigUpdateInfo(ResourceTypesDaoCm.CONFIG_NAME, "bad", parse("{\"resourceType\":{\"name\":[1]}}"))));
        assertThrows(ValidationException.class,
                () -> validate.accept(new ConfigUpdateInfo(ResourceTypesDaoCm.CONFIG_NAME, "empty")));
    }

    @Test
    public void ignoresUnreadableConfiguration() {
        configs.put("a-bad", parse("{\"resourceType\":{\"name\":[1]}}"));
        configs.put("b-good", json(type("good", "Good")));

        dao.bind(cms, jsonStore);

        assertEquals("Good", dao.getResourceTypeByName("good").getLabel());
    }

    @Test
    public void publishesStoredTypesToJsonStore() throws Exception {
        configs.put("a-file", json(type("cmType", "CM")));

        dao.bind(cms, jsonStore);

        final ArgumentCaptor<String> published = ArgumentCaptor.forClass(String.class);
        verify(jsonStore).put(eq(AbstractResourceTypesDao.JSON_STORE_KEY), published.capture(), eq(ConfigDaoConstants.JSON_KEY_STORE_CONTEXT));
        // Sentinel reads it with Jackson 1; the data collection types are not published, Sentinel has its own
        final ResourceTypes read = JacksonUtils.createDefaultObjectMapper().readValue(published.getValue(), ResourceTypes.class);
        assertEquals(List.of(type("cmType", "CM")), read.getResourceTypes());
    }

    @Test
    public void publishesEmptyListSoSentinelSeesTheKey() {
        dao.bind(cms, jsonStore);
        verify(jsonStore).put(eq(AbstractResourceTypesDao.JSON_STORE_KEY), anyString(), eq(ConfigDaoConstants.JSON_KEY_STORE_CONTEXT));
    }

    @Test
    public void jsonStoreFailureDoesNotFailReload() {
        configs.put("a-file", json(type("cmType", "CM")));
        when(jsonStore.put(anyString(), anyString(), anyString())).thenThrow(new RuntimeException("store down"));

        dao.bind(cms, jsonStore);

        assertEquals("CM", dao.getResourceTypeByName("cmType").getLabel());
    }

    @Test
    public void readsConfigurationsSortedByIdAndSkipsUnreadable() {
        configs.put("b-file", json(type("onlyB", "B")));
        configs.put("a-file", json(type("onlyA", "A")));
        configs.put("c-bad", parse("{\"resourceType\":{\"name\":[1]}}"));
        dao.bind(cms, jsonStore);

        assertEquals(List.of("a-file", "b-file"), List.copyOf(dao.getConfigurations().keySet()));
        assertEquals(List.of(type("onlyB", "B")), dao.getConfiguration("b-file").orElseThrow().getResourceTypes());
        assertFalse(dao.getConfiguration("c-bad").isPresent());
        assertFalse(dao.getConfiguration("missing").isPresent());
    }

    @Test
    public void writesThroughTheConfigurationManager() {
        dao.bind(cms, jsonStore);
        final ResourceTypes types = new ResourceTypes();
        types.setResourceTypes(List.of(type("t", "T")));

        dao.createConfiguration("new", types);
        final ArgumentCaptor<JsonAsString> created = ArgumentCaptor.forClass(JsonAsString.class);
        verify(cms).registerConfiguration(eq(ResourceTypesDaoCm.CONFIG_NAME), eq("new"), created.capture());
        assertEquals(types, ConfigConvertUtil.jsonToObject(created.getValue().toString(), ResourceTypes.class));

        dao.updateConfiguration("new", new ResourceTypes());
        // replaced as a whole, or removing the last type would be merged away
        verify(cms).updateConfiguration(eq(ResourceTypesDaoCm.CONFIG_NAME), eq("new"), any(JsonAsString.class), eq(true));

        dao.deleteConfiguration("new");
        verify(cms).unregisterConfiguration(ResourceTypesDaoCm.CONFIG_NAME, "new");
    }

    @Test
    public void writingUnboundFails() {
        assertThrows(IllegalStateException.class, () -> dao.createConfiguration("x", new ResourceTypes()));
        assertThrows(IllegalStateException.class, () -> dao.getConfigurations());
    }

    @Test
    public void namesTheTypesOfOtherSources() {
        dao.bind(cms, jsonStore);
        assertEquals(List.of("fromDc", "overridden"), List.copyOf(dao.getDataCollectionResourceTypeNames()));
        assertTrue(dao.getExtensionResourceTypeNames().isEmpty());
    }

    private static ResourceType type(final String name, final String label) {
        final ResourceType type = new ResourceType();
        type.setName(name);
        type.setLabel(label);
        type.setPersistenceSelectorStrategy(new PersistenceSelectorStrategy("org.opennms.netmgt.collection.support.PersistAllSelectorStrategy"));
        type.setStorageStrategy(new StorageStrategy("org.opennms.netmgt.collection.support.IndexStorageStrategy"));
        return type;
    }

    private static JSONObject json(final ResourceType... types) {
        final ResourceTypes resourceTypes = new ResourceTypes();
        resourceTypes.setResourceTypes(List.of(types));
        return parse(ConfigConvertUtil.objectToJson(resourceTypes));
    }

    private static JSONObject parse(final String json) {
        try {
            return new JSONObject(json);
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static List<String> sortedNames(final Map<String, ResourceType> types) {
        return types.keySet().stream().sorted().collect(Collectors.toList());
    }
}
