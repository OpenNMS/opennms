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
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.config.jdbc.JdbcColumn;
import org.opennms.netmgt.config.jdbc.JdbcDataCollectionConfig;

/**
 * The JDBC data collection configuration is imported from XML into the configuration manager through the XSD, read
 * back with Jackson ({@link ConfigConvertUtil}) and written again by the DAO. Every shipped file has to survive that
 * without losing anything, and what the DAO writes has to pass the schema validation of the configuration manager.
 *
 * <p>The comparison marshals both objects to XML, because the {@code equals()} of the model ignores some
 * attributes.</p>
 */
@RunWith(Parameterized.class)
public class JdbcDataCollectionJsonCompatibilityTest {

    private static ConfigDefinition definition;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> files() {
        return Arrays.asList(new Object[][]{
                {"jdbc-datacollection-config.xml"},
                {"jdbc-datacollection.d/mssqlserver.xml"},
                {"jdbc-datacollection.d/mysql.xml"},
                {"jdbc-datacollection.d/oracle.xml"},
                {"jdbc-datacollection.d/postgresql.xml"},
        });
    }

    private final String file;

    public JdbcDataCollectionJsonCompatibilityTest(final String file) {
        this.file = file;
    }

    @BeforeClass
    public static void setUpDefinition() {
        // Same definition the upgrade's importSchemaFromXsd registers.
        definition = XsdHelper.buildConfigDefinition("jdbc-datacollection-config", "jdbc-datacollection.xsd",
                "jdbc-datacollection-config", "/rest/cm", true);
    }

    @Test
    public void xmlImportedThroughTheSchemaReadsBackUnchanged() throws IOException {
        final String xml = shippedXml();
        // Same conversion the upgrade's importConfig runs.
        final String importedJson = XsdHelper.getConverter(definition).xmlToJson(xml);
        definition.validate(importedJson);

        final JdbcDataCollectionConfig fromXml = JaxbUtils.unmarshal(JdbcDataCollectionConfig.class, xml);
        final JdbcDataCollectionConfig fromJson = ConfigConvertUtil.jsonToObject(importedJson, JdbcDataCollectionConfig.class);
        assertThat(fromXml.getJdbcDataCollections().isEmpty(), equalTo(false));
        assertThat(JaxbUtils.marshal(fromJson), equalTo(JaxbUtils.marshal(fromXml)));
    }

    @Test
    public void jsonWrittenByTheDaoIsValidAndReadsBackUnchanged() throws IOException {
        final JdbcDataCollectionConfig fromXml = JaxbUtils.unmarshal(JdbcDataCollectionConfig.class, shippedXml());

        final String written = ConfigConvertUtil.objectToJson(fromXml);
        definition.validate(written);
        assertThat("model field names must not leak into the stored JSON", written, not(containsString("\"m-")));

        final JdbcDataCollectionConfig readBack = ConfigConvertUtil.jsonToObject(written, JdbcDataCollectionConfig.class);
        assertThat(JaxbUtils.marshal(readBack), equalTo(JaxbUtils.marshal(fromXml)));
    }

    @Test
    public void typeIsStoredByItsXmlName() {
        final JdbcColumn column = new JdbcColumn();
        column.setColumnName("c");
        column.setAlias("a");
        column.setDataType(AttributeType.COUNTER);
        assertThat(ConfigConvertUtil.objectToJson(column), containsString("\"type\":\"counter\""));
        assertThat(ConfigConvertUtil.jsonToObject("{\"name\":\"c\",\"alias\":\"a\",\"type\":\"gauge32\"}", JdbcColumn.class).getDataType(),
                equalTo(AttributeType.GAUGE));
    }

    private String shippedXml() throws IOException {
        return new String(Files.readAllBytes(ConfigurationTestUtils.getFileForConfigFile(file).toPath()), StandardCharsets.UTF_8);
    }
}
