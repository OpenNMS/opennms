/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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
// source: mdt_dialout.proto

package org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto;

public final class MdtDialout {
  private MdtDialout() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface MdtDialoutArgsOrBuilder extends
      // @@protoc_insertion_point(interface_extends:mdt_dialout.MdtDialoutArgs)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>int64 ReqId = 1;</code>
     */
    long getReqId();

    /**
     * <code>bytes data = 2;</code>
     */
    com.google.protobuf.ByteString getData();

    /**
     * <code>string errors = 3;</code>
     */
    java.lang.String getErrors();
    /**
     * <code>string errors = 3;</code>
     */
    com.google.protobuf.ByteString
        getErrorsBytes();
  }
  /**
   * Protobuf type {@code mdt_dialout.MdtDialoutArgs}
   */
  public  static final class MdtDialoutArgs extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:mdt_dialout.MdtDialoutArgs)
      MdtDialoutArgsOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use MdtDialoutArgs.newBuilder() to construct.
    private MdtDialoutArgs(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private MdtDialoutArgs() {
      reqId_ = 0L;
      data_ = com.google.protobuf.ByteString.EMPTY;
      errors_ = "";
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private MdtDialoutArgs(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownFieldProto3(
                  input, unknownFields, extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 8: {

              reqId_ = input.readInt64();
              break;
            }
            case 18: {

              data_ = input.readBytes();
              break;
            }
            case 26: {
              java.lang.String s = input.readStringRequireUtf8();

              errors_ = s;
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return MdtDialout.internal_static_mdt_dialout_MdtDialoutArgs_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return MdtDialout.internal_static_mdt_dialout_MdtDialoutArgs_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              MdtDialout.MdtDialoutArgs.class, MdtDialout.MdtDialoutArgs.Builder.class);
    }

    public static final int REQID_FIELD_NUMBER = 1;
    private long reqId_;
    /**
     * <code>int64 ReqId = 1;</code>
     */
    public long getReqId() {
      return reqId_;
    }

    public static final int DATA_FIELD_NUMBER = 2;
    private com.google.protobuf.ByteString data_;
    /**
     * <code>bytes data = 2;</code>
     */
    public com.google.protobuf.ByteString getData() {
      return data_;
    }

    public static final int ERRORS_FIELD_NUMBER = 3;
    private volatile java.lang.Object errors_;
    /**
     * <code>string errors = 3;</code>
     */
    public java.lang.String getErrors() {
      java.lang.Object ref = errors_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        errors_ = s;
        return s;
      }
    }
    /**
     * <code>string errors = 3;</code>
     */
    public com.google.protobuf.ByteString
        getErrorsBytes() {
      java.lang.Object ref = errors_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        errors_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (reqId_ != 0L) {
        output.writeInt64(1, reqId_);
      }
      if (!data_.isEmpty()) {
        output.writeBytes(2, data_);
      }
      if (!getErrorsBytes().isEmpty()) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 3, errors_);
      }
      unknownFields.writeTo(output);
    }

    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (reqId_ != 0L) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt64Size(1, reqId_);
      }
      if (!data_.isEmpty()) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(2, data_);
      }
      if (!getErrorsBytes().isEmpty()) {
        size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, errors_);
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof MdtDialout.MdtDialoutArgs)) {
        return super.equals(obj);
      }
      MdtDialout.MdtDialoutArgs other = (MdtDialout.MdtDialoutArgs) obj;

      boolean result = true;
      result = result && (getReqId()
          == other.getReqId());
      result = result && getData()
          .equals(other.getData());
      result = result && getErrors()
          .equals(other.getErrors());
      result = result && unknownFields.equals(other.unknownFields);
      return result;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      hash = (37 * hash) + REQID_FIELD_NUMBER;
      hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
          getReqId());
      hash = (37 * hash) + DATA_FIELD_NUMBER;
      hash = (53 * hash) + getData().hashCode();
      hash = (37 * hash) + ERRORS_FIELD_NUMBER;
      hash = (53 * hash) + getErrors().hashCode();
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static MdtDialout.MdtDialoutArgs parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static MdtDialout.MdtDialoutArgs parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static MdtDialout.MdtDialoutArgs parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static MdtDialout.MdtDialoutArgs parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static MdtDialout.MdtDialoutArgs parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static MdtDialout.MdtDialoutArgs parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static MdtDialout.MdtDialoutArgs parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static MdtDialout.MdtDialoutArgs parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static MdtDialout.MdtDialoutArgs parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static MdtDialout.MdtDialoutArgs parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static MdtDialout.MdtDialoutArgs parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static MdtDialout.MdtDialoutArgs parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(MdtDialout.MdtDialoutArgs prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code mdt_dialout.MdtDialoutArgs}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:mdt_dialout.MdtDialoutArgs)
        MdtDialout.MdtDialoutArgsOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return MdtDialout.internal_static_mdt_dialout_MdtDialoutArgs_descriptor;
      }

      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return MdtDialout.internal_static_mdt_dialout_MdtDialoutArgs_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                MdtDialout.MdtDialoutArgs.class, MdtDialout.MdtDialoutArgs.Builder.class);
      }

      // Construct using mdt_dialout.MdtDialout.MdtDialoutArgs.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
        }
      }
      public Builder clear() {
        super.clear();
        reqId_ = 0L;

        data_ = com.google.protobuf.ByteString.EMPTY;

        errors_ = "";

        return this;
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return MdtDialout.internal_static_mdt_dialout_MdtDialoutArgs_descriptor;
      }

      public MdtDialout.MdtDialoutArgs getDefaultInstanceForType() {
        return MdtDialout.MdtDialoutArgs.getDefaultInstance();
      }

      public MdtDialout.MdtDialoutArgs build() {
        MdtDialout.MdtDialoutArgs result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public MdtDialout.MdtDialoutArgs buildPartial() {
        MdtDialout.MdtDialoutArgs result = new MdtDialout.MdtDialoutArgs(this);
        result.reqId_ = reqId_;
        result.data_ = data_;
        result.errors_ = errors_;
        onBuilt();
        return result;
      }

      public Builder clone() {
        return (Builder) super.clone();
      }
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return (Builder) super.setField(field, value);
      }
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return (Builder) super.clearField(field);
      }
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return (Builder) super.clearOneof(oneof);
      }
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, java.lang.Object value) {
        return (Builder) super.setRepeatedField(field, index, value);
      }
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return (Builder) super.addRepeatedField(field, value);
      }
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof MdtDialout.MdtDialoutArgs) {
          return mergeFrom((MdtDialout.MdtDialoutArgs)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(MdtDialout.MdtDialoutArgs other) {
        if (other == MdtDialout.MdtDialoutArgs.getDefaultInstance()) return this;
        if (other.getReqId() != 0L) {
          setReqId(other.getReqId());
        }
        if (other.getData() != com.google.protobuf.ByteString.EMPTY) {
          setData(other.getData());
        }
        if (!other.getErrors().isEmpty()) {
          errors_ = other.errors_;
          onChanged();
        }
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        MdtDialout.MdtDialoutArgs parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (MdtDialout.MdtDialoutArgs) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }

      private long reqId_ ;
      /**
       * <code>int64 ReqId = 1;</code>
       */
      public long getReqId() {
        return reqId_;
      }
      /**
       * <code>int64 ReqId = 1;</code>
       */
      public Builder setReqId(long value) {
        
        reqId_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>int64 ReqId = 1;</code>
       */
      public Builder clearReqId() {
        
        reqId_ = 0L;
        onChanged();
        return this;
      }

      private com.google.protobuf.ByteString data_ = com.google.protobuf.ByteString.EMPTY;
      /**
       * <code>bytes data = 2;</code>
       */
      public com.google.protobuf.ByteString getData() {
        return data_;
      }
      /**
       * <code>bytes data = 2;</code>
       */
      public Builder setData(com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  
        data_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>bytes data = 2;</code>
       */
      public Builder clearData() {
        
        data_ = getDefaultInstance().getData();
        onChanged();
        return this;
      }

      private java.lang.Object errors_ = "";
      /**
       * <code>string errors = 3;</code>
       */
      public java.lang.String getErrors() {
        java.lang.Object ref = errors_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          errors_ = s;
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>string errors = 3;</code>
       */
      public com.google.protobuf.ByteString
          getErrorsBytes() {
        java.lang.Object ref = errors_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          errors_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>string errors = 3;</code>
       */
      public Builder setErrors(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  
        errors_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>string errors = 3;</code>
       */
      public Builder clearErrors() {
        
        errors_ = getDefaultInstance().getErrors();
        onChanged();
        return this;
      }
      /**
       * <code>string errors = 3;</code>
       */
      public Builder setErrorsBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
        
        errors_ = value;
        onChanged();
        return this;
      }
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFieldsProto3(unknownFields);
      }

      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:mdt_dialout.MdtDialoutArgs)
    }

    // @@protoc_insertion_point(class_scope:mdt_dialout.MdtDialoutArgs)
    private static final MdtDialout.MdtDialoutArgs DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new MdtDialout.MdtDialoutArgs();
    }

    public static MdtDialout.MdtDialoutArgs getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<MdtDialoutArgs>
        PARSER = new com.google.protobuf.AbstractParser<MdtDialoutArgs>() {
      public MdtDialoutArgs parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
          return new MdtDialoutArgs(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<MdtDialoutArgs> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<MdtDialoutArgs> getParserForType() {
      return PARSER;
    }

    public MdtDialout.MdtDialoutArgs getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_mdt_dialout_MdtDialoutArgs_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_mdt_dialout_MdtDialoutArgs_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\021mdt_dialout.proto\022\013mdt_dialout\"=\n\016MdtD" +
      "ialoutArgs\022\r\n\005ReqId\030\001 \001(\003\022\014\n\004data\030\002 \001(\014\022" +
      "\016\n\006errors\030\003 \001(\t2^\n\016gRPCMdtDialout\022L\n\nMdt" +
      "Dialout\022\033.mdt_dialout.MdtDialoutArgs\032\033.m" +
      "dt_dialout.MdtDialoutArgs\"\000(\0010\001b\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
    internal_static_mdt_dialout_MdtDialoutArgs_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_mdt_dialout_MdtDialoutArgs_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_mdt_dialout_MdtDialoutArgs_descriptor,
        new java.lang.String[] { "ReqId", "Data", "Errors", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
