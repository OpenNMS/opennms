package org.opennms.core.messagebus;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.messagebus.local.LocalMessageBus;

public class LocalMessageBusTest {

    private LocalMessageBus messageBus;

    @Before
    public void setUp() {
        messageBus = new LocalMessageBus();
    }

    @Test
    public void shouldDeliverMessageToSubscribedHandler() {
        List<IpcMessage> received = new ArrayList<>();
        MessageHandler handler = new MessageHandler() {
            @Override
            public String getName() { return "test-handler"; }
            @Override
            public void onMessage(IpcMessage message) { received.add(message); }
        };

        messageBus.subscribe("reloadConfig", handler);
        messageBus.publish(new IpcMessage("reloadConfig", "webui"));

        assertThat(received).hasSize(1);
        assertThat(received.get(0).getType()).isEqualTo("reloadConfig");
    }

    @Test
    public void shouldNotDeliverToUnsubscribedHandler() {
        List<IpcMessage> received = new ArrayList<>();
        MessageHandler handler = new MessageHandler() {
            @Override
            public String getName() { return "test-handler"; }
            @Override
            public void onMessage(IpcMessage message) { received.add(message); }
        };

        messageBus.subscribe("reloadConfig", handler);
        messageBus.unsubscribe(handler);
        messageBus.publish(new IpcMessage("reloadConfig", "webui"));

        assertThat(received).isEmpty();
    }

    @Test
    public void shouldNotDeliverToHandlerSubscribedToDifferentType() {
        List<IpcMessage> received = new ArrayList<>();
        MessageHandler handler = new MessageHandler() {
            @Override
            public String getName() { return "test-handler"; }
            @Override
            public void onMessage(IpcMessage message) { received.add(message); }
        };

        messageBus.subscribe("reloadConfig", handler);
        messageBus.publish(new IpcMessage("newSuspect", "discovery"));

        assertThat(received).isEmpty();
    }

    @Test
    public void shouldSurviveHandlerException() {
        List<IpcMessage> received = new ArrayList<>();
        MessageHandler failingHandler = new MessageHandler() {
            @Override
            public String getName() { return "failing"; }
            @Override
            public void onMessage(IpcMessage message) { throw new RuntimeException("boom"); }
        };
        MessageHandler goodHandler = new MessageHandler() {
            @Override
            public String getName() { return "good"; }
            @Override
            public void onMessage(IpcMessage message) { received.add(message); }
        };

        messageBus.subscribe("test", failingHandler);
        messageBus.subscribe("test", goodHandler);
        messageBus.publish(new IpcMessage("test", "src"));

        assertThat(received).hasSize(1);
    }
}
