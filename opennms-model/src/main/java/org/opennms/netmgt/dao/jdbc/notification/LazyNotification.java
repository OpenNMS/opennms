package org.opennms.netmgt.dao.jdbc.notification;

import java.util.Date;
import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.netmgt.model.OnmsServiceType;

public class LazyNotification extends OnmsNotification {

	private static final long serialVersionUID = -8549615324373817847L;
	private boolean m_loaded;
	private boolean m_dirty;
	private DataSource m_dataSource;
	
	public LazyNotification(DataSource dataSource) {
		m_dataSource = dataSource;
	}

	public void setLoaded(boolean loaded) {
		m_loaded = loaded;
	}
	
	public boolean getLoaded() {
		return m_loaded;
	}

	public boolean isDirty() {
		return m_dirty;
	}

	public void setDirty(boolean dirty) {
		m_dirty = dirty;
	}

	public String toString() {
		load();
		setDirty(true);
		return super.toString();
	}

    private void load() {
		if (!m_loaded) {
			new FindByNotifyId(m_dataSource).findUnique(getNotifyId());
		}
	}

    public String getAnsweredBy() {
        load();
        return super.getAnsweredBy();
    }

    public OnmsEvent getEvent() {
        load();
        return super.getEvent();
    }

    public OnmsIpInterface getInterface() {
        load();
        return super.getInterface();
    }

    public OnmsNode getNode() {
        load();
        return super.getNode();
    }

    public String getNumericMsg() {
        load();
        return super.getNumericMsg();
    }

    public Date getPageTime() {
        load();
        return super.getPageTime();
    }

    public String getQueueId() {
        load();
        return super.getQueueId();
    }

    public Date getRespondTime() {
        load();
        return super.getRespondTime();
    }

    public OnmsServiceType getServiceType() {
        load();
        return super.getServiceType();
    }

    public String getSubject() {
        load();
        return super.getSubject();
    }

    public String getTextMsg() {
        load();
        return super.getTextMsg();
    }

    public Set getUsersNotified() {
        load();
        return super.getUsersNotified();
    }

    public void setAnsweredBy(String answeredby) {
        load();
        super.setAnsweredBy(answeredby);
    }

    public void setEvent(OnmsEvent event) {
        load();
        super.setEvent(event);
    }

    public void setInterface(OnmsIpInterface interfaceId) {
        load();
        super.setInterface(interfaceId);
    }

    public void setNode(OnmsNode node) {
        load();
        super.setNode(node);
    }

    public void setNumericMsg(String numericmsg) {
        load();
        super.setNumericMsg(numericmsg);
    }

    public void setPageTime(Date pagetime) {
        load();
        super.setPageTime(pagetime);
    }

    public void setQueueId(String queueid) {
        load();
        super.setQueueId(queueid);
    }

    public void setRespondTime(Date respondtime) {
        load();
        super.setRespondTime(respondtime);
    }

    public void setServiceType(OnmsServiceType serviceType) {
        load();
        super.setServiceType(serviceType);
    }

    public void setSubject(String subject) {
        load();
        super.setSubject(subject);
    }

    public void setTextMsg(String textmsg) {
        load();
        super.setTextMsg(textmsg);
    }

    public void setUsersNotified(Set usersnotifieds) {
        load();
        super.setUsersNotified(usersnotifieds);
    }

}
