package org.opennms.plugins.dbnotifier;

/**
 * message class used to transport database notifications
 * @author admin
 *
 */
public class DbNotification {

	private int processId;
	private String channelName=null;
	private String payload=null;

	public DbNotification( int processId, String channelName, String payload){
		this.processId=processId;
		this.channelName=channelName;
		this.payload=payload;
	}

	public int getProcessId() {
		return processId;
	}

	public String getChannelName() {
		return channelName;
	}

	public String getPayload() {
		return payload;
	}
}
