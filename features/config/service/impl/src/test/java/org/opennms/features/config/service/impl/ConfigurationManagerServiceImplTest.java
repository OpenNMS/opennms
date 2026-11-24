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
package org.opennms.features.config.service.impl;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.api.ConfigStoreDao;
import org.opennms.features.config.exception.ValidationException;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigValidator;
import org.opennms.features.config.service.api.JsonAsString;

/**
 * The {@link ConfigValidator}s found with {@link java.util.ServiceLoader} run on every write, without anybody
 * registering them. {@link RejectingValidator} is listed in {@code src/test/resources/META-INF/services}.
 */
public class ConfigurationManagerServiceImplTest {

    static final String CONFIG_NAME = "validatedByServiceLoader";

    /** rejects every configuration that has the key 'bad' */
    public static class RejectingValidator implements ConfigValidator {
        @Override
        public String getConfigName() {
            return CONFIG_NAME;
        }

        @Override
        public void validate(final ConfigUpdateInfo info) {
            if (info.getConfigJson().has("bad")) {
                throw new ValidationException("bad " + info.getConfigId());
            }
        }
    }

    private ConfigStoreDao<JSONObject> store;
    private ConfigurationManagerServiceImpl cm;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        store = mock(ConfigStoreDao.class);
        when(store.getConfigDefinition(anyString())).thenReturn(Optional.of(new ConfigDefinition(CONFIG_NAME, true)));
        when(store.getConfigs(anyString())).thenReturn(Optional.empty());
        cm = new ConfigurationManagerServiceImpl(store);
    }

    @Test
    public void validatorRunsOnRegister() {
        assertThrows(ValidationException.class,
                () -> cm.registerConfiguration(CONFIG_NAME, "a", new JsonAsString("{\"bad\":1}")));
        verify(store, never()).addConfig(anyString(), anyString(), any());

        cm.registerConfiguration(CONFIG_NAME, "a", new JsonAsString("{\"good\":1}"));
        verify(store).addConfig(anyString(), anyString(), any());
    }

    @Test
    public void validatorRunsOnUpdate() {
        final ConfigData<JSONObject> data = new ConfigData<>();
        data.getConfigs().put("a", new JSONObject("{\"good\":1}"));
        when(store.getConfigs(CONFIG_NAME)).thenReturn(Optional.of(data));

        // a merge: the validator sees the whole configuration
        assertThrows(ValidationException.class,
                () -> cm.updateConfiguration(CONFIG_NAME, "a", new JsonAsString("{\"bad\":1}"), false));
        verify(store, never()).updateConfigs(anyString(), any());
    }

    @Test
    public void otherSchemasAreNotValidated() {
        cm.registerConfiguration("other", "a", new JsonAsString("{\"bad\":1}"));
        verify(store).addConfig(anyString(), anyString(), any());
    }
}
