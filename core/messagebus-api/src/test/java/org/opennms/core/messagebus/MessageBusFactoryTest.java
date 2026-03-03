package org.opennms.core.messagebus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.opennms.core.messagebus.local.LocalMessageBus;
import org.junit.After;
import org.junit.Test;

public class MessageBusFactoryTest {

    @After
    public void tearDown() {
        MessageBusFactory.reset();
    }

    @Test
    public void shouldReturnConfiguredMessageBus() {
        MessageBus bus = new LocalMessageBus();
        MessageBusFactory.setMessageBus(bus);
        assertThat(MessageBusFactory.getMessageBus()).isSameAs(bus);
    }

    @Test
    public void shouldThrowWhenNotInitialized() {
        assertThatThrownBy(MessageBusFactory::getMessageBus)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void shouldResetCleanly() {
        MessageBusFactory.setMessageBus(new LocalMessageBus());
        MessageBusFactory.reset();
        assertThatThrownBy(MessageBusFactory::getMessageBus)
                .isInstanceOf(IllegalStateException.class);
    }
}
