package org.opennms.core.messagebus;

/**
 * Static factory providing global access to the {@link MessageBus} singleton.
 *
 * <p>Follows the same pattern as {@code EventIpcManagerFactory}: a holder that is
 * populated during Eventd startup via {@code MethodInvokingFactoryBean} and
 * consumed by JMX-managed singletons that lack Spring/OSGi injection.</p>
 */
public class MessageBusFactory {

    private static volatile MessageBus s_messageBus;

    private MessageBusFactory() {}

    public static MessageBus getMessageBus() {
        MessageBus bus = s_messageBus;
        if (bus == null) {
            throw new IllegalStateException(
                    "MessageBus not initialized. Call MessageBusFactory.setMessageBus() first.");
        }
        return bus;
    }

    public static void setMessageBus(MessageBus messageBus) {
        s_messageBus = messageBus;
    }

    public static void reset() {
        s_messageBus = null;
    }
}
