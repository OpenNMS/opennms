package org.opennms.netmgt.capsd.plugins;

import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.utils.ParameterMap;

public class Win32ServicePlugin extends SnmpPlugin {
	private static final String SV_SVC_OPERATING_STATE_OID = ".1.3.6.1.4.1.77.1.2.3.1.3";
	private static final String DEFAULT_SERVICE_NAME = "Server";
	
	public boolean isProtocolSupported(InetAddress address, Map<String, Object> qualifiers) {
		String serviceName = ParameterMap.getKeyedString(qualifiers, "service-name", DEFAULT_SERVICE_NAME);
		int snLength = serviceName.length();
		
		StringBuffer serviceOidBuf = new StringBuffer(SV_SVC_OPERATING_STATE_OID);
		serviceOidBuf.append(".").append(Integer.toString(snLength));
		for (byte thisByte : serviceName.getBytes()) {
			serviceOidBuf.append(".").append(Byte.toString(thisByte));
		}
		
		if (log().isDebugEnabled()) {
			log().debug("For Win32 service '" + serviceName +"', OID to check is " + serviceOidBuf.toString());
		}
		qualifiers.put("vbname", serviceOidBuf.toString());
		qualifiers.put("vbvalue", "1");
		
		return super.isProtocolSupported(address, qualifiers);
	}
	
	public static Category log() {
		return ThreadCategory.getInstance(Win32ServicePlugin.class);
	}
}
