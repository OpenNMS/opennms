/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

    @Override
    public boolean isUseAdviceWith() {
        return true;
    }

    @Override
    public boolean isUseDebugger() {
        // Must enable debugger
        return true;
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
