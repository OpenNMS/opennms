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
package org.opennms.netmgt.trapd;


import org.junit.Before;
import org.junit.BeforeClass;
import org.opennms.netmgt.config.EventConfTestUtil;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.model.EventConfEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;

import java.util.List;

/**
 * {@link TrapHandlerITCase} which uses the snmp strategy {@link org.opennms.netmgt.snmp.joesnmp.JoeSnmpStrategy}.
 *
 * @author brozow
 */
public class JoeSnmpTrapHandlerIT extends TrapHandlerITCase {

    @Autowired
    private EventConfDao eventConfDao;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        System.setProperty("org.opennms.snmp.strategyClass", "org.opennms.netmgt.snmp.joesnmp.JoeSnmpStrategy");
    }

    @Before
    public void setUp() throws Exception {
        List<EventConfEvent> events = EventConfTestUtil.parseResourcesAsEventConfEvents(
                new FileSystemResource("src/test/resources/org/opennms/netmgt/trapd/eventconf.xml"));
        // Load into DB
        eventConfDao.loadEventsFromDB(events);
        super.setUp();
    }

}
