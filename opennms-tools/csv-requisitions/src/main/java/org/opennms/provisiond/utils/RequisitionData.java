/**
 * 
 */
package org.opennms.provisiond.utils;

import org.apache.commons.lang.StringUtils;

class RequisitionData {
	private String m_nodeLabel = null;
	private String m_primaryIp = null;
	private String m_foreignSource = null;
	private String m_foreignId = null;
	private String m_customerId = null;
	private String m_customerName = null;

	public RequisitionData(String[] fields) throws Exception {		
		m_nodeLabel = StringUtils.isBlank(fields[0]) ? "noname" : fields[0];
		m_primaryIp = StringUtils.isBlank(fields[1]) ? "169.254.1.1" : fields[1];
		m_foreignSource = StringUtils.isBlank(fields[2]) ? "TS" : StringUtils.deleteWhitespace(fields[2]);
		m_customerId = StringUtils.isBlank(fields[3]) ? "0" : fields[3];
		m_foreignId = StringUtils.isBlank(fields[4]) ? null : fields[4];
		m_customerName = StringUtils.isBlank(fields[5]) ? "Towerstream" : fields[5];
		
		if (m_foreignId == null) {
			throw new Exception("ForeignId is blank in fields: \n"+fields+"\n");
		}
		
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("Node Label: ");
		sb.append(m_nodeLabel == null ? "Null" : m_nodeLabel);
		sb.append(", Primary IP: ");
		sb.append(m_primaryIp == null ? "Null" : m_primaryIp);
		sb.append(", Foreign Source: ");
		sb.append(m_foreignSource == null ? "Null" : m_foreignSource);
		sb.append(", Customer ID: ");
		sb.append(m_customerId == null ? "Null" : m_customerId);
		sb.append(", Foreign ID: ");
		sb.append(m_foreignId == null ? "Null" : m_foreignId);
		sb.append(", Customer Name: ");
		sb.append(m_customerName == null ? "Null" : m_customerName);
		return sb.toString();
	}

	public void setNodeLabel(String nodeLabel) {
		this.m_nodeLabel = nodeLabel;
	}

	public String getNodeLabel() {
		return m_nodeLabel;
	}

	public void setPrimaryIp(String primaryIp) {
		this.m_primaryIp = primaryIp;
	}

	public String getPrimaryIp() {
		return m_primaryIp;
	}

	public void setForeignSource(String foreignSource) {
		m_foreignSource = foreignSource;
	}

	public String getForeignSource() {
		return m_foreignSource;
	}

	public void setForeignId(String foreignId) {
		m_foreignId = foreignId;
	}

	public String getForeignId() {
		return m_foreignId;
	}

	public void setCustomerId(String customerId) {
		m_customerId = customerId;
	}

	public String getCustomerId() {
		return m_customerId;
	}

	public void setCustomerName(String customerName) {
		m_customerName = customerName;
	}

	public String getCustomerName() {
		return m_customerName;
	}
	
}