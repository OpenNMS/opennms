package org.opennms.sms.reflector.smsservice.internal;

import org.apache.log4j.Logger;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.sms.reflector.smsservice.GatewayGroup;
import org.opennms.sms.reflector.smsservice.OnmsInboundMessageNotification;
import org.opennms.sms.reflector.smsservice.SmsService;
import org.smslib.AGateway;
import org.smslib.GatewayException;
import org.smslib.IGatewayStatusNotification;
import org.smslib.IOutboundMessageNotification;
import org.smslib.Service.ServiceStatus;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

public class SmsServiceListFactoryBean implements FactoryBean<SmsService[]>, InitializingBean {
	private GatewayGroup[] m_gatewayGroups;
	private SmsService[] m_services;
	
	public void setOutboundMessageNotification(IOutboundMessageNotification mOutboundMessageNotification) {
		m_outboundMessageNotification = mOutboundMessageNotification;
	}

	public void setInboundMessageNotification(OnmsInboundMessageNotification mInboundMessageNotification) {
		m_inboundMessageNotification = mInboundMessageNotification;
	}

	public void setGatewayStatusNotification(IGatewayStatusNotification mGatewayStatusNotification) {
		m_gatewayStatusNotification = mGatewayStatusNotification;
	}

	private IOutboundMessageNotification m_outboundMessageNotification;
	private OnmsInboundMessageNotification m_inboundMessageNotification;
	@SuppressWarnings("unused")
	private IGatewayStatusNotification m_gatewayStatusNotification;

	public SmsServiceListFactoryBean() {
		
	}

	public void setGatewayGroupList(GatewayGroup[] groupList) {
		m_gatewayGroups = groupList;
	}

	public void afterPropertiesSet() {
		m_services = new SmsService[m_gatewayGroups.length];

		int count = 0;
		for (GatewayGroup group : m_gatewayGroups) {
			AGateway[] gateways = group.getGateways();
			
			if (gateways.length == 0) {
				log().warn("A Gateway group was registered with ZERO gateways!");
			    return;
			}
			
			SmsServiceImpl smsService = new SmsServiceImpl();
			smsService.setOutboundNotification(m_outboundMessageNotification);
			smsService.setInboundNotification(m_inboundMessageNotification);
	        // smsService.setGatewayStatusNotification(m_gatewayStatusNotification);

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

			m_services[count++] = smsService;
		}
	}
	
	private Logger log() {
		return ThreadCategory.getInstance(getClass());
	}

	public SmsService[] getObject() throws Exception {
		return m_services;
	}

	public Class<? extends SmsService[]> getObjectType() {
		return SmsService[].class;
	}

	public boolean isSingleton() {
		return true;
	}

}
