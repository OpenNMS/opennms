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

public interface FloatValueOrBuilder extends
    // @@protoc_insertion_point(interface_extends:FloatValue)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.google.protobuf.DoubleValue double = 1;</code>
   * @return Whether the double field is set.
   */
  boolean hasDouble();
  /**
   * <code>.google.protobuf.DoubleValue double = 1;</code>
   * @return The double.
   */
  com.google.protobuf.DoubleValue getDouble();
  /**
   * <code>.google.protobuf.DoubleValue double = 1;</code>
   */
  com.google.protobuf.DoubleValueOrBuilder getDoubleOrBuilder();
}
