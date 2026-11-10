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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.features.config.exception.ConfigNotFoundException;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.JsonAsString;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.netmgt.config.jdbc.JdbcDataCollection;
import org.opennms.netmgt.config.jdbc.JdbcDataCollectionConfig;
import org.opennms.netmgt.dao.jaxb.JdbcDataCollectionConfigDaoCm;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * The DAO wired by the jdbc-collector's component-dao.xml against the configuration manager on a real database.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:mock-dao.xml",
        "classpath*:/META-INF/opennms/applicationContext-config-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class JdbcDataCollectionConfigCmDaoIT {
    private static final String CONFIG_NAME = JdbcDataCollectionConfigDaoCm.CONFIG_NAME;

    @Autowired
    private JdbcDataCollectionConfigDaoCm dao;

    @Autowired
    private ConfigurationManagerService cm;

    private ConfigDefinition definition;

    @Before
    public void setUp() throws IOException {
        definition = XsdHelper.buildConfigDefinition(CONFIG_NAME, "jdbc-datacollection.xsd",
                "jdbc-datacollection-config", ConfigurationManagerService.BASE_PATH, true);
        cm.registerConfigDefinition(CONFIG_NAME, definition);
        // registered out of order on purpose
        importShipped("postgresql", "jdbc-datacollection.d/postgresql.xml");
        importShipped(ConfigDefinition.DEFAULT_CONFIG_ID, "jdbc-datacollection-config.xml");
        importShipped("mysql", "jdbc-datacollection.d/mysql.xml");
    }

    @After
    public void tearDown() {
        cm.unregisterSchema(CONFIG_NAME);
    }

    @Test
    public void mergesAllConfigurationsDefaultFirst() {
        assertThat(dao.getConfigIds(), equalTo(Arrays.asList("default", "mysql", "postgresql")));
        assertThat(collectionNames(dao.getConfig()), equalTo(Arrays.asList("default", "MySQL-Global-Stats", "PostgreSQL")));
        assertThat("merged view is cached", dao.getConfig(), sameInstance(dao.getConfig()));

        assertThat(dao.getDataCollectionByIndex(2).getName(), equalTo("PostgreSQL"));
        assertThat(dao.getDataCollectionByName("PostgreSQL").getQueries().size(), equalTo(2));
        assertThat(dao.getDataCollectionByName("does-not-exist"), nullValue());

        final RrdRepository repository = dao.getConfig().buildRrdRepository("PostgreSQL");
        assertThat(repository.getStep(), equalTo(300));
        assertThat(repository.getRrdBaseDir(), equalTo(new File("${install.share.dir}/rrd/snmp/")));
    }

    @Test
    public void updateOfOneConfigurationIsVisible() {
        final JdbcDataCollectionConfig before = dao.getConfig();
        final JdbcDataCollectionConfig mysql = dao.getConfig("mysql");
        mysql.getJdbcDataCollections().get(0).setName("MySQL-Renamed");
        dao.updateConfig("mysql", ConfigConvertUtil.objectToJson(mysql), true);

        assertThat(dao.getConfig() == before, equalTo(false));
        assertThat(dao.getDataCollectionByName("MySQL-Global-Stats"), nullValue());
        assertThat(dao.getDataCollectionByName("MySQL-Renamed"), notNullValue());
    }

    @Test
    public void createdAndDeletedConfigurationsAreTracked() throws IOException {
        assertThat(dao.getDataCollectionByName("Oracle-JDBC"), nullValue());

        importShipped("oracle", "jdbc-datacollection.d/oracle.xml");
        assertThat(dao.getDataCollectionByName("Oracle-JDBC"), notNullValue());
        assertThat(dao.getConfigIds(), equalTo(Arrays.asList("default", "mysql", "oracle", "postgresql")));

        cm.unregisterConfiguration(CONFIG_NAME, "oracle");
        assertThat(dao.getDataCollectionByName("Oracle-JDBC"), nullValue());
    }

    @Test
    public void writesThroughTheDaoAreVisible() {
        final JdbcDataCollectionConfig copy = dao.copyOfConfig("postgresql");
        assertThat("a copy, not the shared instance", copy == dao.getConfig("postgresql"), equalTo(false));

        copy.getJdbcDataCollections().get(0).setName("PostgreSQL-Copy");
        dao.createConfig("copy", copy);
        assertThat(dao.getConfigIds(), equalTo(Arrays.asList("default", "copy", "mysql", "postgresql")));
        assertThat(dao.getDataCollectionByName("PostgreSQL-Copy"), notNullValue());
        assertThat("the original is untouched", dao.getDataCollectionByName("PostgreSQL"), notNullValue());

        // a replace drops the last collection, a merging update would keep it
        final JdbcDataCollectionConfig empty = dao.copyOfConfig("copy");
        empty.getJdbcDataCollections().clear();
        dao.replaceConfig("copy", empty);
        assertThat(dao.getDataCollectionByName("PostgreSQL-Copy"), nullValue());
        assertThat(dao.getConfig("copy").getJdbcDataCollections().isEmpty(), equalTo(true));

        dao.deleteConfig("copy");
        assertThat(dao.getConfigIds(), equalTo(Arrays.asList("default", "mysql", "postgresql")));
    }

    @Test(expected = ConfigNotFoundException.class)
    public void copyOfAnUnknownConfigurationFails() {
        dao.copyOfConfig("unknown");
    }

    @Test
    public void firstDefinitionOfACollectionNameWins() {
        final JdbcDataCollectionConfig duplicate = ConfigConvertUtil.jsonToObject(
                ConfigConvertUtil.objectToJson(dao.getConfig("postgresql")), JdbcDataCollectionConfig.class);
        final JdbcDataCollection collection = duplicate.getJdbcDataCollections().get(0);
        collection.getJdbcRrd().setStep(60);
        cm.registerConfiguration(CONFIG_NAME, "zzz", new JsonAsString(ConfigConvertUtil.objectToJson(duplicate)));

        assertThat(dao.getDataCollectionByName("PostgreSQL").getJdbcRrd().getStep(), equalTo(300));
    }

    private void importShipped(final String configId, final String file) throws IOException {
        final String xml = new String(Files.readAllBytes(ConfigurationTestUtils.getFileForConfigFile(file).toPath()), StandardCharsets.UTF_8);
        cm.registerConfiguration(CONFIG_NAME, configId, new JsonAsString(XsdHelper.getConverter(definition).xmlToJson(xml)));
    }

    private static List<String> collectionNames(final JdbcDataCollectionConfig config) {
        return config.getJdbcDataCollections().stream().map(JdbcDataCollection::getName).collect(Collectors.toList());
    }
}
