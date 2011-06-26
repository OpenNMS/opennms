package org.opennms.netmgt.provision.detector.modbus;

import org.opennms.netmgt.provision.detector.modbus.client.ModBusClient;
import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.detector.simple.response.LineOrientedResponse;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
@Component
@Scope("prototype")
public class ModBusDetector extends
		BasicDetector<LineOrientedRequest, LineOrientedResponse> {

	private static int DEFAULT_PORT=502;
	private static String DEFAULT_SERVICENAME="modbus";
	
	
	protected ModBusDetector() {
		super(DEFAULT_SERVICENAME, DEFAULT_PORT);
	}

	@Override
	protected Client<LineOrientedRequest, LineOrientedResponse> getClient() {
		// TODO Auto-generated method stub
		ModBusClient modbusclient = new ModBusClient();
		return modbusclient;
	}

	@Override
	protected void onInit() {
		// TODO Auto-generated method stub
		
	}
	

}
