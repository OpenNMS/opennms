package org.opennms.sms.reflector.internal;

import java.util.Properties;

import org.opennms.sms.reflector.GatewayStatusNotification;
import org.opennms.sms.reflector.InboundMessageNotification;
import org.opennms.sms.reflector.OutboundMessageNotification;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.smslib.Service;
import org.smslib.AGateway.Protocols;
import org.smslib.modem.SerialModemGateway;

public class Activator implements BundleActivator {
	
	private ServiceRegistration m_registration;
	private Service m_service;
	
	public void start(BundleContext context) throws Exception {
		String port = System.getProperty("modem.port", "/dev/tty.usbmodem412");
		String manufacturer = System.getProperty("modem.manufacturer", "SonyEriccson");
		String model = System.getProperty("modem.model", "w760");
		
		m_service = new Service();
		SerialModemGateway gateway = new SerialModemGateway("modem."+ port, port, 57600, manufacturer, model);
        gateway.setProtocol(Protocols.PDU);
        gateway.setInbound(true);
        gateway.setOutbound(true);
        gateway.setSimPin("0000");
        
        m_service.setOutboundNotification(new OutboundMessageNotification());
        m_service.setInboundNotification(new InboundMessageNotification());
        m_service.setGatewayStatusNotification(new GatewayStatusNotification());
        m_service.addGateway(gateway);
		m_service.startService();
		
        Properties props = new Properties();
        props.put(org.osgi.framework.Constants.SERVICE_PID, "org.smslib.Service");
		
		m_registration = context.registerService(Service.class.getName(), m_service, props);
	}

	public void stop(BundleContext context) throws Exception {
		m_registration.unregister();
		m_service = null;
        m_registration = null;
	}

}
