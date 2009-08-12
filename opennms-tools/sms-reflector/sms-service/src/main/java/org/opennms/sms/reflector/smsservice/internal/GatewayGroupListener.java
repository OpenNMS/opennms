package org.opennms.sms.reflector.smsservice.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.opennms.sms.reflector.smsservice.GatewayGroup;
import org.opennms.sms.reflector.smsservice.SmsService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.smslib.AGateway;
import org.smslib.GatewayException;
import org.smslib.IGatewayStatusNotification;
import org.smslib.IInboundMessageNotification;
import org.smslib.IOutboundMessageNotification;
import org.smslib.Service;
import org.smslib.TimeoutException;
import org.smslib.Service.ServiceStatus;
import org.springframework.osgi.context.BundleContextAware;

public class GatewayGroupListener implements BundleContextAware {
	
	private BundleContext m_bundleContext;
	private List<IOutboundMessageNotification> m_outboundListeners;
    private List<IInboundMessageNotification> m_inboundListeners;
    private List<IGatewayStatusNotification> m_gatewayStatusListeners;
    private List<ServiceRegistration> m_registeredServices = new ArrayList<ServiceRegistration>();
	
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
		smsService.setInboundNotification(new InboundMessageNotification(smsService, getInboundListeners()));
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
		m_registeredServices.add(getBundleContext().registerService(SmsService.class.getName(), smsService, null));
		
	}
	
	public void onGatewayGroupUnRegistered(GatewayGroup gatewayGroup, Map properties){
		System.out.println("\n total services: " + m_registeredServices.size() + "\n\n");
		for(ServiceRegistration regService : m_registeredServices){
			SmsService smsService = (SmsService) getBundleContext().getService(regService.getReference());
			if(gatewayIdMatches(smsService.getGateways(), gatewayGroup.getGateways())){
				regService.unregister();
				try {
					smsService.stopService();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}
	}

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
