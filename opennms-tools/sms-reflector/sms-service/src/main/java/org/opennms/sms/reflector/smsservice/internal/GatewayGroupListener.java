package org.opennms.sms.reflector.smsservice.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.sms.reflector.smsservice.GatewayGroup;
import org.opennms.sms.reflector.smsservice.OnmsInboundMessageNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.AGateway;
import org.smslib.GatewayException;
import org.smslib.IGatewayStatusNotification;
import org.smslib.IOutboundMessageNotification;
import org.smslib.Service.ServiceStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class GatewayGroupListener implements InitializingBean {
    
    private static Logger log = LoggerFactory.getLogger(GatewayGroupListener.class); 
    
	private SmsServiceRegistrar m_smsServiceRegistrar;
	private Map<GatewayGroup, SmsServiceImpl> m_services = new HashMap<GatewayGroup, SmsServiceImpl>();
	private List<IOutboundMessageNotification> m_outboundListeners;
    private List<OnmsInboundMessageNotification> m_inboundListeners;
    private List<IGatewayStatusNotification> m_gatewayStatusListeners;

    public void onGatewayGroupRegistered(GatewayGroup gatewayGroup, Map<String, Object> properties){
		AGateway[] gateways = gatewayGroup.getGateways();
		
		if (gateways.length == 0) {
		    log.error("A Gateway group was registered with ZERO gateways!");
		    return;
		}
		
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
		
		smsService.register(m_smsServiceRegistrar);
		
		m_services.put(gatewayGroup, smsService);

		
	}
	
	public void onGatewayGroupUnRegistered(GatewayGroup gatewayGroup, Map properties){
	    
	    SmsServiceImpl service = m_services.get(gatewayGroup);
	    
	    service.unregister();
	    
	    service.stop();
	    
	}

	@SuppressWarnings("unused")
	private boolean gatewayIdMatches(Collection<AGateway> gateways, AGateway[] aGateways) {
		for(AGateway serviceGateway : gateways){
			for(AGateway groupGateway : aGateways){
				if(serviceGateway.getGatewayId() == groupGateway.getGatewayId()){
					return true;
				}
			}
		}
		return false;
	}

	public void setOutboundListeners(List<IOutboundMessageNotification> m_outboundListeners) {
		this.m_outboundListeners = m_outboundListeners;
	}

	public List<IOutboundMessageNotification> getOutboundListeners() {
		return m_outboundListeners;
	}

	public void setInboundListeners(List<OnmsInboundMessageNotification> m_inboundListeners) {
		this.m_inboundListeners = m_inboundListeners;
	}

	public List<OnmsInboundMessageNotification> getInboundListeners() {
		return m_inboundListeners;
	}

	public void setGatewayStatusListeners(List<IGatewayStatusNotification> m_gatewayStatusListeners) {
		this.m_gatewayStatusListeners = m_gatewayStatusListeners;
	}

	public List<IGatewayStatusNotification> getGatewayStatusListeners() {
		return m_gatewayStatusListeners;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(m_smsServiceRegistrar);
		
	}

	public void setSmsServiceRegistrar(SmsServiceRegistrar smsServiceRegistrar) {
		m_smsServiceRegistrar = smsServiceRegistrar;
	}

	public SmsServiceRegistrar getSmsServiceRegistrar() {
		return m_smsServiceRegistrar;
	}

}
