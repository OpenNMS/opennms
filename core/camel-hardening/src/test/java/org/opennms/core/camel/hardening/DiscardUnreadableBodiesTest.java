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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.jms.JMSException;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultMessage;
import org.junit.Test;

public class DiscardUnreadableBodiesTest {

    /** Mimics JmsMessage: the body is extracted lazily and the client may refuse it. */
    private static class RefusingMessage extends DefaultMessage {
        @Override
        protected Object createBody() {
            throw new RuntimeCamelException("Failed to extract body due to: javax.jms.JMSException",
                    new JMSException("Failed to build body from content. Serializable class not available to broker."));
        }
    }

    private static Exchange exchange(final org.apache.camel.Message in) {
        final Exchange exchange = new DefaultExchange(new DefaultCamelContext(), ExchangePattern.InOut);
        exchange.setIn(in);
        return exchange;
    }

    @Test
    public void dropsRefusedBodiesWithoutReplyOrException() {
        final Exchange exchange = exchange(new RefusingMessage());
        new DiscardUnreadableBodies().process(exchange);
        assertTrue(exchange.getProperty(Exchange.ROUTE_STOP, Boolean.class));
        assertEquals(ExchangePattern.InOnly, exchange.getPattern());
        assertNull(exchange.getException());
    }

    @Test
    public void passesReadableBodies() {
        final DefaultMessage in = new DefaultMessage();
        in.setBody("payload");
        final Exchange exchange = exchange(in);
        new DiscardUnreadableBodies().process(exchange);
        assertFalse(exchange.getProperty(Exchange.ROUTE_STOP, false, Boolean.class));
        assertEquals(ExchangePattern.InOut, exchange.getPattern());
    }

    @Test
    public void rethrowsFailuresThatAreNotFromTheJmsClient() {
        final Exchange exchange = exchange(new DefaultMessage() {
            @Override
            protected Object createBody() {
                throw new RuntimeCamelException("something else");
            }
        });
        try {
            new DiscardUnreadableBodies().process(exchange);
            fail();
        } catch (final RuntimeCamelException expected) {
            assertEquals("something else", expected.getMessage());
        }
    }
}
