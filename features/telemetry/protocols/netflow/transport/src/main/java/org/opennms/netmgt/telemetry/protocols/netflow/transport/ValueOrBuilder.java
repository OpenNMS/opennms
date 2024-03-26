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
package org.opennms.netmgt.telemetry.protocols.netflow.transport;

public interface ValueOrBuilder extends
    // @@protoc_insertion_point(interface_extends:Value)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string name = 1;</code>
   * @return The name.
   */
  java.lang.String getName();
  /**
   * <code>string name = 1;</code>
   * @return The bytes for name.
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>.BooleanValue boolean = 2;</code>
   * @return Whether the boolean field is set.
   */
  boolean hasBoolean();
  /**
   * <code>.BooleanValue boolean = 2;</code>
   * @return The boolean.
   */
  org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValue getBoolean();
  /**
   * <code>.BooleanValue boolean = 2;</code>
   */
  org.opennms.netmgt.telemetry.protocols.netflow.transport.BooleanValueOrBuilder getBooleanOrBuilder();

  /**
   * <code>.DateTimeValue datetime = 3;</code>
   * @return Whether the datetime field is set.
   */
  boolean hasDatetime();
  /**
   * <code>.DateTimeValue datetime = 3;</code>
   * @return The datetime.
   */
  org.opennms.netmgt.telemetry.protocols.netflow.transport.DateTimeValue getDatetime();
  /**
   * <code>.DateTimeValue datetime = 3;</code>
   */
  org.opennms.netmgt.telemetry.protocols.netflow.transport.DateTimeValueOrBuilder getDatetimeOrBuilder();

  /**
   * <code>.FloatValue float = 4;</code>
   * @return Whether the float field is set.
   */
  boolean hasFloat();
  /**
   * <code>.FloatValue float = 4;</code>
   * @return The float.
   */
  org.opennms.netmgt.telemetry.protocols.netflow.transport.FloatValue getFloat();
  /**
   * <code>.FloatValue float = 4;</code>
   */
  org.opennms.netmgt.telemetry.protocols.netflow.transport.FloatValueOrBuilder getFloatOrBuilder();

  /**
   * <code>.IPv4AddressValue ipv4address = 5;</code>
   * @return Whether the ipv4address field is set.
   */
  boolean hasIpv4Address();
  /**
   * <code>.IPv4AddressValue ipv4address = 5;</code>
   * @return The ipv4address.
   */
  org.opennms.netmgt.telemetry.protocols.netflow.transport.IPv4AddressValue getIpv4Address();
  /**
   * <code>.IPv4AddressValue ipv4address = 5;</code>
   */
  org.opennms.netmgt.telemetry.protocols.netflow.transport.IPv4AddressValueOrBuilder getIpv4AddressOrBuilder();

  /**
   * <code>.IPv6AddressValue ipv6address = 6;</code>
   * @return Whether the ipv6address field is set.
   */
  boolean hasIpv6Address();
  /**
   * <code>.IPv6AddressValue ipv6address = 6;</code>
   * @return The ipv6address.
   */
  org.opennms.netmgt.telemetry.protocols.netflow.transport.IPv6AddressValue getIpv6Address();
  /**
   * <code>.IPv6AddressValue ipv6address = 6;</code>
   */
  org.opennms.netmgt.telemetry.protocols.netflow.transport.IPv6AddressValueOrBuilder getIpv6AddressOrBuilder();

  /**
   * <code>.ListValue list = 7;</code>
   * @return Whether the list field is set.
   */
  boolean hasList();
  /**
   * <code>.ListValue list = 7;</code>
   * @return The list.
   */
  org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValue getList();
  /**
   * <code>.ListValue list = 7;</code>
   */
  org.opennms.netmgt.telemetry.protocols.netflow.transport.ListValueOrBuilder getListOrBuilder();

  /**
   * <code>.MacAddressValue macaddress = 8;</code>
   * @return Whether the macaddress field is set.
   */
  boolean hasMacaddress();
  /**
   * <code>.MacAddressValue macaddress = 8;</code>
   * @return The macaddress.
   */
  org.opennms.netmgt.telemetry.protocols.netflow.transport.MacAddressValue getMacaddress();
  /**
   * <code>.MacAddressValue macaddress = 8;</code>
   */
  org.opennms.netmgt.telemetry.protocols.netflow.transport.MacAddressValueOrBuilder getMacaddressOrBuilder();

  /**
   * <code>.NullValue null = 9;</code>
   * @return Whether the null field is set.
   */
  boolean hasNull();
  /**
   * <code>.NullValue null = 9;</code>
   * @return The null.
   */
  org.opennms.netmgt.telemetry.protocols.netflow.transport.NullValue getNull();
  /**
   * <code>.NullValue null = 9;</code>
   */
  org.opennms.netmgt.telemetry.protocols.netflow.transport.NullValueOrBuilder getNullOrBuilder();

  /**
   * <code>.OctetArrayValue octetarray = 10;</code>
   * @return Whether the octetarray field is set.
   */
  boolean hasOctetarray();
  /**
   * <code>.OctetArrayValue octetarray = 10;</code>
   * @return The octetarray.
   */
  org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValue getOctetarray();
  /**
   * <code>.OctetArrayValue octetarray = 10;</code>
   */
  org.opennms.netmgt.telemetry.protocols.netflow.transport.OctetArrayValueOrBuilder getOctetarrayOrBuilder();

  /**
   * <code>.SignedValue signed = 11;</code>
   * @return Whether the signed field is set.
   */
  boolean hasSigned();
  /**
   * <code>.SignedValue signed = 11;</code>
   * @return The signed.
   */
  org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValue getSigned();
  /**
   * <code>.SignedValue signed = 11;</code>
   */
  org.opennms.netmgt.telemetry.protocols.netflow.transport.SignedValueOrBuilder getSignedOrBuilder();

  /**
   * <code>.StringValue string = 12;</code>
   * @return Whether the string field is set.
   */
  boolean hasString();
  /**
   * <code>.StringValue string = 12;</code>
   * @return The string.
   */
  org.opennms.netmgt.telemetry.protocols.netflow.transport.StringValue getString();
  /**
   * <code>.StringValue string = 12;</code>
   */
  org.opennms.netmgt.telemetry.protocols.netflow.transport.StringValueOrBuilder getStringOrBuilder();

  /**
   * <code>.UndeclaredValue undeclared = 13;</code>
   * @return Whether the undeclared field is set.
   */
  boolean hasUndeclared();
  /**
   * <code>.UndeclaredValue undeclared = 13;</code>
   * @return The undeclared.
   */
  org.opennms.netmgt.telemetry.protocols.netflow.transport.UndeclaredValue getUndeclared();
  /**
   * <code>.UndeclaredValue undeclared = 13;</code>
   */
  org.opennms.netmgt.telemetry.protocols.netflow.transport.UndeclaredValueOrBuilder getUndeclaredOrBuilder();

  /**
   * <code>.UnsignedValue unsigned = 14;</code>
   * @return Whether the unsigned field is set.
   */
  boolean hasUnsigned();
  /**
   * <code>.UnsignedValue unsigned = 14;</code>
   * @return The unsigned.
   */
  org.opennms.netmgt.telemetry.protocols.netflow.transport.UnsignedValue getUnsigned();
  /**
   * <code>.UnsignedValue unsigned = 14;</code>
   */
  org.opennms.netmgt.telemetry.protocols.netflow.transport.UnsignedValueOrBuilder getUnsignedOrBuilder();

  public org.opennms.netmgt.telemetry.protocols.netflow.transport.Value.OneofValueCase getOneofValueCase();
}
