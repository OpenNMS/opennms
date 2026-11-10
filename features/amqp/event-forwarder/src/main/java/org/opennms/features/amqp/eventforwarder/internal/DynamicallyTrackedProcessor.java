/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
