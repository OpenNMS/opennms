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

package org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.DateTimeValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.FloatValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.IPv4AddressValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.ListValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.MacAddressValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.SignedValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.StringValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.UnsignedValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.BooleanValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.IPv6AddressValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.OctetArrayValue;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;

import au.com.bytecode.opencsv.CSVReader;

public class InformationElementDatabase {

    @FunctionalInterface
    private interface ValueParserFactory {
        Value.Parser parser(final String name);
    }

    private static final Map<String, ValueParserFactory> TYPE_LOOKUP = ImmutableMap.<String, ValueParserFactory>builder()
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

    private static final String COLUMN_ID = "ElementID";
    private static final String COLUMN_NAME = "Name";
    private static final String COLUMN_TYPE = "Abstract Data Type";
    private static final String COLUMN_SEMANTICS = "Data Type Semantics";

    public static final InformationElementDatabase instance = new InformationElementDatabase();

    private final ImmutableMap<Integer, InformationElement> elements;

    private InformationElementDatabase() {
        // TODO: This is ugly as f**k

        try (final CSVReader reader = new CSVReader(new InputStreamReader(this.getClass().getResourceAsStream("/ipfix-information-elements.csv")),
                ',', '"', '\\', 0, false)) {

            // Read header line and find indices
            final List<String> columns = Arrays.asList(reader.readNext());
            final int indexOfId = columns.indexOf(COLUMN_ID);
            final int indexOfName = columns.indexOf(COLUMN_NAME);
            final int indexOfType = columns.indexOf(COLUMN_TYPE);
            final int indexOfSemantics = columns.indexOf(COLUMN_SEMANTICS);

            // Read lines
            // TODO Something better than this? TreeMap? Array?
            final ImmutableMap.Builder<Integer, InformationElement> elements = ImmutableMap.builder();
            for (String line[] = reader.readNext();
                 line != null;
                 line = reader.readNext()) {
                final int id;
                try {
                    id = Integer.valueOf(line[indexOfId]);
                } catch (final NumberFormatException e) {
                    // TODO: Log me
                    continue;
                }

                final String name = line[indexOfName];
                final ValueParserFactory valueParserFactory = TYPE_LOOKUP.get(line[indexOfType]);

                if (valueParserFactory == null) {
                    // TODO: Log me
                    continue;
                }

                final Value.Parser type = valueParserFactory.parser(name);
                final Optional<Semantics> semantics = Optional.ofNullable(SEMANTICS_LOOKUP.get(line[indexOfSemantics]));


                elements.put(id, new InformationElement(id, name, type, semantics));
            }
            this.elements = elements.build();

        } catch (final IOException e) {
            // TODO: Log me
            throw Throwables.propagate(e);
        }
    }

    public Optional<InformationElement> lookup(final int id) {
        return Optional.ofNullable(this.elements.get(id));
    }
}
