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
package org.opennms.config.upgrade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opennms.features.config.dao.api.ConfigDefinition.DEFAULT_CONFIG_ID;

import java.util.Optional;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.TemporaryDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.TemporaryDatabaseExecutionListener;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.api.ConfigConverter;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.JsonAsString;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.netmgt.config.trapd.TrapdConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@TestExecutionListeners({TemporaryDatabaseExecutionListener.class})
@ContextConfiguration(locations = {
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-config-service.xml"})
@JUnitTemporaryDatabase
public class TrapdSnmpv3UserIdBackfillIT implements TemporaryDatabaseAware<TemporaryDatabase> {

    private static final String CONFIG_NAME = "trapd-config";

    @Autowired
    private ConfigurationManagerService cm;

    private DataSource dataSource;

    @Override
    public void setTemporaryDatabase(TemporaryDatabase database) {
        this.dataSource = database;
    }

    @Before
    public void setUp() throws Exception {
        final ConfigDefinition def = XsdHelper.buildConfigDefinition(
                CONFIG_NAME, "trapd-configuration-1.1.xsd", "trapd-configuration",
                ConfigurationManagerService.BASE_PATH);
        cm.registerConfigDefinition(CONFIG_NAME, def);

        final ConfigDefinition registered = cm.getRegisteredConfigDefinition(CONFIG_NAME).get();
        final ConfigConverter converter = XsdHelper.getConverter(registered);
        final String xml = "<trapd-configuration snmp-trap-port=\"162\" new-suspect-on-trap=\"false\">"
                + "<snmpv3-user security-name=\"opennms\"/>"
                + "</trapd-configuration>";
        cm.registerConfiguration(CONFIG_NAME, DEFAULT_CONFIG_ID, new JsonAsString(converter.xmlToJson(xml)));
    }

    @After
    public void tearDown() {
        if (cm.getRegisteredConfigDefinition(CONFIG_NAME).isPresent()) {
            cm.unregisterSchema(CONFIG_NAME);
        }
    }

    private TrapdConfiguration reload() {
        final Optional<String> json = cm.getJSONStrConfiguration(CONFIG_NAME, DEFAULT_CONFIG_ID);
        assertTrue(json.isPresent());
        return ConfigConvertUtil.jsonToObject(json.get(), TrapdConfiguration.class);
    }

    @Test
    public void assignsIdsToExistingUsersAndPersists() {
        assertTrue(new TrapdSnmpv3UserIdBackfill(cm).execute());

        final TrapdConfiguration cfg = reload();
        final String id = cfg.getSnmpv3User(0).getId();
        assertNotNull("id must be assigned", id);
        assertFalse("id must not be blank", id.trim().isEmpty());
    }

    @Test
    public void isIdempotent() {
        assertTrue(new TrapdSnmpv3UserIdBackfill(cm).execute());
        final String firstId = reload().getSnmpv3User(0).getId();

        assertFalse("second run must report no change", new TrapdSnmpv3UserIdBackfill(cm).execute());
        assertEquals("id must be stable across runs", firstId, reload().getSnmpv3User(0).getId());
    }

    @Test
    public void returnsFalseWhenNoConfigPresent() {
        // XsdHelper.buildConfigDefinition() registers this schema with allowMultiple=false,
        // so CM refuses to delete the sole config instance (JsonConfigStoreDaoImpl throws
        // "Deletion of the last config is not allowed"). Unregister the whole schema instead
        // to genuinely simulate "no trapd configuration present in CM".
        cm.unregisterSchema(CONFIG_NAME);
        assertFalse(new TrapdSnmpv3UserIdBackfill(cm).execute());
    }
}
