/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.amqp.eventforwarder.internal;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicallyTrackedProcessor implements Processor {
    public static final Logger LOG = LoggerFactory.getLogger(DynamicallyTrackedProcessor.class);

    private BundleContext m_context;

    private ServiceTracker<?, Processor> m_tracker = null;

    /**
     * A unique name for the processor we are looking for.
     */
    private String m_processorName;

    /**
     * A tag used to identify processors. Allows processor to be used many times.
     */
    private String m_processorKey;

    @Override
    public void process(final Exchange exchange) throws Exception {
        if (m_tracker == null) {
            String filterString = String.format("(&(%s=%s)(name=%s)(%s=true))",
                    Constants.OBJECTCLASS, Processor.class.getName(),
                    m_processorName, m_processorKey);
            Filter filter = m_context.createFilter(filterString);
            LOG.info("Starting tracker with filter: {}", filterString);
            m_tracker = new ServiceTracker<Object, Processor>(m_context, filter, null);
            m_tracker.open();
        }

        try {
            // Grab the first service that meets our criteria
            Processor processor = m_tracker.getService();
            // Fail if no process is defined
            if (processor == null) {
                throw new RuntimeException("No suitable processer was found.");
            }
            LOG.debug("Processing exchange with: {}", processor.getClass());
            processor.process(exchange);
        } catch (Throwable e) {
            LOG.warn("Message dispatch failed: " + e.getMessage(), e);
            exchange.setException(e);
        }
    }

    public void setContext(BundleContext context) {
        m_context = context;
    }

    public void setProcessorKey(String processorKey) {
        m_processorKey = processorKey;
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
