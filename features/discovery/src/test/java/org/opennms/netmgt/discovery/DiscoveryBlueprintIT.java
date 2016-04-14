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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangeTimedOutException;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;
import org.apache.camel.util.KeyValueHolder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.DiscoveryConfigFactory;
import org.opennms.netmgt.config.api.DiscoveryConfigurationFactory;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.config.discovery.IncludeRange;
import org.opennms.netmgt.config.discovery.Specific;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.discovery.messages.DiscoveryJob;
import org.opennms.netmgt.discovery.messages.DiscoveryResults;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.icmp.Pinger;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.discovery.IPPollRange;
import org.opennms.netmgt.model.events.EventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration( locations = { "classpath:/META-INF/opennms/emptyContext.xml" } )
public class DiscoveryBlueprintIT extends CamelBlueprintTestSupport {

    private static final Logger LOG = LoggerFactory.getLogger(DiscoveryBlueprintIT.class );

    private static final MockEventIpcManager IPC_MANAGER_INSTANCE = new MockEventIpcManager();

    private static final String LOCATION = "RDU";

    private static BrokerService m_broker = null;

    /**
     * Use Aries Blueprint synchronous mode to avoid a blueprint deadlock bug.
     * 
     * @see https://issues.apache.org/jira/browse/ARIES-1051
     * @see https://access.redhat.com/site/solutions/640943
     */
    @Override
    public void doPreSetup() throws Exception
    {
        System.setProperty( "org.apache.aries.blueprint.synchronous", Boolean.TRUE.toString() );
        System.setProperty( "de.kalpatec.pojosr.framework.events.sync", Boolean.TRUE.toString() );
    }

    @Override
    public boolean isUseAdviceWith()
    {
        return true;
    }

    @Override
    public boolean isUseDebugger()
    {
        // must enable debugger
        return true;
    }

    @Override
    public String isMockEndpoints()
    {
        return "*";
    }

    /**
     * Register a mock OSGi {@link SchedulerService} so that we can make sure that the scheduler
     * whiteboard is working properly.
     */
    @SuppressWarnings( "rawtypes" )
    @Override
    protected void addServicesOnStartup( Map<String, KeyValueHolder<Object, Dictionary>> services )
    {
        services.put( Pinger.class.getName(), new KeyValueHolder<Object, Dictionary>(new TestPinger(), new Properties()));

        services.put( EventForwarder.class.getName(),
                new KeyValueHolder<Object, Dictionary>( IPC_MANAGER_INSTANCE, new Properties() ) );

        services.put( EventIpcManager.class.getName(),
                new KeyValueHolder<Object, Dictionary>( IPC_MANAGER_INSTANCE, new Properties() ) );

        OnmsDistPoller distPoller = new OnmsDistPoller();
        distPoller.setId(DistPollerDao.DEFAULT_DIST_POLLER_ID);
        distPoller.setLabel(DistPollerDao.DEFAULT_DIST_POLLER_ID);
        distPoller.setLocation(LOCATION);
        DistPollerDao distPollerDao = new DistPollerDaoMinion(distPoller);

        services.put( DistPollerDao.class.getName(),
                new KeyValueHolder<Object, Dictionary>(distPollerDao, new Properties() ) );

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
    }

    // The location of our Blueprint XML file to be used for testing
    @Override
    protected String getBlueprintDescriptor()
    {
        return "file:blueprint-discovery.xml";
    }

    @BeforeClass
    public static void startActiveMQ() throws Exception {
        m_broker = new BrokerService();
        m_broker.addConnector("tcp://127.0.0.1:61616");
        m_broker.start();
    }

    @AfterClass
    public static void stopActiveMQ() throws Exception {
        if (m_broker != null) {
            m_broker.stop();
        }
    }

