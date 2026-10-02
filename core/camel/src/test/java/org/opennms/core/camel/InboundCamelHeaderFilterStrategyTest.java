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
package org.opennms.core.camel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.apache.camel.spi.HeaderFilterStrategy;
import org.apache.camel.impl.DefaultHeaderFilterStrategy;
import org.junit.Test;

public class InboundCamelHeaderFilterStrategyTest {
    private final HeaderFilterStrategy strategy = new InboundCamelHeaderFilterStrategy(new DefaultHeaderFilterStrategy());

    private boolean blocks(String name) {
        return strategy.applyFilterToExternalHeaders(name, "x", null);
    }

    @Test
    public void blocksCamelHeadersInAnyCase() {
        for (String name : new String[] { "CamelExecCommandExecutable", "CAmelExecCommandExecutable",
                "camelexeccommandexecutable", "CAMELFILENAME", "org.apache.camel.Foo", "ORG.APACHE.CAMEL.foo" }) {
            assertTrue(name, blocks(name));
        }
    }

    @Test
    public void passesOpenNMSHeaders() {
        for (String name : new String[] { "JmsQueueName", "SystemId", "RpcTracingInfo", "SinkTracingInfo",
                "JMSCorrelationID", "myHeader", "org.opennms.Foo" }) {
            assertFalse(name, blocks(name));
        }
    }

    @Test
    public void leavesNullNameToDelegate() {
        assertEquals(new DefaultHeaderFilterStrategy().applyFilterToExternalHeaders(null, "x", null), blocks(null));
    }

    @Test
    public void defersToDelegate() {
        final DefaultHeaderFilterStrategy delegate = new DefaultHeaderFilterStrategy();
        delegate.setInFilter(Collections.singleton("blockedByDelegate"));
        final HeaderFilterStrategy wrapped = new InboundCamelHeaderFilterStrategy(delegate);
        assertTrue(wrapped.applyFilterToExternalHeaders("blockedByDelegate", "x", null));
        assertEquals(delegate.applyFilterToCamelHeaders("CamelFoo", "x", null),
                wrapped.applyFilterToCamelHeaders("CamelFoo", "x", null));
    }
}
