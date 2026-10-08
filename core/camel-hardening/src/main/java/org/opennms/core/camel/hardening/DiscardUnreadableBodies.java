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
package org.opennms.core.camel.hardening;

import javax.jms.JMSException;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.RuntimeCamelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * First step of a JMS consuming route: touches the lazily extracted body and,
 * if the JMS client refused it (an ObjectMessage whose class is not trusted,
 * CVE-2026-40860), logs once, drops the message and stops the route.
 *
 * Letting the exception escape instead would roll the message back, so the
 * broker redelivers it and every attempt logs an error.
 */
public class DiscardUnreadableBodies implements Processor {
    private static final Logger LOG = LoggerFactory.getLogger(DiscardUnreadableBodies.class);

    @Override
    public void process(final Exchange exchange) {
        try {
            exchange.getIn().getBody();
        } catch (final RuntimeCamelException e) {
            final JMSException cause = findJmsCause(e);
            if (cause == null) {
                throw e;
            }
            LOG.warn("Discarding JMS message {} from {}: {}", exchange.getIn().getHeader("JMSMessageID"),
                    exchange.getIn().getHeader("JMSDestination"), cause.getMessage());
            // no reply for InOut consumers, and no further processing
            exchange.setPattern(ExchangePattern.InOnly);
            exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
        }
    }

    private static JMSException findJmsCause(final Throwable t) {
        for (Throwable c = t; c != null && c.getCause() != c; c = c.getCause()) {
            if (c instanceof JMSException) {
                return (JMSException) c;
            }
        }
        return null;
    }
}
