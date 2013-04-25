package org.opennms.netmgt.snmp;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class InetAddrUtils {

	public static String str(InetAddress address) {
		return address == null ? null : address.getHostAddress();
	}

	public static InetAddress addr(String value) {
		try {
			return value == null ? null : InetAddress.getByName(value);
		} catch (UnknownHostException e) {
			throw new RuntimeException("Unable to turn " + value + " into an inet address");
		}
	}

	public static InetAddress getLocalHostAddress() {
		return addr("127.0.0.1");
	}
	
	

}
