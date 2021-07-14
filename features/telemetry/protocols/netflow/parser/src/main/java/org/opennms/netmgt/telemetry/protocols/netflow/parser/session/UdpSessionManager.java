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

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.MissingTemplateException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.state.ExporterState;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.state.OptionState;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.state.ParserState;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.state.TemplateState;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class UdpSessionManager {

    private final Map<TemplateKey, TimeWrapper<Template>> templates = Maps.newHashMap();
    private final Map<TemplateKey, Map<Set<Value<?>>, TimeWrapper<List<Value<?>>>>> options = Maps.newHashMap();
    private final Map<DomainKey, SequenceNumberTracker> sequenceNumbers = Maps.newHashMap();
    private final Duration timeout;
    private final Supplier<SequenceNumberTracker> sequenceNumberTracker;

    public UdpSessionManager(final Duration timeout, final Supplier<SequenceNumberTracker> sequenceNumberTracker) {
        this.timeout = timeout;
        this.sequenceNumberTracker = Objects.requireNonNull(sequenceNumberTracker);
    }

    public void doHousekeeping() {
        final Instant timeout = Instant.now().minus(this.timeout);
        UdpSessionManager.this.templates.entrySet().removeIf(e -> e.getValue().time.isBefore(timeout));
    }

    public Session getSession(final SessionKey sessionKey) {
        return new UdpSession(sessionKey);
    }

    public void drop(final SessionKey sessionKey) {
        this.templates.entrySet().removeIf(e -> Objects.equals(e.getKey().observationDomainId.sessionKey, sessionKey));
    }

    public int count() {
        return this.templates.size();
    }

    public Object dumpInternalState() {
        final ParserState.Builder parser = ParserState.builder();

        final Set<DomainKey> domains = this.templates.keySet().stream()
                                                     .map(key -> key.observationDomainId)
                                                     .collect(Collectors.toSet());

        domains.forEach(domain -> {
            final String key = String.format("%s#%s", domain.sessionKey.getDescription(), domain.observationDomainId);

            final ExporterState.Builder exporter = ExporterState.builder(key);

            this.templates.entrySet().stream()
                          .filter(e -> Objects.equals(e.getKey().observationDomainId, domain))
                          .forEach(e -> exporter.withTemplate(TemplateState.builder(e.getKey().templateId)
                                                                           .withInsertionTime(e.getValue().time)));

            this.options.entrySet().stream()
                        .filter(e -> Objects.equals(e.getKey().observationDomainId, domain))
                        .forEach(e -> e.getValue().forEach((selectors, values) ->
                                                                   exporter.withOptions(OptionState.builder(e.getKey().templateId)
                                                                                                   .withInsertionTime(values.time)
                                                                                                   .withSelectors(selectors)
                                                                                                   .withValues(values.wrapped))));

            parser.withExporter(exporter);
        });

        return parser.build();
    }

    public interface SessionKey {
        String getDescription();

        InetAddress getRemoteAddress();
    }

    private final static class DomainKey {
        public final SessionKey sessionKey;
        public final long observationDomainId;

        private DomainKey(final SessionKey sessionKey,
                          final long observationDomainId) {
            this.sessionKey = Objects.requireNonNull(sessionKey);
            this.observationDomainId = observationDomainId;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof DomainKey)) {
                return false;
            }

            final DomainKey that = (DomainKey) o;
            return Objects.equals(this.observationDomainId, that.observationDomainId) &&
                   Objects.equals(this.sessionKey, that.sessionKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.sessionKey, this.observationDomainId);
        }
    }

    private final static class TemplateKey {
        public final DomainKey observationDomainId;
        public final int templateId;

        TemplateKey(final SessionKey sessionKey,
                    final long observationDomainId,
                    final int templateId) {
            this.observationDomainId = new DomainKey(sessionKey, observationDomainId);
            this.templateId = templateId;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TemplateKey)) {
                return false;
            }

            final TemplateKey that = (TemplateKey) o;
            return Objects.equals(this.observationDomainId, that.observationDomainId) &&
                   Objects.equals(this.templateId, that.templateId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.observationDomainId, this.templateId);
        }
    }

    private final static class TimeWrapper<T> {
        public final Instant time;
        public final T wrapped;

        private TimeWrapper(final T wrapped) {
            this.time = Instant.now();
            this.wrapped = wrapped;
        }
    }

    private final class UdpSession implements Session {
        private final SessionKey sessionKey;

        public UdpSession(final SessionKey sessionKey) {
            this.sessionKey = sessionKey;
        }

        @Override
        public void addTemplate(final long observationDomainId, final Template template) {
            final TemplateKey key = new TemplateKey(this.sessionKey, observationDomainId, template.id);
            UdpSessionManager.this.templates.put(key, new TimeWrapper<>(template));
        }

        @Override
        public void removeTemplate(final long observationDomainId, final int templateId) {
            final TemplateKey key = new TemplateKey(this.sessionKey, observationDomainId, templateId);
            UdpSessionManager.this.templates.remove(key);
        }

        @Override
        public void removeAllTemplate(final long observationDomainId, final Template.Type type) {
            UdpSessionManager.this.templates.entrySet().removeIf(e -> e.getKey().observationDomainId.observationDomainId == observationDomainId && e.getValue().wrapped.type == type);
        }

        @Override
        public void addOptions(final long observationDomainId,
                               final int templateId,
                               final Collection<Value<?>> scopes,
                               final List<Value<?>> values) {
            final TemplateKey key = new TemplateKey(this.sessionKey, observationDomainId, templateId);
            UdpSessionManager.this.options.computeIfAbsent(key, (k) -> new HashMap<>()).put(new HashSet<>(scopes), new TimeWrapper<>(values));
        }

        @Override
        public Session.Resolver getResolver(final long observationDomainId) {
            return new Resolver(observationDomainId);
        }

        @Override
        public InetAddress getRemoteAddress() {
            return this.sessionKey.getRemoteAddress();
        }

        @Override
        public boolean verifySequenceNumber(final long observationDomainId, final long sequenceNumber) {
            final DomainKey key = new DomainKey(this.sessionKey, observationDomainId);
            final SequenceNumberTracker tracker = UdpSessionManager.this.sequenceNumbers.computeIfAbsent(key, (k) -> UdpSessionManager.this.sequenceNumberTracker.get());
            return tracker.verify(sequenceNumber);
        }

        private final class Resolver implements Session.Resolver {
            private final long observationDomainId;

            private Resolver(final long observationDomainId) {
                this.observationDomainId = observationDomainId;
            }

            private TemplateKey key(final int templateId) {
                return new TemplateKey(UdpSession.this.sessionKey, this.observationDomainId, templateId);
            }

            @Override
            public Template lookupTemplate(final int templateId) throws MissingTemplateException {
                final TimeWrapper<Template> templateWrapper = UdpSessionManager.this.templates.get(key(templateId));
                if (templateWrapper != null) {
                    return templateWrapper.wrapped;
                } else {
                    throw new MissingTemplateException(templateId);
                }
            }

            @Override
            public List<Value<?>> lookupOptions(final List<Value<?>> values) {
                final LinkedHashMap<String, Value<?>> options = new LinkedHashMap<>();

                final Set<String> scoped = values.stream().map(Value::getName).collect(Collectors.toSet());

                for (final Map.Entry<TemplateKey, Map<Set<Value<?>>, TimeWrapper<List<Value<?>>>>> e : Iterables.filter(UdpSessionManager.this.options.entrySet(),
                                                                                                                        e -> Objects.equals(e.getKey().observationDomainId.sessionKey, UdpSession.this.sessionKey) &&
                                                                                                                             Objects.equals(e.getKey().observationDomainId.observationDomainId, this.observationDomainId))) {
                    final Template template = UdpSessionManager.this.templates.get(e.getKey()).wrapped;

                    if (scoped.containsAll(template.scopeNames)) {
                        // Found option template where scoped fields is subset of actual data fields

                        final Set<Value<?>> scopeValues = values.stream()
                                                                .filter(s -> template.scopeNames.contains(s.getName()))
                                                                .collect(Collectors.toSet());

                        final TimeWrapper<List<Value<?>>> optionValues = e.getValue().get(scopeValues);
                        if (optionValues != null) {
                            for (final Value<?> value : optionValues.wrapped) {
                                options.put(value.getName(), value);
                            }
                        }
                    }
                }

                return new ArrayList<>(options.values());
            }
        }
    }

}
