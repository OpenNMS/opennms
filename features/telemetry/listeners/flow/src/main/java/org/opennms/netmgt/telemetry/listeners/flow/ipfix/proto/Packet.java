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

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.telemetry.listeners.flow.ipfix.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.session.TemplateManager;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.session.Template;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

public final class Packet implements Iterable<Set<?>> {

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
    public final List<Set<?>> sets;

    public Packet(final TemplateManager templateManager,
                  final Header header,
                  final ByteBuffer buffer) throws InvalidPacketException {
        this.header = Objects.requireNonNull(header);

        final List<Set<?>> sets = new LinkedList<>();
        while (buffer.hasRemaining()) {
            final SetHeader setHeader = new SetHeader(buffer);

            final ByteBuffer payloadBuffer = BufferUtils.slice(buffer, setHeader.length - SetHeader.SIZE);
            final Set<?> set;
            switch (setHeader.getType()) {
                case TEMPLATE_SET: {
                    final Set<TemplateRecord> templateSet = new Set<>(setHeader, TemplateRecord.parser(), payloadBuffer);

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

                case OPTIONS_TEMPLATE_SET: {
                    final Set<OptionsTemplateRecord> optionsTemplateSet = new Set<>(setHeader, OptionsTemplateRecord.parser(), payloadBuffer);

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
                                    record.header.templateId,
                                    Template.builder()
                                    .withType(Template.Type.OPTIONS_TEMPLATE)
                                    .withScopeFieldsCount(record.header.scopeFieldCount)
                                    .withFields(Lists.transform(record.fields, f -> f.specifier))
                                    .build());
                        }
                    }

                    set = optionsTemplateSet;
                    break;
                }

                case DATA_SET: {
                    set = new Set<>(setHeader, DataRecord.parser(templateManager.getResolver(header.observationDomainId), setHeader.setId), payloadBuffer);
                    break;
                }

                default: {
                    throw new InvalidPacketException("Invalid Set ID: %d", setHeader.setId);
                }
            }

            sets.add(set);
        }
        this.sets = Collections.unmodifiableList(sets);
    }

    @Override
    public Iterator<Set<?>> iterator() {
        return this.sets.iterator();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("header", header)
                .add("sets", sets)
                .toString();
    }
}
