package org.opennms.features.amqp.common;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Camel {@link Processor} that delegates calls to an implementation that is
 * dynamically tracked using a {@link ServiceTracker}.
 *
 * Processor implementations are expected to have the following property:
 *  name={$processorName}
 *
 * where ${processorName} is used to identify a specific instance of the processor.
 *
 * @author jwhite
 */
public class DynamicallyTrackedProcessor implements Processor {
    public static final Logger LOG = LoggerFactory.getLogger(DynamicallyTrackedProcessor.class);

    private BundleContext m_context;

    private ServiceTracker<?, Processor> m_tracker = null;

    /**
     * A unique name for the processor we are looking for.
     */
    private String m_processorName;

    @Override
    public void process(final Exchange exchange) throws Exception {
        if (m_tracker == null) {
            String filterString = String.format("(&(%s=%s)(name=%s))",
                    Constants.OBJECTCLASS, Processor.class.getName(),
                    m_processorName);
            Filter filter = m_context.createFilter(filterString);
            LOG.info("Starting tracker with filter: {}", filterString);
            m_tracker = new ServiceTracker<Object, Processor>(m_context, filter, null);
            m_tracker.open();
        }

        // Grab the first service that meets our criteria
        Processor processor = m_tracker.getService();
        // Fail if no process is defined
        if (processor == null) {
            throw new Exception("No suitable processer service was found with name: " + m_processorName);
        }
        processor.process(exchange);
    }

    public void setContext(BundleContext context) {
        m_context = context;
    }

    public void setProcessorName(String processorName) {
        m_processorName = processorName;
    }

    public void destroy() {
        if (m_tracker != null) {
            m_tracker.close();
        }
    }
}
