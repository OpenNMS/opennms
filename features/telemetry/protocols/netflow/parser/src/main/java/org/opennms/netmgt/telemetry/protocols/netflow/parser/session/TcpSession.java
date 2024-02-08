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
package org.opennms.netmgt.telemetry.protocols.netflow.parser.session;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.MissingTemplateException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.state.ExporterState;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.state.OptionState;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.state.TemplateState;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class TcpSession implements Session {
    private final class Resolver implements Session.Resolver {
        private final long observationDomainId;

        private Resolver(final long observationDomainId) {
            this.observationDomainId = observationDomainId;
        }

        @Override
        public Template lookupTemplate(final int templateId) throws MissingTemplateException {
            final TemplateKey key = new TemplateKey(this.observationDomainId, templateId);

            final Template template = TcpSession.this.templates.get(key);
            if (template != null) {
                return template;
            } else {
                throw new MissingTemplateException(templateId);
            }
        }

        @Override
        public List<Value<?>> lookupOptions(final List<Value<?>> values) {
            final LinkedHashMap<String, Value<?>> options = new LinkedHashMap<>();

            final Set<String> scoped = values.stream().map(Value::getName).collect(Collectors.toSet());

            for (final Map.Entry<TemplateKey, Map<Set<Value<?>>, List<Value<?>>>> e : Iterables.filter(TcpSession.this.options.entrySet(),
                                                                                               e -> e.getKey().observationDomainId == this.observationDomainId)) {
                final Template template = TcpSession.this.templates.get(e.getKey());

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

            return new ArrayList<>(options.values());
        }
    }

    private final static class TemplateKey {
        public final long observationDomainId;
        public final int templateId;

        TemplateKey(final long observationDomainId,
                    final int templateId) {
            this.observationDomainId = observationDomainId;
            this.templateId = templateId;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null) return false;
            if (o.getClass() != TemplateKey.class) return false;

            final TemplateKey that = (TemplateKey) o;
            return this.observationDomainId == that.observationDomainId &&
                    this.templateId == that.templateId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.observationDomainId, this.templateId);
        }
    }

    private final InetAddress remoteAddress;
    private final Map<TemplateKey, Template> templates = Maps.newHashMap();
    private final Map<TemplateKey, Map<Set<Value<?>>, List<Value<?>>>> options = Maps.newHashMap();
    private final Map<Long, SequenceNumberTracker> sequenceNumbers = Maps.newHashMap();

    private final Supplier<SequenceNumberTracker> sequenceNumberTracker;

    public TcpSession(final InetAddress remoteAddress, final Supplier<SequenceNumberTracker> sequenceNumberTracker) {
        this.remoteAddress = Objects.requireNonNull(remoteAddress);
        this.sequenceNumberTracker = Objects.requireNonNull(sequenceNumberTracker);
    }

    @Override
    public void addTemplate(final long observationDomainId, final Template template) {
        this.templates.put(new TemplateKey(observationDomainId, template.id), template);
    }

    @Override
    public void removeTemplate(final long observationDomainId, final int templateId) {
        this.templates.remove(new TemplateKey(observationDomainId, templateId));
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
        final TemplateKey key = new TemplateKey(observationDomainId, templateId);
        this.options.computeIfAbsent(key, (k) -> new HashMap<>()).put(new HashSet<>(scopes), values);
    }

    @Override
    public Session.Resolver getResolver(final long observationDomainId) {
        return new Resolver(observationDomainId);
    }

    @Override
    public InetAddress getRemoteAddress() {
        return this.remoteAddress;
    }

    @Override
    public boolean verifySequenceNumber(long observationDomainId, final long sequenceNumber) {
        final SequenceNumberTracker tracker = this.sequenceNumbers.computeIfAbsent(observationDomainId, (k) -> this.sequenceNumberTracker.get());
        return tracker.verify(sequenceNumber);
    }

    public Stream<ExporterState> dumpInternalState() {
        return this.templates.keySet().stream()
                             .mapToLong(key -> key.observationDomainId)
                             .distinct()
                             .mapToObj(domain -> {
                                 final String key = String.format("%s#%s", InetAddressUtils.str(this.remoteAddress), domain);

                                 final ExporterState.Builder exporter = ExporterState.builder(key);

                                 this.templates.entrySet().stream()
                                               .filter(e -> Objects.equals(e.getKey().observationDomainId, domain))
                                               .forEach(e -> exporter.withTemplate(TemplateState.builder(e.getKey().templateId)));

                                 this.options.entrySet().stream()
                                             .filter(e -> Objects.equals(e.getKey().observationDomainId, domain))
                                             .forEach(e -> e.getValue().forEach((selectors, values) ->
                                                                                        exporter.withOptions(OptionState.builder(e.getKey().templateId)
                                                                                                                        .withSelectors(selectors)
                                                                                                                        .withValues(values))));

                                 return exporter.build();
                             });
    }
}
