/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

/**
 * <p>GWTServiceOutage class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;
import java.util.Date;

import org.opennms.features.poller.remote.gwt.client.utils.CompareToBuilder;
import org.opennms.features.poller.remote.gwt.client.utils.EqualsUtil;
import org.opennms.features.poller.remote.gwt.client.utils.HashCodeBuilder;

import com.google.gwt.user.client.rpc.IsSerializable;
public class GWTServiceOutage implements Serializable, IsSerializable, Comparable<GWTServiceOutage> {

	private static final long serialVersionUID = -569168075556078550L;

	private GWTLocationMonitor m_monitor;

    private GWTMonitoredService m_service;

    private Date m_from;

    private Date m_to;

    /**
     * <p>Constructor for GWTServiceOutage.</p>
     */
    public GWTServiceOutage() {
    }

    /**
     * <p>Constructor for GWTServiceOutage.</p>
     *
     * @param monitor a {@link org.opennms.features.poller.remote.gwt.client.GWTLocationMonitor} object.
     * @param service a {@link org.opennms.features.poller.remote.gwt.client.GWTMonitoredService} object.
     */
    public GWTServiceOutage(final GWTLocationMonitor monitor, final GWTMonitoredService service) {
        m_monitor = monitor;
        m_service = service;
    }

    /**
     * <p>getFrom</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getFrom() {
        return m_from;
    }

    /**
     * <p>setFrom</p>
     *
     * @param from a {@link java.util.Date} object.
     */
    public void setFrom(final Date from) {
        m_from = from;
    }

    /**
     * <p>getTo</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getTo() {
        return m_to;
    }

    /**
     * <p>setTo</p>
     *
     * @param to a {@link java.util.Date} object.
     */
    public void setTo(final Date to) {
        m_to = to;
    }

    /**
     * <p>getMonitor</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTLocationMonitor} object.
     */
    public GWTLocationMonitor getMonitor() {
        return m_monitor;
    }

    /**
     * <p>setMonitor</p>
     *
     * @param monitor a {@link org.opennms.features.poller.remote.gwt.client.GWTLocationMonitor} object.
     */
    public void setMonitor(final GWTLocationMonitor monitor) {
        m_monitor = monitor;
    }

    /**
     * <p>getService</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTMonitoredService} object.
     */
    public GWTMonitoredService getService() {
        return m_service;
    }

    /**
     * <p>setService</p>
     *
     * @param service a {@link org.opennms.features.poller.remote.gwt.client.GWTMonitoredService} object.
     */
    public void setService(final GWTMonitoredService service) {
        m_service = service;
    }

    /** {@inheritDoc} */
        @Override
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

    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
        @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.getMonitor()).append(this.getService()).append(this.getFrom()).append(this.getTo()).toHashcode();
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
        @Override
    public String toString() {
        return "GWTServiceOutage[monitor=" + m_monitor + ",service=" + m_service + ",from=" + m_from + ",to=" + m_to + "]";
    }

    /**
     * <p>compareTo</p>
     *
     * @param that a {@link org.opennms.features.poller.remote.gwt.client.GWTServiceOutage} object.
     * @return a int.
     */
        @Override
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
