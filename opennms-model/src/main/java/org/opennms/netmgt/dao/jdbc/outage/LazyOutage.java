package org.opennms.netmgt.dao.jdbc.outage;

import java.util.Date;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;

public class LazyOutage extends OnmsOutage {

	private static final long serialVersionUID = -8549615324373817847L;
	private boolean m_loaded;
	private boolean m_dirty;
	private DataSource m_dataSource;
	
	public LazyOutage(DataSource dataSource) {
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
			new FindByOutageId(m_dataSource).findUnique(getId());
		}
	}

    public OnmsEvent getEventBySvcLostEvent() {
        load();
        return super.getEventBySvcLostEvent();
    }

    public OnmsEvent getEventBySvcRegainedEvent() {
        load();
        return super.getEventBySvcRegainedEvent();
    }

    public Date getIfLostService() {
        load();
        return super.getIfLostService();
    }

    public Date getIfRegainedService() {
        load();
        return super.getIfRegainedService();
    }

    public OnmsMonitoredService getMonitoredService() {
        load();
        return super.getMonitoredService();
    }

    public void setEventBySvcLostEvent(OnmsEvent eventBySvcLostEvent) {
        load();
        super.setEventBySvcLostEvent(eventBySvcLostEvent);
    }

    public void setEventBySvcRegainedEvent(OnmsEvent eventBySvcRegainedEvent) {
        load();
        super.setEventBySvcRegainedEvent(eventBySvcRegainedEvent);
    }

    public void setIfLostService(Date ifLostService) {
        load();
        super.setIfLostService(ifLostService);
    }

    public void setIfRegainedService(Date ifRegainedService) {
        load();
        super.setIfRegainedService(ifRegainedService);
    }

    public void setMonitoredService(OnmsMonitoredService monitoredService) {
        load();
        super.setMonitoredService(monitoredService);
    }

}
