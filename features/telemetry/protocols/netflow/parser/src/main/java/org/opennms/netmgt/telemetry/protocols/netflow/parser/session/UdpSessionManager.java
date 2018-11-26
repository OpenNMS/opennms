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

package org.opennms.netmgt.telemetry.protocols.netflow.parser.session;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class UdpSessionManager {
    private final class UdpSession implements Session {
        private final class Resolver implements Session.Resolver {
            private final long observationDomainId;

            private Resolver(final long observationDomainId) {
                this.observationDomainId = observationDomainId;
            }

            private Key key(final int templateId) {
                return new Key(UdpSession.this.remoteAddress, UdpSession.this.localAddress, this.observationDomainId, templateId);
            }

            @Override
            public Optional<Template> lookupTemplate(final int templateId) {
                return Optional.ofNullable(UdpSessionManager.this.templates.get(key(templateId))).map(v -> v.template);
            }

            @Override
            public List<Value<?>> lookupOptions(final List<Value<?>> values) {
                final LinkedHashMap<String, Value<?>> options = new LinkedHashMap();

                final Set<String> scoped = values.stream().map(Value::getName).collect(Collectors.toSet());

                for (final Map.Entry<Key, Map<Set<Value<?>>, List<Value<?>>>> e : Iterables.filter(UdpSessionManager.this.options.entrySet(),
                                                                                                   e -> Objects.equals(e.getKey().localAddress, UdpSession.this.localAddress) &&
                                                                                                        Objects.equals(e.getKey().remoteAddress, UdpSession.this.remoteAddress) &&
                                                                                                        Objects.equals(e.getKey().observationDomainId, this.observationDomainId))) {
                    final Template template = UdpSessionManager.this.templates.get(e.getKey()).template;

                    final Set<String> scopes = template.scopes.stream().map(Scope::getName).collect(Collectors.toSet());

                    if (scoped.containsAll(scopes)) {
                        // Found option template where scoped fields is subset of actual data fields

                        final Set<Value<?>> scopeValues = values.stream()
                                .filter(s -> scopes.contains(s.getName()))
                                .collect(Collectors.toSet());

                        for (final Value<?> value : e.getValue().getOrDefault(scopeValues, Collections.emptyList())) {
                            options.put(value.getName(), value);
                        }
                    }
                }

                return new ArrayList(options.values());
            }
        }

        private final InetSocketAddress remoteAddress;
        private final InetSocketAddress localAddress;

        public UdpSession(InetSocketAddress remoteAddress, InetSocketAddress localAddress) {
            this.remoteAddress = remoteAddress;
            this.localAddress = localAddress;
        }

        @Override
        public void addTemplate(final long observationDomainId, final Template template) {
            final Key key = new Key(this.remoteAddress, this.localAddress, observationDomainId, template.id);
            UdpSessionManager.this.templates.put(key, new TemplateWrapper(template));
        }

        @Override
        public void removeTemplate(final long observationDomainId, final int templateId) {
            final Key key = new Key(this.remoteAddress, this.localAddress, observationDomainId, templateId);
            UdpSessionManager.this.templates.remove(key);
        }

        @Override
        public void removeAllTemplate(final long observationDomainId, final Template.Type type) {
            UdpSessionManager.this.templates.entrySet().removeIf(e -> e.getKey().observationDomainId == observationDomainId && e.getValue().template.type == type);
        }

        @Override
        public void addOptions(final long observationDomainId,
                               final int templateId,
                               final Collection<Value<?>> scopes,
                               final List<Value<?>> values) {
            if (scopes.isEmpty()) {
                return;
            }

            final Key key = new Key(this.remoteAddress, this.localAddress, observationDomainId, templateId);
            UdpSessionManager.this.options.computeIfAbsent(key, (k) -> new HashMap()).put(new HashSet(scopes), values);
        }

        @Override
        public Session.Resolver getResolver(final long observationDomainId) {
            return new Resolver(observationDomainId);
        }
    }

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

    private final static class TemplateWrapper {
        public final Instant insertionTime;
        public final Template template;

        private TemplateWrapper(final Template template) {
            this.insertionTime = Instant.now();
            this.template = template;
        }
    }

    private final Map<Key, TemplateWrapper> templates = Maps.newHashMap();
    private final Map<Key, Map<Set<Value<?>>, List<Value<?>>>> options = Maps.newHashMap();

    private final Duration timeout;

    public UdpSessionManager(final Duration timeout) {
        this.timeout = timeout;
    }

    public void doHousekeeping() {
        final Instant timeout = Instant.now().minus(this.timeout);
        UdpSessionManager.this.templates.entrySet().removeIf(e -> e.getValue().insertionTime.isBefore(timeout));
    }

    public Session getSession(final InetSocketAddress remoteAddress, final InetSocketAddress localAddress) {
        return new UdpSession(remoteAddress, localAddress);
    }

    public void drop(final InetSocketAddress remoteAddress, final InetSocketAddress localAddress) {
        this.templates.entrySet().removeIf(e -> Objects.equals(e.getKey().remoteAddress, remoteAddress) && Objects.equals(e.getKey().localAddress, localAddress));
    }
}
