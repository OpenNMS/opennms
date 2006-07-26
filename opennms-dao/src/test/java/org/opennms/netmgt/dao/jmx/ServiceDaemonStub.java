/**
 * 
 */
package org.opennms.netmgt.dao.jmx;

import org.opennms.netmgt.model.ServiceDaemon;

public class ServiceDaemonStub implements ServiceDaemonStubMBean {
	
	private boolean startCalled = false;
	private String statusStr = "UNDEFINED";
	private String name;

	public ServiceDaemonStub(String name) {
		this.name = name;
	}
	public String status() {
		// TODO Auto-generated method stub
		return statusStr;
	}

	public void pause() {
		// TODO Auto-generated method stub

	}

	public void resume() {
		// TODO Auto-generated method stub

	}

	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	public int getStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void start() {
		// TODO Auto-generated method stub
		startCalled = true;
		statusStr = "Started";
	}

	public boolean getStartCalled() {
		return startCalled;
	}
	
	public void stop() {
		// TODO Auto-generated method stub

	}

}