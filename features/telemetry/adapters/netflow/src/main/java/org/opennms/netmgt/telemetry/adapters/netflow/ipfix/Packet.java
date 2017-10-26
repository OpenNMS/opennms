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

package org.opennms.netmgt.telemetry.adapters.netflow.ipfix;

import static org.opennms.netmgt.telemetry.adapters.netflow.ipfix.BufferUtils.slice;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.InformationElement;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.InformationElementDatabase;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.session.Session;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.session.Template;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

public final class Packet {

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
    public final List<Set> sets;

    Packet(final Session session,
           final Header header,
           final ByteBuffer buffer) throws InvalidPacketException {
        this.header = Objects.requireNonNull(header);

        final List<Set> sets = new LinkedList<>();
        while (buffer.hasRemaining()) {
            final ByteBuffer headerBuffer = slice(buffer, SetHeader.SIZE);
            final SetHeader setHeader = new SetHeader(headerBuffer);

            final ByteBuffer payloadBuffer = slice(buffer, setHeader.length - SetHeader.SIZE);
            final Set<?> set;

            switch (setHeader.getType()) {
                case TEMPLATE_SET: {
                    final Set<TemplateRecord> templateSet = new Set<>(setHeader, TemplateRecord.parser(), payloadBuffer);

                    for (final TemplateRecord record : templateSet) {
                        session.addTemplate(Template.builder()
                                .withObservationDomainId(this.header.observationDomainId)
                                .withTemplateId(record.header.templateId)
                                .withFields(Lists.transform(record.fields, Packet::buildField))
                                .build());
                    }

                    set = templateSet;
                    break;
                }

                case OPTIONS_TEMPLATE_SET: {
                    final Set<OptionsTemplateRecord> optionsTemplateSet = new Set<>(setHeader, OptionsTemplateRecord.parser(), payloadBuffer);

                    for (final OptionsTemplateRecord record : optionsTemplateSet) {
                        session.addTemplate(Template.builder()
                                .withObservationDomainId(this.header.observationDomainId)
                                .withTemplateId(record.header.templateId)
                                .withScopedCount(record.header.scopeFieldCount)
                                .withFields(Lists.transform(record.fields, Packet::buildField))
                                .build());
                    }

                    set = optionsTemplateSet;
                    break;
                }

                case DATA_SET: {
                    final Optional<Template> template = session.findTemplate(this.header.observationDomainId, setHeader.setId);
                    if (!template.isPresent()) {
                        throw new InvalidPacketException("Unknown Template ID: %d", setHeader.setId);
                    }

                    final Set<DataRecord> dataSet = new Set<>(setHeader, DataRecord.parser(template.get()), payloadBuffer);

                    // TODO: Pass to handler

                    set = dataSet;
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
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("header", header)
                .add("sets", sets)
                .toString();
    }

    private static Template.Field buildField(FieldSpecifier field) {
        if (field.enterpriseNumber.isPresent()) {
            return new Template.EnterpriseField(field.fieldLength, field.informationElementId, field.enterpriseNumber.get());

        } else {
            final Optional<InformationElement> informationElement = InformationElementDatabase.instance.lookup(field.informationElementId);
            if (!informationElement.isPresent()) {
                // TODO: Log me
                throw null;
            }

            if (field.fieldLength > informationElement.get().getMaximumFieldLength()) {
                // TODO: Log me
                throw null;
            }

            return new Template.StandardField(field.fieldLength, informationElement.get());
        }
    }

    public static void main(final String... args) throws Exception {
        final DatagramChannel channel = DatagramChannel.open();
        channel.socket().bind(new InetSocketAddress(4739));

        final Map<SocketAddress, Session> sessions = new HashMap<>();

        while (true) {
            final ByteBuffer buffer = ByteBuffer.allocate(0xFFFF);
            final SocketAddress address = channel.receive(buffer);
            buffer.flip();

            final Session session = sessions.computeIfAbsent(address, k -> new Session());

            final ByteBuffer headerBuffer = slice(buffer, Header.SIZE);
            final Header header = new Header(headerBuffer);

            final ByteBuffer payloadBuffer = slice(buffer, header.length - Header.SIZE);
            final Packet packet = new Packet(session, header, payloadBuffer);

            System.out.println(packet);
        }
    }
}
