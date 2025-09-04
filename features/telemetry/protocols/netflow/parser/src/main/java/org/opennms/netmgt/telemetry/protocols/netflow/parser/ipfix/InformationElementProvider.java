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
package org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.Protocol;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.InformationElementDatabase;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Semantics;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.BooleanValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.DateTimeValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.FloatValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.IPv4AddressValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.IPv6AddressValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.ListValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.MacAddressValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.OctetArrayValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.SignedValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.StringValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.UnsignedValue;

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

    static final Map<String, InformationElementDatabase.ValueParserFactory> TYPE_LOOKUP = ImmutableMap.<String, InformationElementDatabase.ValueParserFactory>builder()
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

    private InformationElementDatabase database;

    @Override
    public InformationElementDatabase getDatabase() {
        return database;
    }

    @Override
    public void setDatabase(InformationElementDatabase database) {
        this.database = database;
    }

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

                adder.add(Protocol.IPFIX, id, valueParserFactory, name, semantics, this.database);
            }
        } catch (final IOException e) {
            // TODO: Log me
            throw Throwables.propagate(e);
        }
    }
}
