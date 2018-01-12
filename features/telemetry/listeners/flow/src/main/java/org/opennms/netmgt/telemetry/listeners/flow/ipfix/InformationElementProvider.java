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

package org.opennms.netmgt.telemetry.listeners.flow.ipfix;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.opennms.netmgt.telemetry.listeners.flow.Protocol;
import org.opennms.netmgt.telemetry.listeners.flow.ie.InformationElementDatabase;
import org.opennms.netmgt.telemetry.listeners.flow.ie.Semantics;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.BooleanValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.DateTimeValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.FloatValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.IPv4AddressValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.IPv6AddressValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.ListValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.MacAddressValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.OctetArrayValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.SignedValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.StringValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.UnsignedValue;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;

public class InformationElementProvider implements InformationElementDatabase.Provider {
    private static final String COLUMN_ID = "ElementID";
    private static final String COLUMN_NAME = "Name";
    private static final String COLUMN_TYPE = "Abstract Data Type";
    private static final String COLUMN_SEMANTICS = "Data Type Semantics";

    private static final CSVFormat CSV_FORMAT = CSVFormat.newFormat(',').withQuote('"').withEscape('\\').withFirstRecordAsHeader();

    private static final Map<String, Semantics> SEMANTICS_LOOKUP = ImmutableMap.<String, Semantics>builder()
            .put("default", Semantics.DEFAULT)
            .put("quantity", Semantics.QUANTITY)
            .put("totalCounter", Semantics.TOTAL_COUNTER)
            .put("deltaCounter", Semantics.DELTA_COUNTER)
            .put("identifier", Semantics.IDENTIFIER)
            .put("flags", Semantics.FLAGS)
            .put("list", Semantics.LIST)
            .put("snmpCounter", Semantics.SNMP_COUNTER)
            .put("snmpGauge", Semantics.SNMP_GAUGE)
            .build();

    private static final Map<String, InformationElementDatabase.ValueParserFactory> TYPE_LOOKUP = ImmutableMap.<String, InformationElementDatabase.ValueParserFactory>builder()
            .put("octetArray", OctetArrayValue::parser)
            .put("unsigned8", UnsignedValue::parserWith8Bit)
            .put("unsigned16", UnsignedValue::parserWith16Bit)
            .put("unsigned32", UnsignedValue::parserWith32Bit)
            .put("unsigned64", UnsignedValue::parserWith64Bit)
            .put("signed8", SignedValue::parserWith8Bit)
            .put("signed16", SignedValue::parserWith16Bit)
            .put("signed32", SignedValue::parserWith32Bit)
            .put("signed64", SignedValue::parserWith64Bit)
            .put("float32", FloatValue::parserWith32Bit)
            .put("float64", FloatValue::parserWith64Bit)
            .put("boolean", BooleanValue::parser)
            .put("macAddress", MacAddressValue::parser)
            .put("string", StringValue::parser)
            .put("dateTimeSeconds", DateTimeValue::parserWithSeconds)
            .put("dateTimeMilliseconds", DateTimeValue::parserWithMilliseconds)
            .put("dateTimeMicroseconds", DateTimeValue::parserWithMicroseconds)
            .put("dateTimeNanoseconds", DateTimeValue::parserWithNanoseconds)
            .put("ipv4Address", IPv4AddressValue::parser)
            .put("ipv6Address", IPv6AddressValue::parser)
            .put("basicList", ListValue::parserWithBasicList)
            .put("subTemplateList", ListValue::parserWithSubTemplateList)
            .put("subTemplateMultiList", ListValue::parserWithSubTemplateMultiList)
            .build();

    @Override
    public void load(final InformationElementDatabase.Adder adder) {
        try (final Reader r = new InputStreamReader(this.getClass().getResourceAsStream("/ipfix-information-elements.csv"))) {
            for (final CSVRecord record : CSV_FORMAT.parse(r)) {
                final int id;
                try {
                    id = Integer.valueOf(record.get(COLUMN_ID));
                } catch (final NumberFormatException e) {
                    continue;
                }

                final String name = record.get(COLUMN_NAME);
                final InformationElementDatabase.ValueParserFactory valueParserFactory = TYPE_LOOKUP.get(record.get(COLUMN_TYPE));

                if (valueParserFactory == null) {
                    // TODO: Log me
                    continue;
                }

                final Optional<Semantics> semantics = Optional.ofNullable(SEMANTICS_LOOKUP.get(record.get(COLUMN_SEMANTICS)));

                adder.add(Protocol.IPFIX, id, valueParserFactory, name, semantics);
            }
        } catch (final IOException e) {
            // TODO: Log me
            throw Throwables.propagate(e);
        }
    }
}
