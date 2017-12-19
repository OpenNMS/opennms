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

package org.opennms.netmgt.telemetry.listeners.flow.netflow9.proto;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.opennms.netmgt.telemetry.listeners.flow.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.flow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.flow.ie.RecordProvider;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto.SetHeader;
import org.opennms.netmgt.telemetry.listeners.flow.session.Template;
import org.opennms.netmgt.telemetry.listeners.flow.session.TemplateManager;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public final class Packet implements Iterable<FlowSet<?>>, RecordProvider {

    /*
     +--------+-------------------------------------------+
     |        | +----------+ +---------+ +----------+     |
     | Packet | | Template | | Data    | | Options  |     |
     | Header | | FlowSet  | | FlowSet | | Template | ... |
     |        | |          | |         | | FlowSet  |     |
     |        | +----------+ +---------+ +----------+     |
     +--------+-------------------------------------------+
    */

    public final Header header;
    public final List<FlowSet<?>> sets;

    public Packet(final TemplateManager templateManager,
                  final Header header,
                  final ByteBuffer buffer) throws InvalidPacketException {
        this.header = Objects.requireNonNull(header);

        final List<FlowSet<?>> sets = new LinkedList<>();
        while(buffer.hasRemaining()) {
            // We ignore header.counter here, because different exporters interpret it as flowset count or record count

            final ByteBuffer headerBuffer = BufferUtils.slice(buffer, SetHeader.SIZE);
            final FlowSetHeader flowSetHeader = new FlowSetHeader(headerBuffer);

            final ByteBuffer payloadBuffer = BufferUtils.slice(buffer, flowSetHeader.length - FlowSetHeader.SIZE);
            final FlowSet<?> set;
            switch (flowSetHeader.getType()) {
                case TEMPLATE_FLOWSET: {
                    final FlowSet<TemplateRecord> templateSet = new FlowSet<>(flowSetHeader, TemplateRecord.parser(), payloadBuffer);

                    for (final TemplateRecord record : templateSet) {
                        if (record.header.fieldCount == 0) {
                            // Empty template means revocation
                            if (record.header.templateId == FlowSetHeader.TEMPLATE_SET_ID) {
                                // Remove all templates
                                templateManager.removeAll(this.header.sourceId, Template.Type.TEMPLATE);

                            } else if (record.header.fieldCount == 0) {
                                // Empty template means revocation
                                templateManager.remove(this.header.sourceId, record.header.templateId);
                            }

                        } else {
                            templateManager.add(this.header.sourceId,
                                    record.header.templateId,
                                    Template.builder()
                                            .withType(Template.Type.TEMPLATE)
                                            .withFields(Lists.transform(record.fields, f -> f.specifier))
                                            .build());
                        }
                    }

                    set = templateSet;
                    break;
                }

                case OPTIONS_TEMPLATE_FLOWSET: {
                    final FlowSet<OptionsTemplateRecord> optionsTemplateSet = new FlowSet<>(flowSetHeader, OptionsTemplateRecord.parser(), payloadBuffer);

                    for (final OptionsTemplateRecord record : optionsTemplateSet) {
                        templateManager.add(this.header.sourceId,
                                record.header.templateId,
                                Template.builder()
                                        .withType(Template.Type.OPTIONS_TEMPLATE)
                                        .withScopeFieldsCount(record.header.optionScopeLength / FieldSpecifier.SIZE)
                                        .withFields(Lists.transform(record.fields, f -> f.specifier))
                                        .build());
                    }

                    set = optionsTemplateSet;
                    break;
                }

                case DATA_FLOWSET: {
                    final TemplateManager.TemplateResolver templateResolver = templateManager.getResolver(header.sourceId);
                    final Template template = templateResolver.lookup(flowSetHeader.flowSetId)
                            .orElseThrow(() -> new InvalidPacketException(buffer, "Unknown Template ID: %d", flowSetHeader.flowSetId));
                    set = new FlowSet<>(flowSetHeader, DataRecord.parser(template, templateResolver), payloadBuffer);
                    break;
                }

                default: {
                    throw new InvalidPacketException(buffer, "Invalid Set ID: %d", flowSetHeader.flowSetId);
                }
            }

            sets.add(set);
        }
        this.sets = Collections.unmodifiableList(sets);
    }

    @Override
    public Iterator<FlowSet<?>> iterator() {
        return this.sets.iterator();
    }

    @Override
    public Stream<RecordProvider.Record> getRecords() {
        return this.sets.stream()
                .filter(s -> s.header.getType() == FlowSetHeader.Type.DATA_FLOWSET)
                .flatMap(s -> ((FlowSet<DataRecord>) s).records.stream())
                .map(r -> new RecordProvider.Record(this.header.sourceId, this.header.unixSecs, r.template.scopeFieldsCount, Iterables.transform(r.fields, f -> f.value)));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("header", header)
                .add("sets", sets)
                .toString();
    }
}
