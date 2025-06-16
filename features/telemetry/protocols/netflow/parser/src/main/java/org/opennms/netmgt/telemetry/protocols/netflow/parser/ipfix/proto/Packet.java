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
package org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.proto;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.slice;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.MissingTemplateException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.RecordProvider;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.UnsignedValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Session;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import io.netty.buffer.ByteBuf;

public final class Packet implements Iterable<FlowSet<?>>, RecordProvider {
    private static final Logger LOG = LoggerFactory.getLogger(Packet.class);

    /*
     +----------------------------------------------------+
     | Message Header                                     |
     +----------------------------------------------------+
     | Set                                                |
     +----------------------------------------------------+
     | Set                                                |
     +----------------------------------------------------+
      ...
     +----------------------------------------------------+
     | Set                                                |
     +----------------------------------------------------+
    */

    public final Header header;

    public final List<TemplateSet> templateSets;
    public final List<OptionsTemplateSet> optionTemplateSets;
    public final List<DataSet> dataSets;

    public Packet(final Session session,
                  final Header header,
                  final ByteBuf buffer) throws InvalidPacketException {
        this.header = Objects.requireNonNull(header);

        final List<TemplateSet> templateSets = new LinkedList();
        final List<OptionsTemplateSet> optionTemplateSets = new LinkedList();
        final List<DataSet> dataSets = new LinkedList();

        while (buffer.isReadable()) {
            final ByteBuf headerBuffer = slice(buffer, FlowSetHeader.SIZE);
            final FlowSetHeader setHeader = new FlowSetHeader(headerBuffer);

            final ByteBuf payloadBuffer = slice(buffer, setHeader.length - FlowSetHeader.SIZE);
            switch (setHeader.getType()) {
                case TEMPLATE_SET: {
                    final TemplateSet templateSet = new TemplateSet(this, setHeader, payloadBuffer);

                    for (final TemplateRecord record : templateSet) {
                        if (record.header.fieldCount == 0) {
                            // Empty template means revocation
                            if (record.header.templateId == FlowSetHeader.TEMPLATE_SET_ID) {
                                // Remove all templates
                                session.removeAllTemplate(this.header.observationDomainId, Template.Type.TEMPLATE);

                            } else if (record.header.fieldCount == 0) {
                                // Empty template means revocation
                                session.removeTemplate(this.header.observationDomainId, record.header.templateId);
                            }

                        } else {
                            session.addTemplate(this.header.observationDomainId,
                                    Template.builder(record.header.templateId, Template.Type.TEMPLATE)
                                            .withFields(record.fields)
                                            .build());
                        }
                    }

                    templateSets.add(templateSet);
                    break;
                }

                case OPTIONS_TEMPLATE_SET: {
                    final OptionsTemplateSet optionsTemplateSet = new OptionsTemplateSet(this, setHeader, payloadBuffer);

                    for (final OptionsTemplateRecord record : optionsTemplateSet) {
                        if (record.header.fieldCount == 0) {
                            // Empty template means revocation
                            if (record.header.templateId == FlowSetHeader.OPTIONS_TEMPLATE_SET_ID) {
                                // Remove all templates
                                session.removeAllTemplate(this.header.observationDomainId, Template.Type.OPTIONS_TEMPLATE);

                            } else if (record.header.fieldCount == 0) {
                                // Empty template means revocation
                                session.removeTemplate(this.header.observationDomainId, record.header.templateId);
                            }

                        } else {
                            session.addTemplate(this.header.observationDomainId,
                                    Template.builder(record.header.templateId, Template.Type.OPTIONS_TEMPLATE)
                                            .withScopes(record.scopes)
                                            .withFields(record.fields)
                                            .build());
                        }
                    }

                    optionTemplateSets.add(optionsTemplateSet);
                    break;
                }

                case DATA_SET: {
                    final Session.Resolver resolver = session.getResolver(header.observationDomainId);

                    final DataSet dataSet;
                    try {
                        dataSet = new DataSet(this, setHeader, resolver, payloadBuffer);
                    } catch (final MissingTemplateException ex) {
                        LOG.debug("Skipping data-set due to missing template: {}", ex.getMessage());
                        break;
                    }

                    if (dataSet.template.type == Template.Type.OPTIONS_TEMPLATE) {
                        for (final DataRecord record : dataSet) {
                            session.addOptions(this.header.observationDomainId, dataSet.template.id, record.scopes, record.fields);
                        }
                    } else {
                        dataSets.add(dataSet);
                    }

                    break;
                }

                default: {
                    throw new InvalidPacketException(buffer, "Invalid Set ID: %d", setHeader.setId);
                }
            }
        }

        this.templateSets = Collections.unmodifiableList(templateSets);
        this.optionTemplateSets = Collections.unmodifiableList(optionTemplateSets);
        this.dataSets = Collections.unmodifiableList(dataSets);
    }

    @Override
    public Iterator<FlowSet<?>> iterator() {
        return Iterators.concat(this.templateSets.iterator(),
                                this.optionTemplateSets.iterator(),
                                this.dataSets.iterator());
    }

    @Override
    public Stream<Iterable<Value<?>>> getRecords() {
        final int recordCount = this.dataSets.stream()
                .mapToInt(s -> s.records.size())
                .sum();

        return this.dataSets.stream()
                .flatMap(s -> s.records.stream())
                .map(r -> Iterables.concat(
                        ImmutableList.of(
                                new UnsignedValue("@recordCount", recordCount),
                                new UnsignedValue("@sequenceNumber", this.header.sequenceNumber),
                                new UnsignedValue("@exportTime", this.header.exportTime),
                                new UnsignedValue("@observationDomainId", this.header.observationDomainId)),
                        r.fields,
                        r.options
                ));
    }

    @Override
    public long getObservationDomainId() {
        return this.header.observationDomainId;
    }

    @Override
    public long getSequenceNumber() {
        return this.header.sequenceNumber;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("header", this.header)
                .add("templateSets", this.templateSets)
                .add("optionTemplateSets", this.optionTemplateSets)
                .add("dataTemplateSets", this.dataSets)
                .toString();
    }
}
