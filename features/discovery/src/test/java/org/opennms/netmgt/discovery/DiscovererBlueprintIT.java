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

import org.apache.camel.Component;
import org.apache.camel.component.seda.SedaComponent;
import org.apache.camel.util.KeyValueHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.minion.core.api.MinionIdentity;
import org.opennms.netmgt.config.DiscoveryConfigFactory;
import org.opennms.netmgt.config.api.DiscoveryConfigurationFactory;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.config.discovery.IncludeRange;
import org.opennms.netmgt.dao.DistPollerDaoMinion;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.icmp.Pinger;
import org.springframework.test.context.ContextConfiguration;

@RunWith( OpenNMSJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath:/META-INF/opennms/emptyContext.xml" } )
public class DiscovererBlueprintIT extends CamelBlueprintTest {

    private static final MockEventIpcManager IPC_MANAGER_INSTANCE = new MockEventIpcManager();

    private static final String LOCATION = "RDU";

    /**
     * Register a mock OSGi {@link SchedulerService} so that we can make sure that the scheduler
     * whiteboard is working properly.
     */
    @SuppressWarnings( "rawtypes" )
    @Override
    protected void addServicesOnStartup( Map<String, KeyValueHolder<Object, Dictionary>> services ) {
        services.put( Pinger.class.getName(), new KeyValueHolder<Object, Dictionary>(new TestPinger(), new Properties()));

        services.put( EventForwarder.class.getName(),
                new KeyValueHolder<Object, Dictionary>( IPC_MANAGER_INSTANCE, new Properties() ) );

        services.put( EventIpcManager.class.getName(),
                new KeyValueHolder<Object, Dictionary>( IPC_MANAGER_INSTANCE, new Properties() ) );

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
                }, new Properties())
        );

        DiscoveryConfiguration config = new DiscoveryConfiguration();
        IncludeRange range = new IncludeRange();
        range.setBegin("127.0.1.1");
        range.setEnd("127.0.1.20");
        config.setChunkSize(1);
        config.setIncludeRange(new IncludeRange[] { range });
        config.setInitialSleepTime(30000);
        config.setRestartSleepTime(30000);
        DiscoveryConfigFactory configFactory = new DiscoveryConfigFactory(config);

        services.put( DiscoveryConfigurationFactory.class.getName(),
                new KeyValueHolder<Object, Dictionary>(configFactory, new Properties() ) );

        Properties props = new Properties();
        props.setProperty("alias", "opennms.broker");
        services.put( Component.class.getName(),
                new KeyValueHolder<Object, Dictionary>( new SedaComponent(), props ) );
    }

    // The location of our Blueprint XML file to be used for testing
    @Override
    protected String getBlueprintDescriptor() {
        return "file:blueprint-discoverer.xml";
    }

    @Test
    public void testDiscoverer() throws Exception {
        // pass
    }
}
