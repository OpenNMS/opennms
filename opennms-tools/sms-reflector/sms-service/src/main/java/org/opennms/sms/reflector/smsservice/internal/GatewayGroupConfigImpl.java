package org.opennms.sms.reflector.smsservice.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.opennms.sms.reflector.smsservice.GatewayGroup;
import org.smslib.AGateway;
import org.smslib.GatewayException;
import org.smslib.TimeoutException;
import org.smslib.AGateway.Protocols;
import org.smslib.modem.SerialModemGateway;
import org.springframework.core.io.Resource;

public class GatewayGroupConfigImpl implements GatewayGroup {
	
	AGateway[] m_gateways;
	
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
		
		String modems = modemProperties.getProperty("modems");
		StringTokenizer tokens = new StringTokenizer(modems);
		
		m_gateways = new AGateway[tokens.countTokens()];
		
		for(int i = 0; i < tokens.countTokens(); i++){
			String modemId = tokens.nextToken();
			String port = modemProperties.getProperty(modemId + ".port");
			String baudRate = modemProperties.getProperty(modemId + ".baudrate");
			String manufacturer = modemProperties.getProperty(modemId + ".manufacturer");
			String model = modemProperties.getProperty(modemId + ".model");
			String pin = modemProperties.getProperty(modemId+".pin", "0000");
			
			SerialModemGateway gateway = new SerialModemGateway(modemId, port, Integer.parseInt(baudRate), manufacturer, model);
			gateway.setProtocol(Protocols.PDU);
			gateway.setInbound(true);
			gateway.setOutbound(true);
			gateway.setSimPin(pin);

            m_gateways[i] = gateway;
		}
		
	}
	
	public void stopGateways(){
		System.out.printf("%s stopping all gateways in group %s", "\n", "\n");
		for(AGateway gateway : m_gateways){
			try {
				if(gateway.getStatus() != AGateway.GatewayStatuses.STOPPED){
					gateway.stopGateway();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
