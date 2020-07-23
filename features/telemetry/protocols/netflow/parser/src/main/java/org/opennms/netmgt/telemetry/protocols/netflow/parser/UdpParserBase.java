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
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.distributed.core.api.Identity;
import org.opennms.netmgt.dnsresolver.api.DnsResolver;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.listeners.UdpParser;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.RecordProvider;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Session;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.UdpSessionManager;

import com.codahale.metrics.MetricRegistry;

import io.netty.buffer.ByteBuf;

public abstract class UdpParserBase extends ParserBase implements UdpParser {
    public final static long HOUSEKEEPING_INTERVAL = 60000;

    private UdpSessionManager sessionManager;

    private ScheduledFuture<?> housekeepingFuture;
    private Duration templateTimeout = Duration.ofMinutes(30);

    public UdpParserBase(final Protocol protocol,
                         final String name,
                         final AsyncDispatcher<TelemetryMessage> dispatcher,
                         final EventForwarder eventForwarder,
                         final Identity identity,
                         final DnsResolver dnsResolver,
                         final MetricRegistry metricRegistry) {
        super(protocol, name, dispatcher, eventForwarder, identity, dnsResolver, metricRegistry);
    }

    protected abstract RecordProvider parse(final Session session, final ByteBuf buffer) throws Exception;

    protected abstract UdpSessionManager.SessionKey buildSessionKey(final InetSocketAddress remoteAddress, final InetSocketAddress localAddress);

    public final CompletableFuture<?> parse(final ByteBuf buffer,
                                            final InetSocketAddress remoteAddress,
                                            final InetSocketAddress localAddress) throws Exception {
        final UdpSessionManager.SessionKey sessionKey = this.buildSessionKey(remoteAddress, localAddress);
        final Session session = this.sessionManager.getSession(sessionKey);

        try {
            return this.transmit(this.parse(session, buffer), remoteAddress);
        } catch (Exception e) {
            this.sessionManager.drop(sessionKey);
            throw e;
        }
    }

    @Override
    public void start(final ScheduledExecutorService executorService) {
        super.start(executorService);
        this.sessionManager = new UdpSessionManager(this.templateTimeout);
        this.housekeepingFuture = executorService.scheduleAtFixedRate(this.sessionManager::doHousekeeping,
                HOUSEKEEPING_INTERVAL,
                HOUSEKEEPING_INTERVAL,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        this.housekeepingFuture.cancel(false);
        super.stop();
    }

    public Duration getTemplateTimeout() {
        return this.templateTimeout;
    }

    public void setTemplateTimeout(final Duration templateTimeout) {
        this.templateTimeout = templateTimeout;
    }
}
