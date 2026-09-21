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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;

/**
 * Covers the evaluation of <code>type="sql"</code> translation values: how many times the
 * statement is issued per event, and how a matched-but-null column differs from no match.
 */
public class EventTranslatorSqlValueTest {

    private static final String LINK_DOWN_UEI = "uei.opennms.org/generic/traps/SNMP_Link_Down";
    private static final String IF_INDEX_OID = ".1.3.6.1.2.1.2.2.1.1.2";

    private Connection m_connection;
    private DataSource m_dataSource;

    @Before
    public void setUp() throws SQLException {
        m_connection = mock(Connection.class);
        m_dataSource = mock(DataSource.class);
        when(m_dataSource.getConnection()).thenReturn(m_connection);
    }

    /** Deciding whether a value matches and fetching its result must share one round trip. */
    @Test
    public void testSqlValueIsQueriedOnlyOncePerEvent() throws Exception {
        stubQueryResult("eth0");

        final List<Event> translated = translate(translationConfig(null), linkDownEvent(1, 2));

        assertEquals(1, translated.size());
        assertEquals("eth0", parmValue(translated.get(0), "ifName"));
        verify(m_connection, times(1)).prepareStatement(anyString());
    }

    @Test
    public void testSqlValueFindingNoRowRejectsTheMappingWithASingleQuery() throws Exception {
        stubEmptyQueryResult();

        assertEquals(0, translate(translationConfig(null), linkDownEvent(1, 2)).size());
        verify(m_connection, times(1)).prepareStatement(anyString());
    }

    @Test
    public void testDefaultIsUsedWhenSqlValueFindsNoRow() throws Exception {
        stubEmptyQueryResult();

        final List<Event> translated = translate(translationConfig("unknown"), linkDownEvent(1, 2));

        assertEquals(1, translated.size());
        assertEquals("unknown", parmValue(translated.get(0), "ifName"));
    }

    /**
     * A row whose column is null is still a match, so the default must not kick in. Collapsing the
     * evaluation result into an Optional would silently turn this case into the default.
     */
    @Test
    public void testNullColumnIsAMatchRatherThanAFallbackToTheDefault() throws Exception {
        stubQueryResult(null);

        final List<Event> translated = translate(translationConfig("unknown"), linkDownEvent(1, 2));

        assertEquals(1, translated.size());
        assertEquals("", parmValue(translated.get(0), "ifName"));
    }

    @Test
    public void testEventWithoutTheExpectedParmDoesNotReachTheDatabase() throws Exception {
        final Event event = linkDownEvent(1, 2);
        event.getParmCollection().get(0).setParmName("someOtherParm");

        assertEquals(0, translate(translationConfig(null), event).size());
        verify(m_connection, times(0)).prepareStatement(anyString());
    }

    private List<Event> translate(final String config, final Event event) throws Exception {
        final InputStream stream = new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8));
        return new EventTranslatorConfigFactory(stream, m_dataSource).translateEvent(event);
    }

    private void stubQueryResult(final Object columnValue) throws SQLException {
        stubStatement(true, columnValue);
    }

    private void stubEmptyQueryResult() throws SQLException {
        stubStatement(false, null);
    }

    /**
     * Hands out a fresh ResultSet per execution, so re-running the query yields the same rows.
     * Query counts are therefore asserted by verifying prepareStatement, not inferred from a
     * result set that happens to be exhausted.
     */
    private void stubStatement(final boolean hasRow, final Object columnValue) throws SQLException {
        final PreparedStatement statement = mock(PreparedStatement.class);
        when(statement.executeQuery()).thenAnswer(invocation -> {
            final ResultSet resultSet = mock(ResultSet.class);
            when(resultSet.next()).thenReturn(hasRow, false);
            when(resultSet.getObject(1)).thenReturn(columnValue);
            return resultSet;
        });
        when(m_connection.prepareStatement(anyString())).thenReturn(statement);
    }

    private static String parmValue(final Event event, final String parmName) {
        for (final Parm parm : event.getParmCollection()) {
            if (parmName.equals(parm.getParmName())) {
                return parm.getValue() == null ? null : parm.getValue().getContent();
            }
        }
        return null;
    }

    private static Event linkDownEvent(final long nodeId, final int ifIndex) {
        final Value value = new Value();
        value.setContent(String.valueOf(ifIndex));

        final Parm parm = new Parm();
        parm.setParmName(IF_INDEX_OID);
        parm.setValue(value);

        final Event event = new Event();
        event.setUei(LINK_DOWN_UEI);
        event.setNodeid(nodeId);
        event.addParm(parm);
        return event;
    }

    /** Mirrors the shipped link-down translation: one sql value fed by a field and a '~' regex parm. */
    private static String translationConfig(final String ifNameDefault) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<event-translator-configuration xmlns=\"http://xmlns.opennms.org/xsd/translator-configuration\">\n"
                + "  <translation>\n"
                + "    <event-translation-spec uei=\"" + LINK_DOWN_UEI + "\">\n"
                + "      <mappings>\n"
                + "        <mapping>\n"
                + "          <assignment name=\"ifName\" type=\"parameter\""
                + (ifNameDefault == null ? "" : " default=\"" + ifNameDefault + "\"") + ">\n"
                + "            <value type=\"sql\" result=\"SELECT snmpifname FROM snmpinterface WHERE nodeid = ?::integer AND snmpifindex = ?::integer\">\n"
                + "              <value type=\"field\" name=\"nodeid\" matches=\".*\" result=\"${0}\" />\n"
                + "              <value type=\"parameter\" name=\"~^\\.1\\.3\\.6\\.1\\.2\\.1\\.2\\.2\\.1\\.1\\.([0-9]*)$\" matches=\".*\" result=\"${0}\" />\n"
                + "            </value>\n"
                + "          </assignment>\n"
                + "        </mapping>\n"
                + "      </mappings>\n"
                + "    </event-translation-spec>\n"
                + "  </translation>\n"
                + "</event-translator-configuration>\n";
    }
}
