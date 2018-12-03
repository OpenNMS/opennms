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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class TcpSession implements Session {
    private final class Resolver implements Session.Resolver {
        private final long observationDomainId;

        private Resolver(final long observationDomainId) {
            this.observationDomainId = observationDomainId;
        }

        @Override
        public Optional<Template> lookupTemplate(final int templateId) {
            final Key key = new Key(this.observationDomainId, templateId);
            return Optional.ofNullable(TcpSession.this.templates.get(key));
        }

        @Override
        public List<Value<?>> lookupOptions(final List<Value<?>> values) {
            final LinkedHashMap<String, Value<?>> options = new LinkedHashMap();

            final Set<String> scoped = values.stream().map(Value::getName).collect(Collectors.toSet());

            for (final Map.Entry<Key, Map<Set<Value<?>>, List<Value<?>>>> e : Iterables.filter(TcpSession.this.options.entrySet(),
                                                                                               e -> e.getKey().observationDomainId == this.observationDomainId)) {
                final Template template = this.lookupTemplate(e.getKey().templateId).get();

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

    private final static class Key {
        public final long observationDomainId;
        public final int templateId;

        Key(final long observationDomainId,
            final int templateId) {
            this.observationDomainId = observationDomainId;
            this.templateId = templateId;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o.getClass() != Key.class) return false;

            final Key that = (Key) o;
            return this.observationDomainId == that.observationDomainId &&
                    this.templateId == that.templateId;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.observationDomainId, this.templateId);
        }
    }

    private final Map<Key, Template> templates = Maps.newHashMap();
    private final Map<Key, Map<Set<Value<?>>, List<Value<?>>>> options = Maps.newHashMap();

    public TcpSession() {
    }

    @Override
    public void addTemplate(final long observationDomainId, final Template template) {
        this.templates.put(new Key(observationDomainId, template.id), template);
    }

    @Override
    public void removeTemplate(final long observationDomainId, final int templateId) {
        this.templates.remove(new Key(observationDomainId, templateId));
    }

    @Override
    public void removeAllTemplate(final long observationDomainId, final Template.Type type) {
        this.templates.entrySet().removeIf(e -> e.getKey().observationDomainId == observationDomainId && e.getValue().type == type);
    }

    @Override
    public void addOptions(final long observationDomainId,
                           final int templateId,
                           final Collection<Value<?>> scopes,
                           final List<Value<?>> values) {
        if (scopes.isEmpty()) {
            return;
        }

        final Key key = new Key(observationDomainId, templateId);
        this.options.computeIfAbsent(key, (k) -> new HashMap()).put(new HashSet(scopes), values);
    }

    @Override
    public Session.Resolver getResolver(final long observationDomainId) {
        return new Resolver(observationDomainId);
    }
}
