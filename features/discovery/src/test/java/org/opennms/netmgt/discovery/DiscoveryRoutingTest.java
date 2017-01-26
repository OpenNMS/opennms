package org.opennms.netmgt.discovery;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.discovery.messages.DiscoveryJob;
import org.opennms.netmgt.discovery.messages.DiscoveryResults;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.icmp.EchoPacket;
import org.opennms.netmgt.model.events.EventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@RunWith( OpenNMSJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath:/META-INF/opennms/emptyContext.xml" } )
public class DiscoveryRoutingTest extends CamelTestSupport
{
    static public class Discoverer
    {
        public DiscoveryResults discover( DiscoveryJob job )
        {
            return new DiscoveryResults(Maps.newHashMap(), job.getForeignSource(), job.getLocation());
        }

    }

    static public class EventWriter
    {
        private static final Logger LOG = LoggerFactory.getLogger(EventWriter.class);

        public void sendEvents( DiscoveryResults results )
        {
            results.getResponses().entrySet()
                .forEach(e -> sendNewSuspectEvent(e.getKey(), e.getValue(),results.getForeignSource()));
        }

        private void sendNewSuspectEvent(InetAddress address, EchoPacket response, String foreignSource) {
            EventBuilder eb = new EventBuilder(EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, "OpenNMS.Discovery");
            eb.setInterface(address);
            eb.setHost(InetAddressUtils.getLocalHostName());

            eb.addParam("RTT", response.getReceivedTimeNanos() - response.getSentTimeNanos());

            if (foreignSource != null) {
                eb.addParam("foreignSource", foreignSource);
            }

            try {
                EventIpcManagerFactory.getIpcManager().sendNow(eb.getEvent());
                LOG.debug("Sent event: {}", EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI);
            } catch (Throwable t) {
                LOG.warn("run: unexpected throwable exception caught during send to middleware", t);
            }
        }
    }

    @Override
    protected JndiRegistry createRegistry() throws Exception
    {
        JndiRegistry registry = super.createRegistry();

        registry.bind( "discoverer", new Discoverer() );
        registry.bind( "eventWriter", new EventWriter() );

        // SnmpMetricRepository snmpMetricRepository = new SnmpMetricRepository( url(
        // "datacollection-config.xml" ),
        // url( "datacollection/mib2.xml" ), url( "datacollection/netsnmp.xml" ),
        // url( "datacollection/dell.xml" ) );
        //
        // registry.bind( "collectdConfiguration", new
        // SingletonBeanFactoryImpl<CollectdConfiguration>() );
        // registry.bind( "snmpConfig", new SingletonBeanFactoryImpl<SnmpConfig>() );
        // registry.bind( "snmpMetricRepository", snmpMetricRepository );
        // registry.bind( "urlNormalizer", new UrlNormalizer() );
        // registry.bind( "packageServiceSplitter", new PackageServiceSplitter() );
        // registry.bind( "jaxbXml", DataFormatUtils.jaxbXml() );

        return registry;
    }

    /**
     * Delay calling context.start() so that you can attach an {@link AdviceWithRouteBuilder} to the
     * context before it starts.
     */
    @Override
    public boolean isUseAdviceWith()
    {
        return true;
    }

    /**
     * Build the route for all of the config parsing messages.
     */
    @Override
    protected RouteBuilder createRouteBuilder() throws Exception
    {
        return new RouteBuilder() {

            @Override
            public void configure() throws Exception
            {

                // Add exception handlers
                onException( IOException.class ).handled( true )

                // .transform().constant(null)
                .logStackTrace( true ).stop();

                from( "direct:submitDiscoveryJob" ).to( "seda:discoveryJobQueue" );
                from( "seda:discoveryJobQueue" ).to( "bean:discoverer" ).to( "bean:eventWriter" );

                // // Call this to retrieve a URL in string form or URL form into the JAXB objects
                // they
                // // represent
                // from( "direct:parseJaxbXml" ).beanRef( "urlNormalizer" ).unmarshal( "jaxbXml" );
                //
                // // Direct route to fetch the config
                // from( "direct:collectdConfig" ).beanRef( "collectdConfiguration", "getInstance"
                // );

                // // TODO: Create a reload timer that will check for changes to the config
                // from( "direct:loadCollectdConfiguration" ).transform(
                // constant( url( "collectd-configuration.xml" ) ) ).to( "direct:parseJaxbXml"
                // ).beanRef(
                // "collectdConfiguration", "setInstance" );
            }
        };
    }

    @Test
    public void testDiscover() throws Exception
    {
        for ( RouteDefinition route : new ArrayList<RouteDefinition>( context.getRouteDefinitions() ) )
        {
            route.adviceWith( context, new AdviceWithRouteBuilder() {
                @Override
                public void configure() throws Exception
                {
                    mockEndpoints();
                }
            } );
        }
        context.start();

        // We should get 1 call to the scheduler endpoint
        MockEndpoint endpoint = getMockEndpoint( "mock:bean:eventWriter", false );
        endpoint.setExpectedMessageCount( 1 );

        // Create message
        // Service svc = new Service();
        // svc.setInterval( 1000L );
        // svc.setName( "SNMP" );
        // svc.setStatus( "on" );
        // svc.setUserDefined( "false" );
        // Package pkg = new Package();
        // pkg.setName( "example1" );
        // pkg.setServices( Collections.singletonList( svc ) );

        DiscoveryJob job = new DiscoveryJob(Lists.newArrayList(), "myForeignSource", "myLocation" );
        template.requestBody( "direct:submitDiscoveryJob", job ); // pass
                                                                                           // in
                                                                                           // message

        assertMockEndpointsSatisfied();
    }
}
