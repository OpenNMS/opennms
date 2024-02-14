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

import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;

import org.apache.camel.util.KeyValueHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.distributed.core.api.Identity;
import org.opennms.distributed.core.api.MinionIdentity;
import org.opennms.distributed.core.api.SystemType;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsMonitoringSystem;
import org.springframework.test.context.ContextConfiguration;

@RunWith( OpenNMSJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath:/META-INF/opennms/emptyContext.xml" } )
public class BlueprintDistPollerDaoDistributedIT extends CamelBlueprintTest {
    private static final String LOCATION = "TEST_LOCATION";
    private static final String DEFAULT_DIST_POLLER_ID = "00000000-0000-0000-0000-000000000000";

    @SuppressWarnings("rawtypes")
    @Override
    protected void addServicesOnStartup( Map<String, KeyValueHolder<Object, Dictionary>> services ) {
        MinionIdentity identity = new MinionIdentity() {
            @Override
            public String getId() {
                return DEFAULT_DIST_POLLER_ID;
            }
            @Override
            public String getLocation() {
                return LOCATION;
            }

            @Override
            public String getType() {
                return SystemType.Minion.name();
            }
        };

        services.put(MinionIdentity.class.getName(), new KeyValueHolder<>(identity, new Properties()));
        services.put(Identity.class.getName(), new KeyValueHolder<>(identity, new Properties()));
    }

    // The location of our Blueprint XML file to be used for testing
    @Override
    protected String getBlueprintDescriptor() {
        return "file:src/main/resources/OSGI-INF/blueprint/blueprint-distPollerDao.xml,blueprint-empty-camel-context.xml";
    }

    @Test
    public void testDistPollerDao() throws Exception {
        DistPollerDao dao = getOsgiService(DistPollerDao.class);
        assertEquals(1, dao.countAll());

        // Test get()
        OnmsDistPoller poller = dao.get(DEFAULT_DIST_POLLER_ID);
        assertNotNull(poller);
        assertEquals(DEFAULT_DIST_POLLER_ID, poller.getId());
        assertEquals(DEFAULT_DIST_POLLER_ID, poller.getLabel());
        assertEquals(LOCATION, poller.getLocation());
        assertEquals(OnmsMonitoringSystem.TYPE_MINION, poller.getType());

        // Test whoami()
        poller = dao.whoami();
        assertNotNull(poller);
        assertEquals(DEFAULT_DIST_POLLER_ID, poller.getId());
        assertEquals(DEFAULT_DIST_POLLER_ID, poller.getLabel());
        assertEquals(LOCATION, poller.getLocation());
        assertEquals(OnmsMonitoringSystem.TYPE_MINION, poller.getType());
    }
}
