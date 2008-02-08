package org.opennms.netmgt.scriptd.ins.events;

public abstract class InsAbstractSession extends Thread {

	/**
	 * the shared string for client authentication
	 * If the shared string is not set, then server doesn't require authentication 
	 */
	public String sharedAuthAsciiString = null;
	
	/**
	 * the criteria for getting active alarms
	 */
	public String criteriaRestriction = "";
	
	public void setSharedASCIIString(String sharedASCIIString) {
		this.sharedAuthAsciiString = sharedASCIIString;
	}
	
	public String getSharedASCIIString() {
		return sharedAuthAsciiString;
	}

	public String getCriteriaRestriction() {
		return criteriaRestriction;
	}

	public void setCriteriaRestriction(String criteriaRestriction) {
		this.criteriaRestriction = criteriaRestriction;
	}
	


}
