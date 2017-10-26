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

package org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values.BooleanValue;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values.DateTimeMicrosecondsValue;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values.DateTimeMillisecondsValue;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values.DateTimeNanosecondsValue;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values.Float32Value;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values.Float64Value;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values.OctetArrayValue;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values.Signed16Value;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values.Signed32Value;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values.Signed64Value;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values.Signed8Value;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values.Unsigned16Value;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values.Unsigned32Value;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values.Unsigned64Value;
import org.opennms.netmgt.telemetry.adapters.netflow.ipfix.ie.values.Unsigned8Value;

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
            .put("unsigned8", Unsigned8Value::parser)
            .put("unsigned16", Unsigned16Value::parser)
            .put("unsigned32", Unsigned32Value::parser)
            .put("unsigned64", Unsigned64Value::parser)
            .put("signed8", Signed8Value::parser)
            .put("signed16", Signed16Value::parser)
            .put("signed32", Signed32Value::parser)
            .put("signed64", Signed64Value::parser)
            .put("float32", Float32Value::parser)
            .put("float64", Float64Value::parser)
            .put("boolean", BooleanValue::parser)
//            .put("macAddress", MacAddressValue::parse)
//            .put("string", StringValue::parse)
//            .put("dateTimeSeconds", DateTimeSecondsValue::parse)
            .put("dateTimeMilliseconds", DateTimeMillisecondsValue::parser)
            .put("dateTimeMicroseconds", DateTimeMicrosecondsValue::parser)
            .put("dateTimeNanoseconds", DateTimeNanosecondsValue::parser)
//            .put("ipv4Address", IPv4AddressValue::parse)
//            .put("ipv6Address", IPv6AddressValue::parse)
//            .put("basicList", BasicListValue::parse)
//            .put("subTemplateList", SubTemplateListValue::parse)
//            .put("subTemplateMultiList", SubTemplateMultiListValue::parse)
            .build();

    private static final Map<String, Semantics> SEMANTICS_LOOKUP = ImmutableMap.<String,Semantics>builder()
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
                final Value.Parser type = TYPE_LOOKUP.get(line[indexOfType]).parser(name);
                final Optional<Semantics> semantics = Optional.ofNullable(SEMANTICS_LOOKUP.get(line[indexOfSemantics]));

                if (type == null) {
                    // TODO: Log me
                    continue;
                }

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
