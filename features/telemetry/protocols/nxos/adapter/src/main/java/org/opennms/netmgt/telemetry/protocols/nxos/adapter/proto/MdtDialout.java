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
     * @return The reqId.
     */
    long getReqId();

    /**
     * <code>bytes data = 2;</code>
     * @return The data.
     */
    com.google.protobuf.ByteString getData();

    /**
     * <code>string errors = 3;</code>
     * @return The errors.
     */
    java.lang.String getErrors();
    /**
     * <code>string errors = 3;</code>
     * @return The bytes for errors.
     */
    com.google.protobuf.ByteString
        getErrorsBytes();
  }
  /**
   * Protobuf type {@code mdt_dialout.MdtDialoutArgs}
   */
  public static final class MdtDialoutArgs extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:mdt_dialout.MdtDialoutArgs)
      MdtDialoutArgsOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use MdtDialoutArgs.newBuilder() to construct.
    private MdtDialoutArgs(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private MdtDialoutArgs() {
      data_ = com.google.protobuf.ByteString.EMPTY;
      errors_ = "";
    }

    @java.lang.Override
    @SuppressWarnings({"unused"})
    protected java.lang.Object newInstance(
        UnusedPrivateParameter unused) {
      return new MdtDialoutArgs();
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.internal_static_mdt_dialout_MdtDialoutArgs_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.internal_static_mdt_dialout_MdtDialoutArgs_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs.class, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs.Builder.class);
    }

    public static final int REQID_FIELD_NUMBER = 1;
    private long reqId_;
    /**
     * <code>int64 ReqId = 1;</code>
     * @return The reqId.
     */
    @java.lang.Override
    public long getReqId() {
      return reqId_;
    }

    public static final int DATA_FIELD_NUMBER = 2;
    private com.google.protobuf.ByteString data_;
    /**
     * <code>bytes data = 2;</code>
     * @return The data.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getData() {
      return data_;
    }

    public static final int ERRORS_FIELD_NUMBER = 3;
    private volatile java.lang.Object errors_;
    /**
     * <code>string errors = 3;</code>
     * @return The errors.
     */
    @java.lang.Override
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
     * @return The bytes for errors.
     */
    @java.lang.Override
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
    @java.lang.Override
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    @java.lang.Override
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
      getUnknownFields().writeTo(output);
    }

    @java.lang.Override
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
      size += getUnknownFields().getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs)) {
        return super.equals(obj);
      }
      org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs other = (org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs) obj;

      if (getReqId()
          != other.getReqId()) return false;
      if (!getData()
          .equals(other.getData())) return false;
      if (!getErrors()
          .equals(other.getErrors())) return false;
      if (!getUnknownFields().equals(other.getUnknownFields())) return false;
      return true;
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
      hash = (29 * hash) + getUnknownFields().hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    @java.lang.Override
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    @java.lang.Override
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
        org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgsOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.internal_static_mdt_dialout_MdtDialoutArgs_descriptor;
      }

      @java.lang.Override
      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.internal_static_mdt_dialout_MdtDialoutArgs_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs.class, org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs.Builder.class);
      }

      // Construct using org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs.newBuilder()
      private Builder() {

      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);

      }
      @java.lang.Override
      public Builder clear() {
        super.clear();
        reqId_ = 0L;

        data_ = com.google.protobuf.ByteString.EMPTY;

        errors_ = "";

        return this;
      }

      @java.lang.Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.internal_static_mdt_dialout_MdtDialoutArgs_descriptor;
      }

      @java.lang.Override
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs getDefaultInstanceForType() {
        return org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs.getDefaultInstance();
      }

      @java.lang.Override
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs build() {
        org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @java.lang.Override
      public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs buildPartial() {
        org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs result = new org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs(this);
        result.reqId_ = reqId_;
        result.data_ = data_;
        result.errors_ = errors_;
        onBuilt();
        return result;
      }

      @java.lang.Override
      public Builder clone() {
        return super.clone();
      }
      @java.lang.Override
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.setField(field, value);
      }
      @java.lang.Override
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return super.clearField(field);
      }
      @java.lang.Override
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return super.clearOneof(oneof);
      }
      @java.lang.Override
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, java.lang.Object value) {
        return super.setRepeatedField(field, index, value);
      }
      @java.lang.Override
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.addRepeatedField(field, value);
      }
      @java.lang.Override
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs) {
          return mergeFrom((org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs other) {
        if (other == org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs.getDefaultInstance()) return this;
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
        this.mergeUnknownFields(other.getUnknownFields());
        onChanged();
        return this;
      }

      @java.lang.Override
      public final boolean isInitialized() {
        return true;
      }

      @java.lang.Override
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        if (extensionRegistry == null) {
          throw new java.lang.NullPointerException();
        }
        try {
          boolean done = false;
          while (!done) {
            int tag = input.readTag();
            switch (tag) {
              case 0:
                done = true;
                break;
              case 8: {
                reqId_ = input.readInt64();

                break;
              } // case 8
              case 18: {
                data_ = input.readBytes();

                break;
              } // case 18
              case 26: {
                errors_ = input.readStringRequireUtf8();

                break;
              } // case 26
              default: {
                if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                  done = true; // was an endgroup tag
                }
                break;
              } // default:
            } // switch (tag)
          } // while (!done)
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          throw e.unwrapIOException();
        } finally {
          onChanged();
        } // finally
        return this;
      }

      private long reqId_ ;
      /**
       * <code>int64 ReqId = 1;</code>
       * @return The reqId.
       */
      @java.lang.Override
      public long getReqId() {
        return reqId_;
      }
      /**
       * <code>int64 ReqId = 1;</code>
       * @param value The reqId to set.
       * @return This builder for chaining.
       */
      public Builder setReqId(long value) {
        
        reqId_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>int64 ReqId = 1;</code>
       * @return This builder for chaining.
       */
      public Builder clearReqId() {
        
        reqId_ = 0L;
        onChanged();
        return this;
      }

      private com.google.protobuf.ByteString data_ = com.google.protobuf.ByteString.EMPTY;
      /**
       * <code>bytes data = 2;</code>
       * @return The data.
       */
      @java.lang.Override
      public com.google.protobuf.ByteString getData() {
        return data_;
      }
      /**
       * <code>bytes data = 2;</code>
       * @param value The data to set.
       * @return This builder for chaining.
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
       * @return This builder for chaining.
       */
      public Builder clearData() {
        
        data_ = getDefaultInstance().getData();
        onChanged();
        return this;
      }

      private java.lang.Object errors_ = "";
      /**
       * <code>string errors = 3;</code>
       * @return The errors.
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
       * @return The bytes for errors.
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
       * @param value The errors to set.
       * @return This builder for chaining.
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
       * @return This builder for chaining.
       */
      public Builder clearErrors() {
        
        errors_ = getDefaultInstance().getErrors();
        onChanged();
        return this;
      }
      /**
       * <code>string errors = 3;</code>
       * @param value The bytes for errors to set.
       * @return This builder for chaining.
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
      @java.lang.Override
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFields(unknownFields);
      }

      @java.lang.Override
      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:mdt_dialout.MdtDialoutArgs)
    }

    // @@protoc_insertion_point(class_scope:mdt_dialout.MdtDialoutArgs)
    private static final org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs();
    }

    public static org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<MdtDialoutArgs>
        PARSER = new com.google.protobuf.AbstractParser<MdtDialoutArgs>() {
      @java.lang.Override
      public MdtDialoutArgs parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        Builder builder = newBuilder();
        try {
          builder.mergeFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          throw e.setUnfinishedMessage(builder.buildPartial());
        } catch (com.google.protobuf.UninitializedMessageException e) {
          throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
        } catch (java.io.IOException e) {
          throw new com.google.protobuf.InvalidProtocolBufferException(e)
              .setUnfinishedMessage(builder.buildPartial());
        }
        return builder.buildPartial();
      }
    };

    public static com.google.protobuf.Parser<MdtDialoutArgs> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<MdtDialoutArgs> getParserForType() {
      return PARSER;
    }

    @java.lang.Override
    public org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.MdtDialout.MdtDialoutArgs getDefaultInstanceForType() {
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
      "dt_dialout.MdtDialoutArgs\"\000(\0010\001B;\n9org.o" +
      "pennms.netmgt.telemetry.protocols.nxos.a" +
      "dapter.protob\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        });
    internal_static_mdt_dialout_MdtDialoutArgs_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_mdt_dialout_MdtDialoutArgs_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_mdt_dialout_MdtDialoutArgs_descriptor,
        new java.lang.String[] { "ReqId", "Data", "Errors", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
