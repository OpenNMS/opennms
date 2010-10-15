package org.opennms.sms.reflector.smsservice.internal;

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

/**
 * <p>SmsServiceListFactoryBean class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class SmsServiceListFactoryBean implements FactoryBean<SmsService[]>, InitializingBean {
	private GatewayGroup[] m_gatewayGroups;
	private SmsService[] m_services;
	
	/**
	 * <p>setOutboundMessageNotification</p>
	 *
	 * @param mOutboundMessageNotification a {@link org.smslib.IOutboundMessageNotification} object.
	 */
	public void setOutboundMessageNotification(IOutboundMessageNotification mOutboundMessageNotification) {
		m_outboundMessageNotification = mOutboundMessageNotification;
	}

	/**
	 * <p>setInboundMessageNotification</p>
	 *
	 * @param mInboundMessageNotification a {@link org.opennms.sms.reflector.smsservice.OnmsInboundMessageNotification} object.
	 */
	public void setInboundMessageNotification(OnmsInboundMessageNotification mInboundMessageNotification) {
		m_inboundMessageNotification = mInboundMessageNotification;
	}

	/**
	 * <p>setGatewayStatusNotification</p>
	 *
	 * @param mGatewayStatusNotification a {@link org.smslib.IGatewayStatusNotification} object.
	 */
	public void setGatewayStatusNotification(IGatewayStatusNotification mGatewayStatusNotification) {
		m_gatewayStatusNotification = mGatewayStatusNotification;
	}

	private IOutboundMessageNotification m_outboundMessageNotification;
	private OnmsInboundMessageNotification m_inboundMessageNotification;
	@SuppressWarnings("unused")
	private IGatewayStatusNotification m_gatewayStatusNotification;

	/**
	 * <p>Constructor for SmsServiceListFactoryBean.</p>
	 */
	public SmsServiceListFactoryBean() {
		
	}

	/**
	 * <p>setGatewayGroupList</p>
	 *
	 * @param groupList an array of {@link org.opennms.sms.reflector.smsservice.GatewayGroup} objects.
	 */
	public void setGatewayGroupList(GatewayGroup[] groupList) {
		m_gatewayGroups = groupList;
	}

	/**
	 * <p>afterPropertiesSet</p>
	 */
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
	
	private ThreadCategory log() {
		return ThreadCategory.getInstance(getClass());
	}

	/**
	 * <p>getObject</p>
	 *
	 * @return an array of {@link org.opennms.sms.reflector.smsservice.SmsService} objects.
	 * @throws java.lang.Exception if any.
	 */
	public SmsService[] getObject() throws Exception {
		return m_services;
	}

	/**
	 * <p>getObjectType</p>
	 *
	 * @return a {@link java.lang.Class} object.
	 */
	public Class<? extends SmsService[]> getObjectType() {
		return SmsService[].class;
	}

	/**
	 * <p>isSingleton</p>
	 *
	 * @return a boolean.
	 */
	public boolean isSingleton() {
		return true;
	}

}
