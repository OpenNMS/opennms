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
package org.opennms.netmgt.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.opennms.netmgt.events.api.EventConstants.OPENNMS_STARTED_UEI;
import static org.opennms.netmgt.events.api.EventConstants.OPENNMS_STOPPING_UEI;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

/**
 * The lifecycle events (NMS-9486) are added by a Liquibase changeset, so check that a migrated database
 * defines them in the internal source with a valid order and a parseable definition.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-mockSnmpPeerFactory.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class EventConfLifecycleEventsIT {

    private static final String INTERNAL_SOURCE = "opennms.internal.events";

    @Autowired
    private DataSource m_dataSource;

    @Test
    public void lifecycleEventsAreDefinedInTheInternalSource() {
        final JdbcTemplate jdbc = new JdbcTemplate(m_dataSource);
        for (final String uei : List.of(OPENNMS_STARTED_UEI, OPENNMS_STOPPING_UEI)) {
            final Map<String, Object> row = jdbc.queryForMap(
                    "SELECT s.name, e.event_order, e.enabled, e.xml_content FROM eventconf_events e "
                            + "JOIN eventconf_sources s ON s.id = e.source_id WHERE e.uei = ?", uei);
            assertEquals(INTERNAL_SOURCE, row.get("name"));
            assertNotNull(row.get("event_order"));
            assertEquals(Boolean.TRUE, row.get("enabled"));
            final Event event = JaxbUtils.unmarshal(Event.class, (String) row.get("xml_content"));
            assertEquals(uei, event.getUei());
            assertEquals("Normal", event.getSeverity());
        }

        final Integer stored = jdbc.queryForObject(
                "SELECT COUNT(*) FROM eventconf_events e JOIN eventconf_sources s ON s.id = e.source_id WHERE s.name = ?",
                Integer.class, INTERNAL_SOURCE);
        final Integer counted = jdbc.queryForObject(
                "SELECT event_count FROM eventconf_sources WHERE name = ?", Integer.class, INTERNAL_SOURCE);
        assertEquals(stored, counted);
    }
}
