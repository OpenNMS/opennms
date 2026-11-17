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
package org.opennms.netmgt.config.jdbc;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.InputStream;

import org.junit.Test;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.xml.JaxbUtils;

public class JdbcDataCollectionConfigRulesTest {

    private static final String[] SHIPPED = {
            "jdbc-datacollection-config.xml",
            "jdbc-datacollection.d/mssqlserver.xml",
            "jdbc-datacollection.d/mysql.xml",
            "jdbc-datacollection.d/oracle.xml",
            "jdbc-datacollection.d/postgresql.xml",
    };

    @Test
    public void shippedConfigurationsPass() throws Exception {
        for (final String file : SHIPPED) {
            assertNull(file, JdbcDataCollectionConfigRules.validate(load(file)));
        }
    }

    @Test
    public void sourceIds() {
        assertNull(JdbcDataCollectionConfigRules.validateSourceId("default"));
        assertNull(JdbcDataCollectionConfigRules.validateSourceId("default.d"));
        assertNull(JdbcDataCollectionConfigRules.validateSourceId("my_db-1.2"));
        assertNotNull(JdbcDataCollectionConfigRules.validateSourceId(null));
        assertNotNull(JdbcDataCollectionConfigRules.validateSourceId(""));
        assertNotNull(JdbcDataCollectionConfigRules.validateSourceId("-mysql"));
        assertNotNull(JdbcDataCollectionConfigRules.validateSourceId("../etc"));
        assertNotNull(JdbcDataCollectionConfigRules.validateSourceId("a b"));
        assertNotNull(JdbcDataCollectionConfigRules.validateSourceId("x;filename=evil.sh"));
    }

    @Test
    public void columnWithoutTypeIsRejected() throws Exception {
        final JdbcDataCollectionConfig config = load("jdbc-datacollection.d/postgresql.xml");
        firstColumn(config).setDataType(null);
        assertThat(JdbcDataCollectionConfigRules.validate(config), containsString("the type is required"));
    }

    @Test
    public void tooLongAliasIsRejected() throws Exception {
        final JdbcDataCollectionConfig config = load("jdbc-datacollection.d/postgresql.xml");
        firstColumn(config).setAlias("twenty-characters-xx");
        assertThat(JdbcDataCollectionConfigRules.validate(config), containsString("longer than 19 characters"));
    }

    @Test
    public void duplicateCollectionIsRejected() throws Exception {
        final JdbcDataCollectionConfig config = load("jdbc-datacollection.d/postgresql.xml");
        config.getJdbcDataCollections().add(config.getJdbcDataCollections().get(0));
        assertThat(JdbcDataCollectionConfigRules.validate(config), containsString("is defined more than once"));
    }

    @Test
    public void badRraIsRejected() throws Exception {
        final JdbcDataCollectionConfig config = load("jdbc-datacollection.d/postgresql.xml");
        config.getJdbcDataCollections().get(0).getJdbcRrd().getJdbcRras().set(0, "RRA:SUM:0.5:1:2016");
        assertThat(JdbcDataCollectionConfigRules.validate(config), containsString("RRA:SUM:0.5:1:2016"));
    }

    private static JdbcColumn firstColumn(final JdbcDataCollectionConfig config) {
        return config.getJdbcDataCollections().get(0).getQueries().get(0).getJdbcColumns().get(0);
    }

    private static JdbcDataCollectionConfig load(final String file) throws Exception {
        try (InputStream in = ConfigurationTestUtils.getInputStreamForConfigFile(file)) {
            return JaxbUtils.unmarshal(JdbcDataCollectionConfig.class, in);
        }
    }
}
