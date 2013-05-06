/*_############################################################################
  _## 
  _##  SNMP4J - Integer32.java  
  _## 
  _##  Copyright (C) 2003-2009  Frank Fock and Jochen Katz (SNMP4J.org)
  _##  
  _##  Licensed under the Apache License, Version 2.0 (the "License");
  _##  you may not use this file except in compliance with the License.
  _##  You may obtain a copy of the License at
  _##  
  _##      http://www.apache.org/licenses/LICENSE-2.0
  _##  
  _##  Unless required by applicable law or agreed to in writing, software
  _##  distributed under the License is distributed on an "AS IS" BASIS,
  _##  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  _##  See the License for the specific language governing permissions and
  _##  limitations under the License.
  _##  
  _##########################################################################*/

package org.opennms.netmgt.snmp.snmp4j;

import java.io.IOException;
import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.asn1.BER;
import org.snmp4j.asn1.BERInputStream;
import org.snmp4j.smi.AssignableFromInteger;
import org.snmp4j.smi.AssignableFromString;
import org.snmp4j.smi.Integer32;

/**
 * This is special version of the original library that avoid exceptions
 * to deal with a broken Net-SNMP agent.
 *
 * For more details check: 
 * http://issues.opennms.org/browse/NMS-5747
 */
public class Integer32IgnoreTooManyBytes extends Integer32
    implements AssignableFromInteger, AssignableFromString {
	
  private static final transient Logger LOG = LoggerFactory.getLogger(Integer32IgnoreTooManyBytes.class);

  private static final long serialVersionUID = 5046132399890132416L;

  /**
   * Creates an <code>Integer32</code> with a zero value.
   */
  public Integer32IgnoreTooManyBytes() {
  }

  /**
   * Creates an <code>Integer32</code> variable with the supplied value.
   * @param value
   *    an integer value.
   */
  public Integer32IgnoreTooManyBytes(int value) {
    setValue(value);
  }

  public void decodeBER(BERInputStream inputStream) throws java.io.IOException {
    BER.MutableByte type = new BER.MutableByte();
	int length;
	BigInteger value = BigInteger.ZERO;
	
	type.setValue((byte)inputStream.read());
	
	if ((type.getValue() != 0x02) && (type.getValue() != 0x43) &&
	    (type.getValue() != 0x41)) {
	  throw new IOException("Wrong ASN.1 type. Not an integer: "+type.getValue()+
	                        (" at position "+inputStream.getPosition()));
	}
	length = BER.decodeLength(inputStream);
	if (length > 4) {
		LOG.debug("Working around invalid Integer32 likely dealing with a permissive Net-SNMP agent");
	}
	
	int b = inputStream.read() & 0xFF;
	if ((b & 0x80) > 0) {
	  value = BigInteger.ONE.negate();
	}
	while (length-- > 0) {
		value = value.shiftLeft(8).or(BigInteger.valueOf(b));
		if (length > 0) {
			b = inputStream.read();
		}
	}
    
    int newValue = value.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0 ? 0 : value.intValue();
    
    if (type.getValue() != BER.INTEGER) {
      throw new IOException("Wrong type encountered when decoding Counter: "+type.getValue());
    }
    setValue(newValue);
  }

  public Object clone() {
    return new Integer32IgnoreTooManyBytes(getValue());
  }

}

