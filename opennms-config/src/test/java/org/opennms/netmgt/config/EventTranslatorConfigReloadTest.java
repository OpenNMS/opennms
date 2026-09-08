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
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;

/**
 * Covers the reload path: a Translator config reload must not discard the DataSource that
 * <code>type="sql"</code> translation values are evaluated against.
 */
public class EventTranslatorConfigReloadTest {

    private static final String LINK_DOWN_UEI = "uei.opennms.org/generic/traps/SNMP_Link_Down";
    private static final String IF_INDEX_OID = ".1.3.6.1.2.1.2.2.1.1.2";

    @Rule
    public TemporaryFolder m_home = new TemporaryFolder();

    private String m_previousHome;
    private File m_configFile;
    private DataSource m_dataSource;

    @Before
    public void setUp() throws Exception {
        final File etc = m_home.newFolder("etc");
        m_configFile = new File(etc, "translator-configuration.xml");

        m_previousHome = System.setProperty("opennms.home", m_home.getRoot().getAbsolutePath());

        final Connection connection = mock(Connection.class);
        final PreparedStatement statement = mock(PreparedStatement.class);
        when(statement.executeQuery()).thenAnswer(invocation -> {
            final ResultSet resultSet = mock(ResultSet.class);
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getObject(1)).thenReturn("eth0");
            return resultSet;
        });
        when(connection.prepareStatement(anyString())).thenReturn(statement);

        m_dataSource = mock(DataSource.class);
        when(m_dataSource.getConnection()).thenReturn(connection);
    }

    @After
    public void tearDown() {
        if (m_previousHome == null) {
            System.clearProperty("opennms.home");
        } else {
            System.setProperty("opennms.home", m_previousHome);
        }
    }

    /**
     * update() used to unmarshal with a null DataSource, so every sql value spec threw a
     * NullPointerException after the first reload and those mappings stopped translating
     * until OpenNMS was restarted.
     */
    @Test
    public void testSqlValuesStillResolveAfterAReload() throws Exception {
        writeConfig(translationConfig("before-reload"));

        final EventTranslatorConfigFactory config =
                new EventTranslatorConfigFactory(configStream("before-reload"), m_dataSource);

        List<Event> translated = config.translateEvent(linkDownEvent());
        assertEquals(1, translated.size());
        assertEquals("eth0", parmValue(translated.get(0), "ifName"));
        assertEquals("before-reload", parmValue(translated.get(0), "configMarker"));

        writeConfig(translationConfig("after-reload"));
        config.update();

        translated = config.translateEvent(linkDownEvent());
        assertEquals(1, translated.size());
        assertEquals("eth0", parmValue(translated.get(0), "ifName"));
        assertEquals("after-reload", parmValue(translated.get(0), "configMarker"));
    }

    private void writeConfig(final String config) throws Exception {
        Files.write(m_configFile.toPath(), config.getBytes(StandardCharsets.UTF_8));
    }

    private static InputStream configStream(final String marker) {
        return new ByteArrayInputStream(translationConfig(marker).getBytes(StandardCharsets.UTF_8));
    }

    private static String parmValue(final Event event, final String parmName) {
        for (final Parm parm : event.getParmCollection()) {
            if (parmName.equals(parm.getParmName())) {
                return parm.getValue() == null ? null : parm.getValue().getContent();
            }
        }
        return null;
    }

    private static Event linkDownEvent() {
        final Value value = new Value();
        value.setContent("2");

        final Parm parm = new Parm();
        parm.setParmName(IF_INDEX_OID);
        parm.setValue(value);

        final Event event = new Event();
        event.setUei(LINK_DOWN_UEI);
        event.setNodeid(1L);
        event.addParm(parm);
        return event;
    }

    /** A sql value alongside a constant, so a reload is observable independently of the query. */
    private static String translationConfig(final String marker) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<event-translator-configuration xmlns=\"http://xmlns.opennms.org/xsd/translator-configuration\">\n"
                + "  <translation>\n"
                + "    <event-translation-spec uei=\"" + LINK_DOWN_UEI + "\">\n"
                + "      <mappings>\n"
                + "        <mapping>\n"
                + "          <assignment name=\"ifName\" type=\"parameter\">\n"
                + "            <value type=\"sql\" result=\"SELECT snmpifname FROM snmpinterface WHERE nodeid = ?::integer AND snmpifindex = ?::integer\">\n"
                + "              <value type=\"field\" name=\"nodeid\" matches=\".*\" result=\"${0}\" />\n"
                + "              <value type=\"parameter\" name=\"~^\\.1\\.3\\.6\\.1\\.2\\.1\\.2\\.2\\.1\\.1\\.([0-9]*)$\" matches=\".*\" result=\"${0}\" />\n"
                + "            </value>\n"
                + "          </assignment>\n"
                + "          <assignment name=\"configMarker\" type=\"parameter\">\n"
                + "            <value type=\"constant\" result=\"" + marker + "\" />\n"
                + "          </assignment>\n"
                + "        </mapping>\n"
                + "      </mappings>\n"
                + "    </event-translation-spec>\n"
                + "  </translation>\n"
                + "</event-translator-configuration>\n";
    }
}