    @Test
    public void testDiscover() throws Exception {

        /*
         * Create a Camel listener for the location queue that will respond with
         * {@link DiscoveryResult} objects.
         */
        SimpleRegistry registry = new SimpleRegistry();
        CamelContext mockDiscoverer = new DefaultCamelContext(registry);
        mockDiscoverer.addComponent("activemq", ActiveMQComponent.activeMQComponent("tcp://127.0.0.1:61616"));
        mockDiscoverer.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                String from = String.format("activemq:Location-%s", LOCATION);

                from(from)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        DiscoveryJob job = exchange.getIn().getBody(DiscoveryJob.class);
                        String foreignSource = job.getForeignSource();
                        String location = job.getLocation();

                        Message out = exchange.getOut();
                        DiscoveryResults results = new DiscoveryResults(
                            Collections.singletonMap(InetAddressUtils.addr("4.2.2.2"), 1000L),
                            foreignSource,
                            location
                        );
                        out.setBody(results);
                    }
                });
            }
        });

        mockDiscoverer.start();

        final String ipAddress = "4.2.2.2";
        final String foreignSource = "Bogus FS";
        final String location = LOCATION;

        EventAnticipator anticipator = IPC_MANAGER_INSTANCE.getEventAnticipator();

        EventBuilder eb = new EventBuilder( EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, "OpenNMS.Discovery" );
        eb.setInterface( InetAddress.getByName( ipAddress ) );
        eb.setHost( InetAddressUtils.getLocalHostName() );

        eb.addParam( "RTT", 0 );
        eb.addParam( "foreignSource", foreignSource );

        anticipator.anticipateEvent( eb.getEvent() );

        // Create the config aka job
        Specific specific = new Specific();
        specific.setContent( ipAddress );

        DiscoveryConfiguration config = new DiscoveryConfiguration();
        config.addSpecific( specific );
        config.setForeignSource( foreignSource );
        config.setTimeout( 3000 );
        config.setRetries( 2 );
        config.setLocation( location );

        // Execute the job
        template.requestBody( "seda:submitDiscoveryTask", config );

        Thread.sleep( 1000 );
        anticipator.verifyAnticipated();

        mockDiscoverer.stop();
    }
    
    @Test
    public void testDiscoverToTestTimeout() throws Exception {

        /*
         * Create a Camel listener for the location queue that will respond with
         * {@link DiscoveryResult} objects.
         */
        SimpleRegistry registry = new SimpleRegistry();
        CamelContext mockDiscoverer = new DefaultCamelContext(registry);
        mockDiscoverer.addComponent("activemq", ActiveMQComponent.activeMQComponent("tcp://127.0.0.1:61616"));
        mockDiscoverer.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                String from = String.format("activemq:Location-%s", LOCATION);

                from(from)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        DiscoveryJob job = exchange.getIn().getBody(DiscoveryJob.class);
                        String foreignSource = job.getForeignSource();
                        String location = job.getLocation();

                        // Sleep to trigger a timeout, make this greater than the timeout calculated below
                        Thread.sleep(15000);

                        Message out = exchange.getOut();
                        DiscoveryResults results = new DiscoveryResults(
                            Collections.singletonMap(InetAddressUtils.addr("4.2.2.2"), 1000L),
                            foreignSource,
                            location
                        );
                        out.setBody(results);
                    }
                });
            }
        });

        mockDiscoverer.start();

        final String ipAddress = "4.2.2.2";
        final String foreignSource = "Bogus FS";
        final String location = LOCATION;

        // Create the config aka job
        Specific specific = new Specific();
        specific.setContent( ipAddress );

        DiscoveryConfiguration config = new DiscoveryConfiguration();
        config.addSpecific( specific );
        config.setForeignSource( foreignSource );
        config.setTimeout( 3000 );
        config.setRetries( 2 );
        config.setLocation( location );

        // Timeout should be 1 * 3000 * (2 + 1) * 1.5 = 13500ms

        // Execute the job
        try {
            template.requestBody( "seda:submitDiscoveryTask", config );
        } catch(CamelExecutionException e) {
            // Expected failure exception
            assertEquals(ExchangeTimedOutException.class, e.getCause().getClass());
            return;
        } finally {
            mockDiscoverer.stop();
        }
        fail("A timeout exception should be thrown from the exchange");
    }
}
