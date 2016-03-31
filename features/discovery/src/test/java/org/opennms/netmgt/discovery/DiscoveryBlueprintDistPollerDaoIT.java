/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.discovery;

import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;

import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;
import org.apache.camel.util.KeyValueHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.minion.core.api.MinionIdentity;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsMonitoringSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;

@RunWith( OpenNMSJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath:/META-INF/opennms/emptyContext.xml" } )
public class DiscoveryBlueprintDistPollerDaoIT extends CamelBlueprintTestSupport
{
    private static final Logger LOG = LoggerFactory.getLogger(DiscoveryBlueprintDistPollerDaoIT.class);

    private static final String LOCATION = "TEST_LOCATION";

    /**
     * Use Aries Blueprint synchronous mode to avoid a blueprint deadlock bug.
     * 
     * @see https://issues.apache.org/jira/browse/ARIES-1051
     * @see https://access.redhat.com/site/solutions/640943
     */
    @Override
    public void doPreSetup() throws Exception {
        System.setProperty( "org.apache.aries.blueprint.synchronous", Boolean.TRUE.toString() );
        System.setProperty( "de.kalpatec.pojosr.framework.events.sync", Boolean.TRUE.toString() );
    }

    @Override
    public boolean isUseAdviceWith() {
        return true;
    }

    @Override
    public boolean isUseDebugger() {
        // must enable debugger
        return true;
    }

    @Override
    public String isMockEndpoints() {
        return "*";
    }

    @SuppressWarnings( "rawtypes" )
    @Override
    protected void addServicesOnStartup( Map<String, KeyValueHolder<Object, Dictionary>> services ) {
        services.put( MinionIdentity.class.getName(),
                new KeyValueHolder<Object, Dictionary>( new MinionIdentity() {
                    @Override
                    public String getId() {
                        return DistPollerDao.DEFAULT_DIST_POLLER_ID;
                    }
                    @Override
                    public String getLocation() {
                        return LOCATION;
                    }
                }, new Properties() ) );
    }

    // The location of our Blueprint XML file to be used for testing
    @Override
    protected String getBlueprintDescriptor() {
        return "file:blueprint-discovery-distPollerDaoMinion.xml,file:src/test/resources/blueprint-empty-camel-context.xml";
    }

    @Test
    public void testDistPollerDao() throws Exception {
        DistPollerDao dao = getOsgiService(DistPollerDao.class);
        assertEquals(1, dao.countAll());

        // Test get()
        OnmsDistPoller poller = dao.get(DistPollerDao.DEFAULT_DIST_POLLER_ID);
        assertNotNull(poller);
        assertEquals(DistPollerDao.DEFAULT_DIST_POLLER_ID, poller.getId());
        assertEquals(DistPollerDao.DEFAULT_DIST_POLLER_ID, poller.getLabel());
        assertEquals(LOCATION, poller.getLocation());
        assertEquals(OnmsMonitoringSystem.TYPE_MINION, poller.getType());

        // Test whoami()
        poller = dao.whoami();
        assertNotNull(poller);
        assertEquals(DistPollerDao.DEFAULT_DIST_POLLER_ID, poller.getId());
        assertEquals(DistPollerDao.DEFAULT_DIST_POLLER_ID, poller.getLabel());
        assertEquals(LOCATION, poller.getLocation());
        assertEquals(OnmsMonitoringSystem.TYPE_MINION, poller.getType());
    }
}
