package org.opennms.netmgt.model;

public class AbstractEntityVisitor implements EntityVisitor {

	public void visitNode(OnmsNode node) {
	}

	public void visitSnmpInterface(OnmsSnmpInterface snmpIface) {
	}

	public void visitIpInterface(OnmsIpInterface iface) {
	}

	public void visitMonitoredService(OnmsMonitoredService monSvc) {
	}

	public void visitNodeComplete(OnmsNode node) {
	}

	public void visitSnmpInterfaceComplete(OnmsSnmpInterface snmpIface) {
	}

	public void visitIpInterfaceComplete(OnmsIpInterface iface) {
	}

	public void visitMonitoredServiceComplete(OnmsMonitoredService monSvc) {
	}

}
