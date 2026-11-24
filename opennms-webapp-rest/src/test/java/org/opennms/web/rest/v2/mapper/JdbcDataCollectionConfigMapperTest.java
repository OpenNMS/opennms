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
package org.opennms.web.rest.v2.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.jdbc.JdbcColumn;
import org.opennms.netmgt.config.jdbc.JdbcDataCollectionConfig;
import org.opennms.netmgt.config.jdbc.JdbcQuery;
import org.opennms.web.rest.v2.JdbcDataCollectionConfigValidator;
import org.opennms.web.rest.v2.model.JdbcColumnDto;
import org.opennms.web.rest.v2.model.JdbcQueryDto;
import org.opennms.web.rest.v2.model.JdbcSourceDto;

/**
 * Every shipped file has to pass the validator and survive the way through the DTOs unchanged, or editing a
 * shipped collection in the UI would change or refuse what the user did not touch.
 */
@RunWith(Parameterized.class)
public class JdbcDataCollectionConfigMapperTest {

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

    public JdbcDataCollectionConfigMapperTest(final String file) {
        this.file = file;
    }

    @Test
    public void shippedFilesAreValidAndRoundTripUnchanged() throws IOException {
        final String xml = new String(Files.readAllBytes(ConfigurationTestUtils.getFileForConfigFile(file).toPath()), StandardCharsets.UTF_8);
        final JdbcDataCollectionConfig config = JaxbUtils.unmarshal(JdbcDataCollectionConfig.class, xml);

        final JdbcSourceDto dto = JdbcDataCollectionConfigMapper.toDto("id", config);
        assertNull(JdbcDataCollectionConfigValidator.validateSource(dto));
        assertEquals(JaxbUtils.marshal(config), JaxbUtils.marshal(JdbcDataCollectionConfigMapper.toEntity(dto)));
    }

    @Test
    public void blankOptionalValuesAreDroppedAndNamesTrimmed() {
        final JdbcColumnDto column = new JdbcColumnDto();
        column.setName(" c ");
        column.setDataSourceName("  ");
        column.setType("Gauge32");
        column.setAlias(" a ");
        final JdbcQueryDto dto = new JdbcQueryDto();
        dto.setName(" q ");
        dto.setIfType("ignore");
        dto.setResourceType("");
        dto.setQueryString(" SELECT 1 ");
        dto.setColumns(List.of(column));

        final JdbcQuery query = JdbcDataCollectionConfigMapper.toEntity(dto);
        assertEquals("q", query.getQueryName());
        assertEquals(0, query.getRecheckInterval());
        assertNull(query.getResourceType());
        assertNull(query.getInstanceColumn());
        assertEquals("the statement is kept as written", " SELECT 1 ", query.getJdbcStatement().getJdbcQuery());

        final JdbcColumn entity = query.getJdbcColumns().get(0);
        assertEquals("c", entity.getColumnName());
        assertNull(entity.getDataSourceName());
        assertEquals("gauge", entity.getDataType().getName());
        assertEquals("a", entity.getAlias());
    }
}
