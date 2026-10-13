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
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.apache.camel.impl.DefaultHeaderFilterStrategy;
import org.apache.camel.spi.HeaderFilterStrategy;
import org.junit.Test;

public class InboundHeaderAllowListTest {
    private final HeaderFilterStrategy strategy = new InboundHeaderAllowList(new DefaultHeaderFilterStrategy());

    private boolean blocks(final String name) {
        return strategy.applyFilterToExternalHeaders(name, "x", null);
    }

    @Test
    public void passesOnlyTheAllowedNames() {
        assertEquals(new HashSet<>(Arrays.asList("JmsQueueName", "SystemId", "RpcTracingInfo", "SinkTracingInfo")),
                InboundHeaderAllowList.DEFAULT_ALLOWED);
        for (String name : InboundHeaderAllowList.DEFAULT_ALLOWED) {
            assertFalse(name, blocks(name));
        }
    }

    @Test
    public void blocksEverythingElse() {
        for (String name : new String[] { "CamelExecCommandExecutable", "CAmelExecCommandExecutable",
                "org.apache.camel.Foo", "org_DOT_apache_DOT_camel_DOT_Foo", "CamelJmsDestinationName",
                "systemid", "JMSXGroupID", "myHeader", "org.opennms.Foo", "" }) {
            assertTrue(name, blocks(name));
        }
        assertTrue(blocks(null));
    }

    @Test
    public void leavesOutboundToTheDelegate() {
        final DefaultHeaderFilterStrategy outbound = new DefaultHeaderFilterStrategy();
        outbound.setOutFilter(Collections.singleton("blockedOutbound"));
        final HeaderFilterStrategy wrapped = new InboundHeaderAllowList(outbound, Collections.emptyList());
        assertTrue(wrapped.applyFilterToCamelHeaders("blockedOutbound", "x", null));
        assertEquals(outbound.applyFilterToCamelHeaders("CamelJmsRequestTimeout", 5000L, null),
                wrapped.applyFilterToCamelHeaders("CamelJmsRequestTimeout", 5000L, null));
    }
}
