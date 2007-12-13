/**
 * 
 */
package org.opennms.netmgt.snmp.snmp4j;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.snmp4j.asn1.BER;
import org.snmp4j.asn1.BERInputStream;
import org.snmp4j.smi.IpAddress;

/**
 * @author Jeff Gehlbach <jeffg@opennms.org>
 * 
 * This class exists solely to work around a bug in the Net-SNMP BER library
 * that causes Net-SNMP agents prior to version 5.4.1 on 64-bit platforms to
 * encode IPv4 addresses as 8 bytes rather than 4 bytes.  SNMP4J correctly
 * discards these representations by default.
 * 
 *  This class will be used in place of org.snmp4j.smi.IpAddress only if we
 *  call org.snmp4j.SNMP4JSettings.setExtensibilityEnabled(true) AND set the
 *  system property org.snmp4j.smisyntaxes to refer to a classpath properties
 *  resource (relative to class org.snmp4j.smi.AbstractVariable) that overrides
 *  the default mapping for BER type 64 (nominally IpAddress).  See the file
 *  opennms-snmp4j-smisyntaxes.properties.
 *  
 *  The workaround for 8-bit long IP addresses will be enabled only if the
 *  system property org.opennms.netmgt.snmp.workarounds.allow64BitIpAddress is
 *  set to "true".
 *
 */
public class IpAddressNetSnmp64bitBugAware extends IpAddress {
	private static final long serialVersionUID = 1L;
	
	public IpAddressNetSnmp64bitBugAware() {
		super();
	}
	
	public IpAddressNetSnmp64bitBugAware(InetAddress address) {
		super(address);
	}
	
	public IpAddressNetSnmp64bitBugAware(String address) {
		super(address);
	}
	
	public void decodeBER(BERInputStream inputStream) throws java.io.IOException {
		BER.MutableByte type = new BER.MutableByte();
		byte[] value = BER.decodeString(inputStream, type);
		if (type.getValue() != BER.IPADDRESS) {
			throw new IOException("Wrong type encountered when decoding Counter: "+
					type.getValue());
		}
		if (value.length != 4) {
			if ( (value.length == 8) && Boolean.getBoolean("org.opennms.snmp.workarounds.allow64BitIpAddress") ) {
	            byte[] tempValue = { 0,0,0,0 };
	            for (int i = 0; i < 4; i++) {
	            	tempValue[i] = value[i];
	            }
	            value = tempValue;
	            if (log().isDebugEnabled()) {
	            	log().debug("Working around misencoded IpAddress (8 bytes, truncating to 4); likely dealing with a buggy Net-SNMP agent");
	            }
			} else {
				throw new IOException("IpAddress encoding error, wrong length: " +
						value.length);
			}
		}

		this.setInetAddress(InetAddress.getByAddress(value));
	}
	
    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
}