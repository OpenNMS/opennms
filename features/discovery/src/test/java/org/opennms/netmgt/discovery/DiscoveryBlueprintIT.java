package org.opennms.netmgt.discovery;

import java.net.InetAddress;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;
import org.apache.camel.util.KeyValueHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.config.discovery.Specific;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.icmp.EchoPacket;
import org.opennms.netmgt.icmp.PingResponseCallback;
import org.opennms.netmgt.icmp.Pinger;
import org.opennms.netmgt.model.events.EventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;

@RunWith( OpenNMSJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath:/META-INF/opennms/emptyContext.xml" } )
public class DiscoveryBlueprintIT extends CamelBlueprintTestSupport
{
    private static final Logger              LOG                  = LoggerFactory.getLogger(
                    DiscoveryBlueprintIT.class );
    private static final MockEventIpcManager IPC_MANAGER_INSTANCE = new MockEventIpcManager();
    private static final EventAnticipator    ANTICIPATOR          = new EventAnticipator();

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
        services.put( Pinger.class.getName(), new KeyValueHolder<Object, Dictionary>( new Pinger() {

            @Override
            public void ping( InetAddress host, long timeout, int retries, int packetsize, int sequenceId,
                            PingResponseCallback cb ) throws Exception
            {
                cb.handleResponse( host, new EchoPacket() {

                    @Override
                    public boolean isEchoReply()
                    {
                        // TODO Auto-generated method stub
                        return true;
                    }

                    @Override
                    public int getIdentifier()
                    {
                        // TODO Auto-generated method stub
                        return 0;
                    }

                    @Override
                    public int getSequenceNumber()
                    {
                        // TODO Auto-generated method stub
                        return 0;
                    }

                    @Override
                    public long getThreadId()
                    {
                        // TODO Auto-generated method stub
                        return 0;
                    }

                    @Override
                    public long getReceivedTimeNanos()
                    {
                        // TODO Auto-generated method stub
                        return 0;
                    }

                    @Override
                    public long getSentTimeNanos()
                    {
                        // TODO Auto-generated method stub
                        return 0;
                    }

                    @Override
                    public double elapsedTime( TimeUnit timeUnit )
                    {
                        // TODO Auto-generated method stub
                        return 0;
                    }
                } );

            }

            @Override
            public void ping( InetAddress host, long timeout, int retries, int sequenceId, PingResponseCallback cb )
                            throws Exception
            {
                ping( host, timeout, retries, 0, sequenceId, cb );
            }

            @Override
            public Number ping( InetAddress host, long timeout, int retries, int packetsize ) throws Exception
            {
                return 1;
            }

            @Override
            public Number ping( InetAddress host, long timeout, int retries ) throws Exception
            {
                return 1;
            }

            @Override
            public Number ping( InetAddress host ) throws Exception
            {
                return 1;
            }

            @Override
            public List<Number> parallelPing( InetAddress host, int count, long timeout, long pingInterval )
                            throws Exception
            {
                return null;
            }

            @Override
            public void initialize4() throws Exception
            {
            }

            @Override
            public void initialize6() throws Exception
            {
            }

            @Override
            public boolean isV4Available()
            {
                return true;
            }

            @Override
            public boolean isV6Available()
            {
                return true;
            }

        }, new Properties() ) );

        services.put( EventIpcManager.class.getName(),
                        new KeyValueHolder<Object, Dictionary>( IPC_MANAGER_INSTANCE, new Properties() ) );
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
        final String ipAddress = "4.2.2.2";
        final String foreignSource = "Bogus FS";
        final String location = "LOC1";

        IPC_MANAGER_INSTANCE.setEventAnticipator( ANTICIPATOR );

        EventBuilder eb = new EventBuilder( EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, "OpenNMS.Discovery" );
        eb.setInterface( InetAddress.getByName( ipAddress ) );
        eb.setHost( InetAddressUtils.getLocalHostName() );

        eb.addParam( "RTT", 0 );
        eb.addParam( "foreignSource", foreignSource );

        ANTICIPATOR.anticipateEvent( eb.getEvent() );

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
        template.requestBody( "direct:submitDiscoveryTask", config );

        Thread.sleep( 1000 );
        ANTICIPATOR.verifyAnticipated();
    }
}
