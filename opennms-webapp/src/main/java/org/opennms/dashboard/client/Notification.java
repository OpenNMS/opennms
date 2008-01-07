package org.opennms.dashboard.client;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Notification implements IsSerializable {
	
	private String m_nodeLabel;
	private String m_serviceName;
	private String m_severity;
	private Date m_sentTime;
	private String m_responder;
	private Date m_respondTime;
	private String m_textMessage;
    
    public String getNodeLabel() {
        return m_nodeLabel;
    }
    public void setNodeLabel(String nodeLabel) {
        m_nodeLabel = nodeLabel;
    }
    public String getResponder() {
        return m_responder;
    }
    public void setResponder(String responder) {
        m_responder = responder;
    }
    public Date getRespondTime() {
        return m_respondTime;
    }
    public void setRespondTime(Date respondTime) {
        m_respondTime = respondTime;
    }
    public Date getSentTime() {
        return m_sentTime;
    }
    public void setSentTime(Date sentTime) {
        m_sentTime = sentTime;
    }
    public String getServiceName() {
        return m_serviceName;
    }
    public void setServiceName(String serviceName) {
        m_serviceName = serviceName;
    }
    public String getSeverity() {
        return m_severity;
    }
    public void setSeverity(String severity) {
        m_severity = severity;
    }
	public String getTextMessage() {
		return m_textMessage;
	}
	public void setTextMessage(String message) {
		m_textMessage = message;
	}
    
    

}
