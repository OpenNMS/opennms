package org.opennms.sms.reflector.smsservice.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.ResourceBundle;

import org.opennms.sms.reflector.smsservice.GatewayGroup;
import org.smslib.AGateway;
import org.smslib.modem.SerialModemGateway;
import org.springframework.core.io.Resource;

public class GatewayGroupConfigImpl implements GatewayGroup {
	
	AGateway[] m_gateways = new AGateway[1];
	
	public AGateway[] getGateways() {
		return m_gateways;
	}
	
	public void setConfig(Resource config){
		
		Properties modemProperties = new Properties();
		InputStream in = null;
		
		try{
			in = config.getInputStream();
			modemProperties.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(in != null){
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("\nmodemPort: " + modemProperties.getProperty("modem.port") + "\n");
		String port = modemProperties.getProperty("modem.port", "/dev/tty.usbmodem2412");
		String model = modemProperties.getProperty("modem.model", "w760i");
		String manufacturer = modemProperties.getProperty("modem.manufacturer", "SonyEricsson");
		String baudrate = modemProperties.getProperty("modem.baudrate", "56700");
		
		
		m_gateways[0] = new SerialModemGateway("modem." + port, port, new Integer(baudrate), manufacturer, model);
		
	}

}
