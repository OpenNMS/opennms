package org.opennms.features.eifadapter;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

import org.apache.camel.BeanInject;
import org.apache.camel.util.KeyValueHolder;
import org.junit.Test;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.snmp.InetAddrUtils;
import org.opennms.netmgt.xml.event.Event;

import com.google.common.collect.Lists;

public class EifAdapterBlueprintTest extends CamelBlueprintTest {

    @BeanInject
    protected EventIpcManager eventIpcManager;

    @Override
    protected String getBlueprintDescriptor() {
        return "OSGI-INF/blueprint/blueprint-eif-adapter.xml";
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void addServicesOnStartup(Map<String, KeyValueHolder<Object, Dictionary>> services) {
        MockEventIpcManager mockEventIpcManager = new MockEventIpcManager();
        services.put(EventIpcManager.class.getName(), asService(mockEventIpcManager, null));
    }

    @Test
    public void canParseEifPacketsAndGenerateEvents() throws Exception {
        // Register an event listener
        final List<Event> receivedEvents = Lists.newArrayList();
        eventIpcManager.addEventListener(new EventListener() {
            @Override
            public String getName() {
                return "test";
            }

            @Override
            public void onEvent(Event e) {
                receivedEvents.add(e);
            }
        });

        Socket clientSocket = new Socket(InetAddrUtils.getLocalHostAddress(), 1828);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        // TODO: Send the right stuff
        outToServer.writeBytes("a\n");
        outToServer.flush();
        clientSocket.close();
        
        await().atMost(15, SECONDS).until(() -> receivedEvents.size() == 1);

        // TODO: Validate event
    }
}
