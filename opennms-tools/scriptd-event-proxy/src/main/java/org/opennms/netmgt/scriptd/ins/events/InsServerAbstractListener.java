package org.opennms.netmgt.scriptd.ins.events;

public abstract class InsServerAbstractListener extends InsAbstractSession {

	public final static int DEFAULT_LISTENING_PORT = 8154;

	/**
	 * the port on which server listens
	 */
	public int listeningPort = DEFAULT_LISTENING_PORT;
	
	public int getListeningPort() {
		return listeningPort;
	}

	public void setListeningPort(int listeningPort) {
		this.listeningPort = listeningPort;
	}	

}
