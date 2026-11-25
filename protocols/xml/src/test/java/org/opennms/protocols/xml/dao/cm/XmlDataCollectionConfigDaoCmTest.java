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
package org.opennms.protocols.xml.dao.cm;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opennms.protocols.xml.dao.cm.XmlDataCollectionConfigDaoCm.CONFIG_NAME;
import static org.opennms.protocols.xml.dao.cm.XmlDataCollectionConfigDaoCm.GROUPS_CONFIG_NAME;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.EventType;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.features.config.service.util.OpenAPIConfigHelper;
import org.opennms.protocols.xml.config.XmlDataCollection;
import org.opennms.protocols.xml.config.XmlDataCollectionConfig;
import org.opennms.protocols.xml.config.XmlGroup;
import org.opennms.protocols.xml.config.XmlGroups;
import org.opennms.protocols.xml.dao.jaxb.XmlDataCollectionConfigDaoJaxb;
import org.springframework.core.io.FileSystemResource;

public class XmlDataCollectionConfigDaoCmTest {

    private static final String ETC = "src/test/resources/etc";

    /** configName/configId -> json */
    private final Map<String, String> store = new ConcurrentHashMap<>();
    private final List<Registration> handlers = new ArrayList<>();
    private final AtomicInteger reads = new AtomicInteger();
    private final Map<String, ConfigDefinition> definitions = new ConcurrentHashMap<>();

    private ConfigurationManagerService cms;
    private XmlDataCollectionConfigDaoCm dao;
    private String opennmsHome;

    private static final class Registration {
        final EventType type;
        final ConfigUpdateInfo info;
        final Consumer<ConfigUpdateInfo> consumer;

        Registration(EventType type, ConfigUpdateInfo info, Consumer<ConfigUpdateInfo> consumer) {
            this.type = type;
            this.info = info;
            this.consumer = consumer;
        }
    }

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws IOException {
        opennmsHome = System.getProperty("opennms.home");
        cms = mock(ConfigurationManagerService.class);
        when(cms.getJSONConfiguration(anyString(), anyString())).thenAnswer(inv -> {
            reads.incrementAndGet();
            final String configName = inv.getArgument(0);
            // the configuration manager fills in missing optional children when reading, like this
            return Optional.ofNullable(store.get(configName + "/" + inv.getArgument(1)))
                    .map(JSONObject::new)
                    .map(json -> {
                        OpenAPIConfigHelper.fillWithDefaultValue(definitions.get(configName).getSchema(),
                                (String) definitions.get(configName).getMetaValue(ConfigDefinition.TOP_LEVEL_ELEMENT_NAME_TAG), json);
                        return json;
                    });
        });
        doAnswer(inv -> {
            handlers.add(new Registration(inv.getArgument(0), inv.getArgument(1), inv.getArgument(2)));
            return null;
        }).when(cms).registerEventHandler(any(EventType.class), any(ConfigUpdateInfo.class), any(Consumer.class));

        importXml(CONFIG_NAME, ConfigDefinition.DEFAULT_CONFIG_ID, "xml-datacollection-config", ETC + "/xml-datacollection-config.xml");
        importXml(GROUPS_CONFIG_NAME, "3gpp.core", "xml-groups", ETC + "/xml-datacollection/3gpp.core.xml");
        importXml(GROUPS_CONFIG_NAME, "3gpp.platform", "xml-groups", ETC + "/xml-datacollection/3gpp.platform.xml");

        dao = new XmlDataCollectionConfigDaoCm(cms);
    }

    @After
    public void tearDown() {
        if (opennmsHome == null) {
            System.clearProperty("opennms.home");
        } else {
            System.setProperty("opennms.home", opennmsHome);
        }
    }

    /** Stores the file the way the upgrade's importConfig / importConfigDirectory do. */
    private void importXml(String configName, String configId, String rootElement, String file) throws IOException {
        final ConfigDefinition definition = XsdHelper.buildConfigDefinition(configName, "xml-datacollection-config.xsd",
                rootElement, ConfigurationManagerService.BASE_PATH, GROUPS_CONFIG_NAME.equals(configName));
        definitions.put(configName, definition);
        store.put(configName + "/" + configId, XsdHelper.getConverter(definition)
                .xmlToJson(Files.readString(Paths.get(file), StandardCharsets.UTF_8)));
    }

    private void fire(EventType type, String configName, String configId) {
        final ConfigUpdateInfo actual = new ConfigUpdateInfo(configName, configId);
        handlers.stream()
                .filter(r -> r.type == type)
                .filter(r -> r.info.getConfigName().equals(configName))
                .filter(r -> r.info.getConfigId().equals(configId) || r.info.getConfigId().equals(ConfigUpdateInfo.WILDCARD_ID))
                .forEach(r -> r.consumer.accept(actual));
    }

    private static List<String> groupNames(XmlDataCollection collection) {
        return collection.getXmlSources().get(0).getXmlGroups().stream().map(XmlGroup::getName).collect(Collectors.toList());
    }

    @Test
    public void resolvesImportedGroupsLikeTheFileBasedDao() throws Exception {
        System.setProperty("opennms.home", "src/test/resources");
        final XmlDataCollectionConfigDaoJaxb fileDao = new XmlDataCollectionConfigDaoJaxb();
        fileDao.setConfigResource(new FileSystemResource(ETC + "/xml-datacollection-config.xml"));
        fileDao.afterPropertiesSet();

        final XmlDataCollectionConfig config = dao.getConfig();
        assertThat(config.getXmlDataCollections().get(0).getXmlSources().get(0).getXmlGroups().size(), equalTo(2));
        assertThat(config.getXmlDataCollections().get(1).getXmlSources().get(0).getXmlGroups().size(), equalTo(5));
        assertThat(JaxbUtils.marshal(config), equalTo(JaxbUtils.marshal(fileDao.getConfig())));
    }

