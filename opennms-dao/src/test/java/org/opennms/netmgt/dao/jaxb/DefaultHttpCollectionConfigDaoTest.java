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
package org.opennms.netmgt.dao.jaxb;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.opennms.features.config.dao.api.ConfigDefinition.DEFAULT_CONFIG_ID;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.exception.ValidationException;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.EventType;
import org.opennms.features.config.service.api.JsonAsString;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.netmgt.config.httpdatacollection.HttpDatacollectionConfig;
import org.springframework.test.util.ReflectionTestUtils;

public class DefaultHttpCollectionConfigDaoTest {
    private static final String CONFIG_NAME = "http-datacollection-config";

    private ConfigurationManagerService cm;
    private DefaultHttpCollectionConfigDao dao;

    @Before
    public void setUp() {
        cm = mock(ConfigurationManagerService.class);
        when(cm.getConfigIds(CONFIG_NAME)).thenReturn(Set.of(DEFAULT_CONFIG_ID));
        dao = new DefaultHttpCollectionConfigDao();
        ReflectionTestUtils.setField(dao, "configurationManagerService", cm);
    }

    @Test
    public void readsTheConfigFromConfigManagement() {
        when(cm.getJSONStrConfiguration(CONFIG_NAME, DEFAULT_CONFIG_ID)).thenReturn(Optional.of(json(config("/rrd/", "first"))));

        assertThat(dao.getConfigName(), equalTo(CONFIG_NAME));
        assertThat(dao.getConfig(), equalTo(config("/rrd/", "first")));
        // cached after the first read
        assertThat(dao.getConfig(), sameInstance(dao.getConfig()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void configManagementUpdatesRefreshTheCachedConfig() {
        when(cm.getJSONStrConfiguration(CONFIG_NAME, DEFAULT_CONFIG_ID)).thenReturn(Optional.of(json(config("/rrd/", "first"))));
        dao.postConstruct();
        final HttpDatacollectionConfig before = dao.getConfig();

        final ArgumentCaptor<Consumer<ConfigUpdateInfo>> callback = ArgumentCaptor.forClass(Consumer.class);
        verify(cm).registerEventHandler(eq(EventType.UPDATE), eq(new ConfigUpdateInfo(CONFIG_NAME, DEFAULT_CONFIG_ID)), callback.capture());

        when(cm.getJSONStrConfiguration(CONFIG_NAME, DEFAULT_CONFIG_ID)).thenReturn(Optional.of(json(config("/other/", "second"))));
        callback.getValue().accept(new ConfigUpdateInfo(CONFIG_NAME, DEFAULT_CONFIG_ID));

        assertThat(dao.getConfig(), equalTo(config("/other/", "second")));
        assertThat("existing references see the update", before, equalTo(config("/other/", "second")));
    }

    @Test
    public void replaceConfigReplacesTheStoredConfig() {
        dao.replaceConfig(config("/rrd/", "first"));

        final ArgumentCaptor<JsonAsString> stored = ArgumentCaptor.forClass(JsonAsString.class);
        verify(cm).updateConfiguration(eq(CONFIG_NAME), eq(DEFAULT_CONFIG_ID), stored.capture(), eq(true));
        assertThat(ConfigConvertUtil.jsonToObject(stored.getValue().toString(), HttpDatacollectionConfig.class), equalTo(config("/rrd/", "first")));
    }

    @Test
    public void updateConfigMergesIntoTheStoredConfig() {
        dao.updateConfig(config("/rrd/", "first"));

        verify(cm).updateConfiguration(eq(CONFIG_NAME), eq(DEFAULT_CONFIG_ID), any(JsonAsString.class), eq(false));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void validationRejectsUnsupportedAttributeTypes() {
        dao.postConstruct();
        final ArgumentCaptor<Consumer<ConfigUpdateInfo>> callback = ArgumentCaptor.forClass(Consumer.class);
        verify(cm).registerEventHandler(eq(EventType.VALIDATE), eq(new ConfigUpdateInfo(CONFIG_NAME)), callback.capture());

        // a valid config passes
        callback.getValue().accept(new ConfigUpdateInfo(CONFIG_NAME, DEFAULT_CONFIG_ID, new JSONObject(json(config("/rrd/", "first")))));

        // the schema allows timeticks, but the collector doesn't support it
        final JSONObject invalid = new JSONObject(json(config("/rrd/", "first")).replace("\"type\":\"gauge\"", "\"type\":\"timeticks\""));
        final ValidationException e = assertThrows(ValidationException.class,
                () -> callback.getValue().accept(new ConfigUpdateInfo(CONFIG_NAME, DEFAULT_CONFIG_ID, invalid)));
        assertThat(e.getMessage(), containsString("Unsupported attribute type 'timeticks'"));
    }

    private static String json(final HttpDatacollectionConfig config) {
        return ConfigConvertUtil.objectToJson(config);
    }

    private static HttpDatacollectionConfig config(final String rrdRepository, final String collectionName) {
        return JaxbUtils.unmarshal(HttpDatacollectionConfig.class,
                "<http-datacollection-config xmlns=\"http://xmlns.opennms.org/xsd/config/http-datacollection\" rrdRepository=\"" + rrdRepository + "\">"
                + "<http-collection name=\"" + collectionName + "\">"
                + "<rrd step=\"300\"><rra>RRA:AVERAGE:0.5:1:2016</rra></rrd>"
                + "<uris><uri name=\"u\"><url path=\"/\"/><attributes><attrib alias=\"a\" match-group=\"1\" type=\"gauge\"/></attributes></uri></uris>"
                + "</http-collection>"
                + "</http-datacollection-config>");
    }
}
