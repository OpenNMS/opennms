/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.netflow.parser;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.RecordProvider;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Session;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.UdpSessionManager;

public abstract class UdpParserBase extends ParserBase {
    public final static long HOUSEKEEPING_INTERVAL = 60000;

    private UdpSessionManager sessionManager;

    private ScheduledFuture<?> housekeepingFuture;
    private Duration templateTimeout = Duration.ofMinutes(30);

    public UdpParserBase(Protocol protocol, String name, AsyncDispatcher<TelemetryMessage> dispatcher) {
        super(protocol, name, dispatcher);
    }

    protected abstract RecordProvider parse(final Session session, final ByteBuffer buffer) throws Exception;

    public final CompletableFuture<?> parse(final ByteBuffer buffer,
                                            final InetSocketAddress remoteAddress,
                                            final InetSocketAddress localAddress) throws Exception {
        final Session session = this.sessionManager.getSession(remoteAddress, localAddress);

        try {
            return this.transmit(this.parse(session, buffer), remoteAddress);
        } catch (Exception e) {
            this.sessionManager.drop(remoteAddress, localAddress);
            throw e;
        }
    }

    public void start(final ScheduledExecutorService executorService) {
        this.sessionManager = new UdpSessionManager(this.templateTimeout);
        this.housekeepingFuture = executorService.scheduleAtFixedRate(this.sessionManager::doHousekeeping,
                HOUSEKEEPING_INTERVAL,
                HOUSEKEEPING_INTERVAL,
                TimeUnit.MILLISECONDS);
    }

    public void stop() {
        this.housekeepingFuture.cancel(false);
    }

    public Duration getTemplateTimeout() {
        return this.templateTimeout;
    }

    public void setTemplateTimeout(final Duration templateTimeout) {
        this.templateTimeout = templateTimeout;
    }
}
