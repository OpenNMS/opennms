package org.opennms.sms.reflector.smsservice.internal;

import java.util.Map;

import org.smslib.AGateway;
import org.smslib.Service;
import org.smslib.Service.ServiceStatus;

/**
 * <p>GatewayListener class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class GatewayListener {
	
	private Service m_service;
	
	
	/**
	 * <p>setService</p>
	 *
	 * @param service a {@link org.smslib.Service} object.
	 */
	public void setService(Service service){
		m_service = service;
	}
	
	/**
	 * <p>addGateway</p>
	 *
	 * @param gateway a {@link org.smslib.AGateway} object.
	 * @param properties a {@link java.util.Map} object.
	 */
	@SuppressWarnings("unchecked")
	public void addGateway(AGateway gateway, Map properties){
		if(m_service != null){
			try{
				if(m_service.getServiceStatus() == ServiceStatus.STARTED){
					m_service.stopService();
				}
		        
				m_service.addGateway(gateway);
				m_service.startService();
				
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}
	}
	
	/**
	 * <p>removeGateway</p>
	 *
	 * @param gateway a {@link org.smslib.AGateway} object.
	 * @param properties a {@link java.util.Map} object.
	 */
	@SuppressWarnings("unchecked")
	public void removeGateway(AGateway gateway, Map properties){
		if(m_service != null){
			try{
				if(m_service.getServiceStatus() == ServiceStatus.STARTED){
					m_service.stopService();
				}
				m_service.removeGateway(gateway);
				m_service.startService();
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}
	}

	/**
	 * <p>bind</p>
	 *
	 * @param arg0 a {@link java.lang.Object} object.
	 * @param arg1 a {@link java.util.Map} object.
	 * @throws java.lang.Exception if any.
	 */
	@SuppressWarnings("unchecked")
	public void bind(Object arg0, Map arg1) throws Exception {
		// TODO Auto-generated method stub
		
	}

	/**
	 * <p>unbind</p>
	 *
	 * @param arg0 a {@link java.lang.Object} object.
	 * @param arg1 a {@link java.util.Map} object.
	 * @throws java.lang.Exception if any.
	 */
	@SuppressWarnings("unchecked")
	public void unbind(Object arg0, Map arg1) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
