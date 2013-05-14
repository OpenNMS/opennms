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

package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;

import org.opennms.features.poller.remote.gwt.client.utils.CompareToBuilder;
import org.opennms.features.poller.remote.gwt.client.utils.EqualsUtil;
import org.opennms.features.poller.remote.gwt.client.utils.HashCodeBuilder;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * <p>StatusDetails class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class StatusDetails implements Serializable, IsSerializable, Comparable<StatusDetails> {

	private static final long serialVersionUID = 7627737910294456042L;

	private Status m_status;

    private String m_reason;

    /**
     * <p>Constructor for StatusDetails.</p>
     */
    public StatusDetails() {
    }

    /**
     * <p>Constructor for StatusDetails.</p>
     *
     * @param status a {@link org.opennms.features.poller.remote.gwt.client.Status} object.
     * @param reason a {@link java.lang.String} object.
     */
    public StatusDetails(final Status status, final String reason) {
        m_status = status;
        m_reason = reason;
    }

    /**
     * <p>getStatus</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.Status} object.
     */
    public Status getStatus() {
        if (m_status == null) {
            m_status = Status.UNKNOWN;
        }
        return m_status;
    }

    /**
     * <p>getReason</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getReason() {
        return m_reason;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
        @Override
    public String toString() {
        return "StatusDetails[status=" + m_status + ",reason=" + m_reason + "]";
    }

    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
        @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getStatus())
            .append(this.getReason())
            .toHashcode();
    }

    /** {@inheritDoc} */
        @Override
    public boolean equals(final Object o) {
        if (!(o instanceof StatusDetails))
            return false;
        StatusDetails that = (StatusDetails) o;
        return EqualsUtil.areEqual(this.getStatus(), that.getStatus());
    }

    /**
     * <p>compareTo</p>
     *
     * @param that a {@link org.opennms.features.poller.remote.gwt.client.StatusDetails} object.
     * @return a int.
     */
        @Override
    public int compareTo(final StatusDetails that) {
        return new CompareToBuilder()
//            .append(this.getStatus(), that.getStatus())
            // reverse sort!
            .append(that.getStatus(), this.getStatus())
            .append(this.getReason(), that.getReason())
            .toComparison();
    }

    /**
     * <p>up</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.StatusDetails} object.
     */
    public static StatusDetails up() {
        return new StatusDetails(Status.UP, null);
    }

    /**
     * <p>marginal</p>
     *
     * @param reason a {@link java.lang.String} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.StatusDetails} object.
     */
    public static StatusDetails marginal(final String reason) {
        return new StatusDetails(Status.MARGINAL, reason);
    }

    /**
     * <p>down</p>
     *
     * @param reason a {@link java.lang.String} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.StatusDetails} object.
     */
    public static StatusDetails down(final String reason) {
        return new StatusDetails(Status.DOWN, reason);
    }

    /**
     * <p>disconnected</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.StatusDetails} object.
     */
    public static StatusDetails disconnected(final String reason) {
        return new StatusDetails(Status.DISCONNECTED, reason);
    }

    /**
     * <p>stopped</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.StatusDetails} object.
     */
    public static StatusDetails stopped(final String reason) {
        return new StatusDetails(Status.STOPPED, reason);
    }

    /**
     * <p>unknown</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.StatusDetails} object.
     */
    public static StatusDetails unknown() {
        return new StatusDetails(Status.UNKNOWN, null);
    }

    /**
     * <p>unknown</p>
     *
     * @param reason a {@link java.lang.String} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.StatusDetails} object.
     */
    public static StatusDetails unknown(final String reason) {
        return new StatusDetails(Status.UNKNOWN, reason);
    }
}
