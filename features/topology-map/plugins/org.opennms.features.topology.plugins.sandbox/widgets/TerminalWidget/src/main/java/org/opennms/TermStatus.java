package org.opennms;

public class TermStatus {
	
	public boolean isClosed;
	public boolean doClose;
	
	public TermStatus(boolean isClosed, boolean doClose){
		this.isClosed = isClosed;
		this.doClose = doClose;
	}
	
}
