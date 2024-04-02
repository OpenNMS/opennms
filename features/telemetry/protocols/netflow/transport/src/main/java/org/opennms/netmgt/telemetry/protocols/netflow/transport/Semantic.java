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

/**
 * Protobuf enum {@code Semantic}
 */
public enum Semantic
    implements com.google.protobuf.ProtocolMessageEnum {
  /**
   * <code>UNDEFINED = 0;</code>
   */
  UNDEFINED(0),
  /**
   * <code>NONE_OF = 1;</code>
   */
  NONE_OF(1),
  /**
   * <code>EXACTLY_ONE_OF = 2;</code>
   */
  EXACTLY_ONE_OF(2),
  /**
   * <code>ONE_OR_MORE_OF = 3;</code>
   */
  ONE_OR_MORE_OF(3),
  /**
   * <code>ALL_OF = 4;</code>
   */
  ALL_OF(4),
  /**
   * <code>ORDERED = 5;</code>
   */
  ORDERED(5),
  UNRECOGNIZED(-1),
  ;

  /**
   * <code>UNDEFINED = 0;</code>
   */
  public static final int UNDEFINED_VALUE = 0;
  /**
   * <code>NONE_OF = 1;</code>
   */
  public static final int NONE_OF_VALUE = 1;
  /**
   * <code>EXACTLY_ONE_OF = 2;</code>
   */
  public static final int EXACTLY_ONE_OF_VALUE = 2;
  /**
   * <code>ONE_OR_MORE_OF = 3;</code>
   */
  public static final int ONE_OR_MORE_OF_VALUE = 3;
  /**
   * <code>ALL_OF = 4;</code>
   */
  public static final int ALL_OF_VALUE = 4;
  /**
   * <code>ORDERED = 5;</code>
   */
  public static final int ORDERED_VALUE = 5;


  public final int getNumber() {
    if (this == UNRECOGNIZED) {
      throw new java.lang.IllegalArgumentException(
          "Can't get the number of an unknown enum value.");
    }
    return value;
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   * @deprecated Use {@link #forNumber(int)} instead.
   */
  @java.lang.Deprecated
  public static Semantic valueOf(int value) {
    return forNumber(value);
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   */
  public static Semantic forNumber(int value) {
    switch (value) {
      case 0: return UNDEFINED;
      case 1: return NONE_OF;
      case 2: return EXACTLY_ONE_OF;
      case 3: return ONE_OR_MORE_OF;
      case 4: return ALL_OF;
      case 5: return ORDERED;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<Semantic>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      Semantic> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<Semantic>() {
          public Semantic findValueByNumber(int number) {
            return Semantic.forNumber(number);
          }
        };

  public final com.google.protobuf.Descriptors.EnumValueDescriptor
      getValueDescriptor() {
    if (this == UNRECOGNIZED) {
      throw new java.lang.IllegalStateException(
          "Can't get the descriptor of an unrecognized enum value.");
    }
    return getDescriptor().getValues().get(ordinal());
  }
  public final com.google.protobuf.Descriptors.EnumDescriptor
      getDescriptorForType() {
    return getDescriptor();
  }
  public static final com.google.protobuf.Descriptors.EnumDescriptor
      getDescriptor() {
    return org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowProtos.getDescriptor().getEnumTypes().get(3);
  }

  private static final Semantic[] VALUES = values();

  public static Semantic valueOf(
      com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
    if (desc.getType() != getDescriptor()) {
      throw new java.lang.IllegalArgumentException(
        "EnumValueDescriptor is not for this type.");
    }
    if (desc.getIndex() == -1) {
      return UNRECOGNIZED;
    }
    return VALUES[desc.getIndex()];
  }

  private final int value;

  private Semantic(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:Semantic)
}

