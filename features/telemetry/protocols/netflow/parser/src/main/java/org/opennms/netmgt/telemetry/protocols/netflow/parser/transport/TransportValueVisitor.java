/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2024 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2024 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.telemetry.protocols.netflow.parser.transport;

import java.util.List;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.BooleanValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.DateTimeValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.FloatValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.IPv4AddressValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.IPv6AddressValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.ListValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.MacAddressValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.NullValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.OctetArrayValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.SignedValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.StringValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.UndeclaredValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.UnsignedValue;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.Semantic;

import com.google.protobuf.ByteString;

public class TransportValueVisitor implements Value.Visitor {

    org.opennms.netmgt.telemetry.protocols.netflow.transport.Value.Builder valueBuilder = org.opennms.netmgt.telemetry.protocols.netflow.transport.Value.newBuilder();

    @Override
    public void accept(NullValue value) {
        valueBuilder.setNull(org.opennms.netmgt.telemetry.protocols.netflow.transport.NullValue.newBuilder().build());

    }

    @Override
    public void accept(BooleanValue value) {
        valueBuilder.setName(value.getName());
        valueBuilder.setBoolean(org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue.newBuilder()
                .setBool(com.google.protobuf.BoolValue.newBuilder().setValue(value.getValue()).build())
                .build());
    }

    @Override
    public void accept(DateTimeValue value) {
        valueBuilder.setName(value.getName());
        valueBuilder.setDatetime(org.opennms.netmgt.telemetry.protocols.netflow.transport.DateTimeValue.newBuilder()
                .setUint64(com.google.protobuf.UInt64Value.newBuilder().setValue(value.getValue().toEpochMilli()).build())
                .build());

    }

    @Override
    public void accept(FloatValue value) {
        valueBuilder.setName(value.getName());
        valueBuilder.setFloat(org.opennms.netmgt.telemetry.protocols.netflow.transport.FloatValue.newBuilder()
                .setDouble(com.google.protobuf.DoubleValue.newBuilder().setValue(value.getValue()).build())
                .build());
    }

    @Override
    public void accept(IPv4AddressValue value) {
        valueBuilder.setName(value.getName());
        valueBuilder.setIpv4Address(org.opennms.netmgt.telemetry.protocols.netflow.transport.IPv4AddressValue.newBuilder()
                .setString(com.google.protobuf.StringValue.newBuilder().setValue(InetAddressUtils.str(value.getValue())).build())
                .build());
    }

    @Override
    public void accept(IPv6AddressValue value) {
        valueBuilder.setName(value.getName());
        valueBuilder.setIpv6Address(org.opennms.netmgt.telemetry.protocols.netflow.transport.IPv6AddressValue.newBuilder()
                .setString(com.google.protobuf.StringValue.newBuilder().setValue(InetAddressUtils.str(value.getValue())).build())
                .build());
    }

    @Override
    public void accept(MacAddressValue value) {
        valueBuilder.setName(value.getName());
        valueBuilder.setMacaddress(org.opennms.netmgt.telemetry.protocols.netflow.transport.MacAddressValue.newBuilder()
                .setString(com.google.protobuf.StringValue.newBuilder().setValue(InetAddressUtils.macAddressBytesToString(value.getValue())).build())
                .build());
    }

    @Override
    public void accept(OctetArrayValue value) {
        valueBuilder.setName(value.getName());
        valueBuilder.setOctetarray(org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue.newBuilder()
                .setBytes(com.google.protobuf.BytesValue.newBuilder().setValue(ByteString.copyFrom(value.getValue())).build())
                .build());
    }

    @Override
    public void accept(SignedValue value) {
        valueBuilder.setName(value.getName());
        valueBuilder.setSigned(org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue.newBuilder()
                .setInt64(com.google.protobuf.Int64Value.newBuilder().setValue(value.getValue()).build())
                .build());
    }

    @Override
    public void accept(StringValue value) {
        valueBuilder.setName(value.getName());
        valueBuilder.setString(org.opennms.netmgt.telemetry.protocols.netflow.transport.StringValue.newBuilder()
                .setString(com.google.protobuf.StringValue.newBuilder().setValue(value.getValue()).build())
                .build());
    }

    @Override
    public void accept(UnsignedValue value) {
        valueBuilder.setName(value.getName());
        valueBuilder.setUnsigned(org.opennms.netmgt.telemetry.protocols.netflow.transport.UnsignedValue.newBuilder()
                .setUint64(com.google.protobuf.UInt64Value.newBuilder().setValue(value.getValue().longValue()).build())
                .build());
    }

    @Override
    public void accept(ListValue value) {
        valueBuilder.setName(value.getName());
        final List<List<Value<?>>> listOfListsOfValues = value.getValue();

        final org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue.Builder listValueBuilder = org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue.newBuilder()
                .setSemantic(Semantic.valueOf(value.getSemantic().name()));

        for(List<Value<?>> listOfValues : listOfListsOfValues) {
            final org.opennms.netmgt.telemetry.protocols.netflow.transport.List.Builder listBuilder = org.opennms.netmgt.telemetry.protocols.netflow.transport.List.newBuilder();
            for (Value bValue : listOfValues) {
                TransportValueVisitor transportValueVisitor = new TransportValueVisitor();
                bValue.visit(transportValueVisitor);

                listBuilder.addValue(transportValueVisitor.build());
            }
            listValueBuilder.addList(listBuilder.build());
        }

        valueBuilder.setList(listValueBuilder.build());
    }

    @Override
    public void accept(UndeclaredValue value) {
        valueBuilder.setName(value.getName());
        valueBuilder.setUndeclared(org.opennms.netmgt.telemetry.protocols.netflow.transport.UndeclaredValue.newBuilder()
                .setBytes(com.google.protobuf.BytesValue.newBuilder().setValue(ByteString.copyFrom(value.getValue())).build())
                .build());

    }

    public org.opennms.netmgt.telemetry.protocols.netflow.transport.Value build() {
        return valueBuilder.build();
    }
}