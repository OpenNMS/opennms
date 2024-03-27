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
package org.opennms.netmgt.snmp.snmp4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.opennms.netmgt.snmp.snmp4j.opaqueadapter.DoubleAdapter;

import org.opennms.netmgt.snmp.snmp4j.opaqueadapter.ErrorAdapter;
import org.opennms.netmgt.snmp.snmp4j.opaqueadapter.UnsupportedAdapter;
import org.opennms.netmgt.snmp.snmp4j.opaqueadapter.OpaqueTypeAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.asn1.BER;
import org.snmp4j.asn1.BERInputStream;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Opaque;
import org.snmp4j.smi.SMIConstants;

public class OpaqueExt extends Opaque {

    private static final Logger LOG = LoggerFactory.getLogger(OpaqueExt.class);

    private static final byte ID_BITTS = 0b00011111;
    private static final byte BITTS_6 = 0b00100000;
    private static final byte BITTS_EX_TYPE = 0b01111111;

    private OpaqueTypeAdapter adapter;

    private OpaqueTypeAdapter getAdapter() {
        if (adapter == null) {
            adapter = createAdapter(this.getValue());
        }
        return adapter;
    }

    public OpaqueExt() {
        super();
        adapter = null;
    }

    public OpaqueExt(byte[] bytes) {
        super(bytes);
        adapter = null;
    }

    @Override
    public Object clone() {
        OpaqueExt cloned = new OpaqueExt(this.getValue());
        cloned.adapter = this.adapter;
        return cloned;
    }

    @Override
    public String toString() {
        return getAdapter().getString();
    }
    
    public Long getLong() {
        return getAdapter().getLong();
    }

    public Double getDouble() {
        return getAdapter().getDouble();
    }

    @Override
    public void setValue(OctetString value) {
        super.setValue(value);
        adapter = null;
    }

    @Override
    public void decodeBER(BERInputStream inputStream) throws IOException {
        super.decodeBER(inputStream);
        adapter = null;
    }

    @Override
    public int getSyntax() {
        return SMIConstants.SYNTAX_OPAQUE;
    }

    public OpaqueValueType getValueType() {
        return getAdapter().getValueType();
    }

    private static UnsupportedAdapter createUnsupportedAdapter(final byte[] bytes) {
        OctetString octetString = new OctetString(bytes);
        LOG.debug("Unsupported bytes provided to OpaqueExt {}", octetString.toString());
        return new UnsupportedAdapter(octetString);
    }

    private static ErrorAdapter createErrorAdapter(String message, final byte[] bytes) {
        if (LOG.isDebugEnabled()) {
            OctetString octetString = new OctetString(bytes);
            LOG.debug("Cannot create TypeAdapter. {} ;, data: {}", message, octetString.toString());
        }
        return new ErrorAdapter();
    }

    private static OpaqueTypeAdapter createAdapter(final byte[] bytes) {
        if (bytes.length < 3) {
            return createErrorAdapter("To short", bytes);
        }

        //Bits 8 and 7 contains "class". It can be relevant for future extensions.
        //At the time the "class" is ignored. If you nead to filter "classes" additionaly to type
        //use "int clazz = bytes[0] & 0b11000000;"
        
        //existing implementations here do not support extended type so the custom type retrieving is implemented
        int offset;
        byte type = (byte) (bytes[0] & ID_BITTS);
        //when bit 6 == 0 then Ok otherwise compound value -> it will be not decoded
        if ((type & BITTS_6) != 0) {
            return createUnsupportedAdapter(bytes);
        }

        if (type != ID_BITTS) { //type is stored in bits 1-5 but only when <= 30
            offset = 1;
        } else { // otherwise it is stored in first 7 bits of second byte but only when <= 127
            type = bytes[1];
            if (type < BITTS_EX_TYPE) {
                offset = 2;
            } else { // otherwise too big type - not supported yet
                return createUnsupportedAdapter(bytes);
            }
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes, offset, bytes.length - offset);
        try (BERInputStream is = new BERInputStream(buffer)) {
            int length = BER.decodeLength(is);
            
            if (bytes.length - buffer.position() != length) { //remaining length must de equal to data length
                return createErrorAdapter("Wrong length of wrapped data", bytes);
            }

            switch (type) {
                case 120: // If the "class" will not be later ignored, then it was initially implemented for context-specific class (clazz == 0b10000000). I am not sure whether type 120 is the same in all "classes".
                    if (length == 4 ) { //check length
                        float floatValue = buffer.getFloat();
                        return new DoubleAdapter((double)floatValue);
                    }
                    if (length == 8 ) { // not sure if some device also provide double
                        double doubleValue = buffer.getDouble();
                        return new DoubleAdapter(doubleValue);
                    }
                //Only float format 120 as the type field in ASN.1 is supported
                //implement here other formats when required. e.g
                //
                //  break;
                //case ???:
                //  return new StringWithNumberCheckAdapter(???);
                //default:
                //  return createUnsupportedAdapter(bytes);
            }
            
        } catch (IOException ex) {
            LOG.warn("Unable to create an OpaqueTypeAdapter", ex);
            return new ErrorAdapter();
        }

        return createUnsupportedAdapter(bytes);
    }

    @Override
    public boolean isPrintable() {
        OpaqueValueType valueType = getAdapter().getValueType();
        if (valueType == OpaqueValueType.ERROR) {
            return false;
        }
        return getAdapter().getValueType() == OpaqueValueType.DOUBLE ||
                getAdapter().getValueType() == OpaqueValueType.LONG ||
                super.isPrintable();
    }
    
}
