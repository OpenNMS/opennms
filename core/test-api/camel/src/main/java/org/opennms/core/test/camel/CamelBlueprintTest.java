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
package org.opennms.core.test.camel;

import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;
import org.opennms.netmgt.icmp.AbstractPingerFactory;
import org.opennms.netmgt.icmp.PingerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CamelBlueprintTest extends CamelBlueprintTestSupport {
    private static final Logger LOG = LoggerFactory.getLogger(CamelBlueprintTest.class);

    /**
     * Use Aries Blueprint synchronous mode to avoid a blueprint deadlock bug. Also, make sure
     * the PingerFactory is reset so DSCP/fragment bits are cleared.
     *
     * @see https://issues.apache.org/jira/browse/ARIES-1051
     * @see https://access.redhat.com/site/solutions/640943
     */
    @Override
    public void doPreSetup() throws Exception {
        System.setProperty( "org.apache.aries.blueprint.synchronous", Boolean.TRUE.toString() );
        System.setProperty( "de.kalpatec.pojosr.framework.events.sync", Boolean.TRUE.toString() );
        try {
            final PingerFactory pingerFactory = getOsgiService(PingerFactory.class, 2000);
            if (pingerFactory instanceof AbstractPingerFactory) {
                ((AbstractPingerFactory) pingerFactory).reset();
            }
        } catch (final Exception e) {
            LOG.warn("Failed to get PingerFactory. This may be intentional.");
        }
    }

    /*
     * It is not obvious what this does from the name, but if the value is the
     * default of true, the blueprint tests will not start the context
     * automatically at setUp() time.
     */
    @Override
    public boolean isUseAdviceWith() {
        return false;
    }

    @Override
    public String isMockEndpoints() {
        return "*";
    }

    public static final int getAvailablePort(final AtomicInteger current, final int max) {
        while (current.get() < max) {
            try (final ServerSocket socket = new ServerSocket(current.get())) {
                return socket.getLocalPort();
            } catch (final Throwable e) {}
            current.incrementAndGet();
        }
        throw new IllegalStateException("Can't find an available network port");
    }

}
