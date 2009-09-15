package org.opennms.sms.reflector.smsservice.internal;

import java.util.Map;

import org.smslib.AGateway;
import org.smslib.Service;
import org.smslib.Service.ServiceStatus;

public class GatewayListener {
	
	private Service m_service;
	
	
	public void setService(Service service){
		m_service = service;
	}
	
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

	@SuppressWarnings("unchecked")
	public void bind(Object arg0, Map arg1) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@SuppressWarnings("unchecked")
	public void unbind(Object arg0, Map arg1) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
