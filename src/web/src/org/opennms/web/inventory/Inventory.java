package org.opennms.web.inventory;
import java.sql.*;
import java.text.SimpleDateFormat;




public class Inventory
{
    private String nodeLabel = null;
	private int nodeID=0;
	private String name=null;
	private Timestamp createTime=null;
	private Timestamp lastPollTime=null;
	private String pathToFile=null;
	private String status=null;
	private SimpleDateFormat ObjectformatDate = new SimpleDateFormat("dd/MM/yy HH.mm.ss");	
	
    public Inventory()
	{
	}
	
    
	
	/**
	 * @return
	 */
	public Timestamp getCreateTime() {
		return createTime;
	}
	
	public String getCreateTimeString() {
		java.util.Date creTime =  new java.util.Date(createTime.getTime());
		return ObjectformatDate.format(creTime);
	}


	/**
	 * @return
	 */
	public Timestamp getLastPollTime() {
		return lastPollTime;
	}

	public String getLastPollTimeString() {
		java.util.Date lpTime =  new java.util.Date(lastPollTime.getTime());
		return ObjectformatDate.format(lpTime);
	}
	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return
	 */
	public int getNodeID() {
		return nodeID;
	}

	/**
	 * @return
	 */
	public String getPathToFile() {
		return pathToFile;
	}

	/**
	 * @return
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param date
	 */
	public void setCreateTime(Timestamp date) {
		createTime = date;
	}

	/**
	 * @param date
	 */
	public void setLastPollTime(Timestamp date) {
		lastPollTime = date;
	}
	
	

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * @param i
	 */
	public void setNodeID(int i) {
		nodeID = i;
	}

	/**
	 * @param string
	 */
	public void setPathToFile(String path) {
		pathToFile = path;
	}

	/**
	 * @param string
	 */
	public void setStatus(String string) {
		status = string;
	}

	/**
	 * @return Returns the nodeLabel.
	 */
	public String getNodeLabel() {
		return nodeLabel;
	}
	/**
	 * @param nodeLabel The nodeLabel to set.
	 */
	public void setNodeLabel(String nodeLabel) {
		this.nodeLabel = nodeLabel;
	}
}