package org.opennms.netmgt.discovery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.springframework.test.context.ContextConfiguration;

@RunWith( OpenNMSJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath:/META-INF/opennms/emptyContext.xml" } )
public class DiscoveryRoutingTest extends CamelTestSupport
{
    static public class Discoverer
    {
        public String[] discover( DiscoveryConfiguration config )
        {
            return new String[] { "1", "2", "3" };
        }

    }

    static public class EventWriter
    {
        public void writeEvent( String[] eventData )
        {
            System.out.println( Arrays.toString( eventData ) );
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

    /**
     * Test loading the {@link PackageAgentList} based on a given collection package.
     */
    @Test
    public void testLoadServiceAgents() throws Exception
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

        template.requestBody( "direct:submitDiscoveryJob", new DiscoveryConfiguration() ); // pass
                                                                                           // in
                                                                                           // message

        assertMockEndpointsSatisfied();

        // Make sure that we got one exchange to the scheduler
        assertEquals( 1, endpoint.getReceivedCounter() );

        // // That contains 3 SNMP agent instances
        // for ( Exchange exchange : endpoint.getReceivedExchanges() )
        // {
        // PackageAgentList agents = exchange.getIn().getBody( PackageAgentList.class );
        // assertNotNull( agents );
        // assertEquals( 3, agents.getAgents().size() );
        // }
    }

    private <T> T bean( String name, Class<T> type )
    {
        return context().getRegistry().lookupByNameAndType( name, type );
    }
}
