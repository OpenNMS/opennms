package org.opennms.netmgt.dao;

public class ServiceInfo {
    private String status;
    private String name;
    
    public ServiceInfo(String name, String status){
    	this.status = status;
    	this.name = name;
    }
	public String getServiceName(){
		return name;
	}
	public String getServiceStatus(){
		return status;
	}
}
