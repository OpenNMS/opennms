package org.opennms.netmgt.dao.jdbc.usernotification;

import java.util.Date;

import javax.sql.DataSource;

import org.opennms.netmgt.model.EntityVisitor;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.netmgt.model.OnmsUserNotification;

public class LazyUserNotification extends OnmsUserNotification {

	private static final long serialVersionUID = -8549615324373817847L;
	private boolean m_loaded;
	private boolean m_dirty;
	private DataSource m_dataSource;
	
	public LazyUserNotification(DataSource dataSource) {
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
            UserNotificationId key = new UserNotificationId(this);
            FindByKey.get(m_dataSource, key).find(key);
		}
	}

    public String getAutoNotify() {
        load();
        return super.getAutoNotify();
    }

    public String getContactInfo() {
        load();
        return super.getContactInfo();
    }

    public Integer getId() {
        load();
        return super.getId();
    }

    public String getMedia() {
        load();
        return super.getMedia();
    }

    public OnmsNotification getNotification() {
        load();
        return super.getNotification();
    }

    public Date getNotifyTime() {
        load();
        return super.getNotifyTime();
    }

    public String getUserId() {
        load();
        return super.getUserId();
    }

    public void setAutoNotify(String autoNotify) {
        load();
        super.setAutoNotify(autoNotify);
    }

    public void setContactInfo(String contactInfo) {
        load();
        super.setContactInfo(contactInfo);
    }

    public void setId(Integer id) {
        load();
        super.setId(id);
    }

    public void setMedia(String media) {
        load();
        super.setMedia(media);
    }

    public void setNotification(OnmsNotification notification) {
        load();
        super.setNotification(notification);
    }

    public void setNotifyTime(Date notifyTime) {
        load();
        super.setNotifyTime(notifyTime);
    }

    public void setUserId(String userId) {
        load();
        super.setUserId(userId);
    }

    public void visit(EntityVisitor visitor) {
        load();
        super.visit(visitor);
    }

}
