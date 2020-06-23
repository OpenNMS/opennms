/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp.snmp4j;

import java.io.IOException;
import java.net.InetAddress;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.snmp4j.asn1.BER;
import org.snmp4j.asn1.BERInputStream;
import org.snmp4j.smi.IpAddress;

/**
 * @author Jeff Gehlbach &lt;jeffg@opennms.org&gt;
 * 
 * This class was created to work around a bug in the Net-SNMP BER library
 * that causes Net-SNMP agents prior to version 5.4.1 on 64-bit platforms to
 * encode IPv4 addresses as 8 bytes rather than 4 bytes.  SNMP4J correctly
 * discards these representations by default.
 * 
 * It has since been extended to substitute an IPv4 address of 0.0.0.0 when
 * presented with a zero-length IpAddress value. This behavior has been
 * seen from DrayTek Vigor2820 series routers.
 * 
 *  This class will be used in place of org.snmp4j.smi.IpAddress only if we
 *  call org.snmp4j.SNMP4JSettings.setExtensibilityEnabled(true) AND set the
 *  system property org.snmp4j.smisyntaxes to refer to a classpath properties
 *  resource (relative to class org.snmp4j.smi.AbstractVariable) that overrides
 *  the default mapping for BER type 64 (nominally IpAddress).  See the file
 *  opennms-snmp4j-smisyntaxes.properties.
 *  
 *  The workaround for 8-bit long IPv4 addresses will be enabled only if the
 *  system property org.opennms.netmgt.snmp.workarounds.allow64BitIpAddress is
 *  set to "true".
 *  
 *  The workaround for 0-byte IPv4 addresses will be enabled only if the
 *  system property org.opennms.snmp.workarounds.allowZeroLengthIpAddress is
 *  set to "true".
 *
 */
public class IpAddressBadLengthBugAware extends IpAddress {
	
	private static final transient Logger LOG = LoggerFactory.getLogger(IpAddressBadLengthBugAware.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -5736688810851346588L;

	public IpAddressBadLengthBugAware() {
		super();
	}
	
	public IpAddressBadLengthBugAware(InetAddress address) {
		super(address);
	}
	
	public IpAddressBadLengthBugAware(String address) {
		super(address);
	}
	
        @Override
        public void decodeBER(BERInputStream inputStream) throws java.io.IOException {
            BER.MutableByte type = new BER.MutableByte();
            byte[] value = BER.decodeString(inputStream, type);
            if (type.getValue() != BER.IPADDRESS) {
                throw new IOException("Wrong type encountered when decoding IpAddress: "+
                        type.getValue());
            }
            if (value.length != 4) {
                if ( (value.length == 8) && Boolean.getBoolean("org.opennms.snmp.workarounds.allow64BitIpAddress") ) {
                    byte[] tempValue = new byte[4];
                    System.arraycopy(value, 0, tempValue, 0, 4);
                    value = tempValue;
                    LOG.debug("Working around misencoded IpAddress (8 bytes, truncating to 4); likely dealing with a buggy Net-SNMP agent");
                } else {
                    throw new IOException("IpAddress encoding error, wrong length: " +
                            value.length);
                }
            } else if ( (value.length == 0 ) && Boolean.getBoolean("org.opennms.snmp.workarounds.allowZeroLengthIpAddress") ) {
                byte tempValue[] = { 0, 0, 0, 0 };
                value = tempValue;
                LOG.debug("Working around misencoded IpAddress (0 bytes, substituting 0.0.0.0); likely dealing with a buggy DrayTek Vigor2820 Series router");
            }

            this.setInetAddress(InetAddress.getByAddress(value));
	}
	
}
