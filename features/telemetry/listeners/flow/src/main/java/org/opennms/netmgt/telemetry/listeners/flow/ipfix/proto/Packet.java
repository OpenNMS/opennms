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

package org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto;

import static org.opennms.netmgt.telemetry.listeners.flow.BufferUtils.slice;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.opennms.netmgt.telemetry.listeners.flow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.flow.ie.RecordProvider;
import org.opennms.netmgt.telemetry.listeners.flow.session.Template;
import org.opennms.netmgt.telemetry.listeners.flow.session.TemplateManager;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

public final class Packet implements Iterable<Set<?>>, RecordProvider {

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

    public final InetSocketAddress sender;

    public final Header header;

    public final List<TemplateSet> templateSets;
    public final List<OptionsTemplateSet> optionTemplateSets;
    public final List<DataSet> dataSets;

    public Packet(final TemplateManager templateManager,
                  final InetSocketAddress sender,
                  final Header header,
                  final ByteBuffer buffer) throws InvalidPacketException {
        this.sender = Objects.requireNonNull(sender);

        this.header = Objects.requireNonNull(header);

        final List<TemplateSet> templateSets = new LinkedList();
        final List<OptionsTemplateSet> optionTemplateSets = new LinkedList();
        final List<DataSet> dataSets = new LinkedList();

        while (buffer.hasRemaining()) {
            final ByteBuffer headerBuffer = slice(buffer, SetHeader.SIZE);
            final SetHeader setHeader = new SetHeader(headerBuffer);

            final ByteBuffer payloadBuffer = slice(buffer, setHeader.length - SetHeader.SIZE);
            switch (setHeader.getType()) {
                case TEMPLATE_SET: {
                    final TemplateSet templateSet = new TemplateSet(this, setHeader, payloadBuffer);

                    for (final TemplateRecord record : templateSet) {
                        if (record.header.fieldCount == 0) {
                            // Empty template means revocation
                            if (record.header.templateId == SetHeader.TEMPLATE_SET_ID) {
                                // Remove all templates
                                templateManager.removeAll(this.header.observationDomainId, Template.Type.TEMPLATE);

                            } else if (record.header.fieldCount == 0) {
                                // Empty template means revocation
                                templateManager.remove(this.header.observationDomainId, record.header.templateId);
                            }

                        } else {
                            templateManager.add(this.header.observationDomainId,
                                    Template.builder(record.header.templateId)
                                            .withFields(Lists.transform(record.fields, f -> f.specifier))
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
                            if (record.header.templateId == SetHeader.OPTIONS_TEMPLATE_SET_ID) {
                                // Remove all templates
                                templateManager.removeAll(this.header.observationDomainId, Template.Type.OPTIONS_TEMPLATE);

                            } else if (record.header.fieldCount == 0) {
                                // Empty template means revocation
                                templateManager.remove(this.header.observationDomainId, record.header.templateId);
                            }

                        } else {
                            templateManager.add(this.header.observationDomainId,
                                    Template.builder(record.header.templateId)
                                            .withScopeFields(Lists.transform(record.scopes, f -> f.specifier))
                                            .withFields(Lists.transform(record.fields, f -> f.specifier))
                                            .build());
                        }
                    }

                    optionTemplateSets.add(optionsTemplateSet);
                    break;
                }

                case DATA_SET: {
                    final TemplateManager.TemplateResolver templateResolver = templateManager.getResolver(header.observationDomainId);
                    final DataSet dataSet = new DataSet(this, setHeader, templateResolver, payloadBuffer);

                    dataSets.add(dataSet);
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
    public Iterator<Set<?>> iterator() {
        return Iterators.concat(this.templateSets.iterator(),
                                this.optionTemplateSets.iterator(),
                                this.dataSets.iterator());
    }

    @Override
    public Stream<RecordProvider.Record> getRecords() {
        final int recordCount = this.dataSets.stream()
                .mapToInt(s -> s.records.size())
                .sum();

        return this.dataSets.stream()
                .flatMap(s -> s.records.stream())
                .map(r -> new RecordProvider.Record(
                        this.header.observationDomainId,
                        this.header.exportTime,
                        r.template.scopes.size(),
                        recordCount,
                        this.header.sequenceNumber,
                        Iterables.transform(r.fields, f -> f.value)
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
