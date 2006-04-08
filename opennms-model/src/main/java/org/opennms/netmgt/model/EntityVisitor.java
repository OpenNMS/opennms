package org.opennms.netmgt.model;

public interface EntityVisitor {
	
	public void visitNode(OnmsNode node);
	public void visitNodeComplete(OnmsNode node);
	public void visitSnmpInterface(OnmsSnmpInterface snmpIface);
	public void visitSnmpInterfaceComplete(OnmsSnmpInterface snmpIface);
	public void visitIpInterface(OnmsIpInterface iface);
	public void visitIpInterfaceComplete(OnmsIpInterface iface);
	public void visitMonitoredService(OnmsMonitoredService monSvc);
	public void visitMonitoredServiceComplete(OnmsMonitoredService monSvc);

}
