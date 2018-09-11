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

package org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.proto;

import static org.opennms.netmgt.telemetry.common.utils.BufferUtils.slice;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.RecordProvider;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.UnsignedValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Session;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Template;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

public final class Packet implements Iterable<FlowSet<?>>, RecordProvider {

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
                  final ByteBuffer buffer) throws InvalidPacketException {
        this.header = Objects.requireNonNull(header);

        final List<TemplateSet> templateSets = new LinkedList();
        final List<OptionsTemplateSet> optionTemplateSets = new LinkedList();
        final List<DataSet> dataSets = new LinkedList();

        while (buffer.hasRemaining()) {
            final ByteBuffer headerBuffer = slice(buffer, FlowSetHeader.SIZE);
            final FlowSetHeader setHeader = new FlowSetHeader(headerBuffer);

            final ByteBuffer payloadBuffer = slice(buffer, setHeader.length - FlowSetHeader.SIZE);
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
                    final DataSet dataSet = new DataSet(this, setHeader, resolver, payloadBuffer);

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
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("header", this.header)
                .add("templateSets", this.templateSets)
                .add("optionTemplateSets", this.optionTemplateSets)
                .add("dataTemplateSets", this.dataSets)
                .toString();
    }
}
