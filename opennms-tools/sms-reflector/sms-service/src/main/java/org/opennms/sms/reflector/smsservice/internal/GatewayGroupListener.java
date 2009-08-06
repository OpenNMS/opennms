package org.opennms.sms.reflector.smsservice.internal;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.opennms.sms.reflector.smsservice.GatewayGroup;
import org.opennms.sms.reflector.smsservice.SmsService;
import org.osgi.framework.BundleContext;
import org.smslib.AGateway;
import org.smslib.GatewayException;
import org.smslib.IGatewayStatusNotification;
import org.smslib.IInboundMessageNotification;
import org.smslib.IOutboundMessageNotification;
import org.smslib.Service.ServiceStatus;
import org.springframework.osgi.context.BundleContextAware;

public class GatewayGroupListener implements BundleContextAware {
	
	private BundleContext m_bundleContext;
	private List<IOutboundMessageNotification> m_outboundListeners;
    private List<IInboundMessageNotification> m_inboundListeners;
    private List<IGatewayStatusNotification> m_gatewayStatusListeners;
	
	
	public void setBundleContext(BundleContext bundleContext) {
		m_bundleContext = bundleContext;
	}
	
	public BundleContext getBundleContext(){
		return m_bundleContext;
	}
	
	public void onGatewayGroupRegistered(GatewayGroup gatewayGroup, Map properties){
		AGateway[] gateways = gatewayGroup.getGateways();
		
		SmsServiceImpl smsService = new SmsServiceImpl();
		smsService.setOutboundNotification(new OutboundMessageNotification(getOutboundListeners()));
		smsService.setInboundNotification(new InboundMessageNotification(getInboundListeners()));
        smsService.setGatewayStatusNotification(new GatewayStatusNotification(getGatewayStatusListeners()));
        
		for(int i = 0; i < gateways.length; i++){
			
			try {
				if(smsService.getServiceStatus() == ServiceStatus.STARTED){
					smsService.stop();
				}
				smsService.addGateway(gateways[i]);
				
			} catch (GatewayException e) {
				e.printStackTrace();
			}
		}
		
		smsService.start();
		
		//Properties serviceProps = new Properties();
		//serviceProps.put("type", properties.get("gatewayUsageType").toString().toLowerCase());
		
		getBundleContext().registerService(SmsService.class.getName(), smsService, null);
		
	}
	
	public void onGatewayGroupUnRegistered(GatewayGroup gatewayGroup, Map properties){
		
	}

	public void setOutboundListeners(List<IOutboundMessageNotification> m_outboundListeners) {
		this.m_outboundListeners = m_outboundListeners;
	}

	public List<IOutboundMessageNotification> getOutboundListeners() {
		return m_outboundListeners;
	}

	public void setInboundListeners(List<IInboundMessageNotification> m_inboundListeners) {
		this.m_inboundListeners = m_inboundListeners;
	}

	public List<IInboundMessageNotification> getInboundListeners() {
		return m_inboundListeners;
	}

	public void setGatewayStatusListeners(List<IGatewayStatusNotification> m_gatewayStatusListeners) {
		this.m_gatewayStatusListeners = m_gatewayStatusListeners;
	}

	public List<IGatewayStatusNotification> getGatewayStatusListeners() {
		return m_gatewayStatusListeners;
	}

}
