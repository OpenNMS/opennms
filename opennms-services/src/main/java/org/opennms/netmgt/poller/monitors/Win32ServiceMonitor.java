package org.opennms.netmgt.poller.monitors;

import java.util.Map;

import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.utils.ParameterMap;

public class Win32ServiceMonitor extends SnmpMonitor {
	private static final String SV_SVC_OPERATING_STATE_OID = ".1.3.6.1.4.1.77.1.2.3.1.3";
	private static final String DEFAULT_SERVICE_NAME = "Server";
	
	public PollStatus poll(MonitoredService svc, Map parameters) {
		String serviceName = ParameterMap.getKeyedString(parameters, "service-name", DEFAULT_SERVICE_NAME);
		int snLength = serviceName.length();
		
		StringBuffer serviceOidBuf = new StringBuffer(SV_SVC_OPERATING_STATE_OID);
		serviceOidBuf.append(".").append(Integer.toString(snLength));
		for (byte thisByte : serviceName.getBytes()) {
			serviceOidBuf.append(".").append(Byte.toString(thisByte));
		}
		
		if (log().isDebugEnabled()) {
			log().debug("For Win32 service '" + serviceName +"', OID to check is " + serviceOidBuf.toString());
		}
		
		parameters.put("oid", serviceOidBuf.toString());
		parameters.put("operator", "=");
		parameters.put("operand", "1");
		
		return super.poll(svc, parameters);
	}
}
