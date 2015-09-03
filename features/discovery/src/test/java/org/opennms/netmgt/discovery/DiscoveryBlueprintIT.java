package org.opennms.netmgt.discovery;

import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;

import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;
import org.apache.camel.util.KeyValueHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.config.discovery.Specific;
import org.opennms.netmgt.icmp.NullPinger;
import org.opennms.netmgt.icmp.Pinger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;

@RunWith( OpenNMSJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath:/META-INF/opennms/emptyContext.xml" } )
public class DiscoveryBlueprintIT extends CamelBlueprintTestSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( DiscoveryBlueprintIT.class );

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
        services.put( Pinger.class.getName(),
                        new KeyValueHolder<Object, Dictionary>( new NullPinger(), new Properties() ) );
    }

    // The location of our Blueprint XML file to be used for testing
    @Override
    protected String getBlueprintDescriptor()
    {
        return "file:src/main/resources/OSGI-INF/blueprint/blueprint.xml";
    }

    @Test
    public void testDiscover() throws Exception
    {
        // We should get 1 call to the scheduler endpoint
        MockEndpoint endpoint = getMockEndpoint( "mock:bean:eventWriter", false );
        endpoint.setExpectedMessageCount( 1 );

        // Create the config aka job
        Specific specific = new Specific();
        specific.setContent( "4.2.2.2" );

        DiscoveryConfiguration config = new DiscoveryConfiguration();
        config.addSpecific( specific );
        config.setForeignSource( "Bogus FS" );
        config.setTimeout( 3000 );
        config.setRetries( 2 );
        config.setLocation( "LOC1" );

        // Execute the job
        template.requestBody( "direct:createDiscoveryJobs", config );

        assertMockEndpointsSatisfied();
    }
}
