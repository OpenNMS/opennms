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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.opennms.web.rest.v2.model.JdbcCollectionDto;
import org.opennms.web.rest.v2.model.JdbcColumnDto;
import org.opennms.web.rest.v2.model.JdbcQueryDto;
import org.opennms.web.rest.v2.model.JdbcSourceDto;

public class JdbcDataCollectionConfigValidatorTest {

    @Test
    public void acceptsAValidCollection() {
        assertNull(JdbcDataCollectionConfigValidator.validateCollection(JdbcDataCollectionConfigRestServiceTest.collection("c")));
    }

    @Test
    public void checksTheSourceId() {
        assertNull(JdbcDataCollectionConfigValidator.validateSourceId("my-sql_2.0"));
        assertTrue(JdbcDataCollectionConfigValidator.validateSourceId(null).contains("required"));
        assertTrue(JdbcDataCollectionConfigValidator.validateSourceId("a/b").contains("only letters"));
        assertTrue(JdbcDataCollectionConfigValidator.validateSourceId(".hidden").contains("start with"));
    }

    @Test
    public void checksTheRrdSettings() {
        final JdbcCollectionDto collection = valid();
        collection.setRrdStep(0);
        assertMessage(collection, "RRD step");

        final JdbcCollectionDto noRras = valid();
        noRras.setRras(List.of());
        assertMessage(noRras, "at least one RRA");

        final JdbcCollectionDto badRra = valid();
        badRra.setRras(List.of("RRA:AVG:0.5:1:2016"));
        assertMessage(badRra, "is not of the form");

        final JdbcCollectionDto xff = valid();
        xff.setRras(List.of("RRA:MAX:.5:288:366", "RRA:LAST:1:1:10"));
        assertNull(JdbcDataCollectionConfigValidator.validateCollection(xff));
    }

    @Test
    public void checksTheQueries() {
        final JdbcCollectionDto duplicate = valid();
        duplicate.getQueries().add(duplicate.getQueries().get(0));
        assertMessage(duplicate, "query 'nodes' is defined more than once");

        final JdbcCollectionDto noSql = valid();
        noSql.getQueries().get(0).setQueryString(" ");
        assertMessage(noSql, "query 'nodes': the SQL statement is required");

        final JdbcCollectionDto halfInstance = valid();
        halfInstance.getQueries().get(0).setResourceType("pgDatabase");
        assertMessage(halfInstance, "must be set together");

        final JdbcCollectionDto negative = valid();
        negative.getQueries().get(0).setRecheckInterval(-1);
        assertMessage(negative, "must not be negative");

        final JdbcCollectionDto noColumns = valid();
        noColumns.getQueries().get(0).setColumns(new ArrayList<>());
        assertMessage(noColumns, "at least one column");
    }

    @Test
    public void checksTheColumns() {
        final JdbcCollectionDto badType = valid();
        badType.getQueries().get(0).getColumns().get(0).setType("float");
        assertMessage(badType, "column 1: type 'float' is not one of");

        final JdbcCollectionDto longAlias = valid();
        longAlias.getQueries().get(0).getColumns().get(0).setAlias("12345678901234567890");
        assertMessage(longAlias, "longer than 19");

        final JdbcCollectionDto sameAlias = valid();
        sameAlias.getQueries().get(0).getColumns().add(sameAlias.getQueries().get(0).getColumns().get(0));
        assertMessage(sameAlias, "alias 'nodeCount' is used by more than one column");
    }

    @Test
    public void refusesACollectionNameTwiceInASource() {
        final JdbcSourceDto source = new JdbcSourceDto();
        source.setCollections(List.of(valid(), valid()));
        assertEquals("Collection 'c' is defined more than once.", JdbcDataCollectionConfigValidator.validateSource(source));
    }

    private static JdbcCollectionDto valid() {
        final JdbcCollectionDto collection = JdbcDataCollectionConfigRestServiceTest.collection("c");
        final JdbcQueryDto query = collection.getQueries().get(0);
        query.setColumns(new ArrayList<>(List.<JdbcColumnDto>of(query.getColumns().get(0))));
        collection.setQueries(new ArrayList<>(List.of(query)));
        return collection;
    }

    private static void assertMessage(final JdbcCollectionDto collection, final String expected) {
        final String message = JdbcDataCollectionConfigValidator.validateCollection(collection);
        assertTrue(message, message != null && message.contains(expected));
    }
}
