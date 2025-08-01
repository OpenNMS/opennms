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
