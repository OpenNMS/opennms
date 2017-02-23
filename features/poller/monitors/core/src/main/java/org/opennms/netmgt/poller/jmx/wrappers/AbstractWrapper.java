package org.opennms.netmgt.poller.jmx.wrappers;

import javax.management.MBeanServerConnection;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.TabularDataSupport;

/**
 * Base class for all wrappers used to evaluate MBean attributes in JEXL expressions.
 */
public abstract class AbstractWrapper {
    /**
     * The connection to the MBean server.
     */
    protected final MBeanServerConnection connection;

    public AbstractWrapper(final MBeanServerConnection connection) {
        this.connection = connection;
    }

    /**
     * Get the value of the requested attribute.
     *
     * Implementors should wrap the result using the {@link #wrap(Object)} method.
     *
     * @param name the name of the requested attribute
     * @return the value
     */
    public abstract Object get(final String name);

    /**
     * Helper to wrap an arbitary result in a wrapper if required.
     *
     * @param result the result to wrap
     * @return a wrapper or the result itself
     */
    protected Object wrap(final Object result) {
        if (result instanceof TabularDataSupport) {
            return new TabularDataWrapper(this.connection, (TabularDataSupport) result);
        }

        if (result instanceof CompositeDataSupport) {
            return new CompositeDataWrapper(this.connection, (CompositeDataSupport) result);
        }

        return result;
    }
}