    @Test
    public void findsCollectionsByNameAndIndex() {
        assertThat(dao.getDataCollectionByName("3GPP-15min").getName(), equalTo("3GPP-15min"));
        assertThat(dao.getDataCollectionByIndex(0).getName(), equalTo("3GPP-5min"));
        assertThat(dao.getDataCollectionByName("unknown"), nullValue());
        assertThat(dao.getConfig().getRrdRepository(), equalTo("target/snmp/"));
    }

    @Test
    public void importGroupsMayNameTheConfigIdDirectly() {
        final XmlDataCollectionConfig config = ConfigConvertUtil.jsonToObject(store.get(CONFIG_NAME + "/default"), XmlDataCollectionConfig.class);
        config.getXmlDataCollections().get(0).getXmlSources().get(0).getImportGroupsList().set(0, "3gpp.platform");
        store.put(CONFIG_NAME + "/default", ConfigConvertUtil.objectToJson(config));

        assertThat(groupNames(dao.getDataCollectionByName("3GPP-5min")).size(), equalTo(3));
    }

    @Test
    public void missingGroupsAreSkipped() {
        store.remove(GROUPS_CONFIG_NAME + "/3gpp.core");

        assertThat(dao.getDataCollectionByName("3GPP-5min").getXmlSources().get(0).getXmlGroups().size(), equalTo(0));
        assertThat(dao.getDataCollectionByName("3GPP-15min").getXmlSources().get(0).getXmlGroups().size(), equalTo(3));
    }

    @Test
    public void missingConfigurationMeansNoCollections() {
        store.clear();

        assertThat(dao.getConfig().getXmlDataCollections().size(), equalTo(0));
        assertThat(dao.getDataCollectionByName("3GPP-5min"), nullValue());
    }

    @Test
    public void configurationIsCachedUntilItChanges() {
        final XmlDataCollectionConfig first = dao.getConfig();
        final int readsAfterLoad = reads.get();

        assertThat(dao.getConfig(), sameInstance(first));
        assertThat(dao.getDataCollectionByName("3GPP-5min"), sameInstance(first.getDataCollectionByName("3GPP-5min")));
        assertThat(reads.get(), equalTo(readsAfterLoad));

        fire(EventType.UPDATE, CONFIG_NAME, ConfigDefinition.DEFAULT_CONFIG_ID);
        assertThat(dao.getConfig(), not(sameInstance(first)));
    }

    @Test
    public void groupChangesTakeEffectWithoutRestart() {
        assertThat(groupNames(dao.getDataCollectionByName("3GPP-5min")).size(), equalTo(2));

        final XmlGroups core = ConfigConvertUtil.jsonToObject(store.get(GROUPS_CONFIG_NAME + "/3gpp.core"), XmlGroups.class);
        core.getXmlGroups().remove(0);
        store.put(GROUPS_CONFIG_NAME + "/3gpp.core", ConfigConvertUtil.objectToJson(core));
        // no event yet: still the cached configuration
        assertThat(groupNames(dao.getDataCollectionByName("3GPP-5min")).size(), equalTo(2));

        fire(EventType.UPDATE, GROUPS_CONFIG_NAME, "3gpp.core");
        assertThat(groupNames(dao.getDataCollectionByName("3GPP-5min")).size(), equalTo(1));

        store.remove(GROUPS_CONFIG_NAME + "/3gpp.core");
        fire(EventType.DELETE, GROUPS_CONFIG_NAME, "3gpp.core");
        assertThat(groupNames(dao.getDataCollectionByName("3GPP-5min")).size(), equalTo(0));

        store.put(GROUPS_CONFIG_NAME + "/3gpp.core", ConfigConvertUtil.objectToJson(core));
        fire(EventType.CREATE, GROUPS_CONFIG_NAME, "3gpp.core");
        assertThat(groupNames(dao.getDataCollectionByName("3GPP-5min")).size(), equalTo(1));
    }

    @Test
    public void sourcesWithoutRequestStayWithoutRequest() {
        // the stored source has no request, the configuration manager hands out an empty one
        assertThat(cms.getJSONConfiguration(CONFIG_NAME, "default").get().toString().contains("\"request\""), equalTo(true));

        assertThat(dao.getDataCollectionByName("3GPP-5min").getXmlSources().get(0).getRequest(), nullValue());
    }

    @Test
    public void emptyContainersAreRemoved() {
        final JSONObject json = new JSONObject("{\"a\":{\"b\":[],\"c\":{}},\"d\":[{\"e\":{}}],\"f\":\"x\",\"g\":{\"h\":1}}");
        final JSONObject expected = new JSONObject("{\"d\":[{}],\"f\":\"x\",\"g\":{\"h\":1}}");
        assertThat(json + " is " + expected, XmlDataCollectionConfigDaoCm.withoutEmptyContainers(json).similar(expected), equalTo(true));
    }

    @Test
    public void groupsConfigIdOfImportGroups() {
        assertThat(XmlDataCollectionConfigDaoCm.toGroupsConfigId("xml-datacollection/3gpp.core.xml"), equalTo("3gpp.core"));
        assertThat(XmlDataCollectionConfigDaoCm.toGroupsConfigId(" xml-datacollection/foo.XML "), equalTo("foo"));
        assertThat(XmlDataCollectionConfigDaoCm.toGroupsConfigId("xml-datacollection\\foo.xml"), equalTo("foo"));
        assertThat(XmlDataCollectionConfigDaoCm.toGroupsConfigId("foo"), equalTo("foo"));
        assertThat(XmlDataCollectionConfigDaoCm.toGroupsConfigId("3gpp.full"), equalTo("3gpp.full"));
    }
}
