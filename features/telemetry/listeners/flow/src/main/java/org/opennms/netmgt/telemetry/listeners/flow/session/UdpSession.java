/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.listeners.flow.session;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class UdpSession {

    private final static class Key {
        public final InetSocketAddress remoteAddress;
        public final InetSocketAddress localAddress;
        public final long observationDomainId;
        public final int templateId;

        Key(final InetSocketAddress remoteAddress,
            final InetSocketAddress localAddress,
            final long observationDomainId,
            final int templateId) {
            this.remoteAddress = Objects.requireNonNull(remoteAddress);
            this.localAddress = Objects.requireNonNull(localAddress);
            this.observationDomainId = observationDomainId;
            this.templateId = templateId;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;

            final Key that = (Key) o;
            return this.observationDomainId == that.observationDomainId &&
                    this.templateId == that.templateId &&
                    Objects.equals(this.remoteAddress, that.remoteAddress) &&
                    Objects.equals(this.localAddress, that.localAddress);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.remoteAddress, this.localAddress, this.observationDomainId, this.templateId);
        }
    }

    private final static class Value {
        public final Instant insertionTime;
        public final Template template;

        private Value(final Template template) {
            this.insertionTime = Instant.now();
            this.template = template;
        }
    }

    private final Map<Key, Value> templates = new HashMap<>();

    private final Duration timeout;

    public UdpSession(final Duration timeout) {
        this.timeout = timeout;
    }

    public void doHousekeeping() {
        final Instant timeout = Instant.now().minus(this.timeout);
        UdpSession.this.templates.entrySet().removeIf(e -> e.getValue().insertionTime.isBefore(timeout));
    }

    public TemplateManager getTemplateManager(final InetSocketAddress remoteAddress, final InetSocketAddress localAddress) {
        return new TemplateManager() {
            @Override
            public void add(final long observationDomainId, final int templateId, final Template template) {
                UdpSession.this.templates.put(new Key(remoteAddress, localAddress, observationDomainId, templateId), new Value(template));
            }

            @Override
            public void remove(final long observationDomainId, final int templateId) {
                UdpSession.this.templates.remove(new Key(remoteAddress, localAddress, observationDomainId, templateId));
            }

            @Override
            public void removeAll(final long observationDomainId, final Template.Type type) {
                UdpSession.this.templates.entrySet().removeIf(e -> e.getKey().observationDomainId == observationDomainId && e.getValue().template.type == type);
            }

            @Override
            public TemplateResolver getResolver(final long observationDomainId) {
                // TODO: Do we have to check template timeout on each get?
                return templateId -> Optional.ofNullable(UdpSession.this.templates.get(new Key(remoteAddress, localAddress, observationDomainId, templateId))).map(v -> v.template);
            }
        };
    }

    public void drop(final InetSocketAddress remoteAddress, final InetSocketAddress localAddress) {
        this.templates.entrySet().removeIf(e -> Objects.equals(e.getKey().remoteAddress, remoteAddress) && Objects.equals(e.getKey().localAddress, localAddress));
    }
}
