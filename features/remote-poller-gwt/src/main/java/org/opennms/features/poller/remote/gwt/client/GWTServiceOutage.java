/**
 * 
 */
package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;
import java.util.Date;

import org.opennms.features.poller.remote.gwt.client.utils.CompareToBuilder;
import org.opennms.features.poller.remote.gwt.client.utils.EqualsUtil;
import org.opennms.features.poller.remote.gwt.client.utils.HashCodeBuilder;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GWTServiceOutage implements Serializable, IsSerializable, Comparable<GWTServiceOutage> {
    private static final long serialVersionUID = 1L;

    private GWTLocationMonitor m_monitor;

    private GWTMonitoredService m_service;

    private Date m_from;

    private Date m_to;

    public GWTServiceOutage() {
    }

    public GWTServiceOutage(final GWTLocationMonitor monitor, final GWTMonitoredService service) {
        m_monitor = monitor;
        m_service = service;
    }

    public Date getFrom() {
        return m_from;
    }

    public void setFrom(final Date from) {
        m_from = from;
    }

    public Date getTo() {
        return m_to;
    }

    public void setTo(final Date to) {
        m_to = to;
    }

    public GWTLocationMonitor getMonitor() {
        return m_monitor;
    }

    public void setMonitor(final GWTLocationMonitor monitor) {
        m_monitor = monitor;
    }

    public GWTMonitoredService getService() {
        return m_service;
    }

    public void setService(final GWTMonitoredService service) {
        m_service = service;
    }

    public boolean equals(Object o) {
        if (!(o instanceof GWTServiceOutage))
            return false;
        GWTServiceOutage that = (GWTServiceOutage) o;
        final GWTLocationMonitor thisMonitor = this.getMonitor();
        final GWTLocationMonitor thatMonitor = that.getMonitor();
        final GWTMonitoredService thisService = this.getService();
        final GWTMonitoredService thatService = that.getService();
        return EqualsUtil.areEqual(
            thisMonitor == null? null : thisMonitor.getId(),
            thatMonitor == null? null : thatMonitor.getId()
        ) && EqualsUtil.areEqual(
            thisService == null? null : thisService.getId(),
            thatService == null? null : thatService.getId()
        ) && EqualsUtil.areEqual(this.getFrom(), that.getFrom())
          && EqualsUtil.areEqual(this.getTo(), that.getTo());
    }

    public int hashCode() {
        return new HashCodeBuilder().append(this.getMonitor()).append(this.getService()).append(this.getFrom()).append(this.getTo()).toHashcode();
    }

    public String toString() {
        return "GWTServiceOutage[monitor=" + m_monitor + ",service=" + m_service + ",from=" + m_from + ",to=" + m_to + "]";
    }

    public int compareTo(final GWTServiceOutage that) {
        if (that == null) return -1;
        return new CompareToBuilder()
            .append(this.getService(), that.getService())
            .append(this.getFrom(), that.getFrom())
            .append(this.getMonitor(), that.getMonitor())
            .append(this.getTo(),that.getTo())
            .toComparison();
    }

}