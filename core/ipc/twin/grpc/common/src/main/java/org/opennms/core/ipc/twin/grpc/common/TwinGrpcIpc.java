/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2025 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2025 The OpenNMS Group, Inc.
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
package org.opennms.core.ipc.twin.grpc.common;

public final class TwinGrpcIpc {
  private TwinGrpcIpc() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_MinionHeader_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_MinionHeader_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\017twin-grpc.proto\032\022twin-message.proto\"3\n" +
      "\014MinionHeader\022\021\n\tsystem_id\030\001 \001(\t\022\020\n\010loca" +
      "tion\030\002 \001(\t2\205\001\n\016OpenNMSTwinIpc\022;\n\014RpcStre" +
      "aming\022\021.TwinRequestProto\032\022.TwinResponseP" +
      "roto\"\000(\0010\001\0226\n\rSinkStreaming\022\r.MinionHead" +
      "er\032\022.TwinResponseProto\"\0000\001B6\n%org.opennm" +
      "s.core.ipc.twin.grpc.commonB\013TwinGrpcIpc" +
      "P\001b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          org.opennms.core.ipc.twin.model.TwinMessageProto.getDescriptor(),
        });
    internal_static_MinionHeader_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_MinionHeader_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_MinionHeader_descriptor,
        new java.lang.String[] { "SystemId", "Location", });
    org.opennms.core.ipc.twin.model.TwinMessageProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
