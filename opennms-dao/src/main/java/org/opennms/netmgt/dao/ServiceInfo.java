package org.opennms.netmgt.dao;

public class ServiceInfo {
    private String serviceStatus;
    private String serviceName;
    
    public ServiceInfo(String name, String status){
    	this.serviceStatus = status;
    	this.serviceName = name;
    }
	public String getServiceName(){
		return serviceName;
	}
	public String getServiceStatus(){
		return serviceStatus;
	}
}
